package com.example.di

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.QuranDao
import com.example.data.local.dao.AyahAudioDao
import com.example.data.local.dao.AyahDao
import com.example.data.local.dao.HadithDao
import com.example.data.local.dao.SurahDao
import com.example.data.local.dao.TafseerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideQuranDao(database: AppDatabase): QuranDao = database.quranDao()

    @Provides
    fun provideSurahDao(database: AppDatabase): SurahDao = database.surahDao()

    @Provides
    fun provideAyahDao(database: AppDatabase): AyahDao = database.ayahDao()

    @Provides
    fun provideHadithDao(database: AppDatabase): HadithDao = database.hadithDao()

    @Provides
    fun provideTafseerDao(database: AppDatabase): TafseerDao = database.tafseerDao()

    @Provides
    fun provideAyahAudioDao(database: AppDatabase): AyahAudioDao = database.ayahAudioDao()
}
