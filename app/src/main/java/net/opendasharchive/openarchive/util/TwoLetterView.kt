package net.opendasharchive.openarchive.util

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import net.opendasharchive.openarchive.R

class TwoLetterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var letterDrawable: TwoLetterDrawable? = null

    fun setLetters(letters: String) {
        require(letters == "RO" || letters == "RW") { "Only 'RO' or 'RW' are supported" }
        letterDrawable = TwoLetterDrawable(
            text = letters,
            textColor = ContextCompat.getColor(context, android.R.color.white),
            backgroundColor = ContextCompat.getColor(context, android.R.color.holo_blue_dark),
            textSize = context.resources.getDimension(R.dimen.letter_text_size)
        )
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        letterDrawable?.let {
            it.setBounds(0, 0, width, height)
            it.draw(canvas)
        }
    }
}