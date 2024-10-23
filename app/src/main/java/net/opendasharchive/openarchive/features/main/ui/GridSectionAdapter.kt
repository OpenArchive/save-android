package net.opendasharchive.openarchive.features.main.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.databinding.LayoutGridSectionHeaderBinding
import net.opendasharchive.openarchive.features.main.ui.GridSectionAdapter.Companion.UPDATE_GRID_CONTENT_PAYLOAD
import net.opendasharchive.openarchive.features.main.ui.GridSectionAdapter.Companion.UPDATE_GRID_STATE_PAYLOAD
import net.opendasharchive.openarchive.upload.MediaWithState

sealed class GridSectionItem {
    data class Header(val title: String, val numItems: Int) : GridSectionItem()
    data class Thumbnail(val mediaWithState: MediaWithState) : GridSectionItem()
}

class GridItemDiffCallback : DiffUtil.ItemCallback<GridSectionItem>() {
    override fun areItemsTheSame(oldItem: GridSectionItem, newItem: GridSectionItem): Boolean {
        return when {
            oldItem is GridSectionItem.Header && newItem is GridSectionItem.Header -> oldItem.title == newItem.title
            oldItem is GridSectionItem.Thumbnail && newItem is GridSectionItem.Thumbnail -> oldItem.mediaWithState.media.id == newItem.mediaWithState.media.id
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: GridSectionItem, newItem: GridSectionItem): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: GridSectionItem, newItem: GridSectionItem): Any? {
        if (oldItem is GridSectionItem.Thumbnail && newItem is GridSectionItem.Thumbnail) {
            if (oldItem.mediaWithState.state != newItem.mediaWithState.state) {
                return UPDATE_GRID_STATE_PAYLOAD
            }
            if (oldItem.mediaWithState.media.title != newItem.mediaWithState.media.title) {
                return UPDATE_GRID_CONTENT_PAYLOAD
            }
        }
        return null
    }
}

class GridSectionAdapter(
    private val selectedItemsFlow: StateFlow<Set<Int>>,
    private val onItemSelectionChanged: (Int, Boolean) -> Unit) : ListAdapter<GridSectionItem, RecyclerView.ViewHolder>(GridItemDiffCallback()) {

    private var selectedItems: Set<Int> = emptySet()

    init {
        CoroutineScope(Dispatchers.Main).launch {
            selectedItemsFlow.collect { newSelectedItems ->
                val oldSelectedItems = selectedItems
                selectedItems = newSelectedItems
                (oldSelectedItems + newSelectedItems).forEach { position ->
                    notifyItemChanged(position)
                }
            }
        }
    }

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_THUMBNAIL = 1
        const val UPDATE_GRID_STATE_PAYLOAD = "UPDATE_STATE"
        const val UPDATE_GRID_CONTENT_PAYLOAD = "UPDATE_CONTENT"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            VIEW_TYPE_THUMBNAIL -> ThumbnailViewHolder.from(parent)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
//        if (payloads.isEmpty()) {
//            onBindViewHolder(holder, position)
//        } else {
//            val item = getItem(position)
//            when (holder) {
//                is MediaViewHolder -> {
//                    if (item is GridSectionItem.Thumbnail) {
//                        for (payload in payloads) {
//                            when (payload) {
//                                UPDATE_STATE_PAYLOAD -> holder.updateState(item)
//                                UPDATE_CONTENT_PAYLOAD -> holder.updateContent(item)
//                            }
//                        }
//                    }
//                }
//                // Handle other ViewHolder types if necessary
//            }
//        }
//    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is GridSectionItem.Header -> (holder as HeaderViewHolder).bind(item)
            is GridSectionItem.Thumbnail -> {
                val thumbnailViewHolder = holder as ThumbnailViewHolder
                thumbnailViewHolder.bind(item.mediaWithState)
                thumbnailViewHolder.setOnSelectionChangedListener { isSelected ->
                    onItemSelectionChanged(position, isSelected)
                }
            }
        }
    }

    override fun getItemCount() = currentList.size

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GridSectionItem.Header -> VIEW_TYPE_HEADER
            is GridSectionItem.Thumbnail -> VIEW_TYPE_THUMBNAIL
        }
    }

    override fun submitList(list: List<GridSectionItem>?) {
        val oldList = currentList

        if (list == null || list == oldList) {
            // If the new list is null or the same instance as the current list, don't update
            return
        }

        if (list.size == oldList.size && list.containsAll(oldList)) {
            // If the lists have the same size and elements, don't update
            return
        }

        super.submitList(list)
    }

    class HeaderViewHolder(private val binding: LayoutGridSectionHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LayoutGridSectionHeaderBinding.inflate(layoutInflater, parent, false)
                return HeaderViewHolder(binding)
            }
        }

        fun bind(header: GridSectionItem.Header) {
            binding.timestamp.text = header.title
            binding.count.text = header.numItems.toString()
        }
    }

    class ThumbnailViewHolder(private val thumbnailView: MediaThumbnailView) : RecyclerView.ViewHolder(thumbnailView) {
        companion object {
            fun from(parent: ViewGroup): ThumbnailViewHolder {
                val thumbnail = MediaThumbnailView(parent.context)
                return ThumbnailViewHolder(thumbnail)
            }
        }

        fun bind(mediaWithState: MediaWithState) {
            thumbnailView.setMedia(mediaWithState.media)
            thumbnailView.setUploadState(mediaWithState.state)
        }

        fun setOnSelectionChangedListener(listener: (Boolean) -> Unit) {
            thumbnailView.setOnSelectionChangedListener(listener)
        }
    }
}