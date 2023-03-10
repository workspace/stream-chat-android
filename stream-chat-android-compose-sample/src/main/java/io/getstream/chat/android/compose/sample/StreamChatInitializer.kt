package io.getstream.chat.android.compose.sample

import android.content.Context
import androidx.startup.Initializer
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.compose.sample.data.PredefinedUserCredentials
import io.getstream.chat.android.compose.sample.data.UserCredentialsRepository
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory

class StreamChatInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val credentialsRepository = UserCredentialsRepository(context)
        ChatHelper.initializeSdk(
            context,
            credentialsRepository.loadApiKey() ?: PredefinedUserCredentials.API_KEY
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}