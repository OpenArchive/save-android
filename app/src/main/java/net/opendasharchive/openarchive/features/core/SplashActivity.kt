package net.opendasharchive.openarchive.features.core

import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.features.main.TabBarActivity
import net.opendasharchive.openarchive.features.onboarding.Onboarding23Activity

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Keep the splash screen visible for this Activity
        splashScreen.setKeepOnScreenCondition { true }

        if (requiresOnboarding()) {
            startActivity(Intent(this, Onboarding23Activity::class.java))
        } else {
            startActivity(Intent(this, TabBarActivity::class.java))
        }
        finish()
    }

    private fun requiresOnboarding(): Boolean {
        return !settings.didCompleteOnboarding
    }
}