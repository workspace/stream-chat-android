/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.offline.interceptor.internal

import android.content.Context
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.internal.hasPendingAttachments
import io.getstream.chat.android.client.extensions.internal.populateMentions
import io.getstream.chat.android.client.interceptor.SendMessageInterceptor
import io.getstream.chat.android.client.interceptor.message.PrepareMessageLogic
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.AttachmentRepository
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.message.attachments.internal.UploadAttachmentsAndroidWorker
import io.getstream.chat.android.offline.model.message.attachments.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Implementation of [SendMessageInterceptor] that upload attachments, update original message
 * with new attachments and return updated message.
 */
@Suppress("LongParameterList")
internal class SendMessageInterceptorImpl(
    private val context: Context,
    private val logic: LogicRegistry,
    private val clientState: ClientState,
    private val channelRepository: ChannelRepository,
    private val messageRepository: MessageRepository,
    private val attachmentRepository: AttachmentRepository,
    private val scope: CoroutineScope,
    private val networkType: UploadAttachmentsNetworkType,
    private val prepareMessageLogic: PrepareMessageLogic,
    private val user: User
) : SendMessageInterceptor {

    private var jobsMap: Map<String, Job> = emptyMap()
    private val uploadIds = mutableMapOf<String, UUID>()

    override suspend fun interceptMessage(
        channelType: String,
        channelId: String,
        message: Message,
        isRetrying: Boolean,
    ): Result<Message> {
        val channel = logic.channel(channelType, channelId)

        val preparedMessage = prepareMessageLogic.prepareMessage(message, channelId, channelType, user).apply {
            message.populateMentions(channel.toChannel())
        }

        // Update flow in channel controller
        val parentId = preparedMessage.parentId
        if (parentId != null) {
            logic.thread(parentId).upsertMessage(preparedMessage)
            if (message.showInChannel) {
                channel.upsertMessage(preparedMessage)
            }
        } else {
            channel.upsertMessage(preparedMessage)
        }
        // we insert early to ensure we don't lose messages
        messageRepository.insertMessage(preparedMessage)
        channelRepository.updateLastMessageForChannel(message.cid, preparedMessage)

        // TODO: an event broadcasting feature for LOCAL/offline events on the LLC would be a cleaner approach
        // Update flow for currently running queries
        logic.getActiveQueryChannelsLogic().forEach { query -> query.refreshChannel(channel.cid) }

        if (preparedMessage.replyMessageId != null) {
            channel.replyMessage(null)
        }

        return if (!isRetrying) {
            if (preparedMessage.hasPendingAttachments()) {
                uploadAttachments(preparedMessage, channelType, channelId)
            } else {
                Result.success(preparedMessage)
            }
        } else {
            retryMessage(preparedMessage, channelType, channelId)
        }
    }

    /**
     * Tries to upload attachments of this [message] without preparing.
     *
     * It is used when we have some messages already pending in database (due to any non permanent error)
     *
     * @param message [Message] to be retried.
     *
     * @return [Result] having message with latest attachments state or error if there was any.
     */
    private suspend fun retryMessage(message: Message, channelType: String, channelId: String): Result<Message> =
        uploadAttachments(message, channelType, channelId)

    /**
     * Uploads the attachment of this message if there is any pending attachments and return the updated message.
     *
     * @param message [Message] whose attachments are to be uploaded.
     *
     * @return [Result] having message with latest attachments state or error if there was any.
     */
    private suspend fun uploadAttachments(message: Message, channelType: String, channelId: String): Result<Message> {
        return if (clientState.isNetworkAvailable) {
            waitForAttachmentsToBeSent(message, channelType, channelId)
        } else {
            enqueueAttachmentUpload(message, channelType, channelId)
            Result(ChatError("Chat is offline, not sending message with id ${message.id} and text ${message.text}"))
        }
    }

    /**
     * Waits till all attachments are uploaded or either of them fails.
     *
     * @param newMessage Message whose attachments are to be uploaded.
     */
    private suspend fun waitForAttachmentsToBeSent(
        newMessage: Message,
        channelType: String,
        channelId: String,
    ): Result<Message> {
        jobsMap[newMessage.id]?.cancel()
        var allAttachmentsUploaded = false
        var messageToBeSent = newMessage

        jobsMap = jobsMap + (
            newMessage.id to scope.launch {
                attachmentRepository.observeAttachmentsForMessage(newMessage.id)
                    .filterNot(Collection<Attachment>::isEmpty)
                    .collect { attachments ->
                        when {
                            attachments.all { it.uploadState == Attachment.UploadState.Success } -> {
                                messageToBeSent = messageRepository.selectMessage(newMessage.id) ?: newMessage.copy(
                                    attachments = attachments.toMutableList()
                                )
                                allAttachmentsUploaded = true
                                jobsMap[newMessage.id]?.cancel()
                            }
                            attachments.any { it.uploadState is Attachment.UploadState.Failed } -> {
                                jobsMap[newMessage.id]?.cancel()
                            }
                            else -> Unit
                        }
                    }
            }
            )
        enqueueAttachmentUpload(newMessage, channelType, channelId)
        jobsMap[newMessage.id]?.join()
        return if (allAttachmentsUploaded) {
            Result.success(messageToBeSent.copy(type = Message.TYPE_REGULAR))
        } else {
            Result.error(ChatError("Could not upload attachments, not sending message with id ${newMessage.id}"))
        }.also {
            uploadIds.remove(newMessage.id)
        }
    }

    /**
     * Enqueues attachment upload work.
     */
    private fun enqueueAttachmentUpload(message: Message, channelType: String, channelId: String) {
        val workId = UploadAttachmentsAndroidWorker.start(context, channelType, channelId, message.id, networkType)
        uploadIds[message.id] = workId
    }

    /**
     * Cancels all the running job.
     */
    fun cancelJobs() {
        jobsMap.values.forEach { it.cancel() }
        uploadIds.values.forEach { UploadAttachmentsAndroidWorker.stop(context, it) }
    }
}
