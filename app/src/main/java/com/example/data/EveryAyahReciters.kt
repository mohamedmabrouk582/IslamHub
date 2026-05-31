package com.example.data

import com.example.data.model.Reciter

object EveryAyahReciters {
    private val popularOrder = listOf(15, 7, 1, 34, 20, 29, 44, 11, 41, 16, 25, 37)

    private val arabicNamesByEnglish = mapOf(
        "Abdul Basit Murattal" to "عبد الباسط عبد الصمد",
        "Abdul Basit Mujawwad" to "عبد الباسط عبد الصمد",
        "Abdullah Basfar" to "عبد الله بصفر",
        "Abdurrahmaan As-Sudais" to "عبد الرحمن السديس",
        "AbdulSamad QuranExplorer.Com" to "عبد الصمد",
        "Abu Bakr Ash-Shaatree" to "أبو بكر الشاطري",
        "Ahmed ibn Ali al-Ajamy QuranExplorer.Com" to "أحمد بن علي العجمي",
        "Ahmed ibn Ali al-Ajamy KetabAllah.Net" to "أحمد بن علي العجمي",
        "Alafasy" to "مشاري راشد العفاسي",
        "Ghamadi" to "سعد الغامدي",
        "Hani Rifai" to "هاني الرفاعي",
        "Husary" to "محمود خليل الحصري",
        "Husary Mujawwad" to "محمود خليل الحصري",
        "Hudhaify" to "علي بن عبد الرحمن الحذيفي",
        "Ibrahim Akhdar" to "إبراهيم الأخضر",
        "Maher Al Muaiqly" to "ماهر المعيقلي",
        "Menshawi" to "محمد صديق المنشاوي",
        "Minshawy Mujawwad" to "محمد صديق المنشاوي",
        "Minshawy Murattal" to "محمد صديق المنشاوي",
        "Mohammad al Tablaway" to "محمد الطبلاوي",
        "Muhammad Ayyoub" to "محمد أيوب",
        "Muhammad Jibreel" to "محمد جبريل",
        "Mustafa Ismail" to "مصطفى إسماعيل",
        "Saood bin Ibraaheem Ash-Shuraym" to "سعود الشريم",
    )

