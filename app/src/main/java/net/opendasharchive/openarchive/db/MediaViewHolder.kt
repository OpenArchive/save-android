package net.opendasharchive.openarchive.db

import android.annotation.SuppressLint
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.load
import coil.request.videoFrameMillis
import com.github.derlio.waveform.SimpleWaveformView
import com.github.derlio.waveform.soundfile.SoundFile
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.RvMediaBoxSmallBinding
import net.opendasharchive.openarchive.databinding.RvMediaRowSmallBinding
import net.opendasharchive.openarchive.extensions.uriToPath
import net.opendasharchive.openarchive.util.extensions.hide
import net.opendasharchive.openarchive.util.extensions.show
import timber.log.Timber
import java.io.InputStream
import kotlin.math.roundToInt

abstract class MediaViewHolder(protected val binding: ViewBinding): RecyclerView.ViewHolder(binding.root) {

    class SmallBox(parent: ViewGroup): MediaViewHolder(
        RvMediaBoxSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    ) {
        override val image: ImageView
            get() = (binding as RvMediaBoxSmallBinding).image

        override val waveform: SimpleWaveformView
            get() = (binding as RvMediaBoxSmallBinding).waveform

        override val videoIndicator: ImageView
            get() = (binding as RvMediaBoxSmallBinding).videoIndicator

        override val overlayContainer: View
            get() = (binding as RvMediaBoxSmallBinding).overlayContainer

        override val progress: CircularProgressIndicator
            get() = (binding as RvMediaBoxSmallBinding).progress

//        override val progressText: TextView
//            get() = (binding as RvMediaBoxSmallBinding).progressText

        override val error: ImageView
            get() = (binding as RvMediaBoxSmallBinding).error

        override val title: TextView?
            get() = null //(binding as RvMediaBoxSmallBinding).title

        override val fileInfo: TextView?
            get() = null //(binding as RvMediaBoxSmallBinding).fileInfo

        override val locationIndicator: ImageView?
            get() = null

        override val tagsIndicator: ImageView?
            get() = null

        override val descIndicator: ImageView?
            get() = null

        override val flagIndicator: ImageView?
            get() = null

        override val deleteIndicator: View
            get() = (binding as RvMediaBoxSmallBinding).deleteIndicator

        override val handle: ImageView?
            get() = null

        override val mediaView: View
            get() = (binding as RvMediaBoxSmallBinding).mediaView
    }

    class SmallRow(parent: ViewGroup): MediaViewHolder(
        RvMediaRowSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    ) {
        override val image: ImageView
            get() = (binding as RvMediaRowSmallBinding).image

        override val waveform: SimpleWaveformView
            get() = (binding as RvMediaRowSmallBinding).waveform

        override val videoIndicator: ImageView?
            get() = null

        override val overlayContainer: View
            get() = (binding as RvMediaRowSmallBinding).overlayContainer

        override val progress: CircularProgressIndicator
            get() = (binding as RvMediaRowSmallBinding).progress

//        override val progressText: TextView
//            get() = (binding as RvMediaRowSmallBinding).progressText

        override val error: ImageView
            get() = (binding as RvMediaRowSmallBinding).error

        override val title: TextView
            get() = (binding as RvMediaRowSmallBinding).title

        override val fileInfo: TextView
            get() = (binding as RvMediaRowSmallBinding).fileInfo

        override val locationIndicator: ImageView?
            get() = null

        override val tagsIndicator: ImageView?
            get() = null

        override val descIndicator: ImageView?
            get() = null

        override val flagIndicator: ImageView?
            get() = null

        override val deleteIndicator: View?
            get() = null

        override val handle: ImageView
            get() = (binding as RvMediaRowSmallBinding).handle

        override val mediaView: View? = null
    }

    companion object {
        val soundCache = HashMap<String, SoundFile>()
    }

    abstract val image: ImageView
    abstract val waveform: SimpleWaveformView
    abstract val videoIndicator: ImageView?
    abstract val overlayContainer: View?
    abstract val progress: CircularProgressIndicator?
//    abstract val progressText: TextView?
    abstract val error: ImageView?
    abstract val title: TextView?
    abstract val fileInfo: TextView?
    abstract val locationIndicator: ImageView?
    abstract val tagsIndicator: ImageView?
    abstract val descIndicator: ImageView?
    abstract val flagIndicator: ImageView?
    abstract val deleteIndicator: View?
    abstract val handle: ImageView?
    abstract val mediaView: View?

    private val mContext = itemView.context

    val wiggleAnimation: Animation = AnimationUtils.loadAnimation(itemView.context, R.anim.wiggle)

    private val imageLoader = ImageLoader.Builder(mContext)
        .components {
            add(VideoFrameDecoder.Factory())
        }
        .build()

    fun updateState(media: Media) {
        Timber.d("Updaitng state")
    }

