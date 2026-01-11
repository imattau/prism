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
package com.vitorpamplona.amethyst.ui.theme

import androidx.annotation.StringRes
import com.vitorpamplona.amethyst.R

object FontScale {
    data class Preset(
        @StringRes val labelResId: Int,
        val multiplier: Float,
    )

    val presets =
        listOf(
            Preset(R.string.font_scale_smaller, 0.85f),
            Preset(R.string.font_scale_small, 0.93f),
            Preset(R.string.font_scale_normal, 1.0f),
            Preset(R.string.font_scale_large, 1.1f),
            Preset(R.string.font_scale_extra_large, 1.2f),
        )

    fun multiplierFor(index: Int): Float = presets.getOrNull(index)?.multiplier ?: 1.0f
}
