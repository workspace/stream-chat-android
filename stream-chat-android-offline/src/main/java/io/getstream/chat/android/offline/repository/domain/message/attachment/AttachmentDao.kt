package io.getstream.chat.android.offline.repository.domain.message.attachment

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
public interface AttachmentDao {
    @Query("SELECT * FROM attachment_inner_entity WHERE messageId == :messageId")
    public fun observeAttachmentsForMessage(messageId: String): Flow<List<AttachmentEntity>>
}
