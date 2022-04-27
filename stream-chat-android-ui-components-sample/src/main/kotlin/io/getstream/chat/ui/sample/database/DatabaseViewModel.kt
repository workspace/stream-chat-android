package io.getstream.chat.ui.sample.database

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.repository.database.ChatDatabase
import io.getstream.chat.android.offline.repository.domain.channel.ChannelEntity
import io.getstream.chat.android.offline.repository.domain.channel.member.MemberEntity
import io.getstream.chat.android.offline.repository.domain.channel.userread.ChannelUserReadEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.random.Random

private const val TAG = "DatabaseViewModel"

class DatabaseViewModel(
    private val application: Application,
) : ViewModel() {

    private val database by lazy { ChatDatabase.getDatabase(application, userId = "test") }
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    private fun generateString(length: Int): String {
        return (1..length)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    fun onDeleteClick() {
        viewModelScope.launch {
            val traceId = Random.nextInt(100)
            logD("[onDeleteClick] no args ($traceId)")
            withContext(Dispatchers.IO) {
                logV("[onDeleteClick] switched to IO ($traceId)")
                val count = database.channelStateDao().deleteAll()
                logV("[onDeleteClick] deleted($traceId): $count")
            }
            logV("[onDeleteClick] completed($traceId)")
        }
    }

    fun onGenerateClick() {
        viewModelScope.launch {
            val traceId = Random.nextInt(100)
            logD("[onGenerateClick] no args ($traceId)")
            withContext(Dispatchers.IO) {
                logV("[onGenerateClick] switched to IO ($traceId)")
                val channels = generateChannels(count = 10)
                logV("[onGenerateClick] generated($traceId)")
                database.channelStateDao().insertMany(channels)
                logV("[onGenerateClick] inserted($traceId): ${channels.size}")
            }
            logV("[onGenerateClick] completed($traceId)")
        }
    }

    fun onReadClick() {
        viewModelScope.launch {
            val traceId = Random.nextInt(100)
            logD("[onReadClick] no args ($traceId)")
            withContext(Dispatchers.IO) {
                logV("[onReadClick] switched to IO ($traceId)")
                val result = database.channelStateDao().selectSyncNeeded(SyncStatus.SYNC_NEEDED)
                logV("[onReadClick] received($traceId): ${result.size}")
            }
            logV("[onReadClick] completed($traceId)")
        }
    }

    fun onReadManyClick() {
        viewModelScope.launch {
            val traceId = Random.nextInt(100)
            logD("[onReadClick] no args ($traceId)")
            val result = (0..10).map { index ->
                async(Dispatchers.IO) {
                    logV("[onReadClick] switched to IO ($traceId-$index)")
                    database.channelStateDao().selectSyncNeeded(SyncStatus.SYNC_NEEDED).also {
                        logV("[onReadClick] received($traceId-$index): ${it.size}")
                    }
                }
            }.awaitAll()
            logV("[onReadClick] completed($traceId): ${result.size}")
        }
    }

    private fun generateChannels(count: Int): List<ChannelEntity> {
        return (0 until count).map { index ->
            val channelId = "${index}_${generateString(8)}"
            ChannelEntity(
                type = "messaging",
                channelId = channelId,
                cooldown = 0,
                frozen = false,
                createdAt = Date(),
                updatedAt = Date(),
                deletedAt = null,
                extraData = generateExtraData(count = 20),
                syncStatus = SyncStatus.SYNC_NEEDED,
                hidden = false,
                hideMessagesBefore = Date(),
                members = generateMembers(count = 10_000),
                memberCount = 100,
                reads = generateReads(count = 100),
                lastMessageId = generateString(length = 10),
                lastMessageAt = Date(),
                createdByUserId = "createdBy.id",
                watcherIds = listOf(),
                watcherCount = 100,
                team = "team",
                ownCapabilities = setOf(
                    "ban-channel-members",
                    "connect-events",
                    "delete-own-message",
                    "flag-message",
                    "freeze-channel",
                    "join-channel",
                    "leave-channel",
                    "mute-channel",
                    "pin-message",
                    "quote-message",
                    "read-events",
                    "search-messages",
                    "send-custom-events",
                    "send-links",
                    "send-message",
                    "send-reaction",
                    "send-reply",
                    "send-typing-events",
                    "set-channel-cooldown",
                    "typing-events",
                    "update-channel",
                    "update-channel-members",
                    "update-own-message",
                    "upload-file"
                ),
            )
        }
    }

    private fun generateMembers(count: Int): Map<String, MemberEntity> {
        return (0 until count).map { index ->
            MemberEntity(
                userId = "${index}_${generateString(8)}",
                role = "member",
                createdAt = Date(),
                updatedAt = Date(),
                isInvited = false,
                inviteAcceptedAt = null,
                inviteRejectedAt = null,
                shadowBanned = false,
                banned = false,
                channelRole = "chat_member"
            )
        }.associateBy { it.userId }
    }

    private fun generateReads(count: Int): Map<String, ChannelUserReadEntity> {
        return (0 until count).map { index ->
            ChannelUserReadEntity(
                userId = "${index}_${generateString(8)}",
                lastRead = Date(),
                unreadMessages = Random.nextInt(until = 100),
                lastMessageSeenDate = Date()
            )
        }.associateBy { it.userId }
    }

    private fun generateExtraData(count: Int): Map<String, Any> {
        return (0 until count).associate { index ->
            "${index}_${generateString(length = 8)}" to generateString(length = 20)
        }
    }

    private fun logD(message: String) {
        val thread = Thread.currentThread().run { "${id}:${name}" }
        Log.d(TAG, "($thread) $message")
    }

    private fun logV(message: String) {
        val thread = Thread.currentThread().run { "${id}:${name}" }
        Log.v(TAG, "($thread) $message")
    }

    class Factory(
        private val application: Application,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DatabaseViewModel(application) as T
        }
    }
}