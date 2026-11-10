package com.example.data

enum class StoryCategory(val titleArabic: String, val titleEnglish: String) {
    ALL("الكل", "All"),
    PROPHETS("قصص الأنبياء", "Prophets"),
    COMPANIONS("قصص الصحابة والتابعين", "Companions"),
    QURAN_STORIES("قصص القرآن والعِبر", "Quran Stories"),
    LESSONS("مواعظ وقصص مؤثرة", "Lessons & Faith"),
    KIDS("كرتون وأطفال", "Kids Animation")
}

enum class PlayerMode {
    HIDDEN,
    MINI,
    EXPANDED
}

data class IslamicStory(
    val id: String,
    val title: String,
    val englishTitle: String,
    val youtubeVideoId: String,
    val channelName: String,
    val channelAvatarUrl: String = "",
    val category: StoryCategory,
    val duration: String,
    val viewsCount: String,
    val publishDate: String,
    val description: String,
    val playlistTitle: String,
    val orderInPlaylist: Int = 1,
    val tags: List<String> = emptyList()
) {
    val thumbnailUrl: String
        get() = "https://img.youtube.com/vi/$youtubeVideoId/hqdefault.jpg"
}

data class IslamicChannel(
    val id: String,
    val name: String,
    val subscribers: String,
    val avatarUrl: String,
    val verified: Boolean = true,
    val description: String
)

data class StoryPlaylist(
    val id: String,
    val title: String,
    val channelName: String,
    val coverVideoId: String,
    val category: StoryCategory,
    val storiesCount: Int,
    val stories: List<IslamicStory> = emptyList()
) {
    val coverThumbnailUrl: String
        get() = "https://img.youtube.com/vi/$coverVideoId/hqdefault.jpg"
}
