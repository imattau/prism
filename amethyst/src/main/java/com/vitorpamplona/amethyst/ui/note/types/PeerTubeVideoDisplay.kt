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
package com.vitorpamplona.amethyst.ui.note.types

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import com.vitorpamplona.amethyst.commons.richtext.BaseMediaContent
import com.vitorpamplona.amethyst.commons.richtext.MediaUrlVideo
import com.vitorpamplona.amethyst.ui.components.SensitivityWarning
import com.vitorpamplona.amethyst.ui.components.ZoomableContentView
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.quartz.peertube.UnifiedVideoItem

@Composable
fun PeerTubeVideoDisplay(
    videoItem: UnifiedVideoItem,
    roundedCorner: Boolean,
    contentScale: ContentScale,
    accountViewModel: AccountViewModel,
    showControls: Boolean = true,
) {
    val content by remember(videoItem.id) {
        mutableStateOf<BaseMediaContent>(
            MediaUrlVideo(
                url = videoItem.url,
                description = videoItem.description,
                hash = videoItem.hash,
                blurhash = videoItem.blurhash,
                dim = videoItem.dim,
                uri = videoItem.nostrUri ?: videoItem.url, // Use nostrUri if available, else fallback to video url
                authorName = videoItem.author,
                mimeType = videoItem.mimeType,
            ),
        )
    }

    SensitivityWarning(accountViewModel = accountViewModel) {
        // No Nostr Note for PeerTube, so pass null
        ZoomableContentView(
            content = content,
            roundedCorner = roundedCorner,
            contentScale = contentScale,
            accountViewModel = accountViewModel,
            showControls = showControls,
        )
    }
}
