package net.opendasharchive.openarchive.features.backends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentBackendSelectionBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.folders.NewFolderDataViewModel
import net.opendasharchive.openarchive.features.folders.NewFolderNavigationViewModel
import net.opendasharchive.openarchive.util.SpacingItemDecoration
import timber.log.Timber

class BackendSelectionFragment : Fragment() {

    private lateinit var viewBinding: FragmentBackendSelectionBinding
    private lateinit var recyclerView: RecyclerView
    private val viewModel: BackendViewModel by viewModels() {
        BackendViewModelFactory(BackendViewModel.Companion.Filter.CONNECTED)
    }
    private val adapter = BackendAdapter { backend, action ->
        when (action) {
            ItemAction.REQUEST_EDIT -> editBackend(backend)
            ItemAction.REQUEST_REMOVE -> deleteBackend(backend)
            ItemAction.SELECTED -> connectToExistingBackend(backend)
        }
    }
    private val newFolderDataViewModel: NewFolderDataViewModel by activityViewModels()
    private val newFolderNavigationViewModel: NewFolderNavigationViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentBackendSelectionBinding.inflate(inflater)

        createBackendList()

        viewBinding.connectNewMediaServerButton.setOnClickListener {
            findNavController().navigate(BackendSelectionFragmentDirections.navigationSegueConnectToNewBackend())
        }

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
        newFolderDataViewModel.updateFolder { folder ->
            folder.copy(backend = backend)
        }

        findNavController().navigate(BackendSelectionFragmentDirections.navigationSegueCreateNewFolder())
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