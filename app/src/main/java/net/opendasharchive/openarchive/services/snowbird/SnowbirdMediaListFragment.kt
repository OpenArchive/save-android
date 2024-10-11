package net.opendasharchive.openarchive.services.snowbird

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdListReposBinding
import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.extensions.collectLifecycleFlow
import net.opendasharchive.openarchive.util.SpacingItemDecoration
import timber.log.Timber

class SnowbirdMediaListFragment : BaseSnowbirdFragment() {

    private lateinit var viewBinding: FragmentSnowbirdListReposBinding
    private lateinit var adapter: SnowbirdMediaListAdapter
    private lateinit var repoId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            repoId = it.getString("repoId", "")
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
        adapter = SnowbirdMediaListAdapter { fileId ->
            Timber.d("Cleeeck!")
        }

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.list_item_spacing)
        viewBinding.repoList.addItemDecoration(SpacingItemDecoration(spacingInPixels))

        viewBinding.repoList.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.repoList.adapter = adapter

//        viewLifecycleOwner.collectLifecycleFlow(snowbirdRepoViewModel.repos) {
//            adapter.submitList(it)
//        }

        viewBinding.repoList.setEmptyView(R.layout.view_empty_state)

        viewLifecycleOwner.collectLifecycleFlow(snowbirdRepoViewModel.error) { error ->
            error?.let { handle(it) }
        }

        viewLifecycleOwner.collectLifecycleFlow(snowbirdRepoViewModel.isProcessing) { isProcessing ->
            handleProcessingStatus(isProcessing)
            viewBinding.swipeRefreshLayout.isRefreshing = false
        }

        snowbirdRepoViewModel.fetchRepos(repoId, forceRefresh = false)
    }

    private fun handle(error: SnowbirdError) {
        viewBinding.swipeRefreshLayout.isRefreshing = false
        Toast.makeText(requireContext(), error.friendlyMessage, Toast.LENGTH_SHORT).show()
    }

    private fun setupSwipeRefresh() {
        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            snowbirdRepoViewModel.fetchRepos(repoId, forceRefresh = true)
        }

        viewBinding.swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary, R.color.colorPrimaryDark
        )
    }
}