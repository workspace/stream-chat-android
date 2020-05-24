package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateChannelImplTest : BaseConnectedIntegrationTest() {

    @Test
    fun createChannel() = runBlocking(Dispatchers.IO) {
        // use case style syntax
        var channel = chatDomain.useCases.createChannel(data.channel1).execute()
        Truth.assertThat(channel.isSuccess).isTrue()
    }

    @Test
    fun createChannelOffline() = runBlocking(Dispatchers.IO) {
        // 1. create channel in the db
        // - verify it's added to existing queries
        // 2. send a message on this channel
        // TODO: properly mock the offline state
        var channel = chatDomain.useCases.createChannel(data.channel3).execute()
        val channels = queryControllerImpl.channels.getOrAwaitValue()
        Truth.assertThat(channels.first().cid).isEqualTo(data.channel3.cid)
        val message = data.createMessage()
        message.cid = data.channel3.cid
        val result = chatDomain.useCases.sendMessage(message).execute()
        val channelController = chatDomainImpl.channel(data.channel3)
        val messages = channelController.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isEqualTo(1)
        Truth.assertThat(messages.first().id).isEqualTo(message.id)
    }
}
