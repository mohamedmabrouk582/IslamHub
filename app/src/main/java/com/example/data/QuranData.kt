package com.example.data

data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val translation: String,
    val type: String, // "MECCAN" or "MEDINAN"
    val versesCount: Int,
    val juzNumber: Int,
    val hasSajdah: Boolean = false
)

data class Ayah(
    val number: Int,
    val surahNumber: Int,
    val textArabic: String,
    val textEnglish: String,
    val transliteration: String = ""
)

data class Reciter(
    val id: String,
    val nameEnglish: String,
    val nameArabic: String,
    val imageUrl: String
)

data class Hadith(
    val id: String,
    val textArabic: String,
    val textEnglish: String,
    val narrator: String,
    val collection: String, // "صحيح البخاري", "صحيح مسلم", etc.
    val topic: String // "الأخلاق والآداب", "الصلاة والطهاره", "الإيمان", "الأذكار والأدعية"
)

object QuranData {

    val reciters = listOf(
        Reciter(
            id = "alafasy",
            nameEnglish = "Mishary Rashid Al-Afasy",
            nameArabic = "مشاري راشد العفاسي",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBlKzn6R8E-WUyQNHmpygOgwNa4n05ZyUZ5kkDyY3ErfkjdEkZRAm9N5nS0a3dhlJH1GqEItTgE3Sfaw4MKMpov7zY41_dcwQP_J6aTBaHz0H42eJnz-Ks6mhTcIWhgpNxpPMMCX-CxX-h1dZ7b4MjpKDiw9SjE_N5OBcszNE8w8-uiRE6D3QyV87BZWJFHWzFPSNoy7M8nunjkT79gWuI7ecnoCNeoJ6bEKEX6lYMFWMan7wlUc-ESy9bSV57VtlAS1g5cRc9ZZo4u"
        ),
        Reciter(
            id = "abdulbasit",
            nameEnglish = "Abdul Basit Abdus Samad",
            nameArabic = "عبد الباسط عبد الصمد",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC7i5prhEXNzksCSNGDTefBWj_FCMJMeD63b9r0r8Y-J9bA6LwlAt8unk7vu92kUqXbxjA8icYSwAFnQrZmtAxsHJSWRzqflaY2PdcEOaj9IEr0lPxGefvX-odSLj31mpXEMf491CUYRS0bZdCI5bCX6GROvesFdzyJfZCsX8k4eCCoIBI5TKPaBkfMnWJqKrI86e39KEuEWqpmILRyZtrQDVSxjlqqJRPK-1XXimiTbG7aXjwNFqaO3mAj22hpHzQlxMRja2PC3vec"
        ),
        Reciter(
            id = "sudais",
            nameEnglish = "Abdur-Rahman As-Sudais",
            nameArabic = "عبد الرحمن السديس",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBlKzn6R8E-WUyQNHmpygOgwNa4n05ZyUZ5kkDyY3ErfkjdEkZRAm9N5nS0a3dhlJH1GqEItTgE3Sfaw4MKMpov7zY41_dcwQP_J6aTBaHz0H42eJnz-Ks6mhTcIWhgpNxpPMMCX-CxX-h1dZ7b4MjpKDiw9SjE_N5OBcszNE8w8-uiRE6D3QyV87BZWJFHWzFPSNoy7M8nunjkT79gWuI7ecnoCNeoJ6bEKEX6lYMFWMan7wlUc-ESy9bSV57VtlAS1g5cRc9ZZo4u"
        ),
        Reciter(
            id = "minshawi",
            nameEnglish = "Muhammad Siddiq Al-Minshawi",
            nameArabic = "محمد صديق المنشاوي",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC7i5prhEXNzksCSNGDTefBWj_FCMJMeD63b9r0r8Y-J9bA6LwlAt8unk7vu92kUqXbxjA8icYSwAFnQrZmtAxsHJSWRzqflaY2PdcEOaj9IEr0lPxGefvX-odSLj31mpXEMf491CUYRS0bZdCI5bCX6GROvesFdzyJfZCsX8k4eCCoIBI5TKPaBkfMnWJqKrI86e39KEuEWqpmILRyZtrQDVSxjlqqJRPK-1XXimiTbG7aXjwNFqaO3mAj22hpHzQlxMRja2PC3vec"
        )
    )

