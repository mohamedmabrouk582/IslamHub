package com.example.data.mapper

import com.example.data.local.entity.AyahEntity
import com.example.data.local.entity.HadithBookEntity
import com.example.data.local.entity.HadithCollectionEntity
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.SurahEntity
import com.example.data.local.entity.TafseerEntity
import com.example.data.model.Ayah
import com.example.data.model.Hadith
import com.example.data.model.HadithBook
import com.example.data.model.HadithCollection
import com.example.data.model.Surah
import com.example.data.remote.quran.ChapterDto
import com.example.data.remote.quran.VerseDto
import com.example.data.remote.sunnah.SunnahBookDto
import com.example.data.remote.sunnah.SunnahCollectionDto
import com.example.data.remote.sunnah.SunnahHadithDto
import com.example.data.remote.tafseer.TafseerDto
import com.example.util.HtmlUtils

fun ChapterDto.toEntity(): SurahEntity {
    return SurahEntity(
        number = chapterNumber,
        nameArabic = nameArabic,
        nameEnglish = nameSimple,
        translation = translatedName.name,
        type = revelationPlace.uppercase(),
        versesCount = versesCount,
    )
}

fun SurahEntity.toDomain(): Surah {
    return Surah(
        number = number,
        nameArabic = nameArabic,
        nameEnglish = nameEnglish,
        translation = translation,
        type = type,
        versesCount = versesCount,
        juzNumber = juzNumber,
        hasSajdah = hasSajdah,
    )
}

fun VerseDto.toEntity(): AyahEntity {
    val transliteration = words
        .orEmpty()
        .filter { it.charType == "word" }
        .mapNotNull { it.transliteration?.text }
        .joinToString(" ")
    return AyahEntity(
        surahNumber = chapterId,
        ayahNumber = verseNumber,
        textArabic = textMadani.orEmpty(),
        textEnglish = translations?.firstOrNull()?.text.orEmpty(),
        transliteration = transliteration,
    )
}

fun AyahEntity.toDomain(): Ayah {
    return Ayah(
        number = ayahNumber,
        surahNumber = surahNumber,
        textArabic = textArabic,
        textEnglish = textEnglish,
        transliteration = transliteration,
    )
}

fun SunnahCollectionDto.toEntity(): HadithCollectionEntity {
    val english = collection.firstOrNull { it.lang == "en" }?.title.orEmpty()
    val arabic = collection.firstOrNull { it.lang == "ar" }?.title.orEmpty()
    return HadithCollectionEntity(
        slug = name,
        nameEnglish = english,
        nameArabic = arabic,
        totalHadith = totalHadith,
    )
}

fun HadithCollectionEntity.toDomain(): HadithCollection {
    return HadithCollection(
        slug = slug,
        nameEnglish = nameEnglish,
        nameArabic = nameArabic,
        totalHadith = totalHadith,
    )
}

fun SunnahBookDto.toEntity(collectionSlug: String): HadithBookEntity {
    val english = book.firstOrNull { it.lang == "en" }?.name.orEmpty()
    val arabic = book.firstOrNull { it.lang == "ar" }?.name.orEmpty()
    return HadithBookEntity(
        collectionSlug = collectionSlug,
        bookNumber = bookNumber,
        nameEnglish = english,
        nameArabic = arabic,
        hadithCount = numberOfHadith,
    )
}

fun HadithBookEntity.toDomain(): HadithBook {
    return HadithBook(
        collectionSlug = collectionSlug,
        bookNumber = bookNumber,
        nameEnglish = nameEnglish,
        nameArabic = nameArabic,
        hadithCount = hadithCount,
    )
}

fun SunnahHadithDto.toEntity(
    collectionSlug: String,
    bookNumber: String,
    collectionName: String,
    bookName: String,
): HadithEntity {
    val english = hadith.firstOrNull { it.lang == "en" }
    val arabic = hadith.firstOrNull { it.lang == "ar" }
    return HadithEntity(
        id = "$collectionSlug:$bookNumber:$hadithNumber",
        collectionSlug = collectionSlug,
        bookNumber = bookNumber,
        textArabic = HtmlUtils.fromHtml(arabic?.body.orEmpty()),
        textEnglish = HtmlUtils.fromHtml(english?.body.orEmpty()),
        narrator = HtmlUtils.fromHtml(english?.chapterTitle.orEmpty()),
        collectionName = collectionName,
        bookName = bookName,
    )
}

fun HadithEntity.toDomain(): Hadith {
    return Hadith(
        id = id,
        textArabic = textArabic,
        textEnglish = textEnglish,
        narrator = narrator,
        collection = collectionName,
        bookName = bookName,
    )
}

fun TafseerDto.toEntity(surahNumber: Int): TafseerEntity {
    return TafseerEntity(
        tafseerId = tafseerId,
        surahNumber = surahNumber,
        ayahNumber = ayahNumber,
        text = text,
        tafseerName = tafseerName,
    )
}

object TafseerSourceMapper {
    private val sourceToId = mapOf(
        "التفسير الميسر" to 1,
        "ابن كثير" to 1,
        "الطبري" to 1,
        "السعدي" to 1,
    )

    fun toTafseerId(sourceLabel: String): Int = sourceToId[sourceLabel] ?: 1
}
