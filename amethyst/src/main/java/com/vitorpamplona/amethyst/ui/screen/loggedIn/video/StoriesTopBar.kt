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
package com.vitorpamplona.amethyst.ui.screen.loggedIn.video

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitorpamplona.amethyst.FeatureFlags
import com.vitorpamplona.amethyst.LocalPreferences
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.service.relayClient.reqCommand.user.observeUserPicture
import com.vitorpamplona.amethyst.ui.components.RobohashFallbackAsyncImage
import com.vitorpamplona.amethyst.ui.navigation.drawer.AccountSwitchBottomSheet
import com.vitorpamplona.amethyst.ui.navigation.navs.INav
import com.vitorpamplona.amethyst.ui.navigation.routes.Route
import com.vitorpamplona.amethyst.ui.navigation.topbars.FeedFilterSpinner
import com.vitorpamplona.amethyst.ui.navigation.topbars.ShorterTopAppBar
import com.vitorpamplona.amethyst.ui.navigation.topbars.UserDrawerSearchTopBar
import com.vitorpamplona.amethyst.ui.note.SearchIcon
import com.vitorpamplona.amethyst.ui.screen.AccountStateViewModel
import com.vitorpamplona.amethyst.ui.screen.FeedDefinition
import com.vitorpamplona.amethyst.ui.screen.TopNavFilterState
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.screen.loggedIn.keyBackup.AccountBackupDialog
import com.vitorpamplona.amethyst.ui.stringRes
import com.vitorpamplona.amethyst.ui.theme.HeaderPictureModifier
import com.vitorpamplona.amethyst.ui.theme.Size22Modifier
import com.vitorpamplona.amethyst.ui.theme.placeholderText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoriesTopBar(
    accountViewModel: AccountViewModel,
    accountStateViewModel: AccountStateViewModel,
    nav: INav,
) {
    if (FeatureFlags.isPrism) {
        var menuExpanded by remember { mutableStateOf(false) }
        var backupDialogOpen by remember { mutableStateOf(false) }
        var accountSwitcherOpen by remember { mutableStateOf(false) }
        var logoutDialogOpen by remember { mutableStateOf(false) }
        val userProfile = accountViewModel.userProfile()
        val scope = rememberCoroutineScope()
        val sheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { it != SheetValue.PartiallyExpanded },
            )
        val accounts by LocalPreferences.accountsFlow().collectAsStateWithLifecycle()
        val currentAccount =
            accounts?.firstOrNull { it.npub == accountStateViewModel.currentAccountNPub() }
        val newestFirst by accountViewModel.account.settings.videoFeedNewestFirst
            .collectAsStateWithLifecycle()

        ShorterTopAppBar(
            title = {
                val list by accountViewModel.account.settings.defaultStoriesFollowList
                    .collectAsStateWithLifecycle()

                TopNavFilterBar(
                    followListsModel = accountViewModel.feedStates.feedListOptions,
                    listName = list,
                    accountViewModel = accountViewModel,
                ) { listName ->
                    accountViewModel.account.settings.changeDefaultStoriesFollowList(listName.code)
                }
            },
            navigationIcon = {
                IconButton(onClick = { menuExpanded = true }) {
                    val profilePicture by observeUserPicture(userProfile, accountViewModel)
                    RobohashFallbackAsyncImage(
                        robot = userProfile.pubkeyHex,
                        model = profilePicture,
                        contentDescription = stringRes(id = R.string.your_profile_image),
                        modifier = HeaderPictureModifier,
                        contentScale = ContentScale.Crop,
                        loadProfilePicture = accountViewModel.settings.showProfilePictures(),
                        loadRobohash = accountViewModel.settings.isNotPerformanceMode(),
                    )
                }
            },
            actions = {
                IconButton(onClick = { accountViewModel.account.settings.setVideoFeedNewestFirst(!newestFirst) }) {
                    val icon =
                        if (newestFirst) {
                            Icons.Filled.ArrowDownward
                        } else {
                            Icons.Filled.ArrowUpward
                        }
                    val description =
                        if (newestFirst) {
                            stringRes(id = R.string.video_feed_order_newest_first)
                        } else {
                            stringRes(id = R.string.video_feed_order_oldest_first)
                        }
                    Icon(
                        imageVector = icon,
                        contentDescription = description,
                        tint = MaterialTheme.colorScheme.placeholderText,
                    )
                }
                IconButton(onClick = { nav.nav(Route.Search) }) {
                    SearchIcon(modifier = Size22Modifier, MaterialTheme.colorScheme.placeholderText)
                }
            },
        )

        if (backupDialogOpen) {
            AccountBackupDialog(
                accountViewModel,
                onClose = { backupDialogOpen = false },
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringRes(id = R.string.profile)) },
                onClick = {
                    menuExpanded = false
                    nav.nav(Route.Profile(userProfile.pubkeyHex))
                },
            )
            DropdownMenuItem(
                text = { Text(stringRes(id = R.string.account_switch_select_account)) },
                onClick = {
                    menuExpanded = false
                    accountSwitcherOpen = true
                    scope.launch { sheetState.show() }
                },
            )
            DropdownMenuItem(
                text = { Text(stringRes(id = R.string.log_out)) },
                enabled = currentAccount != null,
                onClick = {
                    menuExpanded = false
                    logoutDialogOpen = currentAccount != null
                },
            )
            DropdownMenuItem(
                text = { Text(stringRes(id = R.string.preferences)) },
                onClick = {
                    menuExpanded = false
                    nav.nav(Route.Settings)
                },
            )
            DropdownMenuItem(
                text = { Text(stringRes(id = R.string.user_preferences)) },
                onClick = {
                    menuExpanded = false
                    nav.nav(Route.UserSettings)
                },
            )
            DropdownMenuItem(
                text = { Text(stringRes(id = R.string.security_filters)) },
                onClick = {
                    menuExpanded = false
                    nav.nav(Route.SecurityFilters)
                },
            )
            DropdownMenuItem(
                text = { Text(stringRes(id = R.string.privacy_options)) },
                onClick = {
                    menuExpanded = false
                    nav.nav(Route.PrivacyOptions)
                },
            )
            DropdownMenuItem(
                text = { Text(stringRes(id = R.string.relay_setup)) },
                onClick = {
                    menuExpanded = false
                    nav.nav(Route.EditRelays)
                },
            )
            DropdownMenuItem(
                text = { Text(stringRes(id = R.string.media_servers)) },
                onClick = {
                    menuExpanded = false
                    nav.nav(Route.EditMediaServers)
                },
            )
            DropdownMenuItem(
                text = { Text(stringRes(id = R.string.show_npub_as_a_qr_code)) },
                onClick = {
                    menuExpanded = false
                    nav.nav(Route.QRDisplay(userProfile.pubkeyHex))
                },
            )
            accountViewModel.account.settings.keyPair.privKey?.let {
                DropdownMenuItem(
                    text = { Text(stringRes(id = R.string.backup_keys)) },
                    onClick = {
                        menuExpanded = false
                        backupDialogOpen = true
                    },
                )
            }
        }

        if (accountSwitcherOpen) {
            ModalBottomSheet(
                onDismissRequest = {
                    scope
                        .launch { sheetState.hide() }
                        .invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                accountSwitcherOpen = false
                            }
                        }
                },
                sheetState = sheetState,
            ) {
                AccountSwitchBottomSheet(
                    accountViewModel = accountViewModel,
                    accountStateViewModel = accountStateViewModel,
                )
            }
        }

        if (logoutDialogOpen && currentAccount != null) {
            AlertDialog(
                title = { Text(text = stringRes(R.string.log_out)) },
                text = { Text(text = stringRes(R.string.are_you_sure_you_want_to_log_out)) },
                onDismissRequest = { logoutDialogOpen = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            logoutDialogOpen = false
                            accountStateViewModel.logOff(currentAccount)
                        },
                    ) {
                        Text(text = stringRes(R.string.log_out))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { logoutDialogOpen = false },
                    ) {
                        Text(text = stringRes(R.string.cancel))
                    }
                },
            )
        }
    } else {
        UserDrawerSearchTopBar(accountViewModel, nav) {
            val list by accountViewModel.account.settings.defaultStoriesFollowList
                .collectAsStateWithLifecycle()

            TopNavFilterBar(
                followListsModel = accountViewModel.feedStates.feedListOptions,
                listName = list,
                accountViewModel = accountViewModel,
            ) { listName ->
                accountViewModel.account.settings.changeDefaultStoriesFollowList(listName.code)
            }
        }
    }
}

