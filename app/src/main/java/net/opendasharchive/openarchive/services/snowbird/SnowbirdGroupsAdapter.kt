package net.opendasharchive.openarchive.services.snowbird

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.opendasharchive.openarchive.databinding.OneLineRowBinding
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

//            val context = binding.button.context

//            val color = ContextCompat.getColor(context, R.color.colorPrimary)
//            val icon = ContextCompat.getDrawable(context, R.drawable.baseline_folder_24)?.tint(color)

//            binding.button.icon = icon
//            binding.button.text = folder.description

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