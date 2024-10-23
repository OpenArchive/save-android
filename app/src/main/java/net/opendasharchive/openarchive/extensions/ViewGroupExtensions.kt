package net.opendasharchive.openarchive.extensions

import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import net.opendasharchive.openarchive.R

fun ViewGroup.showDimmingOverlay() {
    children.firstOrNull { it.id == R.id.progress_bar_overlay }?.apply {
        if (!isVisible) {
            alpha = 0f
            isVisible = true
            animate().alpha(1f).setDuration(200).start()
        }
    }
}

fun ViewGroup.hideDimmingOverlay() {
    children.firstOrNull { it.id == R.id.progress_bar_overlay }?.apply {
        if (isVisible) {
            animate().alpha(0f).setDuration(200).withEndAction {
                isVisible = false
            }.start()
        }
    }
}