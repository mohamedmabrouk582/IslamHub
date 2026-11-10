package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QuranViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.quranDao()

    // Preferences key helper
    private val prefs = application.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE)

    // Bookmarks and history observed from Database
    val bookmarks: StateFlow<List<Bookmark>> = dao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastRead: StateFlow<LastRead?> = dao.getLastRead()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Navigation and screen tracking
    private val _currentScreen = MutableStateFlow("home") // "home", "quran", "bookmarks", "settings", "hadith", "reader", "tafseer", "audio_player"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Reading & Audio state
    private val _currentSurah = MutableStateFlow(QuranData.surahs[0]) // Default Al-Fatiha
    val currentSurah: StateFlow<Surah> = _currentSurah.asStateFlow()

    private val _currentAyahNumber = MutableStateFlow(1)
    val currentAyahNumber: StateFlow<Int> = _currentAyahNumber.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0.65f) // 65% progress mock
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _selectedReciter = MutableStateFlow(QuranData.reciters[0])
    val selectedReciter: StateFlow<Reciter> = _selectedReciter.asStateFlow()

    // Settings States
    private val _fontSizeProgress = MutableStateFlow(prefs.getFloat("font_size_progress", 75f))
    val fontSizeProgress: StateFlow<Float> = _fontSizeProgress.asStateFlow()

    private val _showTranslation = MutableStateFlow(prefs.getBoolean("show_translation", true))
    val showTranslation: StateFlow<Boolean> = _showTranslation.asStateFlow()

    private val _showTransliteration = MutableStateFlow(prefs.getBoolean("show_transliteration", true))
    val showTransliteration: StateFlow<Boolean> = _showTransliteration.asStateFlow()

    private val _nightReadingMode = MutableStateFlow(prefs.getBoolean("night_reading_mode", false))
    val nightReadingMode: StateFlow<Boolean> = _nightReadingMode.asStateFlow()

    private val _selectedTafseerSource = MutableStateFlow(prefs.getString("tafseer_source", "ابن كثير") ?: "ابن كثير")
    val selectedTafseerSource: StateFlow<String> = _selectedTafseerSource.asStateFlow()

    private val _translationLanguage = MutableStateFlow(prefs.getString("translation_lang", "English (Sahih International)") ?: "English (Sahih International)")
    val translationLanguage: StateFlow<String> = _translationLanguage.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _dailyAyahNotification = MutableStateFlow(prefs.getBoolean("daily_notification", true))
    val dailyAyahNotification: StateFlow<Boolean> = _dailyAyahNotification.asStateFlow()

    // Tafseer navigation helper
    private val _tafseerSurahNumber = MutableStateFlow(2)
    val tafseerSurahNumber: StateFlow<Int> = _tafseerSurahNumber.asStateFlow()

    private val _tafseerAyahNumber = MutableStateFlow(255)
    val tafseerAyahNumber: StateFlow<Int> = _tafseerAyahNumber.asStateFlow()

    // Hadith screen selection
    private val _selectedHadithCollection = MutableStateFlow<String?>(null) // null means Hadith landing screen
    val selectedHadithCollection: StateFlow<String?> = _selectedHadithCollection.asStateFlow()

    private val _selectedHadithTopic = MutableStateFlow<String?>(null) // null means hadith landing
    val selectedHadithTopic: StateFlow<String?> = _selectedHadithTopic.asStateFlow()

    // Islamic Stories and YouTube Player State
    private val storiesRepo = IslamicStoriesRepository(application)
    val stories: StateFlow<List<IslamicStory>> = storiesRepo.stories
    val playlists: StateFlow<List<StoryPlaylist>> = storiesRepo.playlists
    val channels: StateFlow<List<IslamicChannel>> = storiesRepo.channels
    val remoteConfigSyncState: StateFlow<RemoteConfigSyncState> = storiesRepo.syncState
    val remoteConfigMessage: StateFlow<String> = storiesRepo.lastSyncMessage

    private val _selectedStory = MutableStateFlow<IslamicStory?>(null)
    val selectedStory: StateFlow<IslamicStory?> = _selectedStory.asStateFlow()

    private val _playerMode = MutableStateFlow(PlayerMode.HIDDEN)
    val playerMode: StateFlow<PlayerMode> = _playerMode.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<IslamicStory>>(emptyList())
    val currentPlaylist: StateFlow<List<IslamicStory>> = _currentPlaylist.asStateFlow()

    private val _currentPlaylistIndex = MutableStateFlow(0)
    val currentPlaylistIndex: StateFlow<Int> = _currentPlaylistIndex.asStateFlow()

    private val _isStoryVideoPlaying = MutableStateFlow(false)
    val isStoryVideoPlaying: StateFlow<Boolean> = _isStoryVideoPlaying.asStateFlow()

    private val _isAutoPlayNext = MutableStateFlow(true)
    val isAutoPlayNext: StateFlow<Boolean> = _isAutoPlayNext.asStateFlow()

    private val _selectedStoryCategory = MutableStateFlow(StoryCategory.ALL)
    val selectedStoryCategory: StateFlow<StoryCategory> = _selectedStoryCategory.asStateFlow()

    private val _storySearchQuery = MutableStateFlow("")
    val storySearchQuery: StateFlow<String> = _storySearchQuery.asStateFlow()

    // Simulation job for audio playback progress
    private var playbackJob: Job? = null

    init {
        // Automatically start updating playback position if isPlaying
        viewModelScope.launch {
            _isPlaying.collect { playing ->
                playbackJob?.cancel()
                if (playing) {
                    playbackJob = launch {
                        while (true) {
                            delay(100)
                            val increment = 0.005f * _playbackSpeed.value
                            var current = _playbackProgress.value + increment
                            if (current >= 1.0f) {
                                current = 0.0f
                                // Automatically advance to next verse in Surah
                                val nextAyah = _currentAyahNumber.value + 1
                                if (nextAyah <= (_currentSurah.value.versesCount)) {
                                    _currentAyahNumber.value = nextAyah
                                    updateLastRead(_currentSurah.value.number, nextAyah)
                                } else {
                                    _isPlaying.value = false
                                }
                            }
                            _playbackProgress.value = current
                        }
                    }
                }
            }
        }
    }

    // Screen Nav
    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    // Set Reading Surah & Ayah
    fun selectSurah(surah: Surah, startAyah: Int = 1) {
        _currentSurah.value = surah
        _currentAyahNumber.value = startAyah
        _playbackProgress.value = 0.0f
        updateLastRead(surah.number, startAyah)
    }

    fun selectAyah(ayahNumber: Int) {
        if (ayahNumber in 1.._currentSurah.value.versesCount) {
            _currentAyahNumber.value = ayahNumber
            _playbackProgress.value = 0.0f
            updateLastRead(_currentSurah.value.number, ayahNumber)
        }
    }

    // Toggle Audio Player
    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
    }

    fun selectReciter(reciter: Reciter) {
        _selectedReciter.value = reciter
    }

    // Bookmarking logic
    fun toggleBookmark(surah: Surah, ayahNumber: Int? = null) {
        viewModelScope.launch {
            val bookmarkId = if (ayahNumber == null) "${surah.number}" else "${surah.number}:$ayahNumber"
            if (dao.isBookmarked(bookmarkId)) {
                dao.deleteBookmark(bookmarkId)
            } else {
                val ayahList = QuranData.getAyahsForSurah(surah.number)
                val arabic = if (ayahNumber != null) {
                    ayahList.find { it.number == ayahNumber }?.textArabic ?: ""
                } else ""
                val translation = if (ayahNumber != null) {
                    ayahList.find { it.number == ayahNumber }?.textEnglish ?: ""
                } else ""

                dao.insertBookmark(
                    Bookmark(
                        id = bookmarkId,
                        surahNumber = surah.number,
                        ayahNumber = ayahNumber,
                        surahName = surah.nameArabic,
                        surahNameEnglish = surah.nameEnglish,
                        numberOfVerses = surah.versesCount,
                        arabicText = arabic,
                        translationText = translation
                    )
                )
            }
        }
    }

    fun checkIsBookmarked(id: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(dao.isBookmarked(id))
        }
    }

    private fun updateLastRead(surahNumber: Int, ayahNumber: Int) {
        viewModelScope.launch {
            val surah = QuranData.surahs.find { it.number == surahNumber } ?: return@launch
            dao.updateLastRead(
                LastRead(
                    surahNumber = surahNumber,
                    ayahNumber = ayahNumber,
                    surahName = surah.nameArabic,
                    surahNameEnglish = surah.nameEnglish
                )
            )
        }
    }

    // Related Tafseer navigation helper
    fun openTafseer(surahNumber: Int, ayahNumber: Int) {
        _tafseerSurahNumber.value = surahNumber
        _tafseerAyahNumber.value = ayahNumber
        navigateTo("tafseer")
    }

    // Settings Updates
    fun updateFontSize(progress: Float) {
        _fontSizeProgress.value = progress
        prefs.edit().putFloat("font_size_progress", progress).apply()
    }

    fun toggleTranslation(enabled: Boolean) {
        _showTranslation.value = enabled
        prefs.edit().putBoolean("show_translation", enabled).apply()
    }

    fun toggleTransliteration(enabled: Boolean) {
        _showTransliteration.value = enabled
        prefs.edit().putBoolean("show_transliteration", enabled).apply()
    }

    fun toggleNightReadingMode(enabled: Boolean) {
        _nightReadingMode.value = enabled
        prefs.edit().putBoolean("night_reading_mode", enabled).apply()
    }

    fun setTafseerSource(source: String) {
        _selectedTafseerSource.value = source
        prefs.edit().putString("tafseer_source", source).apply()
    }

    fun setTranslationLanguage(lang: String) {
        _translationLanguage.value = lang
        prefs.edit().putString("translation_lang", lang).apply()
    }

    fun toggleDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
        prefs.edit().putBoolean("dark_theme", enabled).apply()
    }

    fun toggleDailyNotification(enabled: Boolean) {
        _dailyAyahNotification.value = enabled
        prefs.edit().putBoolean("daily_notification", enabled).apply()
    }

    // Hadith navigation
    fun selectHadithCollection(collection: String?) {
        _selectedHadithCollection.value = collection
        _selectedHadithTopic.value = null
    }

    fun selectHadithTopic(topic: String?) {
        _selectedHadithTopic.value = topic
        _selectedHadithCollection.value = null
    }

    // Islamic Stories and YouTube Player Actions
    fun playStory(story: IslamicStory, playlist: List<IslamicStory> = emptyList()) {
        _selectedStory.value = story
        val activePlaylist = if (playlist.isNotEmpty()) playlist else stories.value.filter { it.category == story.category }
        _currentPlaylist.value = activePlaylist
        val index = activePlaylist.indexOfFirst { it.id == story.id }
        _currentPlaylistIndex.value = if (index >= 0) index else 0
        _isStoryVideoPlaying.value = true
        _playerMode.value = PlayerMode.EXPANDED
    }

    fun expandPlayer() {
        if (_selectedStory.value != null) {
            _playerMode.value = PlayerMode.EXPANDED
        }
    }

    fun minimizePlayer() {
        if (_selectedStory.value != null) {
            _playerMode.value = PlayerMode.MINI
        }
    }

    fun closePlayer() {
        _playerMode.value = PlayerMode.HIDDEN
        _isStoryVideoPlaying.value = false
    }

    fun toggleStoryVideoPlaying() {
        _isStoryVideoPlaying.value = !_isStoryVideoPlaying.value
    }

    fun setStoryVideoPlaying(playing: Boolean) {
        _isStoryVideoPlaying.value = playing
    }

    fun playNextInPlaylist() {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return
        val nextIndex = (_currentPlaylistIndex.value + 1) % playlist.size
        _currentPlaylistIndex.value = nextIndex
        _selectedStory.value = playlist[nextIndex]
        _isStoryVideoPlaying.value = true
    }

    fun playPreviousInPlaylist() {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return
        val prevIndex = if (_currentPlaylistIndex.value - 1 < 0) playlist.size - 1 else _currentPlaylistIndex.value - 1
        _currentPlaylistIndex.value = prevIndex
        _selectedStory.value = playlist[prevIndex]
        _isStoryVideoPlaying.value = true
    }

    fun toggleAutoPlayNext() {
        _isAutoPlayNext.value = !_isAutoPlayNext.value
    }

    fun setStoryCategory(category: StoryCategory) {
        _selectedStoryCategory.value = category
    }

    fun setStorySearchQuery(query: String) {
        _storySearchQuery.value = query
    }

    fun syncRemoteConfig() {
        viewModelScope.launch {
            storiesRepo.syncRemoteConfig()
        }
    }
}

