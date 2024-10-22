package net.opendasharchive.openarchive.util

import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity

class SystemBarsController(private val activity: AppCompatActivity) {
    private var isNavigationBarHidden = false

    fun hideNavigationBar() {
        isNavigationBarHidden = true
        applySystemBarsState()
    }

    fun showNavigationBar() {
        isNavigationBarHidden = false
        applySystemBarsState()
    }

    fun toggleNavigationBar() {
        isNavigationBarHidden = !isNavigationBarHidden
        applySystemBarsState()
    }

    private fun applySystemBarsState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Only proceed if the window is available
            val window = activity.window ?: return
            val controller = window.insetsController ?: return

            window.setDecorFitsSystemWindows(false)

            if (isNavigationBarHidden) {
                controller.hide(WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controller.show(WindowInsets.Type.navigationBars())
            }
        } else {
            @Suppress("DEPRECATION")
            activity.window?.decorView?.systemUiVisibility = if (isNavigationBarHidden) {
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            } else {
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
        }
    }
}