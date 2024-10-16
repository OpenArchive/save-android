package net.opendasharchive.openarchive.services.snowbird

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.LayoutSnowbirdMediaItemBinding
import net.opendasharchive.openarchive.db.SnowbirdMediaItem

class SnowbirdMediaViewHolder(val binding: LayoutSnowbirdMediaItemBinding) : RecyclerView.ViewHolder(binding.root)

class SnowbirdMediaListAdapter(listener: ((String) -> Unit)? = null) : ListAdapter<SnowbirdMediaItem, SnowbirdMediaViewHolder>(SnowbirdMediaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnowbirdMediaViewHolder {
        val binding = LayoutSnowbirdMediaItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SnowbirdMediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SnowbirdMediaViewHolder, position: Int) {
        val item = getItem(position)

        with (holder.binding) {
//            imageView.layoutParams.height = (imageView.width / item.aspectRatio).toInt()
//            imageView.requestLayout()

            imageView.load(item.uri) {
                crossfade(true)
                placeholder(R.drawable.ic_delete)
                error(R.drawable.ic_error)
            }
        }
    }
}

class SnowbirdMediaDiffCallback : DiffUtil.ItemCallback<SnowbirdMediaItem>() {
    override fun areItemsTheSame(oldItem: SnowbirdMediaItem, newItem: SnowbirdMediaItem): Boolean {
        return oldItem.uri == newItem.uri
    }

    override fun areContentsTheSame(oldItem: SnowbirdMediaItem, newItem: SnowbirdMediaItem): Boolean {
        return oldItem == newItem
    }
}