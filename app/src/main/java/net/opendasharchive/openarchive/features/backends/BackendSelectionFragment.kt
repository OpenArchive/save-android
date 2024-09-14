package net.opendasharchive.openarchive.features.backends

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentBackendSelectionBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.folders.FolderViewModel
import net.opendasharchive.openarchive.services.gdrive.GDriveConduit
import net.opendasharchive.openarchive.util.SpacingItemDecoration
import net.opendasharchive.openarchive.util.extensions.toggle
import timber.log.Timber
import java.util.Date

class BackendSelectionFragment : Fragment() {

    private lateinit var viewBinding: FragmentBackendSelectionBinding
    private lateinit var recyclerView: RecyclerView
    private val backendViewModel: BackendViewModel by activityViewModels()
    private val folderViewModel: FolderViewModel by activityViewModels()
    private val viewModel: BackendListViewModel by viewModels() {
        BackendListViewModelFactory(BackendListViewModel.Companion.Filter.CONNECTED)
    }
    private val adapter = BackendAdapter { view, backend, action ->
        when (action) {
            ItemAction.SELECTED -> connectToExistingBackend(backend)
            ItemAction.LONG_PRESSED -> showPopupMenu(view, backend)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentBackendSelectionBinding.inflate(inflater)

        createBackendList()

        viewBinding.connectNewMediaServerButton.setOnClickListener {
            findNavController().navigate(BackendSelectionFragmentDirections.navigateToConnectNewBackendScreen())
        }

        viewBinding.progressBar.toggle(false)

        return viewBinding.root
    }

//        val arrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_right)
//        arrow?.tint(ContextCompat.getColor(this, R.color.colorPrimary))

//        mBinding.newFolder.setDrawable(arrow, Position.End, tint = false)
//        mBinding.browseFolders.setDrawable(arrow, Position.End, tint = false)

        // We cannot browse the Internet Archive. Directly forward to creating a project,
        // as it doesn't make sense to show a one-option menu.
//        if (Folder.current?.backend?.tType == Backend.Type.INTERNET_ARCHIVE) {
//            mBinding.browseFolders.hide()
//            setFolder(false)
//            finish()
//        }

    private fun editBackend(backend: Backend) {

    }

    private fun deleteBackend(backend: Backend) {
        val transition = AutoTransition().apply {
            duration = 250
            addTarget(recyclerView)
        }

        TransitionManager.beginDelayedTransition(recyclerView.parent as ViewGroup, transition)

        viewModel.deleteBackend(backend)
    }

    private fun connectToExistingBackend(backend: Backend) {
        viewBinding.progressBar.toggle(true)

        backendViewModel.updateBackend { backend }

        Timber.d("Working folder = ${folderViewModel.folder.value}")

        CoroutineScope(Dispatchers.IO).launch {
            syncBackend(requireContext(), backend)

            MainScope().launch {
                findNavController().navigate(BackendSelectionFragmentDirections.navigateToCreateNewFolderScreen())
            }
        }
    }

    // TODO: Refactor this. Copied from Backend.kt
    //
    private fun syncBackend(context: Context, backend: Backend): Int {
        Timber.d("Syncing folders for ${backend.friendlyName}")

        val numFolders = when (backend.tType) {
            Backend.Type.GDRIVE -> syncGDrive(context, backend)
            else -> 0
        }

        backend.lastSyncDate = Date()
        backend.save()

        Timber.d("Got $numFolders folders")

        return numFolders
    }

    private fun syncGDrive(context: Context, backend: Backend): Int {
        val folders = GDriveConduit.listFoldersInRoot(GDriveConduit.getDrive(context), backend)

        folders.forEach { folder ->
            if (folder.doesNotExist()) {
                Timber.d("Syncing ${folder.name}")
                folder.save()
            }
        }

        return folders.size
    }

    private fun createBackendList() {
        recyclerView = viewBinding.backendList

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.list_item_spacing)
        viewBinding.backendList.addItemDecoration(SpacingItemDecoration(spacingInPixels))

        viewBinding.backendList.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.backendList.adapter = adapter

        viewModel.backends.observe(viewLifecycleOwner) { backends ->
            backends.forEach { backend ->
                Timber.d("Backend = ${backend.id} $backend")
            }
            adapter.submitList(backends)
        }
    }

    private fun showPopupMenu(view: View, backend: Backend) {
        PopupMenu(view.context, view).apply {
            menuInflater.inflate(R.menu.menu_backend_context, menu)

            if (backend.isCurrent) {
                menu.findItem(R.id.menu_remove).isVisible = false
            }

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_edit -> {
                        editBackend(backend)
                        true
                    }

                    R.id.menu_remove -> {
                        deleteBackend(backend)
                        true
                    }

                    else -> return@setOnMenuItemClickListener false
                }
            }
            show()
        }
    }

//    private fun removeInternetArchive() {
//        val backend = Backend.get(type = Backend.Type.INTERNET_ARCHIVE).firstOrNull()
//
//        if (backend != null) {
//            AlertHelper.show(
//                this,
//                R.string.are_you_sure_you_want_to_remove_this_server_from_the_app,
//                R.string.remove_from_app,
//                buttons = listOf(
//                    AlertHelper.positiveButton(R.string.remove) { _, _ ->
//                        backend.delete()
//                    },
//                    AlertHelper.negativeButton()
//                )
//            )
//        } else {
//            Timber.d("Unable to find backend.")
//        }
//    }
}