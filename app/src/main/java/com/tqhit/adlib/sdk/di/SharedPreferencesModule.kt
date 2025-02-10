package com.tqhit.adlib.sdk.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.tqhit.adlib.sdk.data.local.PreferencesHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SharedPreferencesModule {
    private const val PREF_NAME = "ad_lib_pref"

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context) : SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providePreferencesHelper(sharedPreferences: SharedPreferences, gson: Gson) : PreferencesHelper {
        return PreferencesHelper(sharedPreferences, gson)
    }

    @Provides
    @Singleton
    fun provideGson() : Gson {
        return Gson()
    }
}