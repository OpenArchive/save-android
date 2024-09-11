package net.opendasharchive.openarchive.features.folders

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentBrowseFoldersBinding
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.util.Utility.showMaterialPrompt
import net.opendasharchive.openarchive.util.extensions.toggle
import timber.log.Timber


class BrowseFoldersFragment : Fragment() {

    private lateinit var binding: FragmentBrowseFoldersBinding
    private lateinit var viewModel: BrowseFoldersViewModel
    private lateinit var adapter: BrowseFoldersAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBrowseFoldersBinding.inflate(layoutInflater)

        viewModel = BrowseFoldersViewModel()

        setupRecyclerView()
        setupSwipeRefresh()

        try {
            viewModel.loadData(requireContext())
        } catch (e: Error) {
            Timber.e(e)
        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            Timber.d("Observed!")

            Timber.d("Loaded ${viewModel.items.value?.size} items")

            binding.foldersEmpty.toggle(items.isEmpty())

            // Stop the refreshing indicator
            binding.swipeRefreshLayout.isRefreshing = false

            adapter.updateItems(items)
        }

        viewModel.progressBarFlag.observe(viewLifecycleOwner) {
            binding.progressBar.toggle(it)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_browse_folder, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_add -> {
                        addFolder()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun onFolderSelected(folder: Folder) {
        Timber.d("Selected folder!")

        if (folder != Folder.current) {
            showMaterialPrompt(
                requireContext(),
                "Confirming",
                "Would you like to make this your current folder?",
                "Yes", "No") { affirm ->

                if (affirm) {
                    setFolderAsDefault(folder)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setFolderAsDefault(folder: Folder) {
        Folder.current = folder
        adapter.notifyDataSetChanged()
        setFragmentResult("FOLDER_BROWSER", bundleOf("folderId" to 123))
    }

    private fun refreshFolders() {
        try {
            viewModel.loadData(requireContext(),  false)
        } catch (e: Error) {
            Timber.e(e)
            Toast.makeText(requireContext(), "Problem refreshing folders", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = BrowseFoldersAdapter(
            onClick = { folder ->
                onFolderSelected(folder)
            },
            onLongPress = { folder, view ->
                Timber.d("long press!")
                showContextMenu(view)
                //folderContextMenu?.showAsAnchorCenter(view)
            }
        )

        binding.folderList.layoutManager = LinearLayoutManager(requireContext())

        binding.folderList.adapter = adapter
    }

    private fun showContextMenu(anchorView: View) {
//        val menuItems = listOf(
//            CustomPopupMenuDialog.MenuItem(R.drawable.ic_description, "Edit"),
//            CustomPopupMenuDialog.MenuItem(R.drawable.ic_error, "Share"),
//            CustomPopupMenuDialog.MenuItem(R.drawable.ic_delete, "Delete")
//        )
//
//        val popupMenu = CustomPopupMenuDialog(requireContext(), menuItems) { item ->
//            when (item.text) {
//                "Edit" -> { /* Handle edit action */ }
//                "Share" -> { /* Handle share action */ }
//                "Delete" -> { /* Handle delete action */ }
//            }
//        }
//
//        popupMenu.show(anchorView)
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshFolders()
        }

        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorPrimaryDark
        )
    }

    private fun addFolder() {
        findNavController().navigate(BrowseFoldersFragmentDirections.navigateToAddBackendScreen())
    }
}