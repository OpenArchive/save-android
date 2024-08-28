package net.opendasharchive.openarchive.features.main.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton

class DualIconButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr) {

    private var leftIcon: Drawable? = null
    private var rightIcon: Drawable? = null

    fun setLeftIcon(drawable: Drawable?) {
        leftIcon = drawable
        updateIcons()
    }

    fun setRightIcon(drawable: Drawable?) {
        rightIcon = drawable
        updateIcons()
    }

    private fun updateIcons() {
        setCompoundDrawablesWithIntrinsicBounds(leftIcon, null, rightIcon, null)
    }
}