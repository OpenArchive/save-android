package net.opendasharchive.openarchive.services.snowbird

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.OneLineRowBinding
import net.opendasharchive.openarchive.util.extensions.scaled
import java.lang.ref.WeakReference

interface SnowbirdGroupsAdapterListener {
    fun groupSelected(group: SnowbirdGroup)
}

class SnowbirdGroupsAdapter(listener: SnowbirdGroupsAdapterListener?)
    : ListAdapter<SnowbirdGroup, SnowbirdGroupsAdapter.ViewHolder>(DIFF_CALLBACK), SnowbirdGroupsAdapterListener {

    class ViewHolder(private val binding: OneLineRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: WeakReference<SnowbirdGroupsAdapterListener>?, group: SnowbirdGroup?) {
            if (group == null) {
                return
            }

            val context = binding.button.context

            binding.button.setLeftIcon(ContextCompat.getDrawable(context, R.drawable.snowbird)?.scaled(40, context))
            binding.button.setBackgroundResource(R.drawable.button_outlined_ripple)

            binding.button.setTitle(group.name)

            binding.button.setOnClickListener {
                listener?.get()?.groupSelected(group)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SnowbirdGroup>() {
            override fun areItemsTheSame(oldItem: SnowbirdGroup, newItem: SnowbirdGroup): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: SnowbirdGroup, newItem: SnowbirdGroup): Boolean {
                return oldItem.name == newItem.name
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
        holder.bind(WeakReference(this), group)
    }

    override fun groupSelected(group: SnowbirdGroup) {
        mListener.get()?.groupSelected(group)
    }
}