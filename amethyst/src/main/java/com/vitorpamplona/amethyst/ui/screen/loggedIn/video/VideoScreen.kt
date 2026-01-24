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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitorpamplona.amethyst.FeatureFlags
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.commons.richtext.BaseMediaContent
import com.vitorpamplona.amethyst.commons.richtext.MediaUrlVideo
import com.vitorpamplona.amethyst.commons.ui.feeds.FeedContentState
import com.vitorpamplona.amethyst.commons.ui.feeds.FeedState
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.service.CachedRichTextParser
import com.vitorpamplona.amethyst.ui.actions.CrossfadeIfEnabled
import com.vitorpamplona.amethyst.ui.components.ClickableBox
import com.vitorpamplona.amethyst.ui.components.ClickableTextColor
import com.vitorpamplona.amethyst.ui.components.ObserveDisplayNip05Status
import com.vitorpamplona.amethyst.ui.components.RichTextViewer
import com.vitorpamplona.amethyst.ui.components.SensitivityWarning
import com.vitorpamplona.amethyst.ui.components.ZoomableContentView
import com.vitorpamplona.amethyst.ui.feeds.FeedEmpty
import com.vitorpamplona.amethyst.ui.feeds.FeedError
import com.vitorpamplona.amethyst.ui.feeds.LoadingFeed
import com.vitorpamplona.amethyst.ui.feeds.RefresheableBox
import com.vitorpamplona.amethyst.ui.feeds.ScrollStateKeys
import com.vitorpamplona.amethyst.ui.feeds.WatchLifecycleAndUpdateModel
import com.vitorpamplona.amethyst.ui.feeds.WatchScrollToTop
import com.vitorpamplona.amethyst.ui.feeds.rememberForeverPagerState
import com.vitorpamplona.amethyst.ui.layouts.DisappearingScaffold
import com.vitorpamplona.amethyst.ui.navigation.bottombars.AppBottomBar
import com.vitorpamplona.amethyst.ui.navigation.navs.INav
import com.vitorpamplona.amethyst.ui.navigation.routes.Route
import com.vitorpamplona.amethyst.ui.navigation.routes.routeFor
import com.vitorpamplona.amethyst.ui.note.BaseUserPicture
import com.vitorpamplona.amethyst.ui.note.BoostReaction
import com.vitorpamplona.amethyst.ui.note.BoostText
import com.vitorpamplona.amethyst.ui.note.CheckHiddenFeedWatchBlockAndReport
import com.vitorpamplona.amethyst.ui.note.LikeReaction
import com.vitorpamplona.amethyst.ui.note.NoteAuthorPicture
import com.vitorpamplona.amethyst.ui.note.NoteUsernameDisplay
import com.vitorpamplona.amethyst.ui.note.ObserveBoostIcon
import com.vitorpamplona.amethyst.ui.note.RenderAllRelayList
import com.vitorpamplona.amethyst.ui.note.ReplyReaction
import com.vitorpamplona.amethyst.ui.note.RepostedIcon
import com.vitorpamplona.amethyst.ui.note.ZapReaction
import com.vitorpamplona.amethyst.ui.note.elements.NoteDropDownMenu
import com.vitorpamplona.amethyst.ui.note.timeAgo
import com.vitorpamplona.amethyst.ui.note.types.FileHeaderDisplay
import com.vitorpamplona.amethyst.ui.note.types.FileStorageHeaderDisplay
import com.vitorpamplona.amethyst.ui.note.types.JustVideoDisplay
import com.vitorpamplona.amethyst.ui.note.types.PeerTubeVideoDisplay
import com.vitorpamplona.amethyst.ui.note.types.PictureDisplay
import com.vitorpamplona.amethyst.ui.screen.AccountStateViewModel
import com.vitorpamplona.amethyst.ui.screen.PeerTubeFilter
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.screen.loggedIn.video.datasource.VideoFilterAssemblerSubscription
import com.vitorpamplona.amethyst.ui.screen.loggedIn.video.datasource.subassemblies.PeerTubeVideoSource
import com.vitorpamplona.amethyst.ui.screen.matches
import com.vitorpamplona.amethyst.ui.screen.peerTubeFilterForCode
import com.vitorpamplona.amethyst.ui.stringRes
import com.vitorpamplona.amethyst.ui.theme.AuthorInfoVideoFeed
import com.vitorpamplona.amethyst.ui.theme.DoubleHorzSpacer
import com.vitorpamplona.amethyst.ui.theme.HalfFeedPadding
import com.vitorpamplona.amethyst.ui.theme.Size10dp
import com.vitorpamplona.amethyst.ui.theme.Size20Modifier
import com.vitorpamplona.amethyst.ui.theme.Size22Modifier
import com.vitorpamplona.amethyst.ui.theme.Size25dp
import com.vitorpamplona.amethyst.ui.theme.Size35Modifier
import com.vitorpamplona.amethyst.ui.theme.Size40Modifier
import com.vitorpamplona.amethyst.ui.theme.Size40dp
import com.vitorpamplona.amethyst.ui.theme.Size55dp
import com.vitorpamplona.amethyst.ui.theme.VideoReactionColumnPadding
import com.vitorpamplona.amethyst.ui.theme.placeholderText
import com.vitorpamplona.quartz.experimental.nip95.header.FileStorageHeaderEvent
import com.vitorpamplona.quartz.nip01Core.core.EmptyTagList
import com.vitorpamplona.quartz.nip01Core.core.Event
import com.vitorpamplona.quartz.nip01Core.core.firstTagValueFor
import com.vitorpamplona.quartz.nip01Core.core.toImmutableListOfLists
import com.vitorpamplona.quartz.nip01Core.tags.hashtags.hashtags
import com.vitorpamplona.quartz.nip10Notes.TextNoteEvent
import com.vitorpamplona.quartz.nip68Picture.PictureEvent
import com.vitorpamplona.quartz.nip71Video.VideoEvent
import com.vitorpamplona.quartz.nip94FileMetadata.FileHeaderEvent
import com.vitorpamplona.quartz.peertube.UnifiedVideoItem
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.seconds

