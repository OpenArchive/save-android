package net.opendasharchive.openarchive.features.backends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.opendasharchive.openarchive.databinding.OneLineBackendRowBinding
import net.opendasharchive.openarchive.db.Backend
import java.lang.ref.WeakReference

interface BackendAdapterListener {
    fun backendClicked(backend: Backend)
}

class BackendAdapter(listener: BackendAdapterListener?) : ListAdapter<Backend, BackendAdapter.ViewHolder>(DIFF_CALLBACK), BackendAdapterListener {

    class ViewHolder(private val binding: OneLineBackendRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: WeakReference<BackendAdapterListener>?, backend: Backend?) {
            if (backend == null) { return }

            val context = binding.button.context

            val icon = backend.getAvatar(context) // ?.scaled(32, context)
            binding.button.icon = icon

            binding.button.text = backend.friendlyName

//            binding.backendButton.setTextColor(
//                FolderAdapter.getColor(
//                    binding.backendButton.context,
//                    false
//                    // listener?.get()?.getSelectedBackend()?.id == backend?.id
//                )
//            )

            binding.button.setOnClickListener {
                listener?.get()?.backendClicked(backend)
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

    private val mListener = WeakReference(listener)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(OneLineBackendRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val backend = getItem(position)
        holder.bind(WeakReference(this), backend)
    }

    fun update(backends: List<Backend>) {
//        notifyItemChanged(getIndex(mLastSelected))
//
//        @Suppress("NAME_SHADOWING")
//        val backends = backends.toMutableList()
//        backends.add(Backend(ADD_BACKEND_ID))
        submitList(backends)
    }

    override fun backendClicked(backend: Backend) {
//        notifyItemChanged(getIndex(getSelectedBackend()))
        notifyItemChanged(getIndex(backend))

        mListener.get()?.backendClicked(backend)
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