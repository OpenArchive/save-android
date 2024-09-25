package net.opendasharchive.openarchive.features.main.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import net.opendasharchive.openarchive.databinding.LayoutGridSectionHeaderBinding
import net.opendasharchive.openarchive.databinding.RvMediaBoxSmallBinding
import timber.log.Timber

sealed class GridSectionItem {
    data class Header(val title: String) : GridSectionItem()
    data class Image(val imageUrl: String) : GridSectionItem()
}

class GridSectionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = mutableListOf<GridSectionItem>()

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_IMAGE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            VIEW_TYPE_IMAGE -> ImageViewHolder.from(parent)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is GridSectionItem.Header -> (holder as HeaderViewHolder).bind(item)
            is GridSectionItem.Image -> (holder as ImageViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is GridSectionItem.Header -> VIEW_TYPE_HEADER
            is GridSectionItem.Image -> VIEW_TYPE_IMAGE
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
//            binding.root.setBackgroundResource(R.color.c23_teal_40)
            binding.timestamp.text = header.title
            binding.count.text = "3"
        }
    }

    class ImageViewHolder(private val binding: RvMediaBoxSmallBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ImageViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvMediaBoxSmallBinding.inflate(layoutInflater, parent, false)
                return ImageViewHolder(binding)
            }
        }

        fun bind(gridItem: GridSectionItem.Image) {
            Timber.d("Loading image ${gridItem.imageUrl}")
            binding.image.load(gridItem.imageUrl) {
                crossfade(true)
                crossfade(250)
            }
        }
    }
}