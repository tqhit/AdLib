package com.tqhit.adlib.sdk.ui.crash

import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.tqhit.adlib.R
import com.tqhit.adlib.databinding.ActivityAdLibCustomCrashBinding
import com.tqhit.adlib.sdk.base.ui.AdLibBaseActivity
import com.tqhit.adlib.sdk.ext.copyToClipboard

class AdLibCustomCrashActivity : AdLibBaseActivity<ActivityAdLibCustomCrashBinding>() {
    override val layout: Int
        get() = R.layout.activity_ad_lib_custom_crash

    override fun setupData() {
        super.setupData()

        val config = CustomActivityOnCrash.getConfigFromIntent(intent)

        if (config == null) {
            finish()
            return
        }

        binding.errorDetails.setOnLongClickListener {
            binding.errorDetails.copyToClipboard()
            showToast("Copied to clipboard")
            true
        }

        binding.apply {
            errorDetails.text = CustomActivityOnCrash.getStackTraceFromIntent(intent)

            if (config.isShowRestartButton && config.restartActivityClass != null) {
                restartButton.text = getString(R.string.restart_app)
                restartButton.setOnClickListener {
                    CustomActivityOnCrash.restartApplication(
                        this@AdLibCustomCrashActivity,
                        config
                    )
                }
            } else {
                restartButton.setOnClickListener {
                    CustomActivityOnCrash.closeApplication(
                        this@AdLibCustomCrashActivity,
                        config
                    )
                }
            }
        }
    }
}