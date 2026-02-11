package com.example.timelimit.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.timelimit.core.data.BlockedAppsDataStore
import com.example.timelimit.core.data.database.AppUsageDao
import com.example.timelimit.core.data.database.QuoteDao
import com.example.timelimit.core.data.database.TimeLimitDatabase
import com.example.timelimit.core.data.remote.FocusQuoteApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTimeLimitDatabase(@ApplicationContext context: Context): TimeLimitDatabase {
        return Room.databaseBuilder(
            context,
            TimeLimitDatabase::class.java,
            "timelimit_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideAppUsageDao(database: TimeLimitDatabase): AppUsageDao {
        return database.appUsageDao()
    }

    @Provides
    @Singleton
    fun provideQuoteDao(database: TimeLimitDatabase): QuoteDao {
        return database.quoteDao()
    }

    @Provides
    @Singleton
    fun provideBlockedAppsDataStore(@ApplicationContext context: Context): BlockedAppsDataStore {
        return BlockedAppsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://zenquotes.io/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideFocusQuoteApi(retrofit: Retrofit): FocusQuoteApi {
        return retrofit.create(FocusQuoteApi::class.java)
    }
}

