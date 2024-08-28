package net.opendasharchive.openarchive.features.folders

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.opendasharchive.openarchive.databinding.FolderRowBinding
import net.opendasharchive.openarchive.db.Folder
import java.text.SimpleDateFormat

typealias OnFolderSelectedCallback = (folder: Folder) -> Unit

sealed class ListItem {
    data class SectionHeader(val title: String) : ListItem()
    data class ContentItem(val folder: Folder) : ListItem()
}

class HeaderViewHolder(private val binding: FolderRowBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): HeaderViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = FolderRowBinding.inflate(layoutInflater, parent, false)
            return HeaderViewHolder(binding)
        }
    }

    fun bind(item: ListItem.SectionHeader) {
        binding.name.text = item.title
    }
}

class ContentViewHolder(
    private val binding: FolderRowBinding,
    private val onClick: OnFolderSelectedCallback) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private val formatter = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.MEDIUM)

        fun from(parent: ViewGroup, onClick: OnFolderSelectedCallback): ContentViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = FolderRowBinding.inflate(layoutInflater, parent, false)
            return ContentViewHolder(binding, onClick)
        }
    }

    fun bind(item: ListItem.ContentItem, position: Int) {
        binding.apply {
            name.text = item.folder.description
            timestamp.text = formatter.format(item.folder.created)

            root.setOnClickListener {
                onClick.invoke(item.folder)
            }
        }
    }
}

class BrowseFoldersAdapter(private val onClick: OnFolderSelectedCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_CONTENT = 1
    }

    private var items: List<ListItem> = emptyList()

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.SectionHeader -> VIEW_TYPE_HEADER
            is ListItem.ContentItem -> VIEW_TYPE_CONTENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            VIEW_TYPE_CONTENT -> ContentViewHolder.from(parent, onClick)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.SectionHeader -> {
                (holder as HeaderViewHolder).bind(item)
            }
            is ListItem.ContentItem -> {
                (holder as ContentViewHolder).bind(item, position)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<ListItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}