/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.offline.errorhandler.internal

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.querysort.QuerySorter
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.ReturnOnErrorCall
import io.getstream.chat.android.client.call.onErrorReturn
import io.getstream.chat.android.client.errorhandler.ErrorHandler
import io.getstream.chat.android.client.errorhandler.QueryMembersErrorHandler
import io.getstream.chat.android.client.extensions.internal.toCid
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import io.getstream.logging.StreamLog
import kotlinx.coroutines.CoroutineScope

/**
 * [QueryMembersErrorHandler] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Checks if the change was done offline and can be synced.
 *
 * @param scope [CoroutineScope]
 * @param clientState [ClientState] provided by the [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * @param repos [RepositoryFacade] to access datasource.
 */
internal class QueryMembersErrorHandlerImpl(
    private val scope: CoroutineScope,
    private val clientState: ClientState,
    private val channelRepository: ChannelRepository,
) : QueryMembersErrorHandler {

    private val logger = StreamLog.getLogger("QueryMembersError")

    override fun onQueryMembersError(
        originalCall: Call<List<Member>>,
        channelType: String,
        channelId: String,
        offset: Int,
        limit: Int,
        filter: FilterObject,
        sort: QuerySorter<Member>,
        members: List<Member>,
    ): ReturnOnErrorCall<List<Member>> {
        return originalCall.onErrorReturn(scope) { originalError ->
            logger.d {
                "An error happened while wuery members. " +
                    "Error message: ${originalError.message}. Full error: $originalCall"
            }

            if (clientState.isOnline) {
                Result.error(originalError)
            } else {
                // retrieve from database
                val clampedOffset = offset.coerceAtLeast(0)
                val clampedLimit = limit.coerceAtLeast(0)
                val membersFromDatabase = channelRepository
                    .selectMembersForChannel(Pair(channelType, channelId).toCid())
                    .sortedWith(sort.comparator)
                    .drop(clampedOffset)
                    .let { members ->
                        if (clampedLimit > 0) {
                            members.take(clampedLimit)
                        } else members
                    }
                Result(membersFromDatabase)
            }
        }
    }

    override val name: String
        get() = "QueryMembersErrorHandlerImpl"

    override val priority: Int
        get() = ErrorHandler.DEFAULT_PRIORITY
}
