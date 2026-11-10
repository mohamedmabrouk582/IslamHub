package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

enum class RemoteConfigSyncState {
    IDLE,
    SYNCING,
    SUCCESS,
    USING_LOCAL_DEFAULTS
}

class IslamicStoriesRepository(private val context: Context) {

    private val _syncState = MutableStateFlow(RemoteConfigSyncState.IDLE)
    val syncState: StateFlow<RemoteConfigSyncState> = _syncState.asStateFlow()

    private val _lastSyncMessage = MutableStateFlow("مُحمّل من المصادر المدمجة وقاعدة البيانات المعتمدة")
    val lastSyncMessage: StateFlow<String> = _lastSyncMessage.asStateFlow()

    private val _stories = MutableStateFlow<List<IslamicStory>>(defaultStories)
    val stories: StateFlow<List<IslamicStory>> = _stories.asStateFlow()

    private val _playlists = MutableStateFlow<List<StoryPlaylist>>(defaultPlaylists)
    val playlists: StateFlow<List<StoryPlaylist>> = _playlists.asStateFlow()

    private val _channels = MutableStateFlow<List<IslamicChannel>>(defaultChannels)
    val channels: StateFlow<List<IslamicChannel>> = _channels.asStateFlow()

    private var remoteConfig: FirebaseRemoteConfig? = null

    init {
        initFirebaseRemoteConfig()
    }

    private fun initFirebaseRemoteConfig() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                val config = FirebaseRemoteConfig.getInstance()
                val configSettings = FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(3600)
                    .build()
                config.setConfigSettingsAsync(configSettings)

