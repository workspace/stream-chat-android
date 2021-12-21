package io.getstream.trellsample

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.offline.querychannels.ChatEventHandler
import io.getstream.chat.android.offline.querychannels.ChatEventHandlerFactory
import io.getstream.chat.android.offline.querychannels.NonMemberChatEventHandler
import io.getstream.chat.android.ui.channel.ChannelListFragment
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import kotlinx.coroutines.flow.StateFlow

class TrendyGroupsFragment : ChannelListFragment() {
    override fun getFilter(): FilterObject {
        return Filters.nin("members", listOf(ChatClient.instance().getCurrentUser()!!.id))
    }

    override fun getChatEventHandlerFactory(): ChatEventHandlerFactory {
        return NonMemberChatEventHandlerFactory()
    }
}

private class NonMemberChatEventHandlerFactory : ChatEventHandlerFactory() {

    override fun chatEventHandler(channels: StateFlow<List<Channel>>): ChatEventHandler {
        return NonMemberChatEventHandler(ChatClient.instance(), channels)
    }
}
