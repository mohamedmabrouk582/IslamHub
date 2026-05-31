package com.example.di

import com.example.BuildConfig
import com.example.data.remote.quran.QuranApi
import com.example.data.remote.sunnah.SunnahApi
import com.example.data.remote.tafseer.TafseerApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val QURAN_BASE_URL = "https://api.quran.com/api/v3/"
    private const val SUNNAH_BASE_URL = "https://api.sunnah.com/"
    private const val TAFSEER_BASE_URL = "http://api.quran-tafseer.com/"

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val sunnahKeyInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("X-API-Key", BuildConfig.SUNNAH_API_KEY)
                .build()
            chain.proceed(request)
        }
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(sunnahKeyInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @Named("quran")
    fun provideQuranRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(QURAN_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @Named("sunnah")
    fun provideSunnahRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(SUNNAH_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    @Named("tafseer")
    fun provideTafseerRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(TAFSEER_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideQuranApi(@Named("quran") retrofit: Retrofit): QuranApi {
        return retrofit.create(QuranApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSunnahApi(@Named("sunnah") retrofit: Retrofit): SunnahApi {
        return retrofit.create(SunnahApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTafseerApi(@Named("tafseer") retrofit: Retrofit): TafseerApi {
        return retrofit.create(TafseerApi::class.java)
    }
}