sealed interface VideoFeedEntry {
    val id: String
    val createdAt: Long?

    class NostrEntry(
        val note: Note,
    ) : VideoFeedEntry {
        override val id: String
            get() = note.idHex

        override val createdAt: Long?
            get() = note.createdAt()
    }

    class PeerTubeEntry(
        val video: UnifiedVideoItem,
    ) : VideoFeedEntry {
        override val id: String = "peertube-${video.url.hashCode()}-${video.id}"
        override val createdAt: Long? = video.createdAt
    }
}

private fun mergeVideoEntries(
    notes: List<Note>,
    peerTubeVideos: List<UnifiedVideoItem>,
    newestFirst: Boolean,
): List<VideoFeedEntry> {
    val entries = mutableListOf<VideoFeedEntry>()
    entries += notes.map { VideoFeedEntry.NostrEntry(it) }
    entries += peerTubeVideos.map { VideoFeedEntry.PeerTubeEntry(it) }
    entries.sortWith(videoFeedEntryComparator(newestFirst))
    return entries
}

private fun videoFeedEntryComparator(newestFirst: Boolean): Comparator<VideoFeedEntry> =
    if (newestFirst) {
        compareByDescending<VideoFeedEntry> { it.createdAt ?: 0L }.thenBy { it.id }
    } else {
        compareBy<VideoFeedEntry> { it.createdAt ?: 0L }.thenBy { it.id }
    }

@Composable
fun VideoScreen(
    accountViewModel: AccountViewModel,
    accountStateViewModel: AccountStateViewModel,
    nav: INav,
) {
    VideoScreen(
        accountViewModel.feedStates.videoFeed,
        accountViewModel,
        accountStateViewModel,
        nav,
        accountViewModel.peerTubeVideoSource,
    )
}

@Composable
fun VideoScreen(
    videoFeedContentState: FeedContentState,
    accountViewModel: AccountViewModel,
    accountStateViewModel: AccountStateViewModel,
    nav: INav,
    peerTubeVideoSource: PeerTubeVideoSource,
) {
    val controlsVisible = remember { mutableStateOf(true) }
    val controlsTrigger = remember { mutableStateOf(0) }
    val detailsOverlayVisible = remember { mutableStateOf(false) }
    val toggleControls = {
        if (controlsVisible.value) {
            controlsVisible.value = false
        } else {
            controlsTrigger.value += 1
        }
    }

    LaunchedEffect(controlsTrigger.value) {
        controlsVisible.value = true
        delay(8.seconds)
        controlsVisible.value = false
    }

    WatchLifecycleAndUpdateModel(videoFeedContentState)
    WatchAccountForVideoScreen(videoFeedContentState = videoFeedContentState, accountViewModel = accountViewModel)
    VideoFilterAssemblerSubscription(accountViewModel)

    if (FeatureFlags.isPrism) {
        BackHandler {
            videoFeedContentState.sendToTop()
        }
    }

    DisappearingScaffold(
        isInvertedLayout = false,
        topBar = {
            StoriesTopBar(accountViewModel, accountStateViewModel, nav)
        },
        bottomBar =
            if (FeatureFlags.isPrism) {
                null
            } else {
                {
                    AppBottomBar(Route.Video, accountViewModel) { route ->
                        if (route == Route.Video) {
                            videoFeedContentState.sendToTop()
                        } else {
                            nav.newStack(route)
                        }
                    }
                }
            },
        floatingButton = {
            if (!FeatureFlags.isPrism) {
                NewImageButton(accountViewModel, nav, videoFeedContentState::sendToTop)
            }
        },
        accountViewModel = accountViewModel,
    ) {
        Column(
            modifier = Modifier.padding(it).consumeWindowInsets(it),
        ) {
            RenderPage(
                videoFeedContentState = videoFeedContentState,
                pagerStateKey = ScrollStateKeys.VIDEO_SCREEN,
                accountViewModel = accountViewModel,
                peerTubeVideoSource = peerTubeVideoSource,
                nav = nav,
                controlsVisible = controlsVisible.value,
                onUserInteraction = { controlsTrigger.value += 1 },
                onToggleControls = toggleControls,
                detailsOverlayVisible = detailsOverlayVisible.value,
                onDetailsOverlayVisibleChange = { detailsOverlayVisible.value = it },
            )
        }
    }
}

