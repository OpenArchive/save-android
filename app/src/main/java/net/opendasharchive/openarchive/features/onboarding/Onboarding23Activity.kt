package net.opendasharchive.openarchive.features.onboarding

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.Spanned
import android.view.Window
import android.view.WindowManager
import android.view.animation.BounceInterpolator
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityOnboarding23Binding
import net.opendasharchive.openarchive.features.core.BaseActivity

class Onboarding23Activity : BaseActivity() {

    private lateinit var viewBinding: ActivityOnboarding23Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        viewBinding = ActivityOnboarding23Binding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.getStarted.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    Onboarding23InstructionsActivity::class.java
                )
            )
        }

        val textViews = listOf(
            viewBinding.titleBlock.word1,
            viewBinding.titleBlock.word2,
            viewBinding.titleBlock.word3,
            viewBinding.titleBlock.word4)

        // Wait for layout to be drawn
//        viewBinding.titleBlock.word1.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                viewBinding.titleBlock.word1.viewTreeObserver.removeOnGlobalLayoutListener(this)
//
//                // Find the smallest text size among all TextViews
//                val minTextSize = textViews.minOf { it.textSize }.coerceAtMost(10f)
//
//                Timber.d("min text size = $minTextSize")
//
//                // Set all TextViews to use this size
//                textViews.forEach { it.setTextSize(TypedValue.COMPLEX_UNIT_PX, minTextSize) }
//            }
//        })
        
        textViews.forEach { textView ->
            textView.text = colorizeFirstLetter(textView.text, R.color.c23_teal)
        }
    }

    override fun onResume() {
        super.onResume()

        val oa = ObjectAnimator.ofFloat(viewBinding.arrow, "translationX", 0F, 25F, 0F)
        oa.interpolator = BounceInterpolator()
        oa.startDelay = 3000
        oa.duration = 2000
        oa.repeatCount = 999999
        oa.start()
    }

    private fun colorizeFirstLetter(text: CharSequence, @ColorRes color: Int): Spanned {
        val colorHexString =
            Integer.toHexString(0xffffff and ContextCompat.getColor(this, color))
        val html =
            "<font color=\"#${colorHexString}\">${text.substring(0, 1)}</font>${text.substring(1)}"
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
}