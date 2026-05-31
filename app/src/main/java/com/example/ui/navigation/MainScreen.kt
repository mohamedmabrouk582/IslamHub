package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.ui.adaptive.LocalWindowLayout
import com.example.ui.adaptive.rememberWindowLayout
import com.example.ui.bookmarks.BookmarksScreen
import com.example.ui.components.AdaptiveAppScaffold
import com.example.ui.components.GeometricBackgroundPattern
import com.example.ui.components.QuranAudioPlayerBar
import com.example.ui.components.QuranPlayerBarVariant
import com.example.ui.hadith.HadithBooksScreen
import com.example.ui.hadith.HadithListScreen
import com.example.ui.hadith.HadithScreen
import com.example.ui.home.HomeScreen
import com.example.ui.quran.SurahListScreen
import com.example.ui.reader.ReaderScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainScreen(
    viewModel: QuranViewModel,
    navigationViewModel: NavigationViewModel,
) {
    val darkTheme by viewModel.isDarkTheme.collectAsState()
    val navigator = rememberAppNavigator(navigationViewModel)
    val scaffoldState = rememberNavigationSuiteScaffoldState()
    val windowLayout = rememberWindowLayout()
    val currentKey = navigator.currentKey()
    val isQuranListDetailReader = navigator.isQuranListDetailReader(windowLayout.supportsListDetail)
    val showNavigationSuite = when (currentKey) {
        ReaderKey -> isQuranListDetailReader
        else -> true
    }

    val isPlaying by viewModel.isPlaying.collectAsState()
    val playProgress by viewModel.playbackProgress.collectAsState()
    val versePlayProgress by viewModel.currentVersePlaybackProgress.collectAsState()
    val surah by viewModel.currentSurah.collectAsState()
    val activeAyahNumber by viewModel.currentAyahNumber.collectAsState()
    val reciter by viewModel.selectedReciter.collectAsState()
    val isDownloadingAudio by viewModel.isDownloadingAudio.collectAsState()
    val surahDownloadProgress by viewModel.surahDownloadProgress.collectAsState()

    val showGlobalPlayer = currentKey != ReaderKey && (isPlaying || playProgress > 0f)

    LaunchedEffect(showNavigationSuite) {
        if (showNavigationSuite) {
            scaffoldState.show()
        } else {
            scaffoldState.hide()
        }
    }

    LaunchedEffect(currentKey) {
        when (currentKey) {
            QuranKey -> viewModel.loadSurahs()
            ReaderKey -> viewModel.loadAyahsForCurrentSurah()
            else -> Unit
        }
    }

    BackHandler(enabled = navigator.backStack.size > 1) {
        navigator.pop()
    }

    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<AppNavKey>(directive = directive)

    MyApplicationTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(
            LocalAppNavigator provides navigator,
            LocalWindowLayout provides windowLayout,
        ) {
            AdaptiveAppScaffold(
                navigator = navigator,
                scaffoldState = scaffoldState,
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                    ) {
                        GeometricBackgroundPattern()

                        NavDisplay(
                            backStack = navigator.backStack,
                            onBack = { navigator.pop() },
                            sceneStrategies = listOf(listDetailStrategy),
                            transitionSpec = {
                                EnterTransition.None togetherWith ExitTransition.None
                            },
                            popTransitionSpec = {
                                EnterTransition.None togetherWith ExitTransition.None
                            },
                            predictivePopTransitionSpec = {
                                EnterTransition.None togetherWith ExitTransition.None
                            },
                            entryProvider = entryProvider {
                                entry<HomeKey> {
                                    HomeScreen(viewModel)
                                }
                                entry<QuranKey>(
                                    metadata = ListDetailSceneStrategy.listPane(
                                        sceneKey = QuranListDetailSceneKey,
                                        detailPlaceholder = { QuranDetailPlaceholder() },
                                    ),
                                ) {
                                    SurahListScreen(viewModel)
                                }
                                entry<HadithRootKey>(
                                    metadata = ListDetailSceneStrategy.listPane(
                                        sceneKey = HadithListDetailSceneKey,
                                        detailPlaceholder = { HadithDetailPlaceholder() },
                                    ),
                                ) {
                                    HadithScreen(viewModel)
                                }
                                entry<HadithBooksKey>(
                                    metadata = ListDetailSceneStrategy.detailPane(
                                        sceneKey = HadithListDetailSceneKey,
                                    ),
                                ) { key ->
                                    HadithBooksScreen(viewModel, collectionSlug = key.slug)
                                }
                                entry<HadithListKey>(
                                    metadata = ListDetailSceneStrategy.extraPane(
                                        sceneKey = HadithListDetailSceneKey,
                                    ),
                                ) { key ->
                                    HadithListScreen(
                                        viewModel,
                                        collectionSlug = key.slug,
                                        bookNumber = key.bookNumber,
                                    )
                                }
                                entry<BookmarksKey> {
                                    BookmarksScreen(viewModel)
                                }
                                entry<SettingsKey> {
                                    SettingsScreen(viewModel)
                                }
                                entry<ReaderKey>(
                                    metadata = ListDetailSceneStrategy.detailPane(
                                        sceneKey = QuranListDetailSceneKey,
                                    ),
                                ) {
                                    ReaderScreen(viewModel)
                                }
                            },
                        )
                    }

                    if (showGlobalPlayer) {
                        QuranAudioPlayerBar(
                            surah = surah,
                            reciter = reciter,
                            activeAyahNumber = activeAyahNumber,
                            versesCount = surah.versesCount,
                            isPlaying = isPlaying,
                            versePlayProgress = versePlayProgress,
                            isDownloadingAudio = isDownloadingAudio,
                            surahDownloadProgress = surahDownloadProgress,
                            variant = QuranPlayerBarVariant.Compact,
                            onBarClick = {
                                if (navigator.topLevelKey != QuranKey) {
                                    navigator.navigateToTab(QuranKey)
                                }
                                navigator.openReader()
                            },
                            onPlayPause = { viewModel.setPlaying(!isPlaying) },
                            onPrevious = {
                                if (activeAyahNumber > 1) {
                                    viewModel.selectAyah(activeAyahNumber - 1)
                                }
                            },
                            onNext = {
                                if (activeAyahNumber < surah.versesCount) {
                                    viewModel.selectAyah(activeAyahNumber + 1)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
