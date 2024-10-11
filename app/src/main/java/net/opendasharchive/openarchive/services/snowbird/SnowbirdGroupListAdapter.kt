package net.opendasharchive.openarchive.services.snowbird

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.OneLineRowBinding
import net.opendasharchive.openarchive.db.SnowbirdGroup
import net.opendasharchive.openarchive.db.shortHash
import net.opendasharchive.openarchive.util.extensions.scaled
import java.lang.ref.WeakReference

interface SnowbirdGroupsAdapterListener {
    fun groupSelected(group: SnowbirdGroup)
}

class SnowbirdGroupsAdapter(listener: ((String) -> Unit)? = null)
    : ListAdapter<SnowbirdGroup, SnowbirdGroupsAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(private val binding: OneLineRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: SnowbirdGroup?) {
            if (group == null) {
                return
            }

            val context = binding.button.context

            binding.button.setLeftIcon(ContextCompat.getDrawable(context, R.drawable.snowbird)?.scaled(40, context))
            binding.button.setBackgroundResource(R.drawable.button_outlined_ripple)
            binding.button.setTitle(group.name ?: "No name provided")
            binding.button.setSubTitle(group.shortHash())

            binding.button.setOnClickListener {
                mListener.get()?.invoke(group.key)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SnowbirdGroup>() {
            override fun areItemsTheSame(oldItem: SnowbirdGroup, newItem: SnowbirdGroup): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: SnowbirdGroup, newItem: SnowbirdGroup): Boolean {
                return oldItem.key == newItem.key
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = getItem(position)
        holder.bind(group)
    }
}