@Composable
fun WatchAccountForVideoScreen(
    videoFeedContentState: FeedContentState,
    accountViewModel: AccountViewModel,
) {
    val listState by accountViewModel.account.liveStoriesFollowLists.collectAsStateWithLifecycle()
    val videosOnly by accountViewModel.account.settings.videoFeedVideosOnly
        .collectAsStateWithLifecycle()
    val newestFirst by accountViewModel.account.settings.videoFeedNewestFirst
        .collectAsStateWithLifecycle()
    val hiddenUsers =
        accountViewModel.account.hiddenUsers.flow
            .collectAsStateWithLifecycle()

    LaunchedEffect(accountViewModel, listState, videosOnly, newestFirst, hiddenUsers) {
        videoFeedContentState.checkKeysInvalidateDataAndSendToTop()
    }
}

@Composable
fun RenderPage(
    videoFeedContentState: FeedContentState,
    pagerStateKey: String?,
    accountViewModel: AccountViewModel,
    peerTubeVideoSource: PeerTubeVideoSource,
    nav: INav,
    controlsVisible: Boolean,
    onUserInteraction: () -> Unit,
    onToggleControls: () -> Unit,
    detailsOverlayVisible: Boolean,
    onDetailsOverlayVisibleChange: (Boolean) -> Unit,
) {
    val feedState by videoFeedContentState.feedContent.collectAsStateWithLifecycle()
    val feedCode by accountViewModel.account.settings.defaultStoriesFollowList
        .collectAsStateWithLifecycle()
    val lastPositions by accountViewModel.account.settings.videoFeedLastPositions
        .collectAsStateWithLifecycle()
    val newestFirst by accountViewModel.account.settings.videoFeedNewestFirst
        .collectAsStateWithLifecycle()

    CrossfadeIfEnabled(
        targetState = feedState,
        animationSpec = tween(durationMillis = 100),
        label = "RenderPage",
        accountViewModel = accountViewModel,
    ) { state ->
        when (state) {
            is FeedState.Empty -> {
                FeedEmpty(videoFeedContentState::invalidateData)
            }
            is FeedState.FeedError -> {
                FeedError(state.errorMessage, videoFeedContentState::invalidateData)
            }
            is FeedState.Loaded -> {
                LoadedState(
                    state,
                    pagerStateKey,
                    videoFeedContentState,
                    accountViewModel,
                    peerTubeVideoSource,
                    newestFirst,
                    nav,
                    controlsVisible,
                    onUserInteraction,
                    feedCode,
                    lastPositions,
                    onToggleControls,
                    detailsOverlayVisible,
                    onDetailsOverlayVisibleChange,
                )
            }
            is FeedState.Loading -> {
                LoadingFeed()
            }
        }
    }
}

@Composable
private fun LoadedState(
    loaded: FeedState.Loaded,
    pagerStateKey: String?,
    videoFeedContentState: FeedContentState,
    accountViewModel: AccountViewModel,
    peerTubeVideoSource: PeerTubeVideoSource,
    newestFirst: Boolean,
    nav: INav,
    controlsVisible: Boolean,
    onUserInteraction: () -> Unit,
    feedCode: String,
    lastPositions: Map<String, String>,
    onToggleControls: () -> Unit,
    detailsOverlayVisible: Boolean,
    onDetailsOverlayVisibleChange: (Boolean) -> Unit,
) {
    val peerTubeChannels by accountViewModel.account.settings.peerTubeChannels
        .collectAsStateWithLifecycle()
    val peerTubeVideos by peerTubeVideoSource.videos.collectAsStateWithLifecycle()
    val peerTubeError by peerTubeVideoSource.errorState.collectAsStateWithLifecycle()
    val peerTubeFetching by peerTubeVideoSource.isFetching.collectAsStateWithLifecycle()
    val indicatorTopPadding = 5.dp
    val newNotesState = remember { mutableStateOf<List<VideoFeedEntry>>(emptyList()) }
    val isAtTopState = remember { mutableStateOf(true) }
    RefresheableBox(
        onRefresh = {
            videoFeedContentState.lastNoteCreatedAtWhenFullyLoaded.tryEmit(null)
            accountViewModel.dataSources().video.hardRefresh(accountViewModel.account)
            peerTubeVideoSource.refreshNow()
            videoFeedContentState.invalidateData()
        },
    ) {
        Box(Modifier.fillMaxSize()) {
            SlidingCarousel(
                loaded,
                pagerStateKey,
                videoFeedContentState,
                accountViewModel,
                peerTubeVideoSource,
                newestFirst,
                nav,
                newNotesState,
                isAtTopState,
                controlsVisible,
                onUserInteraction,
                feedCode,
                lastPositions,
                onToggleControls,
                detailsOverlayVisible,
                onDetailsOverlayVisibleChange,
            )

            PeerTubeFetchIndicator(
                hasChannels = peerTubeChannels.isNotEmpty(),
                videoCount = peerTubeVideos.size,
                errorMessage = peerTubeError,
                isFetching = peerTubeFetching,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = indicatorTopPadding),
            )

            if (FeatureFlags.isPrism && newNotesState.value.isNotEmpty() && !isAtTopState.value) {
                NewItemsPill(
                    newEntries = newNotesState.value,
                    accountViewModel = accountViewModel,
                    onClick = {
                        videoFeedContentState.sendToTop()
                        accountViewModel.dataSources().video.hardRefresh(accountViewModel.account)
                        videoFeedContentState.invalidateData()
                    },
                )
            }
        }
    }
}

