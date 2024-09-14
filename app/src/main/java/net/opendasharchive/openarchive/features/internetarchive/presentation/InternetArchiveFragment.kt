package net.opendasharchive.openarchive.features.internetarchive.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.db.BackendResult

class InternetArchiveFragment : Fragment() {
    private var backend: Backend? = null
    private var isNewSpace: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            backend = it.getParcelable("backend")
            isNewSpace = it.getBoolean("isNewSpace", false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                InternetArchiveScreen(backend!!, isNewSpace) { result ->
                    parentFragmentManager.setFragmentResult("internetArchiveResult", bundleOf("result" to result))
                    findNavController().navigateUp()
                }
            }
        }
    }

    companion object {
        fun newInstance(backend: Backend, isNewSpace: Boolean) = InternetArchiveFragment().apply {
            arguments = Bundle().apply {
                putParcelable("backend", backend)
                putBoolean("isNewSpace", isNewSpace)
            }
        }
    }
}

private fun finish(result: BackendResult) {
    when (result) {
        BackendResult.Created -> {
//                startActivity(Intent(this, TabBarActivity::class.java))
        }

        BackendResult.Cancelled -> Unit // finish()

        BackendResult.Deleted -> Unit // Backend.navigate(requireActivity())

        else -> Unit
    }
}