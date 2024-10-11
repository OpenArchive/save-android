package net.opendasharchive.openarchive.services.snowbird

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.OneLineRowBinding
import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.util.extensions.scaled
import java.lang.ref.WeakReference

class SnowbirdMediaListAdapter(listener: ((Long) -> Unit)? = null)
    : ListAdapter<Media, SnowbirdMediaListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(private val binding: OneLineRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(media: Media?) {
            if (media == null) {
                return
            }

            val context = binding.button.context

            binding.button.setLeftIcon(ContextCompat.getDrawable(context, R.drawable.snowbird)?.scaled(40, context))
            binding.button.setBackgroundResource(R.drawable.button_outlined_ripple)
            binding.button.setTitle(media.title)
            binding.button.setSubTitle(media.description)

            binding.button.setOnClickListener {
                mListener.get()?.invoke(media.id)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Media>() {
            override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
                return oldItem.fileUri == newItem.fileUri
            }
        }
    }

    private val mListener = WeakReference(listener)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            OneLineRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SnowbirdMediaListAdapter.ViewHolder, position: Int) {
        val repo = getItem(position)
        holder.bind(repo)
    }
}