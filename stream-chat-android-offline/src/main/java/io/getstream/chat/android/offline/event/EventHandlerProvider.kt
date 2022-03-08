package io.getstream.chat.android.offline.event

internal object EventHandlerProvider {

    private var instance: EventHandlerImpl? = null

    fun set(eventHandlerImpl: EventHandlerImpl) {
        instance = eventHandlerImpl
    }

    fun get(): EventHandlerImpl = instance ?: throw IllegalStateException(
        "EventHandlerImpl is not initialized yet. Did you set OfflinePlugin properly?"
    )
}
