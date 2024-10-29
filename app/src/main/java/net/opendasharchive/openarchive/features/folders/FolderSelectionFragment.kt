package net.opendasharchive.openarchive.features.folders

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
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentFolderSelectionBinding
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.features.backends.BackendViewModel
import net.opendasharchive.openarchive.features.backends.ItemAction
import net.opendasharchive.openarchive.util.SpacingItemDecoration
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class FolderSelectionFragment : Fragment() {

    private lateinit var viewBinding: FragmentFolderSelectionBinding
    private lateinit var recyclerView: RecyclerView
    private val backendViewModel: BackendViewModel by activityViewModels()
    private val folderViewModel: FolderViewModel by activityViewModels()
    private val viewModel: FolderListViewModel by viewModels {
        FolderListViewModelFactory(backendViewModel.backend.value)
    }

    private val adapter: FolderListAdapter by inject {
        parametersOf({ view: View, folder: Folder, action: ItemAction ->
            when (action) {
                ItemAction.SELECTED -> useExistingFolder(folder)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentFolderSelectionBinding.inflate(inflater)

        return viewBinding.root.also {
            viewBinding.folderList.adapter = adapter
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createFolderList()

        viewBinding.createNewFolderButton.setOnClickListener {
            findNavController().navigate(FolderSelectionFragmentDirections.navigateToCreateNewFolderScreen())
        }
    }

    private fun useExistingFolder(folder: Folder) {
        folderViewModel.updateFolder { folder }

        Timber.d("Working folder = ${folderViewModel.folder.value}")

        findNavController().navigate(FolderSelectionFragmentDirections.navigateToSuccessScreen())
    }

    private fun createFolderList() {
        recyclerView = viewBinding.folderList

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.list_item_spacing)
        recyclerView.addItemDecoration(SpacingItemDecoration(spacingInPixels))

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.folders.observe(viewLifecycleOwner) { folders ->
            folders.forEach { folder ->
                Timber.d("Backend = ${folder.id} $folder")
            }
            adapter.submitList(folders)
        }
    }
}