package com.tqhit.adlib.sdk.di

import android.content.Context
import com.tqhit.adlib.sdk.ads.AdmobHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdmobModule {

    @Provides
    @Singleton
    fun provideAdmobHelper(@ApplicationContext context: Context): AdmobHelper {
        return AdmobHelper(context)
    }
}