    @SuppressLint("SetTextI18n")
    fun bind(media: Media? = null, batchMode: Boolean = false, doImageFade: Boolean = true) {
        itemView.tag = media?.id

        if (batchMode && media?.selected == true) {
            val border = ContextCompat.getDrawable(mContext, R.drawable.media_outline)
            mediaView?.setForeground(border)
        }
        else {
            itemView.setBackgroundResource(R.color.transparent)
        }

        mediaView?.setClipToOutline(true)

        image.alpha = if (media?.status == Media.Status.Uploaded || !doImageFade) 1f else 0.75f
        // image.setClipToOutline(true)

        if (media?.mimeType?.startsWith("image") == true) {
            handleImageMediaType(media)
        }
        else if (media?.mimeType?.startsWith("video") == true) {
            handleVideoMediaType(media)
        }
        else if (media?.mimeType?.startsWith("audio") == true) {
            handleAudioMediaType(media)
        }
        else {
            handleUnknownMediaType()
        }

        if (media != null) {
            buildFileInfo(media)
            fileInfo?.show()
        }
        else {
            fileInfo?.hide()
        }

        val sbTitle = StringBuffer()

        when (media!!.status) {
            Media.Status.Unknown -> handleUnknown(media)
            Media.Status.New -> Timber.d("Media is " + media.status.toString())
            Media.Status.Local -> handleLocal()
            Media.Status.Queued -> handleQueued()
            Media.Status.Uploading -> handleUploading(media)
            Media.Status.Uploaded -> handleUploaded()
            Media.Status.Error -> handleError(media)
        }

        if (sbTitle.isNotEmpty()) sbTitle.append(": ")
        sbTitle.append(media.title)

        if (sbTitle.isNotBlank()) {
            title?.text = sbTitle.toString()
            title?.show()
        }
        else {
            title?.hide()
        }

        locationIndicator?.setImageResource(
            if (media.location.isBlank()) R.drawable.ic_location_unselected
            else R.drawable.ic_location_selected)

        tagsIndicator?.setImageResource(
            if (media.tags.isBlank()) R.drawable.ic_tag_unselected
            else R.drawable.ic_tag_selected)

        descIndicator?.setImageResource(
            if (media.description.isBlank()) R.drawable.ic_edit_unselected
            else R.drawable.ic_edit_selected)

        flagIndicator?.setImageResource(
            if (media.flag) R.drawable.ic_flag_selected
            else R.drawable.ic_flag_unselected)
    }

    private fun buildFileInfo(media: Media) {
        val file = media.file

        if (file.exists()) {
            fileInfo?.text = Formatter.formatShortFileSize(mContext, file.length())
        } else {
            if (media.contentLength == -1L) {
                var iStream: InputStream? = null
                try {
                    iStream = mContext.contentResolver.openInputStream(media.fileUri)

                    if (iStream != null) {
                        media.contentLength = iStream.available().toLong()
                        media.save()
                    }
                }
                catch (e: Throwable) {
                    Timber.e(e)
                } finally {
                    iStream?.close()
                }
            }

            fileInfo?.text = if (media.contentLength > 0) {
                Formatter.formatShortFileSize(mContext, media.contentLength)
            } else {
                media.formattedCreateDate
            }
        }
    }

    private fun handleAudioMediaType(media: Media) {
        videoIndicator?.hide()

        val soundFile = soundCache[media.originalFilePath]

        if (soundFile != null) {
            image.hide()
            waveform.setAudioFile(soundFile)
            waveform.show()
        }
        else {
            image.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_thumbnail))
            image.show()
            waveform.hide()

            CoroutineScope(Dispatchers.IO).launch {
                @Suppress("NAME_SHADOWING")
                val soundFile = try {
                    SoundFile.create(media.originalFilePath.uriToPath()) {
                        return@create true
                    }
                }
                catch (e: Throwable) {
                    Timber.d(e)
                    null
                }

                if (soundFile != null) {
                    soundCache[media.originalFilePath] = soundFile

                    MainScope().launch {
                        waveform.setAudioFile(soundFile)
                        image.hide()
                        waveform.show()
                    }
                }
            }
        }
    }

    private fun handleImageMediaType(media: Media) {
        image.load(media.fileUri, imageLoader)

        waveform.hide()
        videoIndicator?.hide()
    }

    private fun handleUnknownMediaType() {
        image.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_thumbnail))
        waveform.hide()
        videoIndicator?.hide()
    }

    private fun handleVideoMediaType(media: Media) {
        image.load(media.originalFilePath, imageLoader) {
            videoFrameMillis(0)
        }

        waveform.hide()
        videoIndicator?.show()
    }

    private fun handleError(media: Media) {
        Timber.d("Media has error")
        // sbTitle.append(mContext.getString(R.string.error))

        overlayContainer?.show()
        progress?.hide()
//        progressText?.hide()
        error?.show()

        if (media.statusMessage.isNotBlank()) {
            Timber.d("Media error: ${media.statusMessage}")
            fileInfo?.text = media.statusMessage
            fileInfo?.show()
        }
    }

    private fun handleLocal() {
        Timber.d("Media is local")
        overlayContainer?.hide()
        progress?.hide()
//        progressText?.hide()
        error?.hide()
    }

    private fun handleQueued() {
        Timber.d("Media is queued")
        overlayContainer?.show()
        progress?.show()
//        progressText?.hide()
        error?.hide()
    }

    private fun handleUploaded() {
        Timber.d("Media is uploaded")
        overlayContainer?.hide()
        progress?.hide()
//        progressText?.hide()
        error?.hide()
    }

    private fun handleUploading(media: Media) {
        Timber.d("Media is uploading")

        val progressValue = if (media.contentLength > 0) {
            (media.progress.toFloat() / media.contentLength.toFloat() * 100f).roundToInt()
        } else 0

        overlayContainer?.show()
        progress?.show()
//        progressText?.show()

        // Make sure to keep spinning until the upload has made some noteworthy progress.
        if (progressValue > 2) {
            progress?.setProgressCompat(progressValue , true)
        }
        else {
            progress?.isIndeterminate = true
        }
//        progressText?.text = "${progressValue}%"

        error?.hide()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleUnknown(media: Media) {
        Timber.d("Media status is unknown")
        overlayContainer?.hide()
        progress?.hide()
//        progressText?.hide()
        error?.hide()
    }
}
