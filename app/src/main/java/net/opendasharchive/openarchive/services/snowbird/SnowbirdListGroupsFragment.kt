package net.opendasharchive.openarchive.services.snowbird

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdListGroupsBinding
import net.opendasharchive.openarchive.db.SnowbirdGroup
import net.opendasharchive.openarchive.services.CommonServiceFragment
import net.opendasharchive.openarchive.util.FullScreenOverlayManager
import net.opendasharchive.openarchive.util.Utility
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SnowbirdListGroupsFragment : CommonServiceFragment(), SnowbirdGroupsAdapterListener {

    private val snowbirdViewModel: SnowbirdViewModel by viewModel()
    private lateinit var viewBinding: FragmentSnowbirdListGroupsBinding
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
        adapter = SnowbirdGroupsAdapter {
            findNavController().navigate(SnowbirdListGroupsFragmentDirections.navigateToSnowbirdListUsersScreen())
        }

//        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.list_item_spacing)
//        viewBinding.groupList.addItemDecoration(SpacingItemDecoration(spacingInPixels))
        viewBinding.groupList.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.groupList.adapter = adapter

//        val decorator = DividerItemDecoration(viewBinding.root.context, DividerItemDecoration.VERTICAL)
//        val divider = ContextCompat.getDrawable(viewBinding.root.context, R.drawable.save_list_item_spacing_small)
//        if (divider != null) decorator.setDrawable(divider)
//        viewBinding.groupList.addItemDecoration(decorator)

        viewLifecycleOwner.lifecycleScope.launch {
            snowbirdViewModel.groups.collect { groups ->
                adapter.submitList(groups)
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                snowbirdViewModel.isProcessing.collect { isProcessing ->
                    Timber.d("is processing? $isProcessing")
                    if (isProcessing) {
                        FullScreenOverlayManager.show(this@SnowbirdListGroupsFragment)
                    } else {
                        FullScreenOverlayManager.hide()
                    }
                }
            }
        }

        snowbirdViewModel.fetchGroups()
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