package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result

public interface SendReactionListener {

    public fun onReactionSendRequest(
        currentUser: User,
        reaction: Reaction,
        enforceUnique: Boolean = false,
    )

    public fun onReactionSendResult(
        result: Result<Reaction>,
        currentUser: User,
        reaction: Reaction,
        enforceUnique: Boolean = false,
    )
}
