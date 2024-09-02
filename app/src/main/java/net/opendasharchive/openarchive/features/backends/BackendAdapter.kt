package net.opendasharchive.openarchive.features.backends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.OneLineRowBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.util.extensions.scaled
import java.lang.ref.WeakReference

interface BackendAdapterListener {
    fun onBackendClicked(backend: Backend)
}

class BackendAdapter(listener: BackendAdapterListener?) : ListAdapter<Backend, BackendAdapter.ViewHolder>(DIFF_CALLBACK), BackendAdapterListener {

    class ViewHolder(private val binding: OneLineRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: WeakReference<BackendAdapterListener>?, backend: Backend?) {
            if (backend == null) { return }

            val context = binding.root.context

            binding.button.setLeftIcon(backend.getAvatar(context)?.scaled(40, context))
            binding.button.setTitle(backend.friendlyName)
            binding.button.setBackgroundResource(R.drawable.button_outlined)

//            setConnectionStatus(binding, backend)

            binding.button.setOnClickListener {
                listener?.get()?.onBackendClicked(backend)
            }
        }

//        private fun setConnectionStatus(binding: OneLineRowBinding, backend: Backend) {
//            if (backend.tType == Backend.Type.GDRIVE) {
//                if (GDriveConduit.permissionsGranted(binding.root.context)) {
//                    binding.button.setSubTitle("Connected")
//                    return
//                }
//            }
//
//            if (backend.displayname.isEmpty()) {
//                binding.button.setSubTitle("Not connected")
//            } else {
//                binding.button.setSubTitle("Connected")
//            }
//        }
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
        return ViewHolder(OneLineRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val backend = getItem(position)
        holder.bind(WeakReference(this), backend)
    }

    fun update(backends: List<Backend>) {
        submitList(backends)
    }

    override fun onBackendClicked(backend: Backend) {
        mListener.get()?.onBackendClicked(backend)
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