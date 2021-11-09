package io.getstream.trellsample

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.ui.channel.ChannelListFragment

class UsersGroupFragment : ChannelListFragment() {
    override fun getFilter(): FilterObject {
        return Filters.`in`("members", listOf(ChatClient.instance().getCurrentUser()!!.id))
    }

    override fun getSort(): QuerySort<Channel> {
        return QuerySort.desc(Channel::lastMessageAt)
    }
}