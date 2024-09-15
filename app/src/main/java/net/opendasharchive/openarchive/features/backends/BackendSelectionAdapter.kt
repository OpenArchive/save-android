package net.opendasharchive.openarchive.features.backends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.opendasharchive.openarchive.databinding.BackendHeaderBinding
import net.opendasharchive.openarchive.databinding.BackendItemBinding

class BackendSelectionAdapter(private val onClick: ((GroupedBackend.Item) -> Unit)? = null) : ListAdapter<GroupedBackend, RecyclerView.ViewHolder>(GroupedItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            BACKEND_TYPE_HEADER -> HeaderViewHolder.from(parent)
            BACKEND_TYPE_ITEM -> ItemViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is GroupedBackend.Header -> (holder as HeaderViewHolder).bind(item)
            is GroupedBackend.Item -> (holder as ItemViewHolder).bind(item, onClick)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GroupedBackend.Header -> BACKEND_TYPE_HEADER
            is GroupedBackend.Item -> BACKEND_TYPE_ITEM
        }
    }

    class HeaderViewHolder private constructor(private val binding: BackendHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = BackendHeaderBinding.inflate(layoutInflater, parent, false)
                return HeaderViewHolder(binding)
            }
        }

        fun bind(header: GroupedBackend.Header) {
            binding.title.text = header.title
        }
    }

    class ItemViewHolder private constructor(private val binding: BackendItemBinding) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = BackendItemBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }

        fun bind(item: GroupedBackend.Item, onClick: ((GroupedBackend.Item) -> Unit)? = null) {
            binding.leftIcon.setImageDrawable(item.backend.getAvatar(binding.root.context))
            binding.title.text = item.backend.friendlyName
            binding.root.setOnClickListener { onClick?.invoke(item) }
        }
    }

    companion object {
        private const val BACKEND_TYPE_HEADER = 0
        private const val BACKEND_TYPE_ITEM = 1
    }
}

class GroupedItemDiffCallback : DiffUtil.ItemCallback<GroupedBackend>() {
    override fun areItemsTheSame(oldItem: GroupedBackend, newItem: GroupedBackend): Boolean {
        return when {
            oldItem is GroupedBackend.Header && newItem is GroupedBackend.Header ->
                oldItem.title == newItem.title
            oldItem is GroupedBackend.Item && newItem is GroupedBackend.Item ->
                oldItem.backend.type == newItem.backend.type
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: GroupedBackend, newItem: GroupedBackend): Boolean {
        return oldItem == newItem
    }
}
