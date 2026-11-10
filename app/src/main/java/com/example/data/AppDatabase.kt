package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AyahAudioDao
import com.example.data.local.dao.AyahDao
import com.example.data.local.dao.HadithDao
import com.example.data.local.dao.SurahDao
import com.example.data.local.dao.TafseerDao
import com.example.data.local.entity.AyahAudioEntity
import com.example.data.local.entity.AyahEntity
import com.example.data.local.entity.HadithBookEntity
import com.example.data.local.entity.HadithCollectionEntity
import com.example.data.local.entity.HadithEntity
import com.example.data.local.entity.HadithRemoteKey
import com.example.data.local.entity.SurahEntity
import com.example.data.local.entity.TafseerEntity

@Database(
    entities = [
        Bookmark::class,
        LastRead::class,
        SurahEntity::class,
        AyahEntity::class,
        HadithCollectionEntity::class,
        HadithBookEntity::class,
        HadithEntity::class,
        HadithRemoteKey::class,
        TafseerEntity::class,
        AyahAudioEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
    abstract fun surahDao(): SurahDao
    abstract fun ayahDao(): AyahDao
    abstract fun hadithDao(): HadithDao
    abstract fun tafseerDao(): TafseerDao
    abstract fun ayahAudioDao(): AyahAudioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quran_database",
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
