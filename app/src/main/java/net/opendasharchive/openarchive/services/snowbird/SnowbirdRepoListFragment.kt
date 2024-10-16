package net.opendasharchive.openarchive.services.snowbird

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdListReposBinding
import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.db.SnowbirdRepo
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
        initializeViewModelObservers()
    }

    private fun handleRepoStateUpdate(state: SnowbirdRepoViewModel.RepoState) {
        handleLoadingStatus(false)
        when (state) {
            is SnowbirdRepoViewModel.RepoState.Idle -> { /* Initial state */ }
            is SnowbirdRepoViewModel.RepoState.Loading -> handleLoadingStatus(true)
            is SnowbirdRepoViewModel.RepoState.MultiRepoSuccess -> handleRepoUpdate(state.repos)
            is SnowbirdRepoViewModel.RepoState.Error -> handleError(state.error)
            else -> Unit
        }
    }

    private fun initializeViewModelObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { snowbirdRepoViewModel.repoState.collect { state -> handleRepoStateUpdate(state) } }
                launch { snowbirdRepoViewModel.fetchRepos(groupKey, forceRefresh = false) }
            }
        }
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

        viewBinding.repoList.setEmptyView(R.layout.view_empty_state)
    }

    private fun handleRepoUpdate(repos: List<SnowbirdRepo>) {
        adapter.submitList(repos)

        if (repos.isEmpty()) {
            Utility.showMaterialMessage(
                requireContext(),
                title = "Info",
                message = "No new repositories found.")
        }
    }

    override fun handleError(error: SnowbirdError) {
        super.handleError(error)
        viewBinding.swipeRefreshLayout.isRefreshing = false
    }

    override fun handleLoadingStatus(isLoading: Boolean) {
        super.handleLoadingStatus(isLoading)
        viewBinding.swipeRefreshLayout.isRefreshing = false
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