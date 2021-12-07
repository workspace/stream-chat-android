package io.getstream.chat.ui.sample.feature.channel.list

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.map
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.querychannels.BaseChatEventHandler
import io.getstream.chat.android.offline.querychannels.EventHandlingResult
import kotlinx.coroutines.runBlocking

/**
 * Default implementation of [ChatEventHandler] which is more generic than [MessagingChatEventHandler]. It skips updates
 * and makes an API request if a channel wasn't yet handled before when receives [NotificationAddedToChannelEvent],
 * [NotificationMessageNewEvent], [NotificationRemovedFromChannelEvent].
 */
internal class CustomChatEventHandler(private val client: ChatClient) :
    BaseChatEventHandler() {

    /**
     * Channel filter function. It makes an API query channel request based on cid of a channel and a filter object to
     * define should be the channel with such cid be in the list of channels or not.
     */
    @OptIn(InternalStreamChatApi::class)
    internal val channelFilter: suspend (cid: String, FilterObject) -> Boolean = { cid, filter ->
        client.queryChannelsInternal(
            QueryChannelsRequest(
                filter = Filters.and(
                    filter,
                    Filters.eq("cid", cid)
                ),
                offset = 0,
                limit = 1,
                messageLimit = 0,
                memberLimit = 0,
            )
        ).await()
            .map { channels -> channels.any { it.cid == cid } }
            .let { filteringResult -> filteringResult.isSuccess && filteringResult.data() }
    }

    override fun handleNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = handleMemberUpdate(event, event.cid, filter)

    override fun handleChannelUpdatedByUserEvent(
        event: ChannelUpdatedByUserEvent,
        filter: FilterObject,
    ): EventHandlingResult = handleMemberUpdate(event, event.cid, filter)

    override fun handleChannelUpdatedEvent(event: ChannelUpdatedEvent, filter: FilterObject): EventHandlingResult =
        handleMemberUpdate(event, event.cid, filter)

    /**
     * Handles [NotificationMessageNewEvent]. It makes a request to API to define outcome of handling.
     *
     * @param event Instance of [NotificationMessageNewEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     */
    override fun handleNotificationMessageNewEvent(
        event: NotificationMessageNewEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

    /**
     * Handles [NotificationRemovedFromChannelEvent]. It makes a request to API to define outcome of handling.
     *
     * @param event Instance of [NotificationRemovedFromChannelEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     */
    override fun handleNotificationRemovedFromChannelEvent(
        event: NotificationRemovedFromChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = handleMemberUpdate(event, event.cid, filter)

    private fun handleMemberUpdate(
        event: HasChannel,
        cid: String,
        filter: FilterObject,
    ): EventHandlingResult {
        val channel = event.channel

        return runBlocking {
            val filterPassed = channelFilter(channel.cid, filter)
            if (filterPassed) {
                EventHandlingResult.Add(channel)
            } else {
                EventHandlingResult.Remove(cid)
            }
        }
    }
}