@Composable
fun SlidingCarousel(
    loaded: FeedState.Loaded,
    pagerStateKey: String?,
    videoFeedContentState: FeedContentState,
    accountViewModel: AccountViewModel,
    peerTubeVideoSource: PeerTubeVideoSource,
    newestFirst: Boolean,
    nav: INav,
    newNotesState: androidx.compose.runtime.MutableState<List<VideoFeedEntry>>,
    isAtTopState: androidx.compose.runtime.MutableState<Boolean>,
    controlsVisible: Boolean,
    onUserInteraction: () -> Unit,
    feedCode: String,
    lastPositions: Map<String, String>,
    onToggleControls: () -> Unit,
    detailsOverlayVisible: Boolean,
    onDetailsOverlayVisibleChange: (Boolean) -> Unit,
) {
    val items by loaded.feed.collectAsStateWithLifecycle()
    val peerTubeVideos by peerTubeVideoSource.videos.collectAsStateWithLifecycle()
    val peerTubeChannels by accountViewModel.account.settings.peerTubeChannels
        .collectAsStateWithLifecycle()
    val combinedEntries =
        remember(items.list, peerTubeVideos, newestFirst) {
            mergeVideoEntries(items.list, peerTubeVideos, newestFirst)
        }
    val peerTubeFilter =
        remember(feedCode, peerTubeChannels) {
            peerTubeFilterForCode(feedCode, peerTubeChannels)
        }
    val visibleEntries =
        remember(combinedEntries, peerTubeFilter) {
            if (peerTubeFilter == PeerTubeFilter.None) {
                combinedEntries
            } else {
                combinedEntries.filter { entry ->
                    entry is VideoFeedEntry.PeerTubeEntry && peerTubeFilter.matches(entry.video)
                }
            }
        }
    val lastSeenTopId = remember { mutableStateOf<String?>(null) }

    val pagerState =
        if (pagerStateKey != null) {
            rememberForeverPagerState(pagerStateKey) { visibleEntries.size }
        } else {
            rememberPagerState(visibleEntries.size) { visibleEntries.size }
        }

    WatchScrollToTop(videoFeedContentState, pagerState)

    val restoredPositionKey = remember(feedCode) { mutableStateOf(false) }

    LaunchedEffect(visibleEntries, feedCode, lastPositions) {
        if (restoredPositionKey.value) return@LaunchedEffect
        val targetId = lastPositions[feedCode] ?: return@LaunchedEffect
        val index = visibleEntries.indexOfFirst { it.id == targetId }
        if (index >= 0) {
            pagerState.scrollToPage(index)
        }
        restoredPositionKey.value = true
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                onUserInteraction()
                visibleEntries.getOrNull(page)?.id?.let { entryId ->
                    accountViewModel.account.settings.setVideoFeedLastPosition(feedCode, entryId)
                }
                val isAtTop = page == 0
                isAtTopState.value = isAtTop
                if (isAtTop) {
                    lastSeenTopId.value = visibleEntries.firstOrNull()?.id
                }
            }
    }

    LaunchedEffect(visibleEntries, isAtTopState.value, lastSeenTopId.value) {
        if (isAtTopState.value) {
            lastSeenTopId.value = visibleEntries.firstOrNull()?.id
            newNotesState.value = emptyList()
            return@LaunchedEffect
        }

        val topId = lastSeenTopId.value ?: return@LaunchedEffect
        val index = visibleEntries.indexOfFirst { it.id == topId }
        newNotesState.value =
            if (index > 0) {
                visibleEntries.take(index)
            } else {
                emptyList()
            }
    }

    VerticalPager(
        state = pagerState,
        beyondViewportPageCount = 1,
        userScrollEnabled = !detailsOverlayVisible,
        modifier = Modifier.fillMaxSize(),
        key = { index -> visibleEntries.getOrNull(index)?.id ?: "$index" },
    ) { index ->
        visibleEntries.getOrNull(index)?.let { entry ->
            when (entry) {
                is VideoFeedEntry.NostrEntry -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CheckHiddenFeedWatchBlockAndReport(
                            note = entry.note,
                            modifier = Modifier.fillMaxWidth(),
                            showHiddenWarning = true,
                            ignoreAllBlocksAndReports = items.showHidden,
                            accountViewModel = accountViewModel,
                            nav = nav,
                        ) {
                            RenderVideoOrPictureNote(
                                entry.note,
                                accountViewModel,
                                nav,
                                controlsVisible = controlsVisible,
                                onUserInteraction = onUserInteraction,
                                onToggleControls = onToggleControls,
                                onDetailsOverlayVisibleChange = onDetailsOverlayVisibleChange,
                                onNavScrollToTop = videoFeedContentState::sendToTop,
                            )
                        }
                    }
                }
                is VideoFeedEntry.PeerTubeEntry -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        RenderPeerTubeVideoEntry(
                            videoItem = entry.video,
                            accountViewModel = accountViewModel,
                            controlsVisible = controlsVisible,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewItemsPill(
    newEntries: List<VideoFeedEntry>,
    accountViewModel: AccountViewModel,
    onClick: () -> Unit,
) {
    val count = newEntries.size
    val label = if (count > 9) "10+" else count.toString()
    val avatarKeys =
        remember(newEntries) {
            newEntries
                .filterIsInstance<VideoFeedEntry.NostrEntry>()
                .mapNotNull { it.note.author?.pubkeyHex }
                .distinct()
                .take(3)
        }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(
            modifier =
                Modifier
                    .clickable { onClick() },
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 6.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row {
                    avatarKeys.forEachIndexed { index, pubkey ->
                        BaseUserPicture(
                            baseUserHex = pubkey,
                            size = Size25dp,
                            accountViewModel = accountViewModel,
                            outerModifier = Modifier.offset(x = (index * -6).dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$label new",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RenderVideoOrPictureNote(
    note: Note,
    accountViewModel: AccountViewModel,
    nav: INav,
    controlsVisible: Boolean,
    onUserInteraction: () -> Unit,
    onToggleControls: () -> Unit,
    onDetailsOverlayVisibleChange: (Boolean) -> Unit,
    onNavScrollToTop: () -> Unit,
) {
    val captionVisible = remember(note.idHex) { mutableStateOf(true) }
    val captionTrigger = remember(note.idHex) { mutableStateOf(0) }
    val playbackControlsVisible = remember(note.idHex) { mutableStateOf(false) }
    val detailsVisible = remember(note.idHex) { mutableStateOf(false) }

    LaunchedEffect(note.idHex, captionTrigger.value) {
        captionVisible.value = true
        delay(2.seconds)
        captionVisible.value = false
    }

    Column(
        Modifier
            .fillMaxSize(1f)
            .pointerInput(note.idHex) {
                detectTapGestures(
                    onTap = { offset ->
                        val bottomStart = size.height * 2f / 3f
                        val middleStart = size.height / 3f
                        val middleEnd = size.height * 2f / 3f
                        if (offset.y >= bottomStart) {
                            onToggleControls()
                        } else if (offset.y in middleStart..middleEnd) {
                            playbackControlsVisible.value = !playbackControlsVisible.value
                        }
                    },
                    onLongPress = { offset ->
                        val bottomStart = size.height * 2f / 3f
                        if (offset.y >= bottomStart) {
                            onUserInteraction()
                            detailsVisible.value = true
                        }
                    },
                )
            },
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            val noteEvent = remember { note.event }
            if (noteEvent is PictureEvent) {
                val backgroundColor = remember { mutableStateOf(Color.Transparent) }
                PictureDisplay(note, false, ContentScale.Fit, HalfFeedPadding, backgroundColor, accountViewModel, nav)
            } else if (noteEvent is FileHeaderEvent) {
                FileHeaderDisplay(note, false, ContentScale.Fit, accountViewModel)
            } else if (noteEvent is FileStorageHeaderEvent) {
                FileStorageHeaderDisplay(note, false, ContentScale.Fit, accountViewModel)
            } else if (noteEvent is VideoEvent) {
                JustVideoDisplay(note, false, ContentScale.Fit, accountViewModel, showControls = playbackControlsVisible.value)
            } else if (noteEvent is TextNoteEvent) {
                val mediaState =
                    remember(noteEvent) {
                        CachedRichTextParser.parseText(
                            noteEvent.content,
                            noteEvent.tags.toImmutableListOfLists(),
                        )
                    }
                val mediaContent =
                    remember(mediaState) {
                        mediaState.imageList.firstOrNull { it is MediaUrlVideo } ?: mediaState.imageList.firstOrNull()
                    }
                mediaContent?.let { content ->
                    val mediaList =
                        remember(mediaState) {
                            mediaState.imageList.map { it as BaseMediaContent }.toImmutableList()
                        }
                    SensitivityWarning(note = note, accountViewModel = accountViewModel) {
                        ZoomableContentView(
                            content = content,
                            images = mediaList,
                            roundedCorner = false,
                            contentScale = ContentScale.Fit,
                            accountViewModel = accountViewModel,
                            showControls = playbackControlsVisible.value,
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(detailsVisible.value) {
        onDetailsOverlayVisibleChange(detailsVisible.value)
    }
    if (FeatureFlags.isPrism && detailsVisible.value) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .zIndex(1f),
            contentAlignment = Alignment.BottomStart,
        ) {
            VideoDetailsOverlay(
                note = note,
                accountViewModel = accountViewModel,
                nav = nav,
                visible = true,
                onDismiss = { detailsVisible.value = false },
                onUserInteraction = onUserInteraction,
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(0.92f)
                        .padding(start = 10.dp, bottom = 10.dp),
            )
        }
    }

    if (FeatureFlags.isPrism) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
            contentAlignment = Alignment.BottomStart,
        ) {
            VideoCaptionOverlay(
                note = note,
                accountViewModel = accountViewModel,
                nav = nav,
                visible = captionVisible.value,
                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp),
            )
        }
    }

    if (!detailsVisible.value) {
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(modifier = Modifier.fillMaxSize(1f).navigationBarsPadding()) {
                Column(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 12.dp)
                        .combinedClickable(
                            onClick = {
                                onUserInteraction()
                                captionTrigger.value += 1
                            },
                        ),
                    verticalArrangement = Arrangement.Center,
                ) {
                    RenderAuthorInformation(note, nav, accountViewModel, captionVisible.value)
                }

                AnimatedVisibility(
                    visible = !captionVisible.value,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = AuthorInfoVideoFeed.align(Alignment.BottomEnd),
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ReactionsColumn(
                            baseNote = note,
                            accountViewModel = accountViewModel,
                            nav = nav,
                            onNavScrollToTop = onNavScrollToTop,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderPeerTubeVideoEntry(
    videoItem: UnifiedVideoItem,
    accountViewModel: AccountViewModel,
    controlsVisible: Boolean,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        PeerTubeVideoDisplay(
            videoItem = videoItem,
            roundedCorner = false,
            contentScale = ContentScale.Fit,
            accountViewModel = accountViewModel,
            showControls = controlsVisible,
        )

        if (FeatureFlags.isPrism && controlsVisible) {
            PeerTubeCaptionOverlay(
                videoItem,
                Modifier.align(Alignment.BottomStart),
            )
        }
    }
}

@Composable
private fun PeerTubeCaptionOverlay(
    videoItem: UnifiedVideoItem,
    modifier: Modifier = Modifier,
) {
    val title = videoItem.title?.takeIf { it.isNotBlank() }
    val subtitle = videoItem.author?.takeIf { it.isNotBlank() } ?: videoItem.url
    if (title == null && subtitle.isBlank()) return

    Column(
        modifier
            .padding(start = 10.dp, bottom = 10.dp)
            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.placeholderText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PeerTubeFetchIndicator(
    hasChannels: Boolean,
    videoCount: Int,
    errorMessage: String?,
    isFetching: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!hasChannels) return

    val horizontalPadding = 5.dp

    val message =
        when {
            !errorMessage.isNullOrBlank() ->
                stringRes(R.string.peertube_fetch_error, errorMessage)
            isFetching && videoCount > 0 ->
                stringRes(R.string.peertube_refreshing)
            isFetching || videoCount == 0 ->
                stringRes(R.string.peertube_fetching)
            else -> null
        } ?: return

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = Size10dp),
        )
    }
}

@Composable
private fun RenderAuthorInformation(
    note: Note,
    nav: INav,
    accountViewModel: AccountViewModel,
    captionVisible: Boolean,
) {
    Row(modifier = Modifier.padding(start = 10.dp, end = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        NoteAuthorPicture(note, Size55dp, accountViewModel = accountViewModel, nav = nav)

        Spacer(modifier = DoubleHorzSpacer)

        val usernameModifier =
            if (FeatureFlags.isPrism && note.author != null) {
                Modifier.clickable { nav.nav(Route.Profile(note.author!!.pubkeyHex)) }
            } else {
                Modifier
            }

        val infoColumnModifier =
            if (FeatureFlags.isPrism) {
                usernameModifier
                    .heightIn(min = 65.dp)
                    .weight(1f)
            } else {
                usernameModifier
                    .height(65.dp)
                    .weight(1f)
            }

        Column(
            infoColumnModifier,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NoteUsernameDisplay(note, Modifier.weight(1f), accountViewModel = accountViewModel)
                VideoUserOptionAction(note, accountViewModel, nav)
            }
            if (!FeatureFlags.isPrism && accountViewModel.settings.isCompleteUIMode()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    ObserveDisplayNip05Status(
                        note.author!!,
                        accountViewModel,
                        nav = nav,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    RenderAllRelayList(baseNote = note, accountViewModel = accountViewModel, nav = nav)
                }
            }
            // Caption overlay is rendered in the bottom-left overlay area for Prism.
        }
    }
}

@Composable
private fun VideoCaptionOverlay(
    note: Note,
    accountViewModel: AccountViewModel,
    nav: INav,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val content =
        remember(note) {
            note.event
                ?.content
                ?.trim()
                .orEmpty()
        }
    if (content.isBlank()) return

    val tags = remember(note) { note.event?.tags?.toImmutableListOfLists() ?: EmptyTagList }
    val backgroundColor = remember { mutableStateOf(Color.Transparent) }
    val maxCaptionLines = 5
    val lineHeightDp =
        with(LocalDensity.current) {
            (LocalTextStyle.current.fontSize * 1.4f).toDp()
        }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Column(
            modifier =
                modifier
                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            val hashtagNav = hashtagFilterNav(accountViewModel, nav)
            RichTextViewer(
                content = content,
                canPreview = false,
                quotesLeft = 0,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = lineHeightDp * maxCaptionLines)
                        .clipToBounds(),
                tags = tags,
                backgroundColor = backgroundColor,
                accountViewModel = accountViewModel,
                nav = hashtagNav,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VideoDetailsOverlay(
    note: Note,
    accountViewModel: AccountViewModel,
    nav: INav,
    visible: Boolean,
    onDismiss: () -> Unit,
    onUserInteraction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val event = note.event as? Event ?: return
    val title =
        remember(event) {
            if (event is VideoEvent) {
                event.title()
            } else {
                event.tags.firstTagValueFor("title", "subject", "name", "alt")
            }
        }
    val content =
        remember(event) {
            event.content.trim()
        }
    val hashtags =
        remember(event) {
            event.hashtags()
        }
    val context = LocalContext.current
    val timeLabel =
        remember(note, context) {
            timeAgo(note.createdAt(), context = context)
        }
    val scrollState = rememberScrollState()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val overlayTextColor = Color.White
        val overlayMutedColor = Color.White.copy(alpha = 0.72f)
        val hasDetails = title != null || content.isNotBlank() || hashtags.isNotEmpty()

        Column(
            modifier =
                modifier
                    .padding(top = 6.dp, end = 10.dp)
                    .background(Color.Black.copy(alpha = 0.78f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .pointerInput(Unit) {
                            var totalDrag = 0f
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    if (totalDrag > 60f) {
                                        onDismiss()
                                    }
                                    totalDrag = 0f
                                },
                                onVerticalDrag = { _, dragAmount ->
                                    if (dragAmount > 0f) {
                                        totalDrag += dragAmount
                                    }
                                },
                            )
                        },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .width(42.dp)
                            .height(4.dp)
                            .background(Color.White.copy(alpha = 0.45f), RoundedCornerShape(999.dp)),
                )
            }

            Column(
                modifier = Modifier.verticalScroll(scrollState),
            ) {
                title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = overlayTextColor,
                        maxLines = 2,
                    )
                }
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = overlayMutedColor,
                    modifier = Modifier.padding(top = 2.dp),
                )
                if (!hasDetails) {
                    Text(
                        text = "No details available",
                        style = MaterialTheme.typography.bodySmall,
                        color = overlayMutedColor,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                if (content.isNotBlank()) {
                    val richTextStyle = MaterialTheme.typography.bodySmall.copy(color = overlayTextColor)
                    val hashtagNav =
                        hashtagFilterNav(
                            accountViewModel,
                            nav,
                        ) {
                            onDismiss()
                            onUserInteraction()
                        }
                    CompositionLocalProvider(LocalTextStyle provides richTextStyle) {
                        RichTextViewer(
                            content = content,
                            canPreview = false,
                            quotesLeft = 0,
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            tags = event.tags.toImmutableListOfLists(),
                            backgroundColor = remember { mutableStateOf(Color.Transparent) },
                            accountViewModel = accountViewModel,
                            nav = hashtagNav,
                        )
                    }
                }
                if (hashtags.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.padding(top = 6.dp).wrapContentWidth(),
                    ) {
                        hashtags.forEach { tag ->
                            val normalized = tag.lowercase()
                            ClickableTextColor(
                                text = "#$normalized ",
                                style = MaterialTheme.typography.labelSmall,
                                linkColor = overlayMutedColor,
                            ) {
                                accountViewModel.account.settings.changeDefaultStoriesFollowList("Hashtag/$normalized")
                                accountViewModel.feedStates.videoFeed.checkKeysInvalidateDataAndSendToTop()
                                accountViewModel.dataSources().video.hardRefresh(accountViewModel.account)
                                onDismiss()
                                onUserInteraction()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoUserOptionAction(
    note: Note,
    accountViewModel: AccountViewModel,
    nav: INav,
) {
    if (FeatureFlags.isPrism) {
        return
    }

    val popupExpanded = remember { mutableStateOf(false) }

    ClickableBox(
        modifier = Size22Modifier,
        onClick = { popupExpanded.value = true },
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = stringRes(id = R.string.more_options),
            modifier = Size20Modifier,
            tint = MaterialTheme.colorScheme.placeholderText,
        )

        if (popupExpanded.value) {
            NoteDropDownMenu(
                note,
                { popupExpanded.value = false },
                null,
                accountViewModel,
                nav,
            )
        }
    }
}

@Composable
fun ReactionsColumn(
    baseNote: Note,
    accountViewModel: AccountViewModel,
    nav: INav,
    onNavScrollToTop: () -> Unit,
) {
//    var wantsToReplyTo by remember { mutableStateOf<Note?>(null) }

//    var wantsToQuote by remember { mutableStateOf<Note?>(null) }

//    if (wantsToReplyTo != null) {
//        NewPostView(
//            onClose = { wantsToReplyTo = null },
//            baseReplyTo = wantsToReplyTo,
//            quote = null,
//            accountViewModel = accountViewModel,
//            nav = nav,
//        )
//    }

//    if (wantsToQuote != null) {
//        NewPostView(
//            onClose = { wantsToQuote = null },
//            baseReplyTo = null,
//            quote = wantsToQuote,
//            accountViewModel = accountViewModel,
//            nav = nav,
//        )
//    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = VideoReactionColumnPadding,
    ) {
        val reactionTint =
            if (FeatureFlags.isPrism) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.onBackground
            }
        ReplyReaction(
            baseNote = baseNote,
            grayTint = reactionTint,
            accountViewModel = accountViewModel,
            iconSizeModifier = Size40Modifier,
        ) {
            routeFor(
                baseNote,
                accountViewModel.account,
            )?.let { nav.nav(it) }
        }
        if (FeatureFlags.isPrism) {
            RepostOnlyReaction(
                baseNote = baseNote,
                grayTint = reactionTint,
                accountViewModel = accountViewModel,
            )
        } else {
            BoostReaction(
                baseNote = baseNote,
                grayTint = reactionTint,
                accountViewModel = accountViewModel,
                iconSizeModifier = Size40Modifier,
                iconSize = Size40dp,
                onQuotePress = {
                    nav.nav(
                        Route.NewShortNote(
                            quote = baseNote.idHex,
                        ),
                    )
                },
                onForkPress = {
                },
            )
        }
        LikeReaction(
            baseNote = baseNote,
            grayTint = reactionTint,
            accountViewModel = accountViewModel,
            nav = nav,
            iconSize = Size40dp,
            heartSizeModifier = Size35Modifier,
            iconFontSize = 28.sp,
        )
        ZapReaction(
            baseNote = baseNote,
            grayTint = reactionTint,
            accountViewModel = accountViewModel,
            iconSize = Size40dp,
            iconSizeModifier = Size40Modifier,
            animationModifier = Size35Modifier,
            nav = nav,
        )
        if (FeatureFlags.isPrism) {
            Spacer(modifier = Modifier.height(12.dp))
            NewImageButton(accountViewModel, nav, onNavScrollToTop)
        }
    }
}

private fun hashtagFilterNav(
    accountViewModel: AccountViewModel,
    nav: INav,
    onAfter: (() -> Unit)? = null,
): INav =
    object : INav {
        override val navigationScope = nav.navigationScope
        override val drawerState = nav.drawerState

        override fun closeDrawer() = nav.closeDrawer()

        override fun openDrawer() = nav.openDrawer()

        override fun nav(route: Route) {
            if (FeatureFlags.isPrism && route is Route.Hashtag) {
                val normalized = route.hashtag.lowercase()
                accountViewModel.account.settings.changeDefaultStoriesFollowList("Hashtag/$normalized")
                accountViewModel.feedStates.videoFeed.checkKeysInvalidateDataAndSendToTop()
                accountViewModel.feedStates.videoFeed.invalidateData()
                accountViewModel.dataSources().video.hardRefresh(accountViewModel.account)
                onAfter?.invoke()
            } else {
                nav.nav(route)
            }
        }

        override fun nav(computeRoute: suspend () -> Route?) {
            nav.nav(computeRoute)
        }

        override fun newStack(route: Route) = nav.newStack(route)

        override fun popBack() = nav.popBack()

        override fun <T : Route> popUpTo(
            route: Route,
            klass: KClass<T>,
        ) = nav.popUpTo(route, klass)
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
