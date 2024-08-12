package net.opendasharchive.openarchive.services.veilid

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.OneLineBackendRowBinding
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.util.extensions.tint
import java.lang.ref.WeakReference

interface VeilidFolderAdapterListener {
    fun folderSelected(folder: Folder)
}

class VeilidFolderAdapter(listener: VeilidFolderAdapterListener?)
    : ListAdapter<Folder, VeilidFolderAdapter.ViewHolder>(DIFF_CALLBACK), VeilidFolderAdapterListener {

    class ViewHolder(private val binding: OneLineBackendRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: WeakReference<VeilidFolderAdapterListener>?, folder: Folder?) {
            if (folder == null) {
                return
            }

            val context = binding.button.context

            val color = ContextCompat.getColor(context, R.color.colorPrimary)
            val icon = ContextCompat.getDrawable(context, R.drawable.baseline_folder_24)?.tint(color)

            binding.button.icon = icon
            binding.button.text = folder.description

            binding.button.setOnClickListener {
                listener?.get()?.folderSelected(folder)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Folder>() {
            override fun areItemsTheSame(oldItem: Folder, newItem: Folder): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Folder, newItem: Folder): Boolean {
                return oldItem.description == newItem.description
            }
        }
    }

    private val mListener = WeakReference(listener)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            OneLineBackendRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = getItem(position)
        holder.bind(WeakReference(this), folder)
    }

    override fun folderSelected(folder: Folder) {
        mListener.get()?.folderSelected(folder)
    }
}