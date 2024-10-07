package net.opendasharchive.openarchive.util

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.view.isVisible

class FullScreenDimmingOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        // Set layout parameters to match parent
        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        // Set a semi-transparent background
        setBackgroundColor(Color.parseColor("#C0000000"))

        // Add a centered ProgressBar
        addView(ProgressBar(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
        })

        // Initially invisible
        isVisible = false

        // Intercept touch events
        isClickable = true
        isFocusable = true
    }

    fun show() {
        if (!isVisible) {
            alpha = 0f
            isVisible = true
            animate().alpha(1f).setDuration(200).start()
        }
    }

    fun hide() {
        if (isVisible) {
            animate().alpha(0f).setDuration(200).withEndAction {
                isVisible = false
            }.start()
        }
    }
}