@Composable
private fun TopNavFilterBar(
    followListsModel: TopNavFilterState,
    listName: String,
    accountViewModel: AccountViewModel,
    onChange: (FeedDefinition) -> Unit,
) {
    val allLists by followListsModel.kind3GlobalPeopleRoutes.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val savedHashtags by accountViewModel.account.hashtagList.flow
        .collectAsStateWithLifecycle()
    val hashtagFilter = if (listName.startsWith("Hashtag/")) listName.removePrefix("Hashtag/") else null
    val canSaveHashtag =
        hashtagFilter != null &&
            hashtagFilter.isNotBlank() &&
            !savedHashtags.contains(hashtagFilter.lowercase())

    Row(verticalAlignment = Alignment.CenterVertically) {
        FeedFilterSpinner(
            placeholderCode = listName,
            explainer = stringRes(R.string.select_list_to_filter),
            options = allLists,
            onSelect = { onChange(allLists.getOrNull(it) ?: followListsModel.allFollows) },
            accountViewModel = accountViewModel,
        )

        if (canSaveHashtag) {
            IconButton(
                onClick = {
                    scope.launch { accountViewModel.account.followHashtag(hashtagFilter.lowercase()) }
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringRes(id = R.string.save_hashtag_filter),
                    modifier = Size22Modifier,
                    tint = MaterialTheme.colorScheme.placeholderText,
                )
            }
        }
    }
}
