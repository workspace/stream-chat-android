package io.getstream.chat.android.offline.repository.domain.reaction

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

@Dao
public interface ReactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insert(reactionEntity: ReactionEntity)

    @Query("SELECT * FROM stream_chat_reaction WHERE stream_chat_reaction.syncStatus IN (:syncStatus)")
    public suspend fun selectSyncNeeded(syncStatus: SyncStatus = SyncStatus.SYNC_NEEDED): List<ReactionEntity>

    @Query("SELECT * FROM stream_chat_reaction WHERE stream_chat_reaction.messageid = :messageId AND userId = :userId")
    public suspend fun selectUserReactionsToMessage(messageId: String, userId: String): List<ReactionEntity>

    @Query("UPDATE stream_chat_reaction SET deletedAt = :deletedAt WHERE userId = :userId AND messageId = :messageId")
    public suspend fun setDeleteAt(userId: String, messageId: String, deletedAt: Date)
}
