package net.opendasharchive.openarchive.services.snowbird

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdListMediaBinding
import net.opendasharchive.openarchive.db.SnowbirdMediaItem
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SnowbirdMediaListFragment : BaseSnowbirdFragment() {

    private lateinit var viewBinding: FragmentSnowbirdListMediaBinding
    private val snowbirdMediaViewModel: SnowbirdMediaViewModel by viewModel()
    private lateinit var adapter: SnowbirdMediaListAdapter
    private lateinit var repoKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            repoKey = it.getString("repoId", "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentSnowbirdListMediaBinding.inflate(inflater)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSwipeRefresh()
        setupViewModel()
        setupMenu()
        initializeViewModelObservers()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_snowbird, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_add -> {
                        Timber.d("Adde!")
                        openFilePicker()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private val getMultipleContents = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        handleSelectedFiles(uris)
    }

    private fun handleAudio(uri: Uri) {
        handleMedia(uri)
    }

    private fun handleImage(uri: Uri) {
        handleMedia(uri)
    }

    private fun handleVideo(uri: Uri) {
        handleMedia(uri)
    }

    private fun handleMedia(uri: Uri) {
        Timber.d("handle medisa")
    }

    private fun handleSelectedFiles(uris: List<Uri>) {
        if (uris.isNotEmpty()) {
            for (uri in uris) {
                val mimeType = requireContext().contentResolver.getType(uri)
                when {
                    mimeType?.startsWith("image/") == true -> handleImage(uri)
                    mimeType?.startsWith("video/") == true -> handleVideo(uri)
                    mimeType?.startsWith("audio/") == true -> handleAudio(uri)
                    else -> {
                        Timber.d("Unknown type picked: $mimeType")
                    }
                }
            }
        } else {
            Timber.d("No images selected")
        }
    }

    private fun openFilePicker() {
        getMultipleContents.launch("*/*")
    }

    private fun setupRecyclerView() {
        viewBinding.snowbirdMediaRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = adapter
            setHasFixedSize(true)
        }
    }

    private fun setupViewModel() {
        adapter = SnowbirdMediaListAdapter { fileId ->
            Timber.d("Cleeeck!")
        }

        viewBinding.snowbirdMediaRecyclerView.setEmptyView(R.layout.view_empty_state)
    }

    private fun handleMediaStateUpdate(state: SnowbirdMediaViewModel.MediaState) {
        when (state) {
            is SnowbirdMediaViewModel.MediaState.Idle -> { /* Initial state */ }
            is SnowbirdMediaViewModel.MediaState.Loading -> handleLoadingStatus(true)
            is SnowbirdMediaViewModel.MediaState.Success -> handleMediaUpdate(state.media)
            is SnowbirdMediaViewModel.MediaState.Error -> handleError(state.error)
            else -> Unit
        }
    }

    private fun handleMediaUpdate(media: List<SnowbirdMediaItem>) {
        adapter.submitList(media)
    }

    private fun initializeViewModelObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { snowbirdMediaViewModel.mediaState.collect { state -> handleMediaStateUpdate(state) } }
                launch { snowbirdMediaViewModel.fetchMedia("123", repoKey, forceRefresh = false) }
            }
        }
    }

    private fun setupSwipeRefresh() {
        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                snowbirdRepoViewModel.fetchRepos(repoKey, forceRefresh = true)
            }
        }

        viewBinding.swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary, R.color.colorPrimaryDark
        )
    }
}