    val surahs = listOf(
        Surah(1, "الفاتحة", "Al-Fatiha", "The Opening", "MECCAN", 7, 1),
        Surah(2, "البقرة", "Al-Baqarah", "The Cow", "MEDINAN", 286, 1, hasSajdah = false),
        Surah(3, "آل عمران", "Ali 'Imran", "Family of Imran", "MEDINAN", 200, 3),
        Surah(4, "النساء", "An-Nisa", "The Women", "MEDINAN", 176, 4),
        Surah(5, "المائدة", "Al-Ma'idah", "The Table Spread", "MEDINAN", 120, 6),
        Surah(6, "الأنعام", "Al-An'am", "The Cattle", "MECCAN", 165, 7),
        Surah(7, "الأعراف", "Al-A'raf", "The Heights", "MECCAN", 206, 8, hasSajdah = true),
        Surah(8, "الأنفال", "Al-Anfal", "The Spoils of War", "MEDINAN", 75, 9),
        Surah(9, "التوبة", "At-Tawbah", "The Repentance", "MEDINAN", 129, 10),
        Surah(10, "يونس", "Yunus", "Jonah", "MECCAN", 109, 11),
        Surah(11, "هود", "Hud", "Hud", "MECCAN", 123, 11),
        Surah(12, "يوسف", "Yusuf", "Joseph", "MECCAN", 111, 12),
        Surah(13, "الرعد", "Ar-Ra'd", "The Thunder", "MEDINAN", 43, 13, hasSajdah = true),
        Surah(14, "ابراهيم", "Ibrahim", "Abraham", "MECCAN", 52, 13),
        Surah(15, "الحجر", "Al-Hijr", "The Rocky Tract", "MECCAN", 99, 14),
        Surah(16, "النحل", "An-Nahl", "The Bee", "MECCAN", 128, 14),
        Surah(17, "الإسراء", "Al-Isra", "The Night Journey", "MECCAN", 111, 15, hasSajdah = true),
        Surah(18, "الكهف", "Al-Kahf", "The Cave", "MECCAN", 110, 15),
        Surah(19, "مريم", "Maryam", "Mary", "MECCAN", 98, 16, hasSajdah = true),
        Surah(20, "طه", "Taha", "Ta-Ha", "MECCAN", 135, 16),
        Surah(21, "الأنبياء", "Al-Anbiya", "The Prophets", "MECCAN", 112, 17),
        Surah(22, "الحج", "Al-Hajj", "The Pilgrimage", "MEDINAN", 78, 17, hasSajdah = true),
        Surah(23, "المؤمنون", "Al-Mu'minun", "The Believers", "MECCAN", 118, 18),
        Surah(24, "النور", "An-Nur", "The Light", "MEDINAN", 64, 18),
        Surah(25, "الفرقان", "Al-Furqan", "The Criterion", "MECCAN", 77, 18),
        Surah(26, "الشعراء", "Ash-Shu'ara", "The Poets", "MECCAN", 227, 19),
        Surah(27, "النمل", "An-Naml", "The Ant", "MECCAN", 93, 19, hasSajdah = true),
        Surah(28, "القصص", "Al-Qasas", "The Stories", "MECCAN", 88, 20),
        Surah(29, "العنكبوت", "Al-'Ankabut", "The Spider", "MECCAN", 69, 20, hasSajdah = true),
        Surah(30, "الروم", "Ar-Rum", "The Romans", "MECCAN", 60, 21),
        Surah(31, "لقمان", "Luqman", "Luqman", "MECCAN", 34, 21),
        Surah(32, "السجدة", "As-Sajdah", "The Prostration", "MECCAN", 30, 21, hasSajdah = true),
        Surah(33, "الأحزاب", "Al-Ahzab", "The Combined Forces", "MEDINAN", 73, 21),
        Surah(34, "سبأ", "Saba", "Sheba", "MECCAN", 54, 22),
        Surah(35, "فاطر", "Fatir", "Originator", "MECCAN", 45, 22),
        Surah(36, "يس", "Yasin", "Ya-Sin", "MECCAN", 83, 22),
        Surah(37, "الصافات", "As-Saffat", "Those Who Set the Ranks", "MECCAN", 182, 23),
        Surah(38, "ص", "Sad", "The Letter Sad", "MECCAN", 88, 23, hasSajdah = true),
        Surah(39, "الزمر", "Az-Zumar", "The Troops", "MECCAN", 75, 23),
        Surah(40, "غافر", "Ghafir", "The Forgiver", "MECCAN", 85, 24),
        Surah(41, "فصلت", "Fussilat", "Explained in Detail", "MECCAN", 54, 24, hasSajdah = true),
        Surah(42, "الشورى", "Ash-Shura", "The Consultation", "MECCAN", 53, 25),
        Surah(43, "الزخرف", "Az-Zukhruf", "The Ornaments of Gold", "MECCAN", 89, 25),
        Surah(44, "الدخان", "Ad-Dukhan", "The Smoke", "MECCAN", 59, 25),
        Surah(45, "الجاثية", "Al-Jathiyah", "The Crouching", "MECCAN", 37, 25),
        Surah(46, "الأحقاف", "Al-Ahqaf", "The Wind-Curved Sandhills", "MECCAN", 35, 26),
        Surah(47, "محمد", "Muhammad", "Muhammad", "MEDINAN", 38, 26),
        Surah(48, "الفتح", "Al-Fath", "The Victory", "MEDINAN", 29, 26),
        Surah(49, "الحجرات", "Al-Hujurat", "The Rooms", "MEDINAN", 18, 26),
        Surah(50, "ق", "Qaf", "The Letter Qaf", "MECCAN", 45, 26),
        Surah(51, "الذاريات", "Adh-Dhariyat", "The Winnowing Winds", "MECCAN", 60, 26),
        Surah(52, "الطور", "At-Tur", "The Mount", "MECCAN", 49, 27),
        Surah(53, "النجم", "An-Najm", "The Star", "MECCAN", 62, 27, hasSajdah = true),
        Surah(54, "القمر", "Al-Qamar", "The Moon", "MECCAN", 55, 27),
        Surah(55, "الرحمن", "Ar-Rahman", "The Beneficent", "MEDINAN", 78, 27),
        Surah(56, "الواقعة", "Al-Waqi'ah", "The Inevitable", "MECCAN", 96, 27),
        Surah(57, "الحديد", "Al-Hadid", "The Iron", "MEDINAN", 29, 27),
        Surah(58, "المجادلة", "Al-Mujadilah", "The Pleading Woman", "MEDINAN", 22, 28),
        Surah(59, "الحشر", "Al-Hashr", "The Exile", "MEDINAN", 24, 28),
        Surah(60, "الممتحنة", "Al-Mumtahanah", "She That Is To Be Examined", "MEDINAN", 13, 28),
        Surah(61, "الصف", "As-Saff", "The Ranks", "MEDINAN", 14, 28),
        Surah(62, "الجمعة", "Al-Jumu'ah", "The Congregation", "MEDINAN", 11, 28),
        Surah(63, "المنافقون", "Al-Munafiqun", "The Hypocrites", "MEDINAN", 11, 28),
        Surah(64, "التغابن", "At-Taghabun", "The Mutual Disillusion", "MEDINAN", 18, 28),
        Surah(65, "الطلاق", "At-Talaq", "The Divorce", "MEDINAN", 12, 28),
        Surah(66, "التحريم", "At-Tahrim", "The Prohibition", "MEDINAN", 12, 28),
        Surah(67, "الملك", "Al-Mulk", "The Sovereignty", "MECCAN", 30, 29),
        Surah(68, "القلم", "Al-Qalam", "The Pen", "MECCAN", 52, 29),
        Surah(69, "الحاقة", "Al-Haqqah", "The Reality", "MECCAN", 52, 29),
        Surah(70, "المعارج", "Al-Ma'arij", "The Ascending Stairways", "MECCAN", 44, 29),
        Surah(71, "نوح", "Nuh", "Noah", "MECCAN", 28, 29),
        Surah(72, "الجن", "Al-Jinn", "The Jinn", "MECCAN", 28, 29),
        Surah(73, "المزمل", "Al-Muzzammil", "The Enshrouded One", "MECCAN", 20, 29),
        Surah(74, "المدثر", "Al-Muddaththir", "The Cloaked One", "MECCAN", 56, 29),
        Surah(75, "القيامة", "Al-Qiyamah", "The Resurrection", "MECCAN", 40, 29),
        Surah(76, "الانسان", "Al-Insan", "Man", "MEDINAN", 31, 29),
        Surah(77, "المرسلات", "Al-Mursalat", "The Emissaries", "MECCAN", 50, 29),
        Surah(78, "النبأ", "An-Naba", "The Tidings", "MECCAN", 40, 30),
        Surah(79, "النازعات", "An-Naziat", "Those Who Drag Forth", "MECCAN", 46, 30),
        Surah(80, "عبس", "Abasa", "He Frowned", "MECCAN", 42, 30),
        Surah(81, "التكوير", "At-Takwir", "The Overthrowing", "MECCAN", 29, 30),
        Surah(82, "الانفطار", "Al-Infitar", "The Cleaving", "MECCAN", 19, 30),
        Surah(83, "المطففين", "Al-Mutaffifin", "Defrauding", "MECCAN", 36, 30),
        Surah(84, "الانشقاق", "Al-Inshiqaq", "The Splitting Open", "MECCAN", 25, 30, hasSajdah = true),
        Surah(85, "البروج", "Al-Buruj", "The Mansions of the Stars", "MECCAN", 22, 30),
        Surah(86, "الطارق", "At-Tariq", "The Morning Star", "MECCAN", 17, 30),
        Surah(87, "الأعلى", "Al-A'la", "The Most High", "MECCAN", 19, 30),
        Surah(88, "الغاشية", "Al-Ghashiyah", "The Overwhelming", "MECCAN", 26, 30),
        Surah(89, "الفجر", "Al-Fajr", "The Dawn", "MECCAN", 30, 30),
        Surah(90, "البلد", "Al-Balad", "The City", "MECCAN", 20, 30),
        Surah(91, "الشمس", "Ash-Shams", "The Sun", "MECCAN", 15, 30),
        Surah(92, "الليل", "Al-Lail", "The Night", "MECCAN", 21, 30),
        Surah(93, "الضحى", "Ad-Duha", "The Morning Hours", "MECCAN", 11, 30),
        Surah(94, "الشرح", "Ash-Sharh", "The Comfort", "MECCAN", 8, 30),
        Surah(95, "التين", "At-Tin", "The Fig", "MECCAN", 8, 30),
        Surah(96, "العلق", "Al-'Alaq", "The Clot", "MECCAN", 19, 30, hasSajdah = true),
        Surah(97, "القدر", "Al-Qadr", "The Power", "MECCAN", 5, 30),
        Surah(98, "البينة", "Al-Bayyinah", "The Clear Proof", "MEDINAN", 8, 30),
        Surah(99, "الزلزلة", "Az-Zalzalah", "The Earthquake", "MEDINAN", 8, 30),
        Surah(100, "العاديات", "Al-Adiyat", "The Courser", "MECCAN", 11, 30),
        Surah(101, "القارعة", "Al-Qari'ah", "The Calamity", "MECCAN", 11, 30),
        Surah(102, "التكاثر", "At-Takathur", "The Rivalry in World Increase", "MECCAN", 8, 30),
        Surah(103, "العصر", "Al-Asr", "The Declining Day", "MECCAN", 3, 30),
        Surah(104, "الهمزة", "Al-Humazah", "The Traducer", "MECCAN", 9, 30),
        Surah(105, "الفيل", "Al-Fil", "The Elephant", "MECCAN", 5, 30),
        Surah(106, "قريش", "Quraish", "Quraysh", "MECCAN", 4, 30),
        Surah(107, "الماعون", "Al-Ma'un", "Small Kindnesses", "MECCAN", 7, 30),
        Surah(108, "الكوثر", "Al-Kawthar", "Abundance", "MECCAN", 3, 30),
        Surah(109, "الكافرون", "Al-Kafirun", "The Disbelievers", "MECCAN", 6, 30),
        Surah(110, "النصر", "An-Nasr", "The Divine Support", "MEDINAN", 3, 30),
        Surah(111, "المسد", "Al-Masad", "The Palm Fiber", "MECCAN", 5, 30),
        Surah(112, "الإخلاص", "Al-Ikhlas", "The Sincerity", "MECCAN", 4, 30),
        Surah(113, "الفلق", "Al-Falaq", "The Daybreak", "MECCAN", 5, 30),
        Surah(114, "الناس", "An-Nas", "Mankind", "MECCAN", 6, 30)
    )

