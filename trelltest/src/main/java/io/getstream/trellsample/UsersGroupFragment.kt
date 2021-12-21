package io.getstream.trellsample

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.querychannels.ChatEventHandler
import io.getstream.chat.android.offline.querychannels.ChatEventHandlerFactory
import io.getstream.chat.android.offline.querychannels.TrellChatEventHandler
import io.getstream.chat.android.ui.channel.ChannelListFragment
import kotlinx.coroutines.flow.StateFlow

class UsersGroupFragment : ChannelListFragment() {

    override fun getChatEventHandlerFactory(): ChatEventHandlerFactory {
        return TrellMemberChatEventHandlerFactory()
    }
}

private class TrellMemberChatEventHandlerFactory : ChatEventHandlerFactory() {

    override fun chatEventHandler(channels: StateFlow<List<Channel>>): ChatEventHandler {
        return TrellChatEventHandler(ChatClient.instance(), channels)
    }
}
