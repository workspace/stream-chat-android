package io.getstream.chat.android.offline.repository.database.converter

import androidx.room.TypeConverter
import java.util.Date

public class DateConverter {
    @TypeConverter
    public fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    public fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
