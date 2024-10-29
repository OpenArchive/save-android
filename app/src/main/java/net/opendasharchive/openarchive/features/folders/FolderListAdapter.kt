package net.opendasharchive.openarchive.features.folders

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.OneLineRowBinding
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.db.FolderRepository
import net.opendasharchive.openarchive.features.backends.ItemAction
import java.text.SimpleDateFormat

class FolderListAdapter(
    private val folderRepo: FolderRepository,
    private val onItemAction: ((View, Folder, ItemAction) -> Unit)? = null
) : ListAdapter<Folder, FolderListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(private val binding: OneLineRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(folder: Folder?) {
            if (folder == null) { return }

            val context = binding.root.context

            binding.button.setLeftIcon(ContextCompat.getDrawable(context, R.drawable.outline_folder_24))
            binding.button.setTitle(folder.name)
            binding.button.setSubTitle("Created: " + FolderListAdapter.formatter.format(folder.created))
            binding.button.setBackgroundResource(R.drawable.button_outlined_ripple)

            binding.button.setOnClickListener {
                onItemAction?.invoke(binding.root, folder, ItemAction.SELECTED)
            }

            changeStrokeColor(binding.button, 1, ContextCompat.getColor(itemView.context, R.color.c23_grey))

            folder.name?.let { name ->
                if (name.isNotEmpty()) {
                    if (folderRepo.currentFolder.value == folder) {
                        changeStrokeColor(binding.button, 3, ContextCompat.getColor(itemView.context, R.color.c23_teal))
                    }
                }
            }
        }

        private fun changeStrokeColor(view: View, width: Int, color: Int) {
            val drawable = view.background as? GradientDrawable
            drawable?.setStroke(width, color)
        }
    }

    companion object {
        private val formatter = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM)

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Folder>() {
            override fun areItemsTheSame(oldItem: Folder, newItem: Folder): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Folder, newItem: Folder): Boolean {
                return oldItem.name == newItem.name
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(OneLineRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = getItem(position)
        holder.bind(folder)
    }

    fun update(folders: List<Folder>) {
        submitList(folders)
    }

    private fun getIndex(folder: Folder?): Int {
        return if (folder == null) {
            -1
        }
        else {
            currentList.indexOf(folder)
        }
    }
}