package io.getstream.chat.android.pushprovider.xiaomi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.xiaomi.channel.commonutils.logger.b
import com.xiaomi.mipush.sdk.MessageHandleService
import com.xiaomi.mipush.sdk.MiPushCommandMessage
import com.xiaomi.mipush.sdk.MiPushMessage
import com.xiaomi.mipush.sdk.PushMessageReceiver
import io.getstream.chat.android.client.logger.ChatLogger
import java.lang.Exception
import java.lang.IllegalStateException

public class ChatXiaomiMessagingReceiver : PushMessageReceiver() {
    private val logger = ChatLogger.get("ChatXiaomiMessagingReceiver")

    // override fun onReceive(var1: Context, var2: Intent?) {
    //     logger.logD("onReceive() intent: $var2")
    //     logger.logD("onReceive() extra: ${var2?.extras}")
    //     var2?.extras?.keySet()?.forEach {
    //     logger.logD("onReceive() [$it] -> ${var2.extras?.get(it)}")
    //         if (it == "mipush_payload") {
    //             val extra = var2.extras?.get(it)
    //             if (extra != null) {
    //                 logger.logD("onReceive() [$it] -> ${extra::class.java}")
    //             }
    //         }
    //     }
    // }

    override fun onNotificationMessageClicked(p0: Context?, miPushMessage: MiPushMessage?) {
        super.onNotificationMessageClicked(p0, miPushMessage)
        logger.logD("onNotificationMessageClicked(): $miPushMessage")
    }

    override fun onNotificationMessageArrived(p0: Context?, miPushMessage: MiPushMessage?) {
        super.onNotificationMessageArrived(p0, miPushMessage)
        logger.logD("onNotificationMessageArrived(): $miPushMessage")
    }

    override fun onReceiveMessage(p0: Context?, miPushMessage: MiPushMessage?) {
        super.onReceiveMessage(p0, miPushMessage)
        logger.logD("onReceiveMessage(): $miPushMessage")
    }

    override fun onCommandResult(p0: Context?, miPushCommandMessage: MiPushCommandMessage?) {
        super.onCommandResult(p0, miPushCommandMessage)
        logger.logD("onCommandResult(): $miPushCommandMessage")
    }

    override fun onRequirePermissions(p0: Context?, p1: Array<out String>?) {
        super.onRequirePermissions(p0, p1)
        logger.logD("onRequirePermissions(): $p1")
    }

    override fun onReceivePassThroughMessage(context: Context, miPushMessage: MiPushMessage) {
        logger.logD("onReceivePassThroughMessage(): $miPushMessage")
        try {
            XiaomiMessagingDelegate.handleMiPushMessage(miPushMessage)
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "Error while handling remote message", exception)
        }
    }

    override fun onReceiveRegisterResult(context: Context, miPushCommandMessage: MiPushCommandMessage) {
        try {
            logger.logD("onReceiveRegisterResult(): $miPushCommandMessage")
            XiaomiMessagingDelegate.registerXiaomiToken(miPushCommandMessage)
        } catch (exception: IllegalStateException) {
            Log.e(TAG, "Error while registering Xiaomi Token", exception)
        }
    }

    private companion object {
        private const val TAG = "Chat:"
    }
}
