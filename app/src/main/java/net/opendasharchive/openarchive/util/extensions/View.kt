package net.opendasharchive.openarchive.util.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar

private object ViewHelper {
    const val ANIMATION_DURATION: Long = 250 // ms

    fun hide(view: View, visibility: Int, animate: Boolean) {
        if (animate && view.isVisible) {
            view.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = visibility
                        view.alpha = 1f

                        view.animate().setListener(null)
                    }
                })
        }
        else {
            view.visibility = visibility
        }
    }
}


fun View.show(animate: Boolean = false) {
    if (isVisible) return

    if (animate) {
        alpha = 0f
        visibility = View.VISIBLE

        animate().alpha(1f).duration = ViewHelper.ANIMATION_DURATION
    }
    else {
        visibility = View.VISIBLE
    }
}

fun View.hide(animate: Boolean = false) {
    ViewHelper.hide(this, View.GONE, animate)
}

fun View.cloak(animate: Boolean = false) {
    ViewHelper.hide(this, View.INVISIBLE, animate)
}

fun View.toggle(state: Boolean? = null, animate: Boolean = false) {
    if (state ?: !isVisible) {
        show(animate)
    }
    else {
        hide(animate)
    }
}

fun View.disableAnimation(around: () -> Unit) {
    val p = parent as? ViewGroup

    val original = p?.layoutTransition
    p?.layoutTransition = null

    around()

    p?.layoutTransition = original
}

val View.isVisible: Boolean
    get() = visibility == View.VISIBLE

fun View.makeSnackBar(message: CharSequence, duration: Int = Snackbar.LENGTH_INDEFINITE): Snackbar {
    return Snackbar.make(this, message, duration)
}