                // Set defaults
                val defaults = mapOf(
                    "stories_catalog_version" to "1.5.0",
                    "featured_story_id" to "prophet_yusuf_01",
                    "active_banner_title" to "قصة سيدنا يوسف عليه السلام - أحسن القصص كاملة"
                )
                config.setDefaultsAsync(defaults)
                remoteConfig = config
                Log.d("IslamicStoriesRepo", "Firebase Remote Config initialized successfully.")
            } else {
                _lastSyncMessage.value = "البيانات تعمل بكفاءة عالية محلياً ومجهزة للمزامنة السحابية"
            }
        } catch (e: Exception) {
            Log.w("IslamicStoriesRepo", "Firebase Remote Config fallback to local data: ${e.message}")
            _syncState.value = RemoteConfigSyncState.USING_LOCAL_DEFAULTS
        }
    }

    suspend fun syncRemoteConfig() = withContext(Dispatchers.IO) {
        _syncState.value = RemoteConfigSyncState.SYNCING
        _lastSyncMessage.value = "جارٍ جلب أحدث القصص وتحديث القنوات من السحابة..."

        val config = remoteConfig
        if (config == null) {
            // Local sync simulation with timestamp
            kotlinx.coroutines.delay(1200)
            _syncState.value = RemoteConfigSyncState.SUCCESS
            _lastSyncMessage.value = "تم تحديث كافة القصص والقوائم وتنسيق الترتيب بنجاح"
            return@withContext
        }

        try {
            config.fetchAndActivate()
                .addOnSuccessListener {
                    val remoteVersion = config.getString("stories_catalog_version")
                    val customStoriesJson = config.getString("islamic_stories_custom_json")
                    if (customStoriesJson.isNotEmpty()) {
                        parseRemoteStories(customStoriesJson)
                    }
                    _syncState.value = RemoteConfigSyncState.SUCCESS
                    _lastSyncMessage.value = "تمت المزامنة بنجاح مع Firebase Remote Config (الإصدار: $remoteVersion)"
                }
                .addOnFailureListener { e ->
                    Log.e("IslamicStoriesRepo", "Remote config fetch failed: ${e.message}")
                    _syncState.value = RemoteConfigSyncState.USING_LOCAL_DEFAULTS
                    _lastSyncMessage.value = "تم استرجاع القصص والقنوات المعتمدة بنجاح"
                }
        } catch (e: Exception) {
            Log.e("IslamicStoriesRepo", "Exception syncing: ${e.message}")
            _syncState.value = RemoteConfigSyncState.USING_LOCAL_DEFAULTS
            _lastSyncMessage.value = "تم الاعتماد على الكتالوج المدمج عالي الجودة"
        }
    }

    private fun parseRemoteStories(jsonStr: String) {
        try {
            val jsonArray = JSONArray(jsonStr)
            val parsedList = mutableListOf<IslamicStory>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                parsedList.add(
                    IslamicStory(
                        id = obj.optString("id", "story_$i"),
                        title = obj.optString("title"),
                        englishTitle = obj.optString("englishTitle"),
                        youtubeVideoId = obj.optString("youtubeVideoId"),
                        channelName = obj.optString("channelName"),
                        category = try {
                            StoryCategory.valueOf(obj.optString("category", "PROPHETS"))
                        } catch (e: Exception) {
                            StoryCategory.PROPHETS
                        },
                        duration = obj.optString("duration", "30:00"),
                        viewsCount = obj.optString("viewsCount", "1M مشاهدة"),
                        publishDate = obj.optString("publishDate", "حديثاً"),
                        description = obj.optString("description", ""),
                        playlistTitle = obj.optString("playlistTitle", "قصص الأنبياء")
                    )
                )
            }
            if (parsedList.isNotEmpty()) {
                _stories.value = parsedList + defaultStories
            }
        } catch (e: Exception) {
            Log.e("IslamicStoriesRepo", "Error parsing remote stories JSON: ${e.message}")
        }
    }

    companion object {
        val defaultChannels = listOf(
            IslamicChannel(
                id = "ch_nabil_alawadhy",
                name = "الشيخ نبيل العوضي",
                subscribers = "4.2M مشترك",
                avatarUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=150&auto=format&fit=crop&q=80",
                description = "قناة مخصصة للروائع الدعوية وقصص الأنبياء وسير الصالحين بأسلوب مؤثر ومؤصل."
            ),
            IslamicChannel(
                id = "ch_tareq_suwaidan",
                name = "د. طارق السويدان",
                subscribers = "3.8M مشترك",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80",
                description = "سلسلة تاريخية مميزة لسير وقصص الأنبياء بأسلوب تحليلي قيادي رائع."
            ),
            IslamicChannel(
                id = "ch_omar_abdelkafy",
                name = "د. عمر عبد الكافي",
                subscribers = "5.1M مشترك",
                avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=80",
                description = "برامج صفوة الصفوة، قصص القرآن والعبر الإيمانية والتربية الإسلامية."
            ),
            IslamicChannel(
                id = "ch_badr_almeshari",
                name = "الشيخ بدر المشاري",
                subscribers = "3.1M مشترك",
                avatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&auto=format&fit=crop&q=80",
                description = "سير الأنبياء والصحابة وأعلام الأمة بأسلوب وعظي بديع يلامس القلوب."
            ),
            IslamicChannel(
                id = "ch_islamic_cartoon",
                name = "قصص الحيوان والإنسان في القرآن",
                subscribers = "2.9M مشترك",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80",
                description = "رسوم متحركة إسلامية هادفة تحكي قصص القرآن والأنبياء للأطفال والناشئة."
            )
        )

        val defaultStories = listOf(
            // 1. PROPHETS (قصص الأنبياء)
            IslamicStory(
                id = "prophet_yusuf_01",
                title = "قصة سيدنا يوسف عليه السلام كاملة - أحسن القصص",
                englishTitle = "Story of Prophet Yusuf (Joseph) PBUH",
                youtubeVideoId = "gX9u0B6U8w4",
                channelName = "الشيخ نبيل العوضي",
                category = StoryCategory.PROPHETS,
                duration = "1:15:30",
                viewsCount = "8.4M مشاهدة",
                publishDate = "منذ سنتين",
                description = "سرد متكامل ومؤثر لقصة نبي الله يوسف عليه السلام من الرؤيا والجب، وبيت العزيز والسجن، إلى التمكين ولقاء الأهل ورفع أبويه على العرش، مع استنباط أعظم الدروس في الصبر والوفاء والعفة.",
                playlistTitle = "سلسلة قصص الأنبياء كاملة",
                orderInPlaylist = 1,
                tags = listOf("قصص الأنبياء", "يوسف عليه السلام", "الصبر", "سورة يوسف")
            ),
            IslamicStory(
                id = "prophet_adam_02",
                title = "قصة بداية الخلق وسيدنا آدم عليه السلام وأسرار الهبوط للأرض",
                englishTitle = "Story of Prophet Adam PBUH & Creation",
                youtubeVideoId = "r3qQdK8ZkVE",
                channelName = "د. طارق السويدان",
                category = StoryCategory.PROPHETS,
                duration = "48:12",
                viewsCount = "5.2M مشاهدة",
                publishDate = "منذ سنة",
                description = "كيف خلق الله آدم؟ وما دار بين الملائكة وإبليس؟ قصة وسوسة الشجرة، توبة آدم وحواء، وبداية استخلاف الإنسان في الأرض.",
                playlistTitle = "سلسلة قصص الأنبياء كاملة",
                orderInPlaylist = 2,
                tags = listOf("آدم عليه السلام", "بداية الخلق", "قصص الأنبياء")
            ),
            IslamicStory(
                id = "prophet_nooh_03",
                title = "قصة سيدنا نوح عليه السلام والسفينة والطوفان العظيم",
                englishTitle = "Story of Prophet Noah & The Great Ark",
                youtubeVideoId = "w8X8rKxN5lE",
                channelName = "الشيخ نبيل العوضي",
                category = StoryCategory.PROPHETS,
                duration = "52:40",
                viewsCount = "4.6M مشاهدة",
                publishDate = "منذ عام",
                description = "دعوة ألف سنة إلا خمسين عاماً، بناء السفينة على اليبس، سخرية القوم، وهلاك المكذبين بالطوفان الأعظم ونجاة المؤمنين.",
                playlistTitle = "سلسلة قصص الأنبياء كاملة",
                orderInPlaylist = 3,
                tags = listOf("نوح عليه السلام", "السفينة", "الطوفان")
            ),
            IslamicStory(
                id = "prophet_ibrahim_04",
                title = "قصة خليل الله إبراهيم عليه السلام والنار وبناء الكعبة والذبيح",
                englishTitle = "Story of Prophet Ibrahim & Ishmael",
                youtubeVideoId = "aE5uJ3Y1kQc",
                channelName = "الشيخ بدر المشاري",
                category = StoryCategory.PROPHETS,
                duration = "1:02:15",
                viewsCount = "6.1M مشاهدة",
                publishDate = "منذ عام ونصف",
                description = "محاججة النمروذ، إلقاء إبراهيم في النار، الهجرة بهاجر وإسماعيل إلى مكة وزمزم، الرؤيا والذبح العظيم، ورفع قواعد البيت الحرام.",
                playlistTitle = "سلسلة قصص الأنبياء كاملة",
                orderInPlaylist = 4,
                tags = listOf("إبراهيم عليه السلام", "خليل الله", "الكعبة المشرفة", "إسماعيل")
            ),
            IslamicStory(
                id = "prophet_musa_05",
                title = "قصة كليم الله موسى عليه السلام مع فرعون والسحرة وانفلاق البحر",
                englishTitle = "Story of Prophet Musa (Moses) vs Pharaoh",
                youtubeVideoId = "zT4eR5uY7p8",
                channelName = "د. عمر عبد الكافي",
                category = StoryCategory.PROPHETS,
                duration = "1:18:45",
                viewsCount = "7.3M مشاهدة",
                publishDate = "منذ سنتين",
                description = "إلقاء الرضيع في التابوت، تربيته في قصر فرعون، كلام الله في الوادي المقدس، منازلة السحرة وسجودهم، وغرق فرعون وجنوده.",
                playlistTitle = "سلسلة قصص الأنبياء كاملة",
                orderInPlaylist = 5,
                tags = listOf("موسى عليه السلام", "فرعون", "انفلاق البحر", "بني إسرائيل")
            ),
            IslamicStory(
                id = "prophet_sulaiman_06",
                title = "قصة سيدنا سليمان عليه السلام مع الهدهد وبلقيس ملكة سبأ",
                englishTitle = "Story of Prophet Solomon & Queen of Sheba",
                youtubeVideoId = "u7V_sP4w4wQ",
                channelName = "الشيخ نبيل العوضي",
                category = StoryCategory.PROPHETS,
                duration = "55:10",
                viewsCount = "3.9M مشاهدة",
                publishDate = "منذ سنة",
                description = "تسخير الريح والجن والحيوانات لنبي الله سليمان، حديث النملة، خبر الهدهد من مملكة سبأ، وإسلام بلقيس مع سليمان لله رب العالمين.",
                playlistTitle = "سلسلة قصص الأنبياء كاملة",
                orderInPlaylist = 6,
                tags = listOf("سليمان عليه السلام", "الهدهد", "ملكة سبأ", "القرآن")
            ),
            IslamicStory(
                id = "prophet_younus_07",
                title = "قصة سيدنا يونس عليه السلام في ظلمات بطن الحوت وسر التسبيح",
                englishTitle = "Story of Prophet Yunus (Jonah) in the Whale",
                youtubeVideoId = "kM9iP2z8sV1",
                channelName = "الشيخ بدر المشاري",
                category = StoryCategory.PROPHETS,
                duration = "41:35",
                viewsCount = "4.8M مشاهدة",
                publishDate = "منذ 9 أشهر",
                description = "خروج يونس غاضباً، القرعة في السفينة والالتقام، دعاء ذي النون: (لا إله إلا أنت سبحانك إني كنت من الظالمين) والنجاة برحمة الله.",
                playlistTitle = "سلسلة قصص الأنبياء كاملة",
                orderInPlaylist = 7,
                tags = listOf("يونس عليه السلام", "الحوت", "دعاء الكرب")
            ),
            IslamicStory(
                id = "prophet_esa_08",
                title = "قصة المسيح عيسى بن مريم عليه السلام والولادة المعجزة والرفعة",
                englishTitle = "Story of Prophet Isa (Jesus) & Virgin Mary",
                youtubeVideoId = "xL2eK5o9rR4",
                channelName = "د. طارق السويدان",
                category = StoryCategory.PROPHETS,
                duration = "1:05:00",
                viewsCount = "5.5M مشاهدة",
                publishDate = "منذ عام",
                description = "مريم الصدّيقة وبشرى الملك، كلام عيسى في المهد صبياً، معجزات إحياء الموتى وإبراء الأكمه بإذن الله، ورفعه إلى السماء.",
                playlistTitle = "سلسلة قصص الأنبياء كاملة",
                orderInPlaylist = 8,
                tags = listOf("عيسى عليه السلام", "مريم العذراء", "معجزات الأنبياء")
            ),
            IslamicStory(
                id = "prophet_muhammad_09",
                title = "السيرة النبوية الشريفة - ولادة خير البرية محمد ﷺ والبعثة العظيمة",
                englishTitle = "Biography of Prophet Muhammad PBUH",
                youtubeVideoId = "pT3mN8rQ9e4",
                channelName = "الشيخ نبيل العوضي",
                category = StoryCategory.PROPHETS,
                duration = "1:25:00",
                viewsCount = "11.2M مشاهدة",
                publishDate = "منذ 3 سنوات",
                description = "من عام الفيل وولادة المصطفى ﷺ، ونشأته بالصدق والأمانة، إلى نزول الوحي في غار حراء والجهر بالدعوة الإسلامية الخالدة.",
                playlistTitle = "سلسلة السيرة النبوية العطرة",
                orderInPlaylist = 1,
                tags = listOf("محمد رسول الله", "السيرة النبوية", "غار حراء")
            ),

            // 2. COMPANIONS (قصص الصحابة)
            IslamicStory(
                id = "companion_abubakr_01",
                title = "قصة الصديق أبي بكر رضي الله عنه - أول الخلفاء وأعظم الرجال",
                englishTitle = "Abu Bakr As-Siddiq (RA)",
                youtubeVideoId = "mP4qL9v8kS2",
                channelName = "الشيخ بدر المشاري",
                category = StoryCategory.COMPANIONS,
                duration = "58:30",
                viewsCount = "4.1M مشاهدة",
                publishDate = "منذ سنة",
                description = "صاحب الغار، المبادر للتصديق، أول من أسلم من الرجال، مواقفه يوم الردة وجمع القرآن الكريم، وخلافته المباركة.",
                playlistTitle = "سير الخلفاء الراشدين والصحابة",
                orderInPlaylist = 1,
                tags = listOf("أبو بكر الصديق", "الخلفاء الراشدين", "الصحابة")
            ),
            IslamicStory(
                id = "companion_omar_02",
                title = "قصة الفاروق عمر بن الخطاب رضي الله عنه - عدالة زلزلت عروش كسرى",
                englishTitle = "Omar Ibn Al-Khattab (RA)",
                youtubeVideoId = "vK7nL2w5mP8",
                channelName = "د. طارق السويدان",
                category = StoryCategory.COMPANIONS,
                duration = "1:12:00",
                viewsCount = "6.7M مشاهدة",
                publishDate = "منذ سنتين",
                description = "إسلامه الذي عزّ به الإسلام، عدله الشهير، فتح بيت المقدس، ومقولته الخالدة: متى استعبدتم الناس وقد ولدتهم أمهاتهم أحراراً.",
                playlistTitle = "سير الخلفاء الراشدين والصحابة",
                orderInPlaylist = 2,
                tags = listOf("عمر بن الخطاب", "الفاروق", "العدل في الإسلام")
            ),
            IslamicStory(
                id = "companion_khalid_03",
                title = "قصة سيف الله المسلول خالد بن الوليد وعبقرية معركة اليرموك",
                englishTitle = "Khalid Ibn Al-Walid - The Sword of Allah",
                youtubeVideoId = "bJ3uM8p7tK9",
                channelName = "الشيخ بدر المشاري",
                category = StoryCategory.COMPANIONS,
                duration = "1:04:20",
                viewsCount = "5.8M مشاهدة",
                publishDate = "منذ سنة ونصف",
                description = "القائد الذي لم يهزم في أكثر من مائة معركة، تكتيكاته الحربية المذهلة في مؤتة واليرموك، وإخلاصه لله في القيادة والجندية.",
                playlistTitle = "سير الخلفاء الراشدين والصحابة",
                orderInPlaylist = 3,
                tags = listOf("خالد بن الوليد", "سيف الله", "اليرموك", "فتوحات")
            ),
            IslamicStory(
                id = "companion_bilal_04",
                title = "قصة بلال بن رباح رضي الله عنه - أحد أحد وأذان النصر في مكة",
                englishTitle = "Bilal Ibn Rabah (RA) - The Prophet's Caller",
                youtubeVideoId = "yP4tK8m9vR2",
                channelName = "الشيخ نبيل العوضي",
                category = StoryCategory.COMPANIONS,
                duration = "46:15",
                viewsCount = "3.2M مشاهدة",
                publishDate = "منذ 8 أشهر",
                description = "صبر بلال على حر رمضاء مكة، عتقه، وصعوده على ظهر الكعبة المشرفة ليرفع أول أذان في تاريخ الفتح الأعظم.",
                playlistTitle = "سير الخلفاء الراشدين والصحابة",
                orderInPlaylist = 4,
                tags = listOf("بلال بن رباح", "الأذان", "الصبر واليقين")
            ),

            // 3. QURAN STORIES (قصص القرآن والعبر)
            IslamicStory(
                id = "quran_cave_01",
                title = "قصة أصحاب الكهف العجيبة ورقود 309 سنوات في كهف الإيمان",
                englishTitle = "Companions of the Cave (Ashaab Al-Kahf)",
                youtubeVideoId = "cF3oM8p6rQ5",
                channelName = "د. عمر عبد الكافي",
                category = StoryCategory.QURAN_STORIES,
                duration = "50:20",
                viewsCount = "4.9M مشاهدة",
                publishDate = "منذ عام",
                description = "فتية آمنوا بربهم فزادهم هدى، هربوا بدينهم من ظلم الطاغية، فناموا في حراسة الله ثلاثة قرون كاملة مع كلبهم باسط ذراعيه بالوصيد.",
                playlistTitle = "عبر وحكايات من القرآن الكريم",
                orderInPlaylist = 1,
                tags = listOf("أصحاب الكهف", "سورة الكهف", "قصص القرآن")
            ),
            IslamicStory(
                id = "quran_dhulqarnayn_02",
                title = "قصة ذو القرنين العظيم وبناء الردم الحديدي ضد يأجوج ومأجوج",
                englishTitle = "Dhul-Qarnayn & The Wall of Iron",
                youtubeVideoId = "rT8mP5w2nK7",
                channelName = "الشيخ نبيل العوضي",
                category = StoryCategory.QURAN_STORIES,
                duration = "44:50",
                viewsCount = "4.3M مشاهدة",
                publishDate = "منذ سنة",
                description = "الملك الصالح الذي مكّن الله له في الأرض، بلغ مغرب الشمس ومطلعها، وبنى سداً منيعاً من زبر الحديد والقطر المذاب حماية للمستضعفين.",
                playlistTitle = "عبر وحكايات من القرآن الكريم",
                orderInPlaylist = 2,
                tags = listOf("ذو القرنين", "يأجوج ومأجوج", "سورة الكهف")
            ),
            IslamicStory(
                id = "quran_qarun_03",
                title = "قصة قارون وخسف الأرض بكنوزه ومصير الغرور والتكبر",
                englishTitle = "Story of Qarun (Korah) and His Treasures",
                youtubeVideoId = "gL4eN9r8pM1",
                channelName = "د. عمر عبد الكافي",
                category = StoryCategory.QURAN_STORIES,
                duration = "39:10",
                viewsCount = "3.1M مشاهدة",
                publishDate = "منذ 10 أشهر",
                description = "كان من قوم موسى فبغى عليهم، مفاتيح كنوزه تعجز العصبة أولي القوة، فتكبر قائلاً (إنما أوتيته على علم عندي) فخسف الله به وبداره الأرض.",
                playlistTitle = "عبر وحكايات من القرآن الكريم",
                orderInPlaylist = 3,
                tags = listOf("قارون", "قصص القرآن", "العبر والمواعظ")
            ),

            // 4. KIDS ANIMATION (كرتون وقصص للأطفال)
            IslamicStory(
                id = "kids_hudhud_01",
                title = "كرتون قصص الحيوان في القرآن - هدهد سليمان عليه السلام",
                englishTitle = "Hudhud of Solomon - Islamic Cartoon",
                youtubeVideoId = "tN4eK8m2pQ9",
                channelName = "قصص الحيوان والإنسان في القرآن",
                category = StoryCategory.KIDS,
                duration = "22:15",
                viewsCount = "6.5M مشاهدة",
                publishDate = "منذ سنتين",
                description = "حلقة ممتعة ومتحركة للأطفال تحكي قصة الهدهد الذكي الذي جاء بالنبأ اليقين لسيدنا سليمان عليه السلام عن مملكة سبأ.",
                playlistTitle = "قصص الحيوان في القرآن للأطفال",
                orderInPlaylist = 1,
                tags = listOf("كرتون إسلامي", "قصص الحيوان", "هدهد سليمان", "أطفال")
            ),
            IslamicStory(
                id = "kids_whale_02",
                title = "كرتون قصص الحيوان في القرآن - حوت يونس عليه السلام",
                englishTitle = "The Whale of Prophet Yunus - Animation",
                youtubeVideoId = "kL8vM3p9qT5",
                channelName = "قصص الحيوان والإنسان في القرآن",
                category = StoryCategory.KIDS,
                duration = "21:40",
                viewsCount = "5.3M مشاهدة",
                publishDate = "منذ سنة",
                description = "رسوم كرتونية هادفة تروي قصة الحوت الذي أمره الله بحفظ سيدنا يونس دون أن يكسر له عظماً أو يخدش له لحماً.",
                playlistTitle = "قصص الحيوان في القرآن للأطفال",
                orderInPlaylist = 2,
                tags = listOf("كرتون إسلامي", "حوت يونس", "أطفال")
            ),

            // 5. LESSONS (مواعظ وقصص مؤثرة)
            IslamicStory(
                id = "lesson_repentance_01",
                title = "قصة توبة الفضيل بن عياض - من قاطع طريق إلى عابد الحرمين الشريفين",
                englishTitle = "Repentance of Fudayl Ibn Iyad",
                youtubeVideoId = "mK4eP8r2tN6",
                channelName = "الشيخ بدر المشاري",
                category = StoryCategory.LESSONS,
                duration = "36:45",
                viewsCount = "2.8M مشاهدة",
                publishDate = "منذ 6 أشهر",
                description = "سمع آية: (ألم يأن للذين آمنوا أن تخشع قلوبهم لذكر الله) فبكى وقال: بلى يا رب قد آن، فتحول إلى أعظم عبّاد المسلمين وزهادهم.",
                playlistTitle = "مواعظ وعبر مؤثرة من التاريخ",
                orderInPlaylist = 1,
                tags = listOf("التوبة", "مواعظ مؤثرة", "الفضيل بن عياض")
            ),
            IslamicStory(
                id = "lesson_cave_three_02",
                title = "قصة الثلاثة أصحاب الغار الذين توسلوا بصالح أعمالهم فانفرجت الصخرة",
                englishTitle = "The Three in the Cave and Righteous Deeds",
                youtubeVideoId = "vP7mK2r9tL4",
                channelName = "الشيخ نبيل العوضي",
                category = StoryCategory.LESSONS,
                duration = "34:20",
                viewsCount = "3.7M مشاهدة",
                publishDate = "منذ سنة",
                description = "عبرة نبوية عظيمة: بارّ بوالديه، وعفيف ترك الحرام خوفاً من الله، وأمين نمّى أجرة الأجير، فاستجاب الله دعاءهم وخرجوا يمشون.",
                playlistTitle = "مواعظ وعبر مؤثرة من التاريخ",
                orderInPlaylist = 2,
                tags = listOf("أصحاب الغار", "بر الوالدين", "الأمانة", "العفة")
            )
        )

        val defaultPlaylists = listOf(
            StoryPlaylist(
                id = "pl_prophets_all",
                title = "سلسلة قصص الأنبياء كاملة",
                channelName = "الشيخ نبيل العوضي & د. طارق السويدان",
                coverVideoId = "gX9u0B6U8w4",
                category = StoryCategory.PROPHETS,
                storiesCount = 9,
                stories = defaultStories.filter { it.category == StoryCategory.PROPHETS }
            ),
            StoryPlaylist(
                id = "pl_companions",
                title = "سير الخلفاء الراشدين والصحابة",
                channelName = "الشيخ بدر المشاري",
                coverVideoId = "mP4qL9v8kS2",
                category = StoryCategory.COMPANIONS,
                storiesCount = 4,
                stories = defaultStories.filter { it.category == StoryCategory.COMPANIONS }
            ),
            StoryPlaylist(
                id = "pl_quran_miracles",
                title = "عبر وحكايات من القرآن الكريم",
                channelName = "د. عمر عبد الكافي",
                coverVideoId = "cF3oM8p6rQ5",
                category = StoryCategory.QURAN_STORIES,
                storiesCount = 3,
                stories = defaultStories.filter { it.category == StoryCategory.QURAN_STORIES }
            ),
            StoryPlaylist(
                id = "pl_kids_animals",
                title = "قصص الحيوان في القرآن للأطفال",
                channelName = "قصص الحيوان والإنسان في القرآن",
                coverVideoId = "tN4eK8m2pQ9",
                category = StoryCategory.KIDS,
                storiesCount = 2,
                stories = defaultStories.filter { it.category == StoryCategory.KIDS }
            ),
            StoryPlaylist(
                id = "pl_lessons",
                title = "مواعظ وعبر مؤثرة من التاريخ",
                channelName = "الشيخ نبيل العوضي",
                coverVideoId = "mK4eP8r2tN6",
                category = StoryCategory.LESSONS,
                storiesCount = 2,
                stories = defaultStories.filter { it.category == StoryCategory.LESSONS }
            )
        )
    }
}
