package net.opendasharchive.openarchive.services.snowbird

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.esafirm.imagepicker.features.ImagePickerLauncher
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdListReposBinding
import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.extensions.collectLifecycleFlow
import net.opendasharchive.openarchive.features.media.Picker.pickMedia
import net.opendasharchive.openarchive.util.SpacingItemDecoration
import timber.log.Timber

class SnowbirdMediaListFragment : BaseSnowbirdFragment() {

    private lateinit var viewBinding: FragmentSnowbirdListReposBinding
    private lateinit var adapter: SnowbirdMediaListAdapter
    private lateinit var repoId: String
    private var mpl: ImagePickerLauncher? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                pickMedia(requireActivity(), mpl!!)
            } else {
                Timber.d("External storage permission denied")
            }
        }

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

//        initializeImagePicker()
        setupSwipeRefresh()
        setupViewModel()
        setupMenu()
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
            // No images were selected
            Timber.d("No images selected")
        }
    }

    private fun openFilePicker() {
        getMultipleContents.launch("*/*")
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

        lifecycleScope.launch {
            snowbirdRepoViewModel.fetchRepos(repoId, forceRefresh = false)
        }
    }

    private fun handle(error: SnowbirdError) {
        viewBinding.swipeRefreshLayout.isRefreshing = false
        Toast.makeText(requireContext(), error.friendlyMessage, Toast.LENGTH_SHORT).show()
    }

    private fun setupSwipeRefresh() {
        viewBinding.swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                snowbirdRepoViewModel.fetchRepos(repoId, forceRefresh = true)
            }
        }

        viewBinding.swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary, R.color.colorPrimaryDark
        )
    }
}