package com.tqhit.adlib.sdk.di

import com.tqhit.adlib.sdk.firebase.FirebaseAnalyticsHelper
import com.tqhit.adlib.sdk.analytics.AnalyticsTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideTrackingManager(firebaseAnalyticsHelper: FirebaseAnalyticsHelper) : AnalyticsTracker {
        return AnalyticsTracker(firebaseAnalyticsHelper)
    }
}