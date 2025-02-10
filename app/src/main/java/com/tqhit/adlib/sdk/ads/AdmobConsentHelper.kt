package com.tqhit.adlib.sdk.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.tqhit.adlib.sdk.ads.callback.IAdmobConsentCallback
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdmobConsentHelper @Inject constructor(
    private val context: Context
) {

    private var consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(context)

    fun canRequestAds(): Boolean {
        return consentInformation.canRequestAds()
    }

    fun isPrivacyOptionsRequired(): Boolean {
        return consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    fun showPrivacyOptionsForm(
        activity: Activity,
        onConsentFormDismissedListener: ConsentForm.OnConsentFormDismissedListener
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onConsentFormDismissedListener)
    }

    fun gatherConsent(
        activity: Activity,
        consentCallback: IAdmobConsentCallback
    ) {
        val debugSettings =
            ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                //.addTestDeviceHashedId("6273DB7376FF67AE690BB65F855ACE64")
                .build()

        val params = ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                if (consentInformation.isConsentFormAvailable) {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        activity
                    ) { formError ->
                        // Consent has been gathered.
                        consentCallback.consentGatheringComplete(formError)
                    }
                    return@requestConsentInfoUpdate
                }
                if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.OBTAINED
                    || consentInformation.consentStatus == ConsentInformation.ConsentStatus.NOT_REQUIRED
                ) {
                    consentCallback.consentGatheringComplete(null)
                }
            },
            { requestConsentError ->
                consentCallback.consentGatheringComplete(requestConsentError)
            }
        )
    }
}