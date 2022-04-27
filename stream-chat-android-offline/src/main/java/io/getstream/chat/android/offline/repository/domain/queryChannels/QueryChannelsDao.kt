package io.getstream.chat.android.offline.repository.domain.queryChannels

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
public abstract class QueryChannelsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract suspend fun insert(queryChannelsEntity: QueryChannelsEntity)

    @Transaction
    @Query("SELECT * FROM stream_channel_query WHERE stream_channel_query.id=:id")
    public abstract suspend fun select(id: String): QueryChannelsEntity?
}
