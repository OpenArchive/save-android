package net.opendasharchive.openarchive.extensions

import android.view.View

fun View.getMeasurments(): Pair<Int, Int> {
    measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    val width = measuredWidth
    val height = measuredHeight
    return width to height
}

fun View.propagateClickToParent() {
    var parent = this.parent as? View
    while (parent != null && !parent.isClickable) {
        parent = parent.parent as? View
    }
    parent?.performClick()
}