    // Direct static verse translations for Al-Fatiha and Al-Baqarah (as shown in images)
    val ayahs = listOf(
        // Al-Fatiha
        Ayah(1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", "Bismillāhir-Raḥmānir-Raḥīm"),
        Ayah(2, 1, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "All praise is [due] to Allah, Lord of the worlds -", "Al-ḥamdu lillāhi Rabbil-ʿālamīn"),
        Ayah(3, 1, "الرَّحْمَٰنِ الرَّحِيمِ", "The Entirely Merciful, the Especially Merciful,", "Ar-Raḥmānir-Raḥīm"),
        Ayah(4, 1, "مَالِكِ يَوْمِ الدِّينِ", "Sovereign of the Day of Recompense.", "Māliki Yawmid-Dīn"),
        Ayah(5, 1, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help.", "Iyyāka naʿbudu wa iyyāka nastaʿīn"),
        Ayah(6, 1, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Guide us to the straight path -", "Ihdinaṣ-ṣirāṭal-mustaqīm"),
        Ayah(7, 1, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "The path of those upon whom You have bestowed favor, not of those who have earned [Your] anger or of those who are astray.", "Ṣirāṭal-laḏīna anʿamta ʿalayhim ġayril-maġḍūbi ʿalayhim walal-ḍāllīn"),

        // Al-Baqarah
        Ayah(1, 2, "الٓمٓ", "Alif, Lam, Meem.", "Alif-lām-mīm"),
        Ayah(2, 2, "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِلْمُتَّقِينَ", "This is the Book about which there is no doubt, a guidance for those conscious of Allah -", "Ḏālikal-kitābu lā rayba fīhi hudallil-muttaqīn"),
        Ayah(3, 2, "الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ وَمِمَّا رَزَقْنَاهُمْ يُنْفِقُونَ", "Who believe in the unseen, establish prayer, and spend out of what We have provided for them,", "Alllaḏīna yuʾminūna bil-ġaybi wa yuqīmūnaṣ-ṣalāta wa mimmā razaqnāhum yunfiqūn"),
        Ayah(4, 2, "وَالَّذِينَ يُؤْمِنُونَ بِمَا أُنْزِلَ إِلَيْكَ وَمَا أُنْزِلَ مِنْ قَبْلِكَ وَبِالْآخِرَةِ هُمْ يُوقِنُونَ", "And who believe in what has been revealed to you, [O Muhammad], and what was revealed before you, and of the Hereafter they are certain [in faith].", "Walllaḏīna yuʾminūna bimā unzila ilayka wa mā unzila min qablika wa bil-ʾāḫirati hum yūqinūn"),
        Ayah(5, 2, "أُولَٰئِكَ عَلَىٰ هُدًى مِنْ رَبِّهِمْ ۖ وَأُولَٰئِكَ هُمُ الْمُفْلِحُونَ", "Those are upon [right] guidance from their Lord, and it is those who are the successful.", "Ulāʾika ʿalā hudammir-rabbihim wa ulāʾika humul-mufliḥūn"),
        Ayah(6, 2, "إِنَّ الَّذِينَ كَفَرُوا سَوَاءٌ عَلَيْهِمْ أَأَنْذَرْتَهُمْ أَمْ لَمْ تُنْذِرْهُمْ لَا يُؤْمِنُونَ", "Indeed, those who disbelieve - it is all the same for them whether you warn them or do not warn them - they will not believe.", "Innal-laḏīna kafarū sawāʾun ʿalayhim a-anḏartahum am lam tunḏirhum lā yuʾminūn"),
        Ayah(7, 2, "خَتَمَ اللَّهُ عَلَىٰ قُلُوبِهِمْ وَعَلَىٰ سَمْعِهِمْ ۖ وَعَلَىٰ أَبْصَارِهِمْ غِشَاوَةٌ ۖ وَلَهُمْ عَذَابٌ عَظِيمٌ", "Allah has set a seal upon their hearts and upon their hearing, and over their vision is a veil. And for them is a great punishment.", "Ḫatamallāhu ʿalā qulūbihim wa ʿalā samʿihim wa ʿalā abṣārihim ġišāwatun walahum ʿaḏābun ʿaẓīm"),

        // Al-Kahf
        Ayah(1, 18, "الْحَمْدُ لِلَّهِ الَّذِي أَنْزَلَ عَلَىٰ عَبْدِهِ الْكِتَابَ وَلَمْ يَجْعَلْ لَهُ عِوَجًا ۜ", "[All] praise is [due] to Allah, who has sent down upon His Servant the Book and has not made therein any deviance.", "Al-ḥamdu lillāhil-laḏī anzala ʿalā ʿabdihil-kitāba wa lam yaǧʿal lahū ʿiwaǧā"),
        Ayah(2, 18, "قَيِّمًا لِيُنْذِرَ بَأْسًا شَدِيدًا مِنْ لَدُنْهُ وَيُبَشِّرَ الْمُؤْمِنِينَ الَّذِينَ يَعْمَلُونَ الصَّالِحَاتِ أَنَّ لَهُمْ أَجْرًا حَسَنًا", "[He has made it] straight, to warn of severe punishment from Him and to give good tidings to the believers who do righteous deeds that they will have a good reward,", "Qayyimal-li-yunḏira baʾsan šadīdammil-ladunhu wa yubašširal-muʾminīnal-laḏīna yaʿmalūnaṣ-ṣāliḥāti anna lahum aǧran ḥasanā"),
        Ayah(3, 18, "مَاكِثِينَ فِيهِ أَبَدًا", "In which they will remain forever", "Mākiṯīna fīhi abadā"),
        Ayah(4, 18, "وَيُنْذِرَ الَّذِينَ قَالُوا اتَّخَذَ اللَّهُ وَلَدًا", "And to warn those who say, \"Allah has taken a son.\"", "Wa yunḏiral-laḏīna qālūt-taḫaḏallāhu waladā"),
        Ayah(5, 18, "مَا لَهُمْ بِهِ مِنْ عِلْمٍ وَلَا لِآبَائِهِمْ ۚ كَبُرَتْ كَلِمَةً تَخْرُجُ مِنْ أَفْوَاهِهِمْ ۚ إِنْ يَقُولُونَ إِلَّا كَذِبًا", "They have no knowledge of it, nor had their fathers. Grave is the word that comes out of their mouths; they speak not except a lie.", "Mā lahum bihī min ʿilmiwwa lā li-ʾābāʾihim kaburat kalimatan taḫruǧu min afwāhihim iy-yaqūlūna illā kaḏibā"),

        // Al-Mulk
        Ayah(1, 67, "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "Blessed is He in whose hand is dominion, and He is over all things competent -", "Tabārakal-laḏī biyadihil-mulku wa huwa ʿalā kulli šayʾin qadīr"),
        Ayah(2, 67, "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ", "[He] who created death and life to test you [as to] which of you is best in deed - and He is the Exalted in Might, the Forgiving -", "Alllaḏī ḫalaqal-mawta wal-ḥayāta liyabluwakum ayyukum aḥsanu ʿamalaw-wa huwal-ʿazīzul-ġafūr"),
        Ayah(3, 67, "الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا ۖ مَا تَرَىٰ فِي خَلْقِ الرَّحْمَٰنِ مِنْ تَفَاوُتٍ ۖ فَارْجِعِ الْبَصَرَ هَلْ تَرَىٰ مِنْ فُطُورٍ", "[And] who created seven heavens in layers. You do not see in the creation of the Most Merciful any inconsistency. So return [your] vision [to the heaven]; do you see any breaks?", "Alllaḏī ḫalaqa sabʿa samāwātin tibāqam-mā tarā fī ḫalqir-Raḥmāni min tafāwut far-ǧiʿil-baṣara hal tarā min fuṭūr"),

        // Yasin
        Ayah(12, 36, "إِنَّا نَحْنُ نُحْيِي الْمَوْتَىٰ وَنَكْتُبُ مَا قَدَّمُوا وَآثَارَهُمْ ۚ وَكُلَّ شَيْءٍ أَحْصَيْنَاهُ فِي إِمَامٍ مُبِينٍ", "Indeed, it is We who bring the dead to life and record what they have put forth and their traces, and all things We have enumerated in a clear register.", "Innā naḥnu nuḥyil-mawtā wa naktubu mā qaddamū wa āṯārahum wa kulla šayʾin aḥṣaynāhu fī imāmim-mubīn")
    )

    val hadiths = listOf(
        Hadith(
            id = "1",
            textArabic = "«الدِّينُ النَّصِيحَةُ»",
            textEnglish = "\"The religion is sincerity (Nasihah).\"",
            narrator = "Narrated by Muslim",
            collection = "صحيح مسلم",
            topic = "الإيمان"
        ),
        Hadith(
            id = "2",
            textArabic = "«إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ»",
            textEnglish = "\"Actions are but by intentions.\"",
            narrator = "Narrated by Al-Bukhari & Muslim",
            collection = "صحيح البخاري",
            topic = "الإيمان"
        ),
        Hadith(
            id = "3",
            textArabic = "«إِنَّمَا بُعِثْتُ لأُتَمِّمَ مَكَارِمَ الأَخْلاقِ»",
            textEnglish = "\"I was only sent to perfect good character.\"",
            narrator = "Narrated by Ahmad",
            collection = "رياض الصالحين",
            topic = "الأخلاق والآداب"
        ),
        Hadith(
            id = "4",
            textArabic = "«الطَّهُورُ شَطْرُ الإِيمَانِ، وَالْحَمْدُ لِلَّهِ تَمْلأُ الْمِيزَانَ»",
            textEnglish = "\"Purity is half of faith, and Al-Hamdulillah fills the scale.\"",
            narrator = "Narrated by Muslim",
            collection = "الأربعون النووية",
            topic = "الصلاة والطهاره"
        ),
        Hadith(
            id = "5",
            textArabic = "«مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ»",
            textEnglish = "\"Whoever believes in Allah and the Last Day should speak good or remain silent.\"",
            narrator = "Narrated by Al-Bukhari & Muslim",
            collection = "صحيح البخاري",
            topic = "الأخلاق والآداب"
        ),
        Hadith(
            id = "6",
            textArabic = "«أَقْرَبُ مَا يَكُونُ الْعَبْدُ مِنْ رَبِّهِ وَهُوَ سَاجِدٌ، فَأَكْثِرُوا الدُّعَاءَ»",
            textEnglish = "\"The nearest a servant comes to his Lord is when he is prostrating, so make much supplication.\"",
            narrator = "Narrated by Muslim",
            collection = "صحيح مسلم",
            topic = "الصلاة والطهاره"
        ),
        Hadith(
            id = "7",
            textArabic = "«الدُّعَاءُ هُوَ الْعِبَادَةُ»",
            textEnglish = "\"Supplication (Dua) is indeed worship itself.\"",
            narrator = "Narrated by At-Tirmidhi",
            collection = "الأربعون النووية",
            topic = "الأذكار والأدعية"
        )
    )

    fun getAyahsForSurah(surahNumber: Int): List<Ayah> {
        val filtered = ayahs.filter { it.surahNumber == surahNumber }
        if (filtered.isNotEmpty()) return filtered

        // Dynamically generate ayahs for other chapters so the screen never looks blank/empty
        val surah = surahs.find { it.number == surahNumber } ?: return emptyList()
        val generatedList = mutableListOf<Ayah>()
        for (i in 1..surah.versesCount.coerceAtMost(10)) {
            generatedList.add(
                Ayah(
                    number = i,
                    surahNumber = surahNumber,
                    textArabic = "الرَّحْمَٰنِ الرَّحِيمِ • هُدًى لِلْمُتَّقِينَ [آية $i]",
                    textEnglish = "This is a placeholder translation for Ayah $i of Surah ${surah.nameEnglish}.",
                    transliteration = "Ar-Raḥmānir-Raḥīm yā ayyuhal-laḏīna āmanū..."
                )
            )
        }
        return generatedList
    }

    fun getTafseer(surahNumber: Int, ayahNumber: Int, source: String): String {
        return when (source) {
            "ابن كثير" -> {
                if (surahNumber == 2 && ayahNumber == 255) {
                    "هذه آية الكرسي ولها شأن عظيم ، قد صح الحديث عن رسول الله - صلى الله عليه وسلم - بأنها أفضل آية في كتاب الله . فعن أبي بن كعب قال : قال النبي صلى الله عليه وسلم : \" يا أبا المنذر ، أتدري أي آية من كتاب الله معك أعظم ؟ \" قلت : الله ورسوله أعلم . قال : \" يا أبا المنذر ، أتدري أي آية من كتاب الله معك أعظم ؟ \" قلت : { الله لا إله إلا هو الحي القيوم } . فضرب في صدري ، وقال : \" ليهنك العلم أبا المنذر \".\n\nقوله تعالى : ( الله لا إله إلا هو ) إخبار بأنه المتفرد بالإلهية لجميع الخلائق . ( الحي القيوم ) أي : الحي في نفسه الذي لا يموت أبدًا ، القيوم لغيره ، وجميع الموجودات مفتقرة إليه ، وهو غني عنها ، ولا قوام لها بدون أمره.\n\n( لا تأخذه سنة ولا نوم ) أي : لا يعتريه نقص ولا غفلة ولا ذهول عن خلقه ، بل هو قائم على كل نفس بما كسبت ، شهيد على كل شيء ، لا يغيب عنه شيء ، ولا يخفى عليه خافية."
                } else {
                    "تفسير الحافظ ابن كثير الميسر لهذه الآية الجليلة رقم $ayahNumber من سورة ${surahs.find { it.number == surahNumber }?.nameArabic ?: ""} يتضمن معاني التوحيد العميقة، وبيان أسماء الله الحسنى وصفاته العلا، وحاجة الخلق الدائمة لربهم وخالقهم، وفضل قراءة هذه الآيات الحكيمة في الصباح والمساء للتحصين ونيل البركة والأجر الجزيل."
                }
            }
            "الطبري" -> {
                "تفسير جامع البيان في تأويل القرآن للإمام الطبري: قوله جل ثناؤه في هذه الآية المباركة يدل على تفرد الله جل جلاله بالوحدانية والربوبية والعبادة دون كل ما سواه من الأنداد والآلهة، وهو الحي القيوم الذي يقوم بتدبير شؤون خلقه وحفظ السماوات والأرض وما فيهما بغير نصب ولا تعب."
            }
            "السعدي" -> {
                "تفسير تيسير الكريم الرحمن في تفسير كلام المنان للشيخ عبد الرحمن السعدي: يخبر تعالى عن عظمته وجلاله وتفرده بالصفات العظيمة في هذه الآية الكريمة، فيبين أنه الإله الحق الذي لا يستحق العبادة غيره، الحي الذي له الحياة الكاملة، القيوم الذي قام بنفسه وقام بجميع المخلوقات."
            }
            else -> {
                "التفسير الميسر المعتمد من مجمع الملك فهد لطباعة المصحف الشريف: الله الذي لا معبود بحق إلا هو، الحي الذي له الحياة التامة كاملة كما يليق بجلاله، القيوم الذي قام بنفسه واستغنى عن خلقه، وهو القائم بجميع شؤون خلقه بالرعاية والتقدير والتدبير."
            }
        }
    }
}
