package net.opendasharchive.openarchive.services.snowbird

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdListGroupsBinding
import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.db.SnowbirdGroup
import net.opendasharchive.openarchive.extensions.collectLifecycleFlow
import net.opendasharchive.openarchive.services.CommonServiceFragment.Companion.RESP_CREATED
import net.opendasharchive.openarchive.util.SpacingItemDecoration
import net.opendasharchive.openarchive.util.Utility

class SnowbirdGroupListFragment : BaseSnowbirdFragment(), SnowbirdGroupsAdapterListener {

    private lateinit var viewBinding: FragmentSnowbirdListGroupsBinding
    private lateinit var adapter: SnowbirdGroupsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentSnowbirdListGroupsBinding.inflate(inflater)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSwipeRefresh()
        setupViewModel()

        snowbirdGroupViewModel.fetchGroups()
    }

    private fun setupSwipeRefresh() {
        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            snowbirdGroupViewModel.fetchGroups(true)
        }

        viewBinding.swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorPrimaryDark
        )
    }

    private fun setupViewModel() {
        adapter = SnowbirdGroupsAdapter { groupId ->
             findNavController().navigate(SnowbirdGroupListFragmentDirections.navigateToSnowbirdListReposScreen(groupId))
        }

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.list_item_spacing)
        viewBinding.groupList.addItemDecoration(SpacingItemDecoration(spacingInPixels))

        viewBinding.groupList.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.groupList.adapter = adapter

        viewBinding.groupList.setEmptyView(R.layout.view_empty_state)

        viewLifecycleOwner.collectLifecycleFlow(snowbirdGroupViewModel.error) { error ->
            error?.let { handle(it) }
        }

        viewLifecycleOwner.collectLifecycleFlow(snowbirdGroupViewModel.groupState) { groupState ->
            handleGroupUpdate(groupState)
        }

        viewLifecycleOwner.collectLifecycleFlow(snowbirdGroupViewModel.isProcessing) { isProcessing ->
            handleProcessingStatus(isProcessing)
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun handle(error: SnowbirdError) {
        viewBinding.swipeRefreshLayout.isRefreshing = false
        Toast.makeText(requireContext(), error.friendlyMessage, Toast.LENGTH_SHORT).show()
    }

    private fun handleGroupUpdate(groupState: SnowbirdGroupViewModel.GroupState) {
        adapter.submitList(groupState.groups)

        if (groupState.updateCount > 0 && groupState.groups.isEmpty()) {
            Utility.showMaterialMessage(
                requireContext(),
                title = "Info",
                message = "No new groups found.")
        }
    }

    private fun showSuccess() {
        Utility.showMaterialMessage(
            requireContext(),
            "Woo!",
            "New backend was successfully created."
        ) {
            setFragmentResult(RESP_CREATED, bundleOf())
        }
    }

    override fun groupSelected(group: SnowbirdGroup) {
        showSuccess()
    }
}