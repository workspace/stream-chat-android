package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.adapters.SimpleListAdapter

public interface AttachmentViewHolderFactory {

    public fun attachmentViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SimpleListAdapter.ViewHolder<List<Attachment>>

}
