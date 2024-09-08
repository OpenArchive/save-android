package net.opendasharchive.openarchive.services.snowbird

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import net.opendasharchive.openarchive.databinding.FragmentShowbirdListGroupsBinding
import net.opendasharchive.openarchive.services.CommonServiceFragment

class SnowbirdGroupsFragment : CommonServiceFragment(), SnowbirdGroupsAdapterListener {

    private lateinit var viewBinding: FragmentShowbirdListGroupsBinding
    private val viewModel: SnowbirdGroupsViewModel by viewModels()
    private lateinit var adapter: SnowbirdGroupsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentShowbirdListGroupsBinding.inflate(inflater)

        createViewModel()

        return viewBinding.root
    }

//    override fun onBackPressed() {
//        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
//
//        builder
//            .setTitle("Just a second")
//            .setMessage("If you cancel you'll lose what you've configured so far. Go ahead?")
//            .setPositiveButton("Yes") { dialog, which ->
//                setFragmentResult(RESP_CANCEL, bundleOf())
//            }
//            .setNegativeButton("No") { dialog, which ->
//                // Nop
//            }
//
//        val dialog: AlertDialog = builder.create()
//        dialog.show()
//    }

    private fun createSnowbirdBackend(group: SnowbirdGroup) {
//        arguments?.getString("uri")?.also { uri ->
//            val backend = Backend(type = Backend.Type.VEILID)
//            backend.host = uri
//            backend.save()
//
//            folder.backendId = backend.id
//            folder.save()
//
//            Backend.current = backend
//
//            setFragmentResult(RESP_CREATED, bundleOf())
//
//            // showSuccess()
//        } ?: {
//            showError()
//        }
    }

    private fun createViewModel() {
        adapter = SnowbirdGroupsAdapter(this)

        viewBinding.groupList.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.groupList.adapter = adapter

        viewModel.groups.observe(viewLifecycleOwner) { groups ->
            adapter.submitList(groups)
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
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder
            .setTitle("Woo!")
            .setMessage("New backend was successfully created.")
            .setPositiveButton("Ok") { dialog, which ->
                setFragmentResult(RESP_CREATED, bundleOf())
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    override fun groupSelected(group: SnowbirdGroup) {
        createSnowbirdBackend(group)
    }
}