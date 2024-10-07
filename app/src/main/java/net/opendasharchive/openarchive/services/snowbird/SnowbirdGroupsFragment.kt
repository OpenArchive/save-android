package net.opendasharchive.openarchive.services.snowbird

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdListGroupsBinding
import net.opendasharchive.openarchive.services.CommonServiceFragment
import net.opendasharchive.openarchive.util.SpacingItemDecoration
import net.opendasharchive.openarchive.util.Utility

class SnowbirdGroupsFragment : CommonServiceFragment(), SnowbirdGroupsAdapterListener {

    private lateinit var viewBinding: FragmentSnowbirdListGroupsBinding
    private val viewModel: SnowbirdViewModel by viewModels()
    private lateinit var adapter: SnowbirdGroupsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentSnowbirdListGroupsBinding.inflate(inflater)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        createMenu()

        createViewModel()

//        viewModel.isLoading.observe(viewLifecycleOwner) {
//            viewBinding.progressBar.toggle(it)
//        }
    }

//    private fun createMenu() {
//        requireActivity().addMenuProvider(object : MenuProvider {
//            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                menuInflater.inflate(R.menu.menu_snowbird, menu)
//            }
//
//            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//                return when (menuItem.itemId) {
//                    R.id.action_add -> {
//                        addGroup()
//                        true
//                    }
//                    else -> false
//                }
//            }
//        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
//    }

    private fun createViewModel() {
        adapter = SnowbirdGroupsAdapter(this)

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.list_item_spacing)
        viewBinding.groupList.addItemDecoration(SpacingItemDecoration(spacingInPixels))

        viewBinding.groupList.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.groupList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.groups.collect { groups ->
                adapter.submitList(groups)
            }
        }
    }

    private fun showError() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder
            .setTitle("Oops")
            .setMessage("We weren't able to create your new backend.")
            .setPositiveButton("Ok") { dialog, which ->
                setFragmentResult(RESP_CANCEL, bundleOf())
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showSuccess() {
        Utility.showMaterialMessage(
            requireContext(),
            "Woo!",
            "New backend was successfully created."
        ) {
            setFragmentResult(RESP_CREATED, bundleOf())
        }
    }

    override fun groupSelected(group: SnowbirdGroup) {
        // createSnowbirdBackend(group)
        showSuccess()
    }
}