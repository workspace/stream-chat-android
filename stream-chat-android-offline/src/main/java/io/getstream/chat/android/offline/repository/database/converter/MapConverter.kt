package io.getstream.chat.android.offline.repository.database.converter

import androidx.room.TypeConverter
import com.squareup.moshi.adapter
import io.getstream.chat.android.offline.repository.domain.channel.member.MemberEntity
import io.getstream.chat.android.offline.repository.domain.channel.userread.ChannelUserReadEntity

public class MapConverter {
    @OptIn(ExperimentalStdlibApi::class)
    private val stringMapAdapter = moshi.adapter<Map<String, String>>()
    @OptIn(ExperimentalStdlibApi::class)
    private val intMapAdapter = moshi.adapter<Map<String, Int>>()
    @OptIn(ExperimentalStdlibApi::class)
    private val channelUserReadMapAdapter = moshi.adapter<Map<String, ChannelUserReadEntity>>()
    @OptIn(ExperimentalStdlibApi::class)
    private val memberEntityMapAdapter = moshi.adapter<Map<String, MemberEntity>>()

    @TypeConverter
    public fun readMapToString(someObjects: Map<String, ChannelUserReadEntity>?): String {
        return channelUserReadMapAdapter.toJson(someObjects)
    }

    @TypeConverter
    public fun stringToReadMap(data: String?): Map<String, ChannelUserReadEntity>? {
        if (data.isNullOrEmpty() || data == "null") {
            return mutableMapOf()
        }
        return channelUserReadMapAdapter.fromJson(data)
    }

    @TypeConverter
    public fun memberMapToString(someObjects: Map<String, MemberEntity>?): String? {
        return memberEntityMapAdapter.toJson(someObjects)
    }

    @TypeConverter
    public fun stringToMemberMap(data: String?): Map<String, MemberEntity>? {
        if (data.isNullOrEmpty() || data == "null") {
            return emptyMap()
        }
        return memberEntityMapAdapter.fromJson(data)
    }

    @TypeConverter
    public fun stringToMap(data: String?): Map<String, Int>? {
        if (data.isNullOrEmpty() || data == "null") {
            return mutableMapOf()
        }
        return intMapAdapter.fromJson(data)
    }

    @TypeConverter
    public fun mapToString(someObjects: Map<String, Int>?): String? {
        return intMapAdapter.toJson(someObjects)
    }

    @TypeConverter
    public fun stringToStringMap(data: String?): Map<String, String>? {
        if (data.isNullOrEmpty() || data == "null") {
            return mutableMapOf()
        }
        return stringMapAdapter.fromJson(data)
    }

    @TypeConverter
    public fun stringMapToString(someObjects: Map<String, String>?): String? {
        return stringMapAdapter.toJson(someObjects)
    }
}
