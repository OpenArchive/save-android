package net.opendasharchive.openarchive.features.core

import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.util.Prefs
import timber.log.Timber

abstract class BaseActivity: AppCompatActivity() {

    companion object {
        const val EXTRA_DATA_BACKEND = "space"
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            val obscuredTouch = event.flags and MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED != 0
            if (obscuredTouch) return false
        }

        return super.dispatchTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.navigationBarColor = ContextCompat.getColor(this, R.color.colorBottomNavbar)
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

//    override fun onBackPressed() {
//        if (supportFragmentManager.backStackEntryCount > 1) {
//            // If there's more than one fragment in the stack, let the system handle it
//            super.onBackPressed()
//        } else if (supportFragmentManager.backStackEntryCount == 1) {
//            // If this is the last fragment, pop it and return to the activity
//            supportFragmentManager.popBackStack()
//        } else {
//            // If the back stack is empty, let the system handle it (will likely finish the activity)
//            super.onBackPressed()
//        }
//    }

    private fun updateScreenshotPrevention() {
        if (Prefs.prohibitScreenshots) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

//    fun alertUserOfError(e: Error) {
//        val builder: AlertDialog.Builder = AlertDialog.Builder(baseContext)
//
//        builder
//            .setTitle("Oops")
//            .setMessage(e.localizedMessage)
//            .setPositiveButton("OK") { dialog, which ->
//            }
//
//        val dialog: AlertDialog = builder.create()
//
//        dialog.show()
//    }
}