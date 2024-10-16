package net.opendasharchive.openarchive.services.snowbird

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdListGroupsBinding
import net.opendasharchive.openarchive.db.SnowbirdGroup
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

        initializeViewModelObservers()
    }

    private fun initializeViewModelObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { snowbirdGroupViewModel.isProcessing.collect { isProcessing -> handleLoadingStatus(isProcessing) } }
                launch { snowbirdGroupViewModel.groupState.collect { state -> handleGroupStateUpdate(state) } }
            }
        }
    }

    private fun handleGroupStateUpdate(state: SnowbirdGroupViewModel.GroupState) {
        when (state) {
            is SnowbirdGroupViewModel.GroupState.Idle -> { /* Initial state */ }
            is SnowbirdGroupViewModel.GroupState.Loading -> handleLoadingStatus(true)
            is SnowbirdGroupViewModel.GroupState.MultiGroupSuccess -> handleGroupsFetched(state.groups)
            is SnowbirdGroupViewModel.GroupState.Error -> handleError(state.error)
            else -> Unit
        }
    }

    private fun handleGroupsFetched(groups: List<SnowbirdGroup>) {
        adapter.submitList(groups)
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