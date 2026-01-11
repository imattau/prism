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
package com.vitorpamplona.amethyst.ui.screen.loggedIn.threadview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitorpamplona.amethyst.FeatureFlags
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.service.relayClient.reqCommand.event.EventFinderFilterAssemblerSubscription
import com.vitorpamplona.amethyst.ui.components.ClickableBox
import com.vitorpamplona.amethyst.ui.components.LoadNote
import com.vitorpamplona.amethyst.ui.feeds.WatchLifecycleAndUpdateModel
import com.vitorpamplona.amethyst.ui.layouts.DisappearingScaffold
import com.vitorpamplona.amethyst.ui.navigation.navs.INav
import com.vitorpamplona.amethyst.ui.navigation.routes.Route
import com.vitorpamplona.amethyst.ui.navigation.routes.routeReplyTo
import com.vitorpamplona.amethyst.ui.navigation.topbars.TopBarExtensibleWithBackButton
import com.vitorpamplona.amethyst.ui.note.BoostText
import com.vitorpamplona.amethyst.ui.note.LikeReaction
import com.vitorpamplona.amethyst.ui.note.ObserveBoostIcon
import com.vitorpamplona.amethyst.ui.note.ReplyReaction
import com.vitorpamplona.amethyst.ui.note.RepostedIcon
import com.vitorpamplona.amethyst.ui.note.ZapReaction
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.screen.loggedIn.threadview.dal.ThreadFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.loggedIn.threadview.datasources.ThreadFilterAssemblerSubscription
import com.vitorpamplona.amethyst.ui.stringRes
import com.vitorpamplona.amethyst.ui.theme.DividerThickness
import com.vitorpamplona.amethyst.ui.theme.Size35Modifier
import com.vitorpamplona.amethyst.ui.theme.Size40Modifier
import com.vitorpamplona.amethyst.ui.theme.Size40dp

@Composable
fun ThreadScreen(
    noteId: String?,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    if (noteId == null) return

    if (FeatureFlags.isPrism) {
        BackHandler { nav.newStack(Route.Video) }
    }

    val feedViewModel: ThreadFeedViewModel =
        viewModel(
            key = noteId + "NostrThreadFeedViewModel",
            factory = ThreadFeedViewModel.Factory(accountViewModel.account, noteId),
        )

    WatchLifecycleAndUpdateModel(feedViewModel)
    ThreadFilterAssemblerSubscription(noteId, accountViewModel)

    LoadNote(noteId, accountViewModel) {
        if (it != null) {
            // this will force loading every post from this thread.
            EventFinderFilterAssemblerSubscription(it, accountViewModel)
        }
    }

    val bottomBar: (@Composable () -> Unit)? =
        if (FeatureFlags.isPrism) {
            {
                LoadNote(noteId, accountViewModel) { note ->
                    if (note != null) {
                        ThreadActionBar(note, accountViewModel, nav)
                    }
                }
            }
        } else {
            null
        }

    DisappearingScaffold(
        isInvertedLayout = false,
        topBar = {
            TopBarExtensibleWithBackButton(
                title = { Text(stringRes(id = R.string.thread_title)) },
                popBack =
                    if (FeatureFlags.isPrism) {
                        { nav.newStack(Route.Video) }
                    } else {
                        nav::popBack
                    },
            )
        },
        bottomBar = bottomBar,
        accountViewModel = accountViewModel,
    ) {
        Column(Modifier.padding(it)) {
            ThreadFeedView(noteId, feedViewModel, accountViewModel, nav)
        }
    }
}

@Composable
private fun ThreadActionBar(
    baseNote: Note,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    Column {
        HorizontalDivider(thickness = DividerThickness)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ReplyReaction(
                baseNote = baseNote,
                grayTint = MaterialTheme.colorScheme.onBackground,
                accountViewModel = accountViewModel,
                iconSizeModifier = Size40Modifier,
                showCounter = true,
            ) {
                nav.nav { routeReplyTo(baseNote, accountViewModel.account) }
            }

            RepostOnlyReaction(
                baseNote = baseNote,
                grayTint = MaterialTheme.colorScheme.onBackground,
                accountViewModel = accountViewModel,
            )

            LikeReaction(
                baseNote = baseNote,
                grayTint = MaterialTheme.colorScheme.onBackground,
                accountViewModel = accountViewModel,
                nav = nav,
                iconSize = Size40dp,
                heartSizeModifier = Size35Modifier,
            )

            ZapReaction(
                baseNote = baseNote,
                grayTint = MaterialTheme.colorScheme.onBackground,
                accountViewModel = accountViewModel,
                iconSize = Size40dp,
                iconSizeModifier = Size40Modifier,
                animationModifier = Size35Modifier,
                nav = nav,
            )
        }
    }
}

@Composable
private fun RepostOnlyReaction(
    baseNote: Note,
    grayTint: Color,
    accountViewModel: AccountViewModel,
) {
    ClickableBox(
        modifier = Size40Modifier,
        onClick = {
            accountViewModel.tryBoost(baseNote) {
                accountViewModel.boost(baseNote)
            }
        },
    ) {
        ObserveBoostIcon(baseNote, accountViewModel) { hasBoosted ->
            RepostedIcon(Size40Modifier, if (hasBoosted) Color.Unspecified else grayTint)
        }
    }

    BoostText(baseNote, grayTint, accountViewModel)
}
