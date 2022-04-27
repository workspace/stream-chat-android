package io.getstream.chat.android.offline.repository.domain.syncState

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
public interface SyncStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public suspend fun insert(syncStateEntity: SyncStateEntity)

    @Query(
        "SELECT * FROM stream_sync_state " +
            "WHERE stream_sync_state.userId = :userId"
    )
    public suspend fun select(userId: String): SyncStateEntity?
}
