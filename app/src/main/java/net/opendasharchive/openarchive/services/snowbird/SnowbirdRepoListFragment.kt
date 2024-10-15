package net.opendasharchive.openarchive.services.snowbird

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdListReposBinding
import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.extensions.collectLifecycleFlow
import net.opendasharchive.openarchive.util.SpacingItemDecoration
import net.opendasharchive.openarchive.util.Utility
import timber.log.Timber

class SnowbirdRepoListFragment : BaseSnowbirdFragment() {

    private lateinit var viewBinding: FragmentSnowbirdListReposBinding
    private lateinit var adapter: SnowbirdRepoListAdapter
    private lateinit var groupKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            groupKey = it.getString("groupKey", "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentSnowbirdListReposBinding.inflate(inflater)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSwipeRefresh()
        setupViewModel()
    }

    private fun setupViewModel() {
        adapter = SnowbirdRepoListAdapter { repoId ->
            Timber.d("Click!!")
            findNavController().navigate(SnowbirdRepoListFragmentDirections.navigateToSnowbirdListFilesScreen(repoId))
        }

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.list_item_spacing)
        viewBinding.repoList.addItemDecoration(SpacingItemDecoration(spacingInPixels))

        viewBinding.repoList.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.repoList.adapter = adapter

        viewLifecycleOwner.collectLifecycleFlow(snowbirdRepoViewModel.repos) {
            adapter.submitList(it)
        }

        viewBinding.repoList.setEmptyView(R.layout.view_empty_state)

        viewLifecycleOwner.collectLifecycleFlow(snowbirdRepoViewModel.error) { error ->
            error?.let { handle(it) }
        }

        viewLifecycleOwner.collectLifecycleFlow(snowbirdRepoViewModel.repoState) { repoState ->
            handleRepoUpdate(repoState)
        }

        viewLifecycleOwner.collectLifecycleFlow(snowbirdRepoViewModel.isProcessing) { isProcessing ->
            handleProcessingStatus(isProcessing)
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }

        lifecycleScope.launch {
            snowbirdRepoViewModel.fetchRepos(groupKey, forceRefresh = false)
        }
    }

    private fun handleRepoUpdate(repoState: SnowbirdRepoViewModel.RepoState) {
        adapter.submitList(repoState.repos)

        if (repoState.updateCount > 1 && repoState.repos.isEmpty()) {
            Utility.showMaterialMessage(
                requireContext(),
                title = "Info",
                message = "No new repositories found.")
        }
    }

    private fun handle(error: SnowbirdError) {
        viewBinding.swipeRefreshLayout.isRefreshing = false
        Toast.makeText(requireContext(), error.friendlyMessage, Toast.LENGTH_SHORT).show()
    }

    private fun setupSwipeRefresh() {
        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                snowbirdRepoViewModel.fetchRepos(groupKey, forceRefresh = true)
            }
        }

        viewBinding.swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary, R.color.colorPrimaryDark
        )
    }
}