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
package com.vitorpamplona.amethyst.ui.screen

import android.content.Context
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.ui.stringRes
import com.vitorpamplona.quartz.peertube.PeerTubeChannelConfig
import com.vitorpamplona.quartz.peertube.UnifiedVideoItem
import com.vitorpamplona.quartz.peertube.VideoSource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

const val PEERTUBE_GROUP_CODE = "PeerTube/Group"
private const val PEERTUBE_CHANNEL_PREFIX = "PeerTube/Channel/"

fun peerTubeChannelCode(channel: PeerTubeChannelConfig): String {
    val encodedInstance = URLEncoder.encode(channel.instanceUrl, StandardCharsets.UTF_8.toString())
    val encodedChannel = URLEncoder.encode(channel.channelName, StandardCharsets.UTF_8.toString())
    return "$PEERTUBE_CHANNEL_PREFIX$encodedInstance/$encodedChannel"
}

fun buildPeerTubeFeedDefinitions(channels: List<PeerTubeChannelConfig>): ImmutableList<PeerTubeFeedDefinition> {
    val definitions = mutableListOf<PeerTubeFeedDefinition>()
    definitions +=
        PeerTubeFeedDefinition(
            code = PEERTUBE_GROUP_CODE,
            name = ResourceName(R.string.peertube_videos),
            type = CodeNameType.PEERTUBE,
            kinds = emptyList(),
            channelConfig = null,
        )
    channels.forEach { channel ->
        definitions +=
            PeerTubeFeedDefinition(
                code = peerTubeChannelCode(channel),
                name = PeerTubeChannelName(channel),
                type = CodeNameType.PEERTUBE,
                kinds = emptyList(),
                channelConfig = channel,
            )
    }
    return definitions.toImmutableList()
}

fun peerTubeFilterForCode(
    code: String,
    channels: List<PeerTubeChannelConfig>,
): PeerTubeFilter =
    when {
        code == PEERTUBE_GROUP_CODE -> PeerTubeFilter.All
        code.startsWith(PEERTUBE_CHANNEL_PREFIX) -> {
            val match = channels.firstOrNull { peerTubeChannelCode(it) == code }
            if (match != null) {
                PeerTubeFilter.Channel(match)
            } else {
                PeerTubeFilter.None
            }
        }
        else -> PeerTubeFilter.None
    }

sealed interface PeerTubeFilter {
    object None : PeerTubeFilter

    object All : PeerTubeFilter

    data class Channel(
        val channel: PeerTubeChannelConfig,
    ) : PeerTubeFilter
}

fun PeerTubeFilter.matches(video: UnifiedVideoItem): Boolean {
    if (video.source != VideoSource.PEERTUBE) return false
    return when (this) {
        is PeerTubeFilter.All -> true
        is PeerTubeFilter.Channel ->
            video.channelInstanceUrl == channel.instanceUrl && video.channelHandle == channel.channelName
        PeerTubeFilter.None -> false
    }
}

class PeerTubeChannelName(
    private val channel: PeerTubeChannelConfig,
) : Name() {
    override fun name(): String = "${channel.channelName} @ ${channel.instanceUrl}"

    override fun name(context: Context): String =
        stringRes(
            context,
            R.string.peertube_channel_label,
            channel.channelName,
            channel.instanceUrl,
        )
}

class PeerTubeFeedDefinition(
    code: String,
    name: Name,
    type: CodeNameType,
    val kinds: List<Int>,
    val channelConfig: PeerTubeChannelConfig?,
) : FeedDefinition(code, name, type, null)
