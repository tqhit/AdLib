package com.tqhit.adlib.sdk.di

import android.app.Activity
import android.content.Context
import com.tqhit.adlib.sdk.ads.AdmobConsentHelper
import com.tqhit.adlib.sdk.ads.AdmobHelper
import com.tqhit.adlib.sdk.ads.BannerHelper
import com.tqhit.adlib.sdk.ads.InterstitialHelper
import com.tqhit.adlib.sdk.ads.NativeHelper
import com.tqhit.adlib.sdk.ads.RewardHelper
import com.tqhit.adlib.sdk.analytics.AnalyticsTracker
import com.tqhit.adlib.sdk.ui.dialog.LoadingAdsDialog
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
    fun provideAdmobConsentHelper(@ApplicationContext context: Context): AdmobConsentHelper {
        return AdmobConsentHelper(context)
    }

    @Provides
    @Singleton
    fun provideAdmobHelper(
        @ApplicationContext context: Context,
        bannerHelper: BannerHelper,
        interstitialHelper: InterstitialHelper,
        rewardHelper: RewardHelper,
        nativeHelper: NativeHelper
    ): AdmobHelper {
        return AdmobHelper(context, bannerHelper, interstitialHelper, rewardHelper, nativeHelper)
    }

    @Provides
    @Singleton
    fun provideBannerHelper(
        admobConsentHelper: AdmobConsentHelper,
        analyticsTracker: AnalyticsTracker,
    ): BannerHelper {
        return BannerHelper(admobConsentHelper, analyticsTracker)
    }

    @Provides
    @Singleton
    fun provideInterstitialHelper(
        admobConsentHelper: AdmobConsentHelper,
        analyticsTracker: AnalyticsTracker,
    ): InterstitialHelper {
        return InterstitialHelper(admobConsentHelper, analyticsTracker)
    }

    @Provides
    @Singleton
    fun provideNativeHelper(
        admobConsentHelper: AdmobConsentHelper,
        analyticsTracker: AnalyticsTracker,
    ): NativeHelper {
        return NativeHelper(admobConsentHelper, analyticsTracker)
    }

    @Provides
    @Singleton
    fun provideRewardHelper(
        admobConsentHelper: AdmobConsentHelper,
        analyticsTracker: AnalyticsTracker,
    ): RewardHelper {
        return RewardHelper(admobConsentHelper, analyticsTracker)
    }
}