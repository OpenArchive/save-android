package net.opendasharchive.openarchive.features.main.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.databinding.LayoutGridSectionHeaderBinding
import net.opendasharchive.openarchive.db.Media
import timber.log.Timber

typealias OnMediaItemClickListener = (GridSectionItem.Thumbnail) -> Unit

sealed class GridSectionItem {
    data class Header(val title: String) : GridSectionItem()
    data class Thumbnail(val media: Media) : GridSectionItem()
}

class GridSectionAdapter(
    private val selectedItemsFlow: StateFlow<Set<Int>>,
    private val onItemSelectionChanged: (Int, Boolean) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<GridSectionItem>()
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            VIEW_TYPE_THUMBNAIL -> ThumbnailViewHolder.from(parent)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is GridSectionItem.Header -> (holder as HeaderViewHolder).bind(item)
            is GridSectionItem.Thumbnail -> {
                val thumbnailViewHolder = holder as ThumbnailViewHolder
                thumbnailViewHolder.bind(item, position in selectedItems)
                thumbnailViewHolder.setOnSelectionChangedListener { isSelected ->
                    onItemSelectionChanged(position, isSelected)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is GridSectionItem.Header -> VIEW_TYPE_HEADER
            is GridSectionItem.Thumbnail -> VIEW_TYPE_THUMBNAIL
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(newItems: List<GridSectionItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
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
            binding.count.text = "3"
        }
    }

    class ThumbnailViewHolder(private val thumbnailView: MediaThumbnailView) : RecyclerView.ViewHolder(thumbnailView) {
        companion object {
            fun from(parent: ViewGroup): ThumbnailViewHolder {
                val thumbnail = MediaThumbnailView(parent.context)
                return ThumbnailViewHolder(thumbnail)
            }
        }

        fun bind(gridItem: GridSectionItem.Thumbnail, isSelected: Boolean) {
            Timber.d("Loading image ${gridItem.media.originalFilePath}")
            thumbnailView.loadImage(gridItem.media.originalFilePath)
            thumbnailView.isItemSelected = isSelected
        }

        fun setOnSelectionChangedListener(listener: (Boolean) -> Unit) {
            thumbnailView.setOnSelectionChangedListener(listener)
        }
    }
}