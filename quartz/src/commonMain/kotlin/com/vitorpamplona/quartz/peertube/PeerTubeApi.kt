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
package com.vitorpamplona.quartz.peertube

import com.vitorpamplona.quartz.nip94FileMetadata.tags.DimensionTag
import com.vitorpamplona.quartz.utils.Log
import com.vitorpamplona.quartz.utils.TimeUtils
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PeerTubeChannelConfig(
    // e.g., "my-awesome-channel"
    val channelName: String,
    // e.g., "https://peertube.example.com"
    val instanceUrl: String,
)

data class UnifiedVideoItem(
    // Unique identifier for the video (Nostr event ID or PeerTube video ID)
    val id: String,
    // Direct URL to the video stream or file
    val url: String,
    // URL for the video thumbnail
    val thumbnailUrl: String?,
    // Title of the video
    val title: String?,
    // Description of the video
    val description: String?,
    // Display name of the video's author/channel
    val author: String?,
    // Public key of the author (for Nostr events)
    val authorPubKey: String?,
    // MIME type of the video (e.g., "video/mp4")
    val mimeType: String?,
    // Duration of the video in milliseconds
    val durationMs: Long?,
    // Enum indicating the source (Nostr or PeerTube)
    val source: VideoSource,
    // Timestamp of the event/upload
    val createdAt: Long? = null,
    // Blurhash for the thumbnail, if available
    val blurhash: String? = null,
    // Dimensions of the video/thumbnail
    val dim: DimensionTag? = null,
    // Hash of the video file, if available
    val hash: String? = null,
    // PeerTube channel handle (when the video came from PeerTube)
    val channelHandle: String? = null,
    // PeerTube instance URL hosting the video
    val channelInstanceUrl: String? = null,
    // Nostr URI for the event, if source is Nostr
    val nostrUri: String? = null,
)

enum class VideoSource {
    NOSTR,
    PEERTUBE,
}

// Data classes for PeerTube API response
@Serializable
data class PeerTubeVideoListResponse(
    val total: Int,
    val data: List<PeerTubeVideo>,
    val pages: Int,
    val currentPage: Int,
    val totalCount: Int,
    val count: Int,
    val previous: String?,
    val next: String?,
)

@Serializable
data class PeerTubeVideo(
    val uuid: String,
    @SerialName("name") val title: String,
    val description: String,
    @SerialName("thumbnailPath") val thumbnailUrl: String?,
    // ISO 8601 format
    @SerialName("publishedAt") val publishedAt: String,
    @SerialName("duration") val durationSec: Long,
    val channel: PeerTubeChannel,
    // Not directly used for UnifiedVideoItem, but potentially useful
    val latency: List<LatencyInfo>?,
    // Path for embedding, may contain video URL
    val embedPath: String?,
    // May contain video file name
    val uuidName: String?,
)

@Serializable
data class PeerTubeChannel(
    val name: String,
    val handle: String,
)

@Serializable
data class LatencyInfo(
    val url: String,
    val ping: Double,
)

class PeerTubeApi(
    private val httpClient: HttpClient = defaultHttpClient,
) {
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    suspend fun getVideosForChannel(
        channelHandle: String,
        instanceUrl: String,
        count: Int = 20,
        start: Int = 0,
    ): Result<List<UnifiedVideoItem>> =
        runCatching {
            val normalizedInstanceUrl = instanceUrl.trimEnd('/')
            val url = "$normalizedInstanceUrl/api/v1/video-channels/$channelHandle/videos?count=$count&start=$start"
            val response = httpClient.get(url)
            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                throw IllegalStateException("PeerTube API ${response.status.value}: ${errorBody.take(256)}")
            }
            val videoListResponse: PeerTubeVideoListResponse = response.body()

            videoListResponse.data.mapNotNull { video ->
                video.toUnifiedVideoItem(instanceUrl)
            }
        }.onFailure { e ->
            Log.e("PeerTubeApi", "Error fetching videos for channel $channelHandle from $instanceUrl", e)
            _errorState.value = "Error fetching videos from PeerTube: ${e.message ?: "Unknown error"}"
        }.onSuccess {
            _errorState.value = null // Clear error state on success
        }

    private fun PeerTubeVideo.toUnifiedVideoItem(instanceUrl: String): UnifiedVideoItem {
        val directVideoUrl = embedPath?.let { instanceUrl + it } // This might be the embed URL, not direct video. Need to check PeerTube API docs for direct video link.

        return UnifiedVideoItem(
            id = uuid,
            url = directVideoUrl ?: "$instanceUrl/w/$uuidName", // Fallback URL, needs verification
            thumbnailUrl = thumbnailUrl?.let { instanceUrl + it },
            title = title,
            description = description,
            author = channel.name,
            authorPubKey = null, // Not available from this API endpoint
            mimeType = null, // PeerTube API might provide this, needs checking
            durationMs = durationSec * 1000,
            source = VideoSource.PEERTUBE,
            createdAt = TimeUtils.parseDate(publishedAt),
            blurhash = null, // Not available from this API endpoint
            dim = null, // Not available from this API endpoint
            hash = null, // Not available from this API endpoint
            channelHandle = channel.handle,
            channelInstanceUrl = instanceUrl,
            nostrUri = null,
        )
    }

    companion object {
        val defaultHttpClient =
            HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            prettyPrint = true
                            isLenient = true
                            ignoreUnknownKeys = true
                        },
                    )
                }
            }
    }
}
