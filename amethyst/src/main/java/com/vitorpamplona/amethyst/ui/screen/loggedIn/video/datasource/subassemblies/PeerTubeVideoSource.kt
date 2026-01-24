/**
 * Copyright (c) 2025 Vitor Pamplona
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.vitorpamplona.amethyst.ui.screen.loggedIn.video.datasource.subassemblies

import com.vitorpamplona.amethyst.model.Account
import com.vitorpamplona.quartz.peertube.PeerTubeApi
import com.vitorpamplona.quartz.peertube.PeerTubeChannelConfig
import com.vitorpamplona.quartz.peertube.UnifiedVideoItem
import com.vitorpamplona.quartz.utils.Log
import com.vitorpamplona.quartz.utils.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PeerTubeVideoSource(
    private val peerTubeApi: PeerTubeApi,
    private val account: Account,
    private val scope: CoroutineScope,
    private val fetchIntervalMs: Long = DEFAULT_FETCH_INTERVAL_MS,
) {
    private val _videos = MutableStateFlow<List<UnifiedVideoItem>>(emptyList())
    val videos: StateFlow<List<UnifiedVideoItem>> = _videos.asStateFlow()
    private val _isFetching = MutableStateFlow(false)
    val isFetching: StateFlow<Boolean> = _isFetching.asStateFlow()

    val errorState: StateFlow<String?> get() = peerTubeApi.errorState

    private var fetchJob: Job? = null

    init {
        startFetching()
    }

    fun refreshNow() {
        fetchJob?.let {
            scope.launch(Dispatchers.IO) {
                val channels = account.settings.peerTubeChannels.value
                if (channels.isNotEmpty()) {
                    fetchChannels(channels)
                }
            }
        }
    }

    private fun startFetching() {
        fetchJob =
            scope.launch(Dispatchers.IO) {
                account.settings.peerTubeChannels.collectLatest { channels ->
                    if (channels.isEmpty()) {
                        _videos.emit(emptyList())
                        return@collectLatest
                    }

                    while (isActive) {
                        fetchChannels(channels)
                        delay(fetchIntervalMs)
                    }
                }
            }
    }

    private suspend fun fetchChannels(channels: List<PeerTubeChannelConfig>) {
        val now = TimeUtils.now()
        val aggregatedVideos = mutableListOf<UnifiedVideoItem>()

        _isFetching.tryEmit(true)
        try {
            channels.forEach { config ->
                peerTubeApi
                    .getVideosForChannel(
                        channelHandle = config.channelName,
                        instanceUrl = config.instanceUrl,
                        count = MAX_VIDEOS_PER_CHANNEL,
                    ).onSuccess { videos ->
                        aggregatedVideos +=
                            videos.map { video ->
                                video.copy(createdAt = video.createdAt ?: now)
                            }
                    }.onFailure { error ->
                        Log.e(
                            "PeerTubeVideoSource",
                            "Failed to fetch videos for ${config.channelName}@${config.instanceUrl}",
                            error,
                        )
                    }
            }
        } finally {
            _isFetching.tryEmit(false)
        }

        val sortedVideos =
            aggregatedVideos.sortedWith(
                compareByDescending<UnifiedVideoItem> {
                    it.createdAt
                }.thenBy { it.id },
            )

        _videos.emit(sortedVideos)
    }

    fun destroy() {
        fetchJob?.cancel()
        fetchJob = null
    }

    companion object {
        const val DEFAULT_FETCH_INTERVAL_MS = 5 * 60 * 1000L
        const val MAX_VIDEOS_PER_CHANNEL = 50
    }
}
