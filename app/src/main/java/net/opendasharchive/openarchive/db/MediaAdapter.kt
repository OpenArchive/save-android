package net.opendasharchive.openarchive.db

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.extensions.propagateClickToParent
import net.opendasharchive.openarchive.features.media.PreviewActivity
import net.opendasharchive.openarchive.upload.BroadcastManager
import net.opendasharchive.openarchive.upload.UploadManagerActivity
import net.opendasharchive.openarchive.upload.UploadService
import net.opendasharchive.openarchive.util.AlertHelper
import net.opendasharchive.openarchive.util.AppSettings
import timber.log.Timber
import java.lang.ref.WeakReference

class MediaAdapter(
    activity: Activity?,
    private val settings: AppSettings,
    private val generator: (parent: ViewGroup) -> MediaViewHolder,
    data: List<Media>,
    private val recyclerView: RecyclerView,
    private val supportedStatuses: List<Media.Status> = listOf(Media.Status.Local, Media.Status.Uploading, Media.Status.Error),
    private val checkSelecting: (() -> Unit)? = null
) : RecyclerView.Adapter<MediaViewHolder>() {

    var media: ArrayList<Media> = ArrayList(data)
        private set

    var doImageFade = false

    var isEditMode = false

    var selecting = false
        private set

    private var mActivity = WeakReference(activity)

    object UPDATE_STATE_PAYLOAD

    var deleteMode = false
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount, UPDATE_STATE_PAYLOAD)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val mediaViewHolder = generator(parent)

        mediaViewHolder.itemView.setOnClickListener { v ->
            if (selecting && checkSelecting != null) {
                selectView(v)
            }
            else {
                val pos = recyclerView.getChildLayoutPosition(v)

                when (media[pos].status) {
                    Media.Status.Local -> handleLocalItemAt(index = pos)
                    Media.Status.Queued,
                    Media.Status.Uploading -> handleQueuedItemAt(index = pos)
                    Media.Status.Error -> handleErrorCaseAt(index = pos)

                    else -> {
                        if (checkSelecting != null) {
                            selectView(v)
                        }
                    }
                }
            }
        }

        if (checkSelecting != null) {
            mediaViewHolder.itemView.setOnLongClickListener { v ->
                selectView(v)
                true
            }
        }

        mediaViewHolder.flagIndicator?.setOnClickListener {
            showFirstTimeFlag()

            // Toggle flag
            val mediaId = mediaViewHolder.itemView.tag as? Long ?: return@setOnClickListener

            val item = media.firstOrNull { it.id == mediaId } ?: return@setOnClickListener
            item.flag = !item.flag
            item.save()

            notifyItemChanged(media.indexOf(item))
        }

        return mediaViewHolder
    }

    private fun handleLocalItemAt(index: Int) {
        if (supportedStatuses.contains(Media.Status.Local)) {
            mActivity.get()?.let {
                PreviewActivity.start(it, media[index].folderId)
            }
        }
    }

    private fun handleQueuedItemAt(index: Int) {
        if (supportedStatuses.contains(Media.Status.Uploading)) {
            mActivity.get()?.let {
                it.startActivity(
                    Intent(it, UploadManagerActivity::class.java)
                )
            }
        }
    }

    private fun handleErrorCaseAt(index: Int) {
        if (supportedStatuses.contains(Media.Status.Error)) {
            mActivity.get()?.let {
                AlertHelper.show(
                    it, it.getString(R.string.upload_unsuccessful_description),
                    R.string.upload_unsuccessful, R.drawable.ic_error, listOf(
                        AlertHelper.positiveButton(R.string.retry) { _, _ ->

                            media[index].apply {
                                status = Media.Status.Queued
                                statusMessage = ""
                                save()

                                BroadcastManager.postChange(it, collectionId, id)
                            }

                            UploadService.startUploadService(it)
                        },
                        AlertHelper.negativeButton(R.string.remove) { _, _ ->
                            removeItemByPosition(index)
                        },
                        AlertHelper.neutralButton()
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int = media.size

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            // Full update
            onBindViewHolder(holder, position)
        } else {
            // Partial update
            val payload = payloads[0]

            if (payload == UPDATE_STATE_PAYLOAD) {
                holder.deleteIndicator?.visibility = if (canDeleteMediaAt(position)) View.VISIBLE else View.GONE

                if (canDeleteMediaAt(position)) {
                    startStaggeredAnimation(holder)
                } else {
//                    holder.mediaView?.clearAnimation()
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(media[position], selecting, doImageFade)

//        holder.handle?.toggle(isEditMode)

        holder.deleteIndicator?.visibility = if (deleteMode) View.VISIBLE else View.GONE

        if (canDeleteMediaAt(position)) {
            startStaggeredAnimation(holder)
        } else {
            holder.image.clearAnimation()
        }

        holder.image.setOnClickListener { v ->
            if (deleteMode) {
                deleteMode = false
            } else {
                v.propagateClickToParent()
            }
        }

        holder.image.setOnLongClickListener {
            if (!deleteMode) {
                deleteMode = true
                true
            } else false
        }

        holder.deleteIndicator?.setOnClickListener {
            Timber.d("CLick!")
            removeItemByPosition(position)
            notifyItemRemoved(position)
            if (media.isEmpty()) {
                deleteMode = false
            }
        }
    }

    private fun canDeleteMediaAt(position: Int): Boolean {
        if (!deleteMode) {
            return false
        }

        val mediaItem = media[position]

        return when (mediaItem.status) {
            Media.Status.Local,
            Media.Status.Queued -> true
            else -> false
        }
    }

    private fun startStaggeredAnimation(holder: MediaViewHolder) {
        Timber.d("Starting wiggle")
        val delay = (0L..125L).random()
//        holder.mediaView?.clearAnimation()
//        holder.mediaView?.postDelayed({ holder.mediaView?.startAnimation(holder.wiggleAnimation) }, delay)
    }

    fun updateItem(mediaId: Long, progress: Long): Boolean {
        val idx = media.indexOfFirst { it.id == mediaId }
        if (idx < 0) return false

        if (progress >= 0) {
            media[idx].progress = progress
        } else {
            val item = Media.get(mediaId) ?: return false
            media[idx] = item
        }
        notifyItemChanged(idx)

        return true
    }

    fun removeItemById(mediaId: Long): Boolean {
        val idx = media.indexOfFirst { it.id == mediaId }
        if (idx < 0) return false

        media.removeAt(idx)

        notifyItemRemoved(idx)

        checkSelecting?.invoke()

        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(media: List<Media>) {
        this.media = ArrayList(media)

        notifyDataSetChanged()
    }

    private fun showFirstTimeFlag() {
        if (settings.flagHintShown) return
        val activity = mActivity.get() ?: return

        AlertHelper.show(activity, R.string.popup_flag_desc, R.string.popup_flag_title)

        settings.flagHintShown = true
    }

    private fun selectView(view: View) {
        val mediaId = view.tag as? Long ?: return

        val m = media.firstOrNull { it.id == mediaId } ?: return
        m.selected = !m.selected
        m.save()

        notifyItemChanged(media.indexOf(m))

        selecting = media.firstOrNull { it.selected } != null
        checkSelecting?.invoke()
    }

    fun onItemMove(oldPos: Int, newPos: Int) {
        if (!isEditMode) return

        val mediaToMov = media.removeAt(oldPos)
        media.add(newPos, mediaToMov)

        var priority = media.size

        for (item in media) {
            item.priority = priority--
            item.save()
        }

        notifyItemMoved(oldPos, newPos)
    }

    fun removeItemByPosition(pos: Int) {
        if (pos < 0 || pos >= media.size) return

        val item = media[pos]

        Timber.d("Removing item ${item.id}")

        removeItemById(item.id)
    }

    fun deleteSelected(): Boolean {
        var hasDeleted = false

        for (item in media.filter { it.selected }) {
            val idx = media.indexOf(item)
            media.remove(item)

            notifyItemRemoved(idx)

            item.delete()

            hasDeleted = true
        }

        selecting = false

        checkSelecting?.invoke()

        return hasDeleted
    }
}