    private val entries = listOf(
        entry(1, "Abdul_Basit_Murattal_64kbps", "Abdul Basit Murattal", "64kbps"),
        entry(2, "Abdul_Basit_Murattal_192kbps", "Abdul Basit Murattal", "192kbps"),
        entry(3, "Abdul_Basit_Mujawwad_128kbps", "Abdul Basit Mujawwad", "128kbps"),
        entry(4, "Abdullah_Basfar_32kbps", "Abdullah Basfar", "32kbps"),
        entry(5, "Abdullah_Basfar_64kbps", "Abdullah Basfar", "64kbps"),
        entry(6, "Abdullah_Basfar_192kbps", "Abdullah Basfar", "192kbps"),
        entry(7, "Abdurrahmaan_As-Sudais_64kbps", "Abdurrahmaan As-Sudais", "64kbps"),
        entry(8, "Abdurrahmaan_As-Sudais_192kbps", "Abdurrahmaan As-Sudais", "192kbps"),
        entry(9, "AbdulSamad_64kbps_QuranExplorer.Com", "AbdulSamad QuranExplorer.Com", "64kbps"),
        entry(10, "Abu_Bakr_Ash-Shaatree_64kbps", "Abu Bakr Ash-Shaatree", "64kbps"),
        entry(11, "Abu_Bakr_Ash-Shaatree_128kbps", "Abu Bakr Ash-Shaatree", "128kbps"),
        entry(12, "Ahmed_ibn_Ali_al-Ajamy_64kbps_QuranExplorer.Com", "Ahmed ibn Ali al-Ajamy QuranExplorer.Com", "64kbps"),
        entry(13, "Ahmed_ibn_Ali_al-Ajamy_128kbps_ketaballah.net", "Ahmed ibn Ali al-Ajamy KetabAllah.Net", "128kbps"),
        entry(14, "Alafasy_64kbps", "Alafasy", "64kbps"),
        entry(15, "Alafasy_128kbps", "Alafasy", "128kbps"),
        entry(16, "Ghamadi_40kbps", "Ghamadi", "40kbps"),
        entry(17, "Hani_Rifai_64kbps", "Hani Rifai", "64kbps"),
        entry(18, "Hani_Rifai_192kbps", "Hani Rifai", "192kbps"),
        entry(19, "Husary_64kbps", "Husary", "64kbps"),
        entry(20, "Husary_128kbps", "Husary", "128kbps"),
        entry(21, "Husary_Mujawwad_64kbps", "Husary Mujawwad", "64kbps"),
        entry(22, "Husary_128kbps_Mujawwad", "Husary Mujawwad", "128kbps"),
        entry(23, "Hudhaify_32kbps", "Hudhaify", "32kbps"),
        entry(24, "Hudhaify_64kbps", "Hudhaify", "64kbps"),
        entry(25, "Hudhaify_128kbps", "Hudhaify", "128kbps"),
        entry(26, "Ibrahim_Akhdar_32kbps", "Ibrahim Akhdar", "32kbps"),
        entry(27, "Ibrahim_Akhdar_64kbps", "Ibrahim Akhdar", "64kbps"),
        entry(28, "Maher_AlMuaiqly_64kbps", "Maher Al Muaiqly", "64kbps"),
        entry(29, "MaherAlMuaiqly128kbps", "Maher Al Muaiqly", "128kbps"),
        entry(30, "Menshawi_16kbps", "Menshawi", "16kbps"),
        entry(31, "Menshawi_32kbps", "Menshawi", "32kbps"),
        entry(32, "Minshawy_Mujawwad_64kbps", "Minshawy Mujawwad", "64kbps"),
        entry(33, "Minshawy_Mujawwad_192kbps", "Minshawy Mujawwad", "192kbps"),
        entry(34, "Minshawy_Murattal_128kbps", "Minshawy Murattal", "128kbps"),
        entry(35, "Mohammad_al_Tablaway_64kbps", "Mohammad al Tablaway", "64kbps"),
        entry(36, "Mohammad_al_Tablaway_128kbps", "Mohammad al Tablaway", "128kbps"),
        entry(37, "Muhammad_Ayyoub_128kbps", "Muhammad Ayyoub", "128kbps"),
        entry(38, "Muhammad_Ayyoub_64kbps", "Muhammad Ayyoub", "64kbps"),
        entry(39, "Muhammad_Ayyoub_32kbps", "Muhammad Ayyoub", "32kbps"),
        entry(40, "Muhammad_Jibreel_64kbps", "Muhammad Jibreel", "64kbps"),
        entry(41, "Muhammad_Jibreel_128kbps", "Muhammad Jibreel", "128kbps"),
        entry(42, "Mustafa_Ismail_48kbps", "Mustafa Ismail", "48kbps"),
        entry(43, "Saood_ash-Shuraym_64kbps", "Saood bin Ibraaheem Ash-Shuraym", "64kbps"),
        entry(44, "Saood_ash-Shuraym_128kbps", "Saood bin Ibraaheem Ash-Shuraym", "128kbps"),
    )

    val popular: List<Reciter> = popularOrder.mapNotNull { id ->
        entries.find { it.everyAyahId == id }
    }

    val all: List<Reciter> = buildList {
        addAll(popular)
        val popularIds = popularOrder.toSet()
        addAll(
            entries
                .filter { it.everyAyahId !in popularIds }
                .sortedWith(
                    compareBy<Reciter> { it.nameEnglish }
                        .thenByDescending { bitrateSortKey(it.bitrateLabel) },
                ),
        )
    }

    fun findById(id: String): Reciter? = all.find { it.id == id }

    private fun entry(
        everyAyahId: Int,
        subfolder: String,
        nameEnglish: String,
        bitrateLabel: String,
    ): Reciter = Reciter(
        id = "ea_$everyAyahId",
        everyAyahId = everyAyahId,
        subfolder = subfolder,
        nameEnglish = nameEnglish,
        nameArabic = arabicNamesByEnglish[nameEnglish],
        bitrateLabel = bitrateLabel,
        avatarResId = ReciterAvatars.resIdFor(nameEnglish),
    )

    private fun bitrateSortKey(label: String): Int {
        val digits = label.filter { it.isDigit() }
        return digits.toIntOrNull() ?: 0
    }
}
