package net.opendasharchive.openarchive.services.snowbird

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.TwoLineBackendRowBinding
import timber.log.Timber

typealias OnMockDataItemCallback = (item: MockDataItem) -> Unit

class MockGroupAdapter(private val onItemClick: OnMockDataItemCallback,) : ListAdapter<MockDataItem, MockGroupAdapter.MockGroupViewHolder>(MockGroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MockGroupViewHolder {
        val binding = TwoLineBackendRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MockGroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MockGroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MockGroupViewHolder(private val binding: TwoLineBackendRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mockDataItem: MockDataItem) {
            binding.spaceIcon.setImageResource(R.drawable.circular_icon)
            binding.spaceLabel.text = mockDataItem.title
            binding.spaceSublabel.text = mockDataItem.description

            binding.root.setOnClickListener {
                Timber.d("Click!")
                onItemClick.invoke((mockDataItem))
            }
        }
    }

    class MockGroupDiffCallback : DiffUtil.ItemCallback<MockDataItem>() {
        override fun areItemsTheSame(oldItem: MockDataItem, newItem: MockDataItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MockDataItem, newItem: MockDataItem): Boolean {
            return oldItem == newItem
        }
    }
}