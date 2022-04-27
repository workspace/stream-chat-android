package io.getstream.chat.ui.sample.database

import android.app.Application
import android.database.Cursor
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.util.CursorUtil
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.repository.database.ChatDatabase
import io.getstream.chat.android.offline.repository.database.converter.DateConverter
import io.getstream.chat.android.offline.repository.database.converter.ExtraDataConverter
import io.getstream.chat.android.offline.repository.database.converter.ListConverter
import io.getstream.chat.android.offline.repository.database.converter.MapConverter
import io.getstream.chat.android.offline.repository.database.converter.SetConverter
import io.getstream.chat.android.offline.repository.database.converter.SyncStatusConverter
import io.getstream.chat.android.offline.repository.domain.channel.ChannelEntity
import io.getstream.chat.android.offline.repository.domain.channel.member.MemberEntity
import io.getstream.chat.android.offline.repository.domain.channel.userread.ChannelUserReadEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.random.Random

private const val TAG = "DatabaseViewModel"

class DatabaseViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DatabaseViewModel(application) as T
    }
}

class DatabaseViewModel(
    private val application: Application,
) : ViewModel() {


    private val database by lazy { ChatDatabase.getDatabase(application, userId = "test") }
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    private val __dateConverter = DateConverter()

    private val __mapConverter = MapConverter()

    private val __listConverter = ListConverter()

    private val __extraDataConverter = ExtraDataConverter()

    private val __syncStatusConverter = SyncStatusConverter()

    private val __setConverter = SetConverter()

    private fun generateString(length: Int): String {
        return (1..length)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    fun onDeleteClick() {
        viewModelScope.launch {
            val traceId = Random.nextInt(100)
            logD("[onDeleteClick] no args ($traceId)")
            withContext(Dispatchers.IO) {
                logV("[onDeleteClick] switched to IO ($traceId)")
                val count = database.channelStateDao().deleteAll()
                logV("[onDeleteClick] deleted($traceId): $count")
            }
            logV("[onDeleteClick] completed($traceId)")
        }
    }

    fun onGenerateClick() {
        viewModelScope.launch {
            val traceId = Random.nextInt(100)
            logD("[onGenerateClick] no args ($traceId)")
            withContext(Dispatchers.IO) {
                logV("[onGenerateClick] switched to IO ($traceId)")
                val channels = generateChannels(count = 500, memberCount = 100)
                logV("[onGenerateClick] generated($traceId)")
                database.channelStateDao().insertMany(channels)
                logV("[onGenerateClick] inserted($traceId): ${channels.size}")
            }
            logV("[onGenerateClick] completed($traceId)")
        }
    }

    fun onReadClick() {
        viewModelScope.launch {
            val traceId = Random.nextInt(100)
            logD("[onReadClick] no args ($traceId)")
            withContext(Dispatchers.IO) {
                logV("[onReadClick] switched to IO ($traceId)")
                val result = database.channelStateDao().selectSyncNeeded(SyncStatus.SYNC_NEEDED)
                logV("[onReadClick] received($traceId): ${result.size}")
            }
            logV("[onReadClick] completed($traceId)")
        }
    }

    fun onReadManyClick() {
        viewModelScope.launch {
            val traceId = Random.nextInt(100)
            logD("[onReadClick] no args ($traceId)")
            val result = (0..10_000).map { index ->
                async(Dispatchers.IO) {
                    logV("[onReadClick] switched to IO ($traceId-$index)")
                    database.query("SELECT * FROM stream_chat_channel_state WHERE stream_chat_channel_state.syncStatus IN (-1)", null).also { cursor ->
                        logV("[onReadClick] received ($traceId-$index): ${cursor.count}")
                        simulateChannelDaoImpl(cursor)
                        logV("[onReadClick] simulation finished ($traceId-$index)")
                    }
                    /*
                    database.channelStateDao().selectSyncNeeded(SyncStatus.SYNC_NEEDED).also {
                        logV("[onReadClick] received($traceId-$index): ${it.size}")
                    }
                    */
                }
            }.awaitAll()
            logV("[onReadClick] completed($traceId): ${result.size}")
        }
    }

    private fun generateChannels(count: Int, memberCount: Int): List<ChannelEntity> {
        return (0 until count).map { index ->
            val channelId = "${index}_${generateString(8)}"
            ChannelEntity(
                type = "messaging",
                channelId = channelId,
                cooldown = 0,
                frozen = false,
                createdAt = Date(),
                updatedAt = Date(),
                deletedAt = null,
                extraData = generateExtraData(count = 20),
                syncStatus = SyncStatus.SYNC_NEEDED,
                hidden = false,
                hideMessagesBefore = Date(),
                members = generateMembers(count = memberCount),
                memberCount = 100,
                reads = generateReads(count = 100),
                lastMessageId = generateString(length = 10),
                lastMessageAt = Date(),
                createdByUserId = "createdBy.id",
                watcherIds = listOf(),
                watcherCount = 100,
                team = "team",
                ownCapabilities = setOf(
                    "ban-channel-members",
                    "connect-events",
                    "delete-own-message",
                    "flag-message",
                    "freeze-channel",
                    "join-channel",
                    "leave-channel",
                    "mute-channel",
                    "pin-message",
                    "quote-message",
                    "read-events",
                    "search-messages",
                    "send-custom-events",
                    "send-links",
                    "send-message",
                    "send-reaction",
                    "send-reply",
                    "send-typing-events",
                    "set-channel-cooldown",
                    "typing-events",
                    "update-channel",
                    "update-channel-members",
                    "update-own-message",
                    "upload-file"
                ),
            )
        }
    }

    private fun generateMembers(count: Int): Map<String, MemberEntity> {
        return (0 until count).map { index ->
            MemberEntity(
                userId = "${index}_${generateString(8)}",
                role = "member",
                createdAt = Date(),
                updatedAt = Date(),
                isInvited = false,
                inviteAcceptedAt = null,
                inviteRejectedAt = null,
                shadowBanned = false,
                banned = false,
                channelRole = "chat_member"
            )
        }.associateBy { it.userId }
    }

    private fun generateReads(count: Int): Map<String, ChannelUserReadEntity> {
        return (0 until count).map { index ->
            ChannelUserReadEntity(
                userId = "${index}_${generateString(8)}",
                lastRead = Date(),
                unreadMessages = Random.nextInt(until = 100),
                lastMessageSeenDate = Date()
            )
        }.associateBy { it.userId }
    }

    private fun generateExtraData(count: Int): Map<String, Any> {
        return (0 until count).associate { index ->
            "${index}_${generateString(length = 8)}" to generateString(length = 20)
        }
    }

    private fun logD(message: String) {
        val thread = Thread.currentThread().run { "${id}:${name}" }
        Log.d(TAG, "($thread) $message")
    }

    private fun logV(message: String) {
        val thread = Thread.currentThread().run { "${id}:${name}" }
        Log.v(TAG, "($thread) $message")
    }

    private fun simulateChannelDaoImpl(_cursor: Cursor) {
        val _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type")
        val _cursorIndexOfChannelId = CursorUtil.getColumnIndexOrThrow(_cursor, "channelId")
        val _cursorIndexOfCooldown = CursorUtil.getColumnIndexOrThrow(_cursor, "cooldown")
        val _cursorIndexOfCreatedByUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "createdByUserId")
        val _cursorIndexOfFrozen = CursorUtil.getColumnIndexOrThrow(_cursor, "frozen")
        val _cursorIndexOfHidden = CursorUtil.getColumnIndexOrThrow(_cursor, "hidden")
        val _cursorIndexOfHideMessagesBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "hideMessagesBefore")
        val _cursorIndexOfMembers = CursorUtil.getColumnIndexOrThrow(_cursor, "members")
        val _cursorIndexOfMemberCount = CursorUtil.getColumnIndexOrThrow(_cursor, "memberCount")
        val _cursorIndexOfWatcherIds = CursorUtil.getColumnIndexOrThrow(_cursor, "watcherIds")
        val _cursorIndexOfWatcherCount = CursorUtil.getColumnIndexOrThrow(_cursor, "watcherCount")
        val _cursorIndexOfReads = CursorUtil.getColumnIndexOrThrow(_cursor, "reads")
        val _cursorIndexOfLastMessageAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageAt")
        val _cursorIndexOfLastMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageId")
        val _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt")
        val _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt")
        val _cursorIndexOfDeletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "deletedAt")
        val _cursorIndexOfExtraData = CursorUtil.getColumnIndexOrThrow(_cursor, "extraData")
        val _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus")
        val _cursorIndexOfTeam = CursorUtil.getColumnIndexOrThrow(_cursor, "team")
        val _cursorIndexOfOwnCapabilities = CursorUtil.getColumnIndexOrThrow(_cursor, "ownCapabilities")
        val _cursorIndexOfCid = CursorUtil.getColumnIndexOrThrow(_cursor, "cid")
        val _result: ArrayList<ChannelEntity> = ArrayList(_cursor.count)

        while (_cursor.moveToNext()) {
            val _item: ChannelEntity
            val _tmpType: String?
            _tmpType = if (_cursor.isNull(_cursorIndexOfType)) {
                null
            } else {
                _cursor.getString(_cursorIndexOfType)
            }
            val _tmpChannelId: String?
            _tmpChannelId = if (_cursor.isNull(_cursorIndexOfChannelId)) {
                null
            } else {
                _cursor.getString(_cursorIndexOfChannelId)
            }
            val _tmpCooldown: Int
            _tmpCooldown = _cursor.getInt(_cursorIndexOfCooldown)
            val _tmpCreatedByUserId: String?
            _tmpCreatedByUserId = if (_cursor.isNull(_cursorIndexOfCreatedByUserId)) {
                null
            } else {
                _cursor.getString(_cursorIndexOfCreatedByUserId)
            }
            val _tmpFrozen: Boolean
            val _tmp_1: Int
            _tmp_1 = _cursor.getInt(_cursorIndexOfFrozen)
            _tmpFrozen = _tmp_1 != 0
            val _tmpHidden: Boolean?
            val _tmp_2: Int?
            _tmp_2 = if (_cursor.isNull(_cursorIndexOfHidden)) {
                null
            } else {
                _cursor.getInt(_cursorIndexOfHidden)
            }
            _tmpHidden = if (_tmp_2 == null) null else _tmp_2 != 0
            val _tmpHideMessagesBefore: Date?
            val _tmp_3: Long?
            _tmp_3 = if (_cursor.isNull(_cursorIndexOfHideMessagesBefore)) {
                null
            } else {
                _cursor.getLong(_cursorIndexOfHideMessagesBefore)
            }
            _tmpHideMessagesBefore = __dateConverter.fromTimestamp(_tmp_3)
            val _tmpMembers: Map<String, MemberEntity>?
            val _tmp_4: String?
            _tmp_4 = if (_cursor.isNull(_cursorIndexOfMembers)) {
                null
            } else {
                _cursor.getString(_cursorIndexOfMembers)
            }
            _tmpMembers = __mapConverter.stringToMemberMap(_tmp_4)
            val _tmpMemberCount: Int
            _tmpMemberCount = _cursor.getInt(_cursorIndexOfMemberCount)
            val _tmpWatcherIds: List<String>?
            val _tmp_5: String?
            _tmp_5 = if (_cursor.isNull(_cursorIndexOfWatcherIds)) {
                null
            } else {
                _cursor.getString(_cursorIndexOfWatcherIds)
            }
            _tmpWatcherIds = __listConverter.stringToStringList(_tmp_5)
            val _tmpWatcherCount: Int
            _tmpWatcherCount = _cursor.getInt(_cursorIndexOfWatcherCount)
            val _tmpReads: Map<String, ChannelUserReadEntity>?
            val _tmp_6: String?
            _tmp_6 = if (_cursor.isNull(_cursorIndexOfReads)) {
                null
            } else {
                _cursor.getString(_cursorIndexOfReads)
            }
            _tmpReads = __mapConverter.stringToReadMap(_tmp_6)
            val _tmpLastMessageAt: Date?
            val _tmp_7: Long?
            _tmp_7 = if (_cursor.isNull(_cursorIndexOfLastMessageAt)) {
                null
            } else {
                _cursor.getLong(_cursorIndexOfLastMessageAt)
            }
            _tmpLastMessageAt = __dateConverter.fromTimestamp(_tmp_7)
            val _tmpLastMessageId: String?
            _tmpLastMessageId = if (_cursor.isNull(_cursorIndexOfLastMessageId)) {
                null
            } else {
                _cursor.getString(_cursorIndexOfLastMessageId)
            }
            val _tmpCreatedAt: Date?
            val _tmp_8: Long?
            _tmp_8 = if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
                null
            } else {
                _cursor.getLong(_cursorIndexOfCreatedAt)
            }
            _tmpCreatedAt = __dateConverter.fromTimestamp(_tmp_8)
            val _tmpUpdatedAt: Date?
            val _tmp_9: Long?
            _tmp_9 = if (_cursor.isNull(_cursorIndexOfUpdatedAt)) {
                null
            } else {
                _cursor.getLong(_cursorIndexOfUpdatedAt)
            }
            _tmpUpdatedAt = __dateConverter.fromTimestamp(_tmp_9)
            val _tmpDeletedAt: Date?
            val _tmp_10: Long?
            _tmp_10 = if (_cursor.isNull(_cursorIndexOfDeletedAt)) {
                null
            } else {
                _cursor.getLong(_cursorIndexOfDeletedAt)
            }
            _tmpDeletedAt = __dateConverter.fromTimestamp(_tmp_10)
            val _tmpExtraData: Map<String, Any>?
            val _tmp_11: String?
            _tmp_11 = if (_cursor.isNull(_cursorIndexOfExtraData)) {
                null
            } else {
                _cursor.getString(_cursorIndexOfExtraData)
            }
            _tmpExtraData = __extraDataConverter.stringToMap(_tmp_11)
            val _tmpSyncStatus: SyncStatus
            val _tmp_12: Int
            _tmp_12 = _cursor.getInt(_cursorIndexOfSyncStatus)
            _tmpSyncStatus = __syncStatusConverter.stringToSyncStatus(_tmp_12)
            val _tmpTeam: String?
            _tmpTeam = if (_cursor.isNull(_cursorIndexOfTeam)) {
                null
            } else {
                _cursor.getString(_cursorIndexOfTeam)
            }
            val _tmpOwnCapabilities: Set<String>?
            val _tmp_13: String?
            _tmp_13 = if (_cursor.isNull(_cursorIndexOfOwnCapabilities)) {
                null
            } else {
                _cursor.getString(_cursorIndexOfOwnCapabilities)
            }
            _tmpOwnCapabilities = __setConverter.stringToSortedSet(_tmp_13)
            _item = ChannelEntity(_tmpType!!,
                _tmpChannelId!!,
                _tmpCooldown,
                _tmpCreatedByUserId!!,
                _tmpFrozen,
                _tmpHidden,
                _tmpHideMessagesBefore,
                _tmpMembers!!,
                _tmpMemberCount,
                _tmpWatcherIds!!,
                _tmpWatcherCount,
                _tmpReads!!,
                _tmpLastMessageAt,
                _tmpLastMessageId,
                _tmpCreatedAt,
                _tmpUpdatedAt,
                _tmpDeletedAt,
                _tmpExtraData!!,
                _tmpSyncStatus,
                _tmpTeam!!,
                _tmpOwnCapabilities!!)
            val _tmpCid: String?
            _tmpCid = if (_cursor.isNull(_cursorIndexOfCid)) {
                null
            } else {
                _cursor.getString(_cursorIndexOfCid)
            }
            _item.cid = _tmpCid!!
            _result.add(_item)
        }
    }
}