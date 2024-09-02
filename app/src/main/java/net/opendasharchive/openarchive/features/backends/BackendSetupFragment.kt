package net.opendasharchive.openarchive.features.backends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentBackendSetupBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.util.AlertHelper
import timber.log.Timber

class BackendSetupFragment : Fragment(), BackendAdapterListener {

    private lateinit var viewBinding: FragmentBackendSetupBinding
    private val viewModel: BackendViewModel by viewModels()
    private lateinit var adapter: BackendAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentBackendSetupBinding.inflate(inflater)

        createBackendList()

        return viewBinding.root
    }

    private fun createBackendList() {
        adapter = BackendAdapter(this)

        viewBinding.backendList.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.backendList.adapter = adapter

        viewModel.backends.observe(viewLifecycleOwner) { backends ->
            adapter.submitList(backends)
        }
    }

    private fun removeInternetArchive() {
        val backend = Backend.get(type = Backend.Type.INTERNET_ARCHIVE).firstOrNull()

        if (backend != null) {
            AlertHelper.show(
                requireContext(),
                R.string.are_you_sure_you_want_to_remove_this_server_from_the_app,
                R.string.remove_from_app,
                buttons = listOf(
                    AlertHelper.positiveButton(R.string.remove) { _, _ ->
                        backend.delete()
                        // refreshUI()
                        Toast.makeText(requireContext(), "Successfully removed media storage!", Toast.LENGTH_SHORT).show()
                    },
                    AlertHelper.negativeButton()
                )
            )
        } else {
            Timber.d("Unable to find backend.")
        }
    }

    private fun removeGoogleSpace() {
        val backend = Backend.get(type = Backend.Type.GDRIVE).firstOrNull()

        if (backend != null) {
            AlertHelper.show(
                requireContext(),
                R.string.are_you_sure_you_want_to_remove_this_server_from_the_app,
                R.string.remove_from_app,
                buttons = listOf(
                    AlertHelper.positiveButton(R.string.remove) { _, _ ->
                        val googleSignInClient = GoogleSignIn.getClient(
                            requireActivity(),
                            GoogleSignInOptions.DEFAULT_SIGN_IN
                        )

                        googleSignInClient.revokeAccess().addOnCompleteListener {
                            googleSignInClient.signOut()
                            backend.delete()
                            // refreshUI()
                            Toast.makeText(requireContext(), "Successfully removed media storage!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    AlertHelper.negativeButton()
                )
            )
        } else {
            Timber.d("Unable to find backend.")
        }
    }

    private fun playServicesAvailable(): Boolean {
        return ConnectionResult.SUCCESS == GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext())
    }

    companion object {
        const val BACKEND_RESULT_REQUEST_KEY = "backend_setup_fragment_result"
        const val BACKEND_RESULT_BUNDLE_TYPE_KEY = "backend_setup_result_type_key"
        const val BACKEND_RESULT_BUNDLE_ACTION_KEY = "backend_setup_result_action_key"
    }

    override fun onBackendClicked(backend: Backend) {
        Timber.d("backendClicked")
        // Backend.current = backend
        setFragmentResult(BACKEND_RESULT_REQUEST_KEY, bundleOf(BACKEND_RESULT_BUNDLE_TYPE_KEY to backend.type))
    }
}