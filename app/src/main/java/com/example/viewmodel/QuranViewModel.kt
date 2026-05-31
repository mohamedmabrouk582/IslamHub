package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.data.EveryAyahReciters
import com.example.data.QuranDao
import com.example.data.QuranData
import com.example.data.Bookmark
import com.example.data.LastRead
import com.example.data.model.Ayah
import com.example.data.model.Hadith
import com.example.data.model.HadithBook
import com.example.data.model.HadithCollection
import com.example.data.model.Reciter
import com.example.data.model.Surah
import com.example.data.audio.QuranAudioController
import com.example.data.audio.SurahDownloadScheduler
import com.example.data.repository.HadithRepository
import com.example.data.repository.QuranRepository
import com.example.data.repository.TafseerRepository
import com.example.ui.reader.ReaderLayoutMode
import com.example.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class QuranViewModel @Inject constructor(
    application: Application,
    private val quranRepository: QuranRepository,
    private val hadithRepository: HadithRepository,
    private val tafseerRepository: TafseerRepository,
    private val dao: QuranDao,
    private val audioController: QuranAudioController,
    private val surahDownloadScheduler: SurahDownloadScheduler,
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE)

    val bookmarks: StateFlow<List<Bookmark>> = dao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastRead: StateFlow<LastRead?> = dao.getLastRead()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentSurah = MutableStateFlow(
        Surah(1, "الفاتحة", "Al-Fatiha", "The Opening", "MECCAN", 7),
    )
    val currentSurah: StateFlow<Surah> = _currentSurah.asStateFlow()

    private val _currentAyahNumber = MutableStateFlow(1)
    val currentAyahNumber: StateFlow<Int> = _currentAyahNumber.asStateFlow()

    val isPlaying: StateFlow<Boolean> = audioController.isPlaying
    val playbackProgress: StateFlow<Float> = audioController.playbackProgress
    val playingSurahNumber: StateFlow<Int> = audioController.playingSurahNumber
    val playingAyahNumber: StateFlow<Int> = audioController.playingAyahNumber

    val currentVersePlaybackProgress: StateFlow<Float?> = combine(
        combine(_currentSurah, _currentAyahNumber) { surah, ayah -> surah to ayah },
        playbackProgress,
        isPlaying,
        playingSurahNumber,
        playingAyahNumber,
    ) { selection, progress, playing, playSurah, playAyah ->
        val (surah, ayah) = selection
        if (playSurah == surah.number && playAyah == ayah && (playing || progress > 0f)) {
            progress
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _surahDownloadProgress = MutableStateFlow<Float?>(null)
    val surahDownloadProgress: StateFlow<Float?> = _surahDownloadProgress.asStateFlow()

    private val _isDownloadingAudio = MutableStateFlow(false)
    val isDownloadingAudio: StateFlow<Boolean> = _isDownloadingAudio.asStateFlow()

    private val _selectedReciter = MutableStateFlow(resolveInitialReciter())
    val selectedReciter: StateFlow<Reciter> = _selectedReciter.asStateFlow()

    private val _fontSizeProgress = MutableStateFlow(prefs.getFloat("font_size_progress", 75f))
    val fontSizeProgress: StateFlow<Float> = _fontSizeProgress.asStateFlow()

    private val _showTranslation = MutableStateFlow(prefs.getBoolean("show_translation", true))
    val showTranslation: StateFlow<Boolean> = _showTranslation.asStateFlow()

    private val _showTransliteration = MutableStateFlow(prefs.getBoolean("show_transliteration", true))
    val showTransliteration: StateFlow<Boolean> = _showTransliteration.asStateFlow()

    private val _readerLayoutMode = MutableStateFlow(resolveReaderLayoutMode())
    val readerLayoutMode: StateFlow<ReaderLayoutMode> = _readerLayoutMode.asStateFlow()

    private val _nightReadingMode = MutableStateFlow(prefs.getBoolean("night_reading_mode", false))
    val nightReadingMode: StateFlow<Boolean> = _nightReadingMode.asStateFlow()

    private val _selectedTafseerSource = MutableStateFlow(
        prefs.getString("tafseer_source", "التفسير الميسر") ?: "التفسير الميسر",
    )
    val selectedTafseerSource: StateFlow<String> = _selectedTafseerSource.asStateFlow()

    private val _translationLanguage = MutableStateFlow(
        prefs.getString("translation_lang", "English (Sahih International)") ?: "English (Sahih International)",
    )
    val translationLanguage: StateFlow<String> = _translationLanguage.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _dailyAyahNotification = MutableStateFlow(prefs.getBoolean("daily_notification", true))
    val dailyAyahNotification: StateFlow<Boolean> = _dailyAyahNotification.asStateFlow()

    private val _tafseerSurahNumber = MutableStateFlow(2)
    val tafseerSurahNumber: StateFlow<Int> = _tafseerSurahNumber.asStateFlow()

    private val _tafseerAyahNumber = MutableStateFlow(255)
    val tafseerAyahNumber: StateFlow<Int> = _tafseerAyahNumber.asStateFlow()

    private val _surahs = MutableStateFlow<UiState<List<Surah>>>(UiState.Loading)
    val surahs: StateFlow<UiState<List<Surah>>> = _surahs.asStateFlow()

    private val _currentAyahs = MutableStateFlow<UiState<List<Ayah>>>(UiState.Loading)
    val currentAyahs: StateFlow<UiState<List<Ayah>>> = _currentAyahs.asStateFlow()

    private val _ayahCache = MutableStateFlow<Map<Int, UiState<List<Ayah>>>>(emptyMap())
    val ayahCache: StateFlow<Map<Int, UiState<List<Ayah>>>> = _ayahCache.asStateFlow()

    val collectionsPaging: Flow<PagingData<HadithCollection>> =
        hadithRepository.collectionsPaging().cachedIn(viewModelScope)

    private val _tafseerText = MutableStateFlow<UiState<String>>(UiState.Loading)
    val tafseerText: StateFlow<UiState<String>> = _tafseerText.asStateFlow()

    private var ayahLoadJobs = mutableMapOf<Int, Job>()

    init {
        loadSurahs()
        audioController.connect()
        audioController.setOnAyahEndedListener { surahNumber, nextAyah ->
            if (nextAyah != null) {
                _currentAyahNumber.value = nextAyah
                updateLastRead(surahNumber, nextAyah)
            }
        }
        viewModelScope.launch {
            combine(_selectedReciter, _currentSurah) { reciter, surah ->
                reciter.id to surah.number
            }.flatMapLatest { (reciterId, surahNumber) ->
                surahDownloadScheduler.observeDownloadProgress(reciterId, surahNumber)
            }.collect { progress ->
                _surahDownloadProgress.value = progress
            }
        }
        viewModelScope.launch {
            combine(_selectedReciter, _currentSurah) { reciter, surah ->
                reciter.id to surah.number
            }.flatMapLatest { (reciterId, surahNumber) ->
                surahDownloadScheduler.isDownloading(reciterId, surahNumber)
            }.collect { downloading ->
                _isDownloadingAudio.value = downloading
            }
        }
    }

    fun loadSurahs() {
        viewModelScope.launch {
            quranRepository.getSurahs().collect { state ->
                _surahs.value = state
            }
        }
    }

    fun loadAyahsForCurrentSurah() {
        prefetchAdjacentAyahs(_currentSurah.value.number)
    }

    fun ensureAyahsLoaded(surahNumber: Int) {
        if (surahNumber !in 1..114) return
        val cached = _ayahCache.value[surahNumber]
        if (cached is UiState.Success) {
            if (_currentSurah.value.number == surahNumber) {
                _currentAyahs.value = cached
            }
            return
        }
        if (ayahLoadJobs[surahNumber]?.isActive == true) return

        ayahLoadJobs[surahNumber] = viewModelScope.launch {
            quranRepository.getAyahs(surahNumber).collect { state ->
                _ayahCache.value = _ayahCache.value + (surahNumber to state)
                if (_currentSurah.value.number == surahNumber) {
                    _currentAyahs.value = state
                }
            }
        }
    }

    fun prefetchAdjacentAyahs(centerSurahNumber: Int) {
        ensureAyahsLoaded(centerSurahNumber)
        ensureAyahsLoaded(centerSurahNumber - 1)
        ensureAyahsLoaded(centerSurahNumber + 1)
    }

    fun booksPagingFlow(collectionSlug: String): Flow<PagingData<HadithBook>> {
        return hadithRepository.booksPaging(collectionSlug).cachedIn(viewModelScope)
    }

    fun hadithsPagingFlow(collectionSlug: String, bookNumber: String): Flow<PagingData<Hadith>> {
        return hadithRepository.hadithsPaging(collectionSlug, bookNumber).cachedIn(viewModelScope)
    }

    suspend fun getHadithCollectionName(slug: String): String? {
        return hadithRepository.getCollectionName(slug)
    }

    suspend fun getHadithBookName(collectionSlug: String, bookNumber: String): String? {
        return hadithRepository.getBookName(collectionSlug, bookNumber)
    }

    fun loadTafseer() {
        viewModelScope.launch {
            tafseerRepository.getTafseer(
                sourceLabel = _selectedTafseerSource.value,
                surahNumber = _tafseerSurahNumber.value,
                ayahNumber = _tafseerAyahNumber.value,
            ).collect { state ->
                _tafseerText.value = state
            }
        }
    }

    fun selectSurah(surah: Surah, startAyah: Int = 1) {
        _currentSurah.value = surah
        _currentAyahNumber.value = startAyah
        updateLastRead(surah.number, startAyah)
        scheduleCurrentSurahDownload()
        prefetchAdjacentAyahs(surah.number)
        _ayahCache.value[surah.number]?.let { cachedState ->
            _currentAyahs.value = cachedState
        }
        if (audioController.isPlaying.value) {
            startPlayback()
        }
    }

    fun selectAyah(ayahNumber: Int) {
        if (ayahNumber in 1.._currentSurah.value.versesCount) {
            _currentAyahNumber.value = ayahNumber
            updateLastRead(_currentSurah.value.number, ayahNumber)
            scheduleCurrentSurahDownload()
            if (audioController.isPlaying.value) {
                startPlayback()
            }
        }
    }

    fun setPlaying(playing: Boolean) {
        if (playing) {
            startPlayback()
        } else {
            audioController.setPlaying(false)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        audioController.setPlaybackSpeed(speed)
    }

    fun selectReciter(reciter: Reciter) {
        val wasPlaying = audioController.isPlaying.value
        _selectedReciter.value = reciter
        prefs.edit().putString("selected_reciter_id", reciter.id).apply()
        scheduleCurrentSurahDownload()
        if (wasPlaying) {
            startPlayback()
        }
    }

    private fun startPlayback() {
        val surah = _currentSurah.value
        val reciter = _selectedReciter.value
        audioController.playAyah(
            reciterId = reciter.id,
            subfolder = reciter.subfolder,
            surahNumber = surah.number,
            ayahNumber = _currentAyahNumber.value,
            verseCount = surah.versesCount,
            surahName = surah.nameEnglish,
            reciterName = reciter.nameEnglish,
            autoPlay = true,
        )
    }

    private fun scheduleCurrentSurahDownload() {
        val surah = _currentSurah.value
        val reciter = _selectedReciter.value
        surahDownloadScheduler.scheduleSurahDownload(
            reciterId = reciter.id,
            subfolder = reciter.subfolder,
            surahNumber = surah.number,
            startAyah = _currentAyahNumber.value,
            verseCount = surah.versesCount,
        )
    }

    private fun resolveReaderLayoutMode(): ReaderLayoutMode {
        return when (prefs.getString("reader_layout_mode", ReaderLayoutMode.List.name)) {
            ReaderLayoutMode.Book.name -> ReaderLayoutMode.Book
            else -> ReaderLayoutMode.List
        }
    }

    private fun resolveInitialReciter(): Reciter {
        val savedId = prefs.getString("selected_reciter_id", null)
        val resolvedId = when (savedId) {
            "alafasy" -> "ea_15"
            "abdulbasit" -> "ea_1"
            "sudais" -> "ea_7"
            "minshawi" -> "ea_34"
            else -> savedId
        }
        return EveryAyahReciters.findById(resolvedId ?: "") ?: EveryAyahReciters.popular.first()
    }

    fun onReaderVisible() {
        scheduleCurrentSurahDownload()
    }

    fun toggleBookmark(surah: Surah, ayahNumber: Int? = null) {
        viewModelScope.launch {
            val bookmarkId = if (ayahNumber == null) "${surah.number}" else "${surah.number}:$ayahNumber"
            if (dao.isBookmarked(bookmarkId)) {
                dao.deleteBookmark(bookmarkId)
            } else {
                val ayahList = when (val state = _currentAyahs.value) {
                    is UiState.Success -> state.data
                    else -> emptyList()
                }
                val arabic = if (ayahNumber != null) {
                    ayahList.find { it.number == ayahNumber }?.textArabic ?: ""
                } else {
                    ""
                }
                val translation = if (ayahNumber != null) {
                    ayahList.find { it.number == ayahNumber }?.textEnglish ?: ""
                } else {
                    ""
                }
                dao.insertBookmark(
                    Bookmark(
                        id = bookmarkId,
                        surahNumber = surah.number,
                        ayahNumber = ayahNumber,
                        surahName = surah.nameArabic,
                        surahNameEnglish = surah.nameEnglish,
                        numberOfVerses = surah.versesCount,
                        arabicText = arabic,
                        translationText = translation,
                    ),
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
            val surah = quranRepository.getSurah(surahNumber) ?: _currentSurah.value
            dao.updateLastRead(
                LastRead(
                    surahNumber = surahNumber,
                    ayahNumber = ayahNumber,
                    surahName = surah.nameArabic,
                    surahNameEnglish = surah.nameEnglish,
                ),
            )
        }
    }

    fun openTafseer(surahNumber: Int, ayahNumber: Int) {
        _tafseerSurahNumber.value = surahNumber
        _tafseerAyahNumber.value = ayahNumber
    }

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

    fun setReaderLayoutMode(mode: ReaderLayoutMode) {
        _readerLayoutMode.value = mode
        prefs.edit().putString("reader_layout_mode", mode.name).apply()
    }

    fun toggleReaderLayoutMode() {
        val nextMode = if (_readerLayoutMode.value == ReaderLayoutMode.List) {
            ReaderLayoutMode.Book
        } else {
            ReaderLayoutMode.List
        }
        setReaderLayoutMode(nextMode)
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

    suspend fun findSurah(number: Int): Surah? {
        return quranRepository.getSurah(number)
    }

    override fun onCleared() {
        audioController.shutdown()
        super.onCleared()
    }
}
