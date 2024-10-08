package net.opendasharchive.openarchive.services.snowbird

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdListUsersBinding
import net.opendasharchive.openarchive.services.CommonServiceFragment
import net.opendasharchive.openarchive.util.Utility
import org.koin.androidx.viewmodel.ext.android.viewModel

class SnowbirdListUsersFragment : CommonServiceFragment(), SnowbirdGroupsAdapterListener {

    private val snowbirdViewModel: SnowbirdViewModel by viewModel()
    private lateinit var viewBinding: FragmentSnowbirdListUsersBinding
    private lateinit var adapter: MockGroupAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentSnowbirdListUsersBinding.inflate(inflater)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createViewModel()

//        viewModel.isLoading.observe(viewLifecycleOwner) {
//            viewBinding.progressBar.toggle(it)
//        }
    }

    private fun createViewModel() {
        adapter = MockGroupAdapter {
            findNavController().navigate(SnowbirdListUsersFragmentDirections.navigateToSnowbirdListDocumentsScreen())
        }

        viewBinding.groupList.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.groupList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            snowbirdViewModel.users.collect { groups ->
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