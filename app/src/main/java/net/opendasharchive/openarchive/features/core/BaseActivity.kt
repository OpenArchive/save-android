package net.opendasharchive.openarchive.features.core

import android.app.AlertDialog
import android.view.MotionEvent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import net.opendasharchive.openarchive.util.Prefs
import timber.log.Timber

abstract class BaseActivity(): AppCompatActivity() {

    companion object {
        const val EXTRA_DATA_SPACE = "space"
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            val obscuredTouch = event.flags and MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED != 0
            if (obscuredTouch) return false
        }

        return super.dispatchTouchEvent(event)
    }

    override fun onResume() {
        super.onResume()

        // updating this in onResume (previously was in onCreate) to make sure setting changes get
        // applied instantly instead after the next app restart
        updateScreenshotPrevention()
    }

    override fun onSupportNavigateUp(): Boolean {
        Timber.d("onSupportNavigateUp")
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    fun updateScreenshotPrevention() {
        if (Prefs.prohibitScreenshots) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    fun alertUserOfError(e: Error) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(baseContext)

        builder
            .setTitle("Oops")
            .setMessage(e.localizedMessage)
            .setPositiveButton("OK") { dialog, which ->
            }

        val dialog: AlertDialog = builder.create()

        dialog.show()
    }
}