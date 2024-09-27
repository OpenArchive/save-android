package net.opendasharchive.openarchive.features.main.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.work.WorkInfo
import coil.load
import com.google.android.material.imageview.ShapeableImageView
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.db.Media

class MediaThumbnailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var thumbnailBorder: View
    private var thumbnailView: ShapeableImageView

    private val selectedBackground by lazy {
        AppCompatResources.getDrawable(context, R.drawable.media_selected)
    }

    private var onSelectionChangedListener: ((Boolean) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.rv_media_box_small, this, true)

        thumbnailView = findViewById(R.id.image)
        thumbnailBorder = findViewById(R.id.image_border)

        setOnClickListener {
            isItemSelected = !isItemSelected
            onSelectionChangedListener?.invoke(isItemSelected)
        }
    }

    var isItemSelected = false
        set(value) {
            if (field != value) {
                field = value
                updateBorder()
            }
        }

    private fun updateBorder() {
        if (isItemSelected) {
            thumbnailBorder.background = selectedBackground
            selectedBackground?.alpha = 0
            fadeDrawable(selectedBackground, 0f, 1f)
        } else {
            fadeDrawable(selectedBackground, 1f, 0f)
        }
    }

    private fun fadeDrawable(drawable: Drawable?, fromAlpha: Float, toAlpha: Float) {
        if (drawable == null) { return }

        ObjectAnimator.ofPropertyValuesHolder(
            drawable,
            PropertyValuesHolder.ofInt("alpha", (fromAlpha * 255).toInt(), (toAlpha * 255).toInt())
        ).apply {
            duration = 200
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (toAlpha == 0f) {
                        background = null
                    }
                }
            })
            start()
        }
    }

    fun setOnSelectionChangedListener(listener: (Boolean) -> Unit) {
        onSelectionChangedListener = listener
    }

    fun loadImage(url: String) {
        thumbnailView.load(url) {
            crossfade(true)
            crossfade(250)
        }
    }

    fun setMedia(media: Media) {
        loadImage(media.originalFilePath)
    }

    fun setUploadState(state: WorkInfo.State?) {

    }
}