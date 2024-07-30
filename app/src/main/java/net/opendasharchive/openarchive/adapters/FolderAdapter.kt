package net.opendasharchive.openarchive.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.RvSimpleRowBinding
import net.opendasharchive.openarchive.db.Folder
import java.lang.ref.WeakReference

interface FolderAdapterListener {

    fun folderClicked(folder: Folder)

    fun getSelectedProject(): Folder?
}

class FolderAdapter(listener: FolderAdapterListener?) : ListAdapter<Folder, FolderAdapter.ViewHolder>(DIFF_CALLBACK), FolderAdapterListener {

    class ViewHolder(private val binding: RvSimpleRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: WeakReference<FolderAdapterListener>?, folder: Folder?) {
            binding.rvTitle.text = folder?.description

            if (listener?.get()?.getSelectedProject()?.id == folder?.id) {
                val icon = ContextCompat.getDrawable(binding.rvIcon.context,
                    R.drawable.outline_folder_24
                )
                val color = ContextCompat.getColor(binding.rvIcon.context, R.color.colorPrimary)
                icon?.setTint(color)
                binding.rvIcon.setImageDrawable(icon)
            } else {
                val icon = ContextCompat.getDrawable(binding.rvIcon.context,
                    R.drawable.outline_folder_24
                )
                val color = ContextCompat.getColor(binding.rvIcon.context,
                    R.color.colorOnBackground
                )
                icon?.setTint(color)
                binding.rvIcon.setImageDrawable(icon)
            }

            binding.rvTitle.setTextColor(
                getColor(binding.rvTitle.context,
                listener?.get()?.getSelectedProject()?.id == folder?.id)
            )

            if (folder != null) {
                binding.root.setOnClickListener {
                    listener?.get()?.folderClicked(folder)
                }
            }
            else {
                binding.root.setOnClickListener(null)
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

        private var highlightColor: Int? = null
        private var defaultColor: Int? = null

        fun getColor(context: Context, highlight: Boolean): Int {
            if (highlight) {
                var color = highlightColor

                if (color != null) return color

                color = ContextCompat.getColor(context, R.color.colorPrimary)
                highlightColor = color

                return color
            }

            var color = defaultColor

            if (color != null) return color

            val textview = TextView(context)
            color = textview.currentTextColor
            defaultColor = color

            return color
        }
    }

    private val mListener: WeakReference<FolderAdapterListener> = WeakReference(listener)

    private var mLastSelected: Folder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RvSimpleRowBinding.inflate(LayoutInflater.from(parent.context),
            parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = getItem(position)

        holder.bind(WeakReference(this), folder)
    }

    fun update(folders: List<Folder>) {
        notifyItemChanged(getIndex(mLastSelected))

        submitList(folders)
    }

    override fun folderClicked(folder: Folder) {
        notifyItemChanged(getIndex(getSelectedProject()))
        notifyItemChanged(getIndex(folder))

        mListener.get()?.folderClicked(folder)
    }

    override fun getSelectedProject(): Folder? {
        mLastSelected = mListener.get()?.getSelectedProject()

        return mLastSelected
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