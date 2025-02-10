package com.tqhit.adlib.sdk.firebase

import androidx.annotation.XmlRes
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRemoteConfigHelper @Inject constructor(
    private val firebaseRemoteConfig: FirebaseRemoteConfig
) {

    fun fetchAndActivate(onComplete: (Boolean) -> Unit, @XmlRes defaultConfig: Int) {
        firebaseRemoteConfig.setDefaultsAsync(defaultConfig)
        firebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun getString(key: String): String {
        return firebaseRemoteConfig.getString(key)
    }

    fun getBoolean(key: String): Boolean {
        return firebaseRemoteConfig.getBoolean(key)
    }

    fun getLong(key: String): Long {
        return firebaseRemoteConfig.getLong(key)
    }

    fun getDouble(key: String): Double {
        return firebaseRemoteConfig.getDouble(key)
    }
}