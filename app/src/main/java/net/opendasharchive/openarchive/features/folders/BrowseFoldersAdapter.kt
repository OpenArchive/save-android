package net.opendasharchive.openarchive.features.folders

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.LayoutFolderRowBinding
import net.opendasharchive.openarchive.databinding.LayoutFolderSectionHeaderBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.db.FolderRepository
import timber.log.Timber
import java.text.SimpleDateFormat

typealias OnFolderSelectedCallback = (folder: Folder) -> Unit
typealias OnFolderLongPressCallback = (folder: Folder, view: View) -> Unit
typealias OnBackendLongPressCallback = (backend: Backend, view: View) -> Unit

sealed class ListItem {
    data class SectionHeader(val backend: Backend) : ListItem()
    data class ContentItem(val folder: Folder) : ListItem()
}

class HeaderViewHolder(
    private val binding: LayoutFolderSectionHeaderBinding,
    private val onHeaderLongPress: OnBackendLongPressCallback) : RecyclerView.ViewHolder(binding.root)
{
    companion object {
        fun from(parent: ViewGroup, onHeaderLongPress: OnBackendLongPressCallback): HeaderViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = LayoutFolderSectionHeaderBinding.inflate(layoutInflater, parent, false)
            return HeaderViewHolder(binding, onHeaderLongPress)
        }
    }

    lateinit var backend: Backend

    fun bind(item: ListItem.SectionHeader) {
        backend = item.backend

        binding.apply {
            title.text = item.backend.friendlyName
            leftIcon.setImageDrawable(item.backend.getAvatar(root.context))
        }

        binding.root.setOnLongClickListener { view ->
            Timber.d("preesss!")
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            onHeaderLongPress.invoke(backend, binding.root)
            true
        }
    }
}

class ContentViewHolder(
    private val binding: LayoutFolderRowBinding,
    private val folderRepo: FolderRepository,
    private val onClick: OnFolderSelectedCallback,
    private val onLongPress: OnFolderLongPressCallback) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private val formatter = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM) //.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.)

        fun from(parent: ViewGroup, folderRepo: FolderRepository, onClick: OnFolderSelectedCallback, onLongPress: OnFolderLongPressCallback): ContentViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = LayoutFolderRowBinding.inflate(layoutInflater, parent, false)
            return ContentViewHolder(binding, folderRepo, onClick, onLongPress)
        }
    }

    lateinit var folder: Folder

    fun bind(item: ListItem.ContentItem) {
        folder = item.folder

        binding.apply {
            name.text = folder.name
            timestamp.text = "Created: " + formatter.format(folder.created)

            root.setOnClickListener {
                onClick.invoke(folder)
            }

            if (folderRepo.currentFolder.value == folder) {
                name.setTextColor(ContextCompat.getColor(itemView.context, R.color.c23_teal))
                timestamp.setTextColor(ContextCompat.getColor(itemView.context, R.color.c23_teal))
            }

            currentFolderCount.text = folder.collections.size.toString()

            root.setOnLongClickListener { view ->
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                onLongPress.invoke(folder, binding.root)
                true
            }
        }
    }
}

class BrowseFoldersAdapter(
    private val folderRepo: FolderRepository,
    private val onItemClick: OnFolderSelectedCallback,
    private val onItemLongPress: OnFolderLongPressCallback,
    private val onHeaderLongPress: OnBackendLongPressCallback) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
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
            VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent, onHeaderLongPress)
            VIEW_TYPE_CONTENT -> ContentViewHolder.from(parent, folderRepo, onItemClick, onItemLongPress)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.SectionHeader -> {
                (holder as HeaderViewHolder).bind(item)
            }
            is ListItem.ContentItem -> {
                (holder as ContentViewHolder).bind(item)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<ListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

//    fun removeItemAt(index: Int) {
//        if (index in items.indices) {
//            items.removeAt(index)
//            notifyItemRemoved(index)
//            notifyItemRangeChanged(index, items.size)
//        }
//    }
}