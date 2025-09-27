package com.tqhit.adlib.sdk.di

import android.content.Context
import com.tqhit.adlib.sdk.ads.applovin.ApplovinHelper
import com.tqhit.adlib.sdk.ads.applovin.MaxBannerHelper
import com.tqhit.adlib.sdk.ads.applovin.MaxInterstitialHelper
import com.tqhit.adlib.sdk.ads.applovin.MaxRewardedHelper
import com.tqhit.adlib.sdk.ads.applovin.MaxNativeHelper
import com.tqhit.adlib.sdk.ads.applovin.MaxAppOpenHelper
import com.tqhit.adlib.sdk.analytics.AnalyticsTracker
import com.tqhit.adlib.sdk.data.local.PreferencesHelper
import com.tqhit.adlib.sdk.firebase.FirebaseRemoteConfigHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplovinModule {

    @Provides
    @Singleton
    fun provideMaxBannerHelper(
        analyticsTracker: AnalyticsTracker,
        remoteConfigHelper: FirebaseRemoteConfigHelper,
        preferencesHelper: PreferencesHelper
    ): MaxBannerHelper {
        return MaxBannerHelper(analyticsTracker, remoteConfigHelper, preferencesHelper)
    }

    @Provides
    @Singleton
    fun provideMaxInterstitialHelper(
        analyticsTracker: AnalyticsTracker,
        remoteConfigHelper: FirebaseRemoteConfigHelper,
        preferencesHelper: PreferencesHelper
    ): MaxInterstitialHelper {
        return MaxInterstitialHelper(analyticsTracker, remoteConfigHelper, preferencesHelper)
    }

    @Provides
    @Singleton
    fun provideMaxRewardedHelper(
        analyticsTracker: AnalyticsTracker,
        remoteConfigHelper: FirebaseRemoteConfigHelper,
        preferencesHelper: PreferencesHelper
    ): MaxRewardedHelper {
        return MaxRewardedHelper(analyticsTracker, remoteConfigHelper, preferencesHelper)
    }

    @Provides
    @Singleton
    fun provideApplovinHelper(
        maxBannerHelper: MaxBannerHelper,
        maxInterstitialHelper: MaxInterstitialHelper,
        maxRewardedHelper: MaxRewardedHelper,
        maxNativeHelper: MaxNativeHelper,
        maxAppOpenHelper: MaxAppOpenHelper
    ): ApplovinHelper {
        return ApplovinHelper(maxBannerHelper, maxInterstitialHelper, maxRewardedHelper, maxNativeHelper, maxAppOpenHelper)
    }

    @Provides
    @Singleton
    fun provideMaxNativeHelper(
        analyticsTracker: AnalyticsTracker,
        remoteConfigHelper: FirebaseRemoteConfigHelper,
        preferencesHelper: PreferencesHelper
    ): MaxNativeHelper {
        return MaxNativeHelper(analyticsTracker, remoteConfigHelper, preferencesHelper)
    }

    @Provides
    @Singleton
    fun provideMaxAppOpenHelper(
        analyticsTracker: AnalyticsTracker,
        remoteConfigHelper: FirebaseRemoteConfigHelper,
        preferencesHelper: PreferencesHelper
    ): MaxAppOpenHelper {
        return MaxAppOpenHelper(analyticsTracker, remoteConfigHelper, preferencesHelper)
    }
}


