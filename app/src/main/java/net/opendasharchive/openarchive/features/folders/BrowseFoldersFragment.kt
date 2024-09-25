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
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentBrowseFoldersBinding
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.util.Analytics
import net.opendasharchive.openarchive.util.Utility
import net.opendasharchive.openarchive.util.Utility.showMaterialPrompt
import net.opendasharchive.openarchive.util.extensions.toggle
import timber.log.Timber


class BrowseFoldersFragment : Fragment() {

    private val viewModel: BrowseFoldersViewModel by activityViewModels()
    private lateinit var binding: FragmentBrowseFoldersBinding
    private lateinit var adapter: BrowseFoldersAdapter
    private lateinit var headerContextMenu: PowerMenu
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBrowseFoldersBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Needs a special case since our root Fragment isn't really the root of our backstack.
        // Will probably look into addressing this properly at a later date.
        //
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

        setupRecyclerView()
//        setupSwipeRefresh()

        try {
            viewModel.loadData(requireContext())
        } catch (e: Error) {
            Timber.e(e)
        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            Timber.d("Observed!")

            Timber.d("Loaded ${viewModel.items.value?.size} items")

            // Stop the refreshing indicator
//            binding.swipeRefreshLayout.isRefreshing = false

            adapter.updateItems(items)
        }

        viewModel.progressBarFlag.observe(viewLifecycleOwner) {
            binding.progressBar.toggle(it)
        }
    }

    private fun onFolderSelected(folder: Folder) {
        Timber.d("Selected folder!")

        if (folder != Folder.current) {
            showMaterialPrompt(
                requireContext(),
                "Question",
                "Would you like to make this your active folder?",
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
        recyclerView = binding.folderList

        adapter = BrowseFoldersAdapter(
            onItemClick = { folder ->
                onFolderSelected(folder)
            },
            onItemLongPress = { _, view ->
                Timber.d("long press!")
                showItemContextMenu(view)
                //folderContextMenu?.showAsAnchorCenter(view)
            },
            onHeaderLongPress = { backend, view ->
                Timber.d("long press on header!")
                showHeaderContextMenu(view) { _ ->
                    Analytics.log(Analytics.BACKEND_DISCONNECTED, mapOf("type" to backend.name))
                    backend.delete()
                }
            }
        )

        recyclerView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showHeaderContextMenu(anchorView: View, completion: (Boolean) -> Unit) {
        headerContextMenu = Utility.getContextMenu(requireContext(), anchorView)
        headerContextMenu.onMenuItemClickListener = OnMenuItemClickListener<PowerMenuItem> { position, item ->
            headerContextMenu.dismiss()
            completion.invoke(true)
            viewModel.loadData(requireContext(), false)
        }
        headerContextMenu.showAsDropDown(anchorView)
    }

    private fun showItemContextMenu(anchorView: View) {
        Toast.makeText(requireContext(), "Context menu coming soon", Toast.LENGTH_LONG).show()

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

//    private fun setupSwipeRefresh() {
//        binding.swipeRefreshLayout.setOnRefreshListener {
//            refreshFolders()
//        }
//
//        binding.swipeRefreshLayout.setColorSchemeResources(
//            R.color.colorPrimary,
//            R.color.colorPrimaryDark
//        )
//    }

    private fun addFolder() {
        findNavController().navigate(BrowseFoldersFragmentDirections.navigateToAddBackendScreen())
    }
}