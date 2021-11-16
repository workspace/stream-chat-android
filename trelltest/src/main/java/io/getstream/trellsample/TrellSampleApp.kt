package io.getstream.trellsample

import android.app.Application
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.experimental.plugin.Config
import io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin

@OptIn(ExperimentalStreamChatApi::class)
class TrellSampleApp : Application() {

    val user = User(id = "oleg").apply {
        name = "Oleg Kuzmin"
        image = "https://ca.slack-edge.com/T02RM6X6B-U019BEATNCD-bad2dcf654ef-128"
    }
    val token =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoib2xlZyJ9.ZucjlxjiNewCORdCLwpKwZw2nNtRC_Bv17TjHlitdLU"

    @OptIn(InternalStreamChatApi::class)
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            TrellAppConfigurator.configure(this)
        }
        val offlinePlugin = OfflinePlugin(Config(userPresence = true, persistenceEnabled = true))

        val client = ChatClient.Builder("qx5us2v6xvmh", this)
            .logLevel(ChatLogLevel.DEBUG)
            .withPlugin(offlinePlugin)
            .build()
        client.connectUser(user, token).enqueue()
        ToggleService.init(this, emptyMap())
    }
}