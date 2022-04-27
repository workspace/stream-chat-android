package io.getstream.chat.android.offline.repository.database.converter

import androidx.room.TypeConverter
import com.squareup.moshi.adapter

public class ListConverter {
    @OptIn(ExperimentalStdlibApi::class)
    private val adapter = moshi.adapter<List<String>>()

    @TypeConverter
    public fun stringToStringList(data: String?): List<String>? {
        if (data.isNullOrEmpty() || data == "null") {
            return emptyList()
        }
        return adapter.fromJson(data)
    }

    @TypeConverter
    public fun stringListToString(someObjects: List<String>?): String? {
        return adapter.toJson(someObjects)
    }
}
