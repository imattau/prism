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
package com.vitorpamplona.amethyst.ui.screen.loggedIn.profile.header

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.model.User
import com.vitorpamplona.amethyst.service.relayClient.reqCommand.account.observeAccountIsHiddenUser
import com.vitorpamplona.amethyst.ui.navigation.navs.INav
import com.vitorpamplona.amethyst.ui.navigation.routes.Route
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.screen.loggedIn.profile.ListButton
import com.vitorpamplona.amethyst.ui.screen.loggedIn.profile.zaps.ShowUserButton
import com.vitorpamplona.amethyst.ui.stringRes
import com.vitorpamplona.amethyst.ui.theme.Size20Modifier
import com.vitorpamplona.amethyst.ui.theme.ZeroPadding

@Composable
fun ProfileActions(
    baseUser: User,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    MessageButton(baseUser, accountViewModel, nav)

    val isMe by
        remember(accountViewModel, baseUser) {
            derivedStateOf { accountViewModel.userProfile().pubkeyHex == baseUser.pubkeyHex }
        }

    if (isMe) {
        EditButton(nav)
    }

    val sensitiveOverrides by accountViewModel.account.settings.sensitiveUserOverrides
        .collectAsStateWithLifecycle()
    val isSensitive = sensitiveOverrides.contains(baseUser.pubkeyHex)

    val isHidden by observeAccountIsHiddenUser(accountViewModel.account, baseUser)

    if (isHidden) {
        ShowUserButton { accountViewModel.showUser(baseUser.pubkeyHex) }
    } else {
        DisplayFollowUnfollowButton(baseUser, accountViewModel)

        SensitiveToggleButton(
            isSensitive = isSensitive,
            onToggle = { accountViewModel.account.settings.setSensitiveUserOverride(baseUser.pubkeyHex, !isSensitive) },
        )

        ListButton { nav.nav(Route.PeopleListManagement(baseUser.pubkeyHex)) }
    }
}

@Composable
private fun SensitiveToggleButton(
    isSensitive: Boolean,
    onToggle: () -> Unit,
) {
    FilledTonalButton(
        modifier =
            Modifier
                .padding(horizontal = 3.dp)
                .width(50.dp),
        onClick = onToggle,
        contentPadding = ZeroPadding,
    ) {
        Icon(
            imageVector = if (isSensitive) Icons.Rounded.Warning else Icons.Outlined.Warning,
            contentDescription = stringRes(R.string.profile_sensitive_toggle),
            modifier = Size20Modifier,
            tint = if (isSensitive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
