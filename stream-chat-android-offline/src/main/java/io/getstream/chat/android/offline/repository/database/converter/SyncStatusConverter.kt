package io.getstream.chat.android.offline.repository.database.converter

import androidx.room.TypeConverter
import io.getstream.chat.android.client.utils.SyncStatus

public class SyncStatusConverter {
    @TypeConverter
    public fun stringToSyncStatus(data: Int): SyncStatus {
        return SyncStatus.fromInt(data)!!
    }

    @TypeConverter
    public fun syncStatusToString(syncStatus: SyncStatus): Int {
        return syncStatus.status
    }
}
