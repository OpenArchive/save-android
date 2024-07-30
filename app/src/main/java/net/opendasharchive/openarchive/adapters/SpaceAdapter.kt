package net.opendasharchive.openarchive.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.RvSimpleRowBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.util.extensions.scaled
import java.lang.ref.WeakReference

interface SpaceAdapterListener {

    fun spaceClicked(backend: Backend)

    fun addSpaceClicked()

    fun getSelectedSpace(): Backend?
}

class SpaceAdapter(listener: SpaceAdapterListener?) : ListAdapter<Backend, SpaceAdapter.ViewHolder>(DIFF_CALLBACK), SpaceAdapterListener {

    class ViewHolder(private val binding: RvSimpleRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: WeakReference<SpaceAdapterListener>?, backend: Backend?) {
            val context = binding.rvTitle.context

            if (listener?.get()?.getSelectedSpace()?.id == backend?.id) {
                val icon = Backend.current?.getAvatar(context)?.scaled(32, context)
                val color = ContextCompat.getColor(binding.rvIcon.context,
                    R.color.colorOnBackground
                )
                icon?.setTint(color)
                binding.rvIcon.setImageDrawable(icon)
            }
            else {
                val icon = backend?.getAvatar(context)?.scaled(32, context)
                val color = ContextCompat.getColor(binding.rvIcon.context,
                    R.color.colorOnBackground
                )
                icon?.setTint(color)
                binding.rvIcon.setImageDrawable(icon)
            }

            if (backend?.type == ADD_SPACE_ID) {
                binding.rvTitle.text = context.getText(R.string.add_another_account)

                val icon = ContextCompat.getDrawable(binding.rvIcon.context, R.drawable.ic_add)
                binding.rvIcon.setImageDrawable(icon)

                binding.root.setOnClickListener {
                    listener?.get()?.addSpaceClicked()
                }

                return
            } else {
                binding.rvTitle.text = backend?.friendlyName
            }

            binding.rvTitle.setTextColor(
                FolderAdapter.getColor(
                    binding.rvTitle.context,
                    listener?.get()?.getSelectedSpace()?.id == backend?.id
                )
            )

            if (backend != null) {
                binding.root.setOnClickListener {
                    listener?.get()?.spaceClicked(backend)
                }
            }
            else {
                binding.root.setOnClickListener(null)
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

        private const val ADD_SPACE_ID: Long = -1
    }

    private val mListener = WeakReference(listener)

    private var mLastSelected: Backend? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RvSimpleRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val space = getItem(position)

        holder.bind(WeakReference(this), space)
    }

    fun update(backends: List<Backend>) {
        notifyItemChanged(getIndex(mLastSelected))

        @Suppress("NAME_SHADOWING")
        val spaces = backends.toMutableList()
        spaces.add(Backend(ADD_SPACE_ID))

        submitList(spaces)
    }

    override fun spaceClicked(backend: Backend) {
        notifyItemChanged(getIndex(getSelectedSpace()))
        notifyItemChanged(getIndex(backend))

        mListener.get()?.spaceClicked(backend)
    }

    override fun addSpaceClicked() {
        mListener.get()?.addSpaceClicked()
    }

    override fun getSelectedSpace(): Backend? {
        mLastSelected = mListener.get()?.getSelectedSpace()

        return mLastSelected
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