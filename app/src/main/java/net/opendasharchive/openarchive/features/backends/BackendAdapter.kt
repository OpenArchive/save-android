package net.opendasharchive.openarchive.features.backends

import android.graphics.drawable.GradientDrawable
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.OneLineRowBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.util.extensions.scaled
import timber.log.Timber

interface BackendAdapterListener {
    fun onItemAction(backend: Backend)
}

enum class ItemAction {
    SELECTED, REQUEST_REMOVE, REQUEST_EDIT
}

class BackendAdapter(private val onItemAction: ((Backend, ItemAction) -> Unit)? = null) : ListAdapter<Backend, BackendAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(private val binding: OneLineRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(backend: Backend?) {
            if (backend == null) { return }

            val context = binding.root.context

            binding.button.setLeftIcon(backend.getAvatar(context)?.scaled(40, context))
            binding.button.setTitle(backend.friendlyName)
            binding.button.setBackgroundResource(R.drawable.button_outlined)

            addAccountInfo(binding, backend)

            binding.button.setOnClickListener {
                onItemAction?.invoke(backend, ItemAction.SELECTED)
            }

            if (backend.id != null && backend.name.isNotEmpty()) {
                if (backend.isCurrent) {
                    Timber.d("Is current ${backend.id}")
                    changeStrokeColor(binding.button, 3, ContextCompat.getColor(itemView.context, R.color.c23_teal))
                } else {
                    changeStrokeColor(binding.button, 1, ContextCompat.getColor(itemView.context, R.color.c23_grey))
                }

                binding.button.setOnLongClickListener { view ->
                    Timber.d("LONG PRESS!")
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    showPopupMenu(view, backend)
                    true
                }
            }
        }

        private fun changeStrokeColor(view: View, width: Int, color: Int) {
            val drawable = view.background as? GradientDrawable
            drawable?.setStroke(width, color)
        }

        private fun addAccountInfo(binding: OneLineRowBinding, backend: Backend) {
            if (backend.tType == Backend.Type.GDRIVE) {
                if (backend.displayname.isNotEmpty()) {
                    binding.button.setSubTitle(backend.displayname)
                    return
                }
            }
        }

        private fun showPopupMenu(view: View, backend: Backend) {
            PopupMenu(view.context, view).apply {
                menuInflater.inflate(R.menu.menu_backend_context, menu)

                if (backend.isCurrent) {
                    menu.findItem(R.id.menu_delete).isVisible = false
                }

                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_edit -> {
                            onItemAction?.invoke(backend, ItemAction.REQUEST_EDIT)
                            true
                        }

                        R.id.menu_delete -> {
                            onItemAction?.invoke(backend, ItemAction.REQUEST_REMOVE)
                            true
                        }

                        else -> return@setOnMenuItemClickListener false
                    }
                }
                show()
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Backend>() {
            override fun areItemsTheSame(oldItem: Backend, newItem: Backend): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Backend, newItem: Backend): Boolean {
                return oldItem.friendlyName == newItem.friendlyName
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(OneLineRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val backend = getItem(position)
        holder.bind(backend)
    }

    fun update(backends: List<Backend>) {
        submitList(backends)
    }

    private fun getIndex(backend: Backend?): Int {
        return if (backend == null) {
            -1
        }
        else {
            currentList.indexOf(backend)
        }
    }
}