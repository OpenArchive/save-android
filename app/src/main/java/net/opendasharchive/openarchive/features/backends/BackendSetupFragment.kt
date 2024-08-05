package net.opendasharchive.openarchive.features.backends

import android.content.Intent
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

//    private fun refreshUI() {
//        if (Space.has(Space.Type.WEBDAV)) {
//            mBinding.privateServerSublabel.text = "Connected"
//            mBinding.iconNextPrivateServer.setIconResource(R.drawable.outline_link_off_24)
//        } else {
//            mBinding.privateServerSublabel.text = "Not connected"
//            mBinding.iconNextPrivateServer.setIconResource(R.drawable.outline_add_link_24)
//            mBinding.iconNextPrivateServer.setOnClickListener {
//                setFragmentResult(RESULT_REQUEST_KEY, bundleOf(RESULT_BUNDLE_KEY to RESULT_VAL_WEBDAV))
//            }
//        }
//
//        if (Space.has(Space.Type.INTERNET_ARCHIVE)) {
//            mBinding.internetArchiveSublabel.text = "Connected"
//            mBinding.iconNextInternetArchive.setIconResource(R.drawable.outline_link_off_24)
//            mBinding.iconNextInternetArchive.setOnClickListener {
//                removeInternetArchive()
//            }
//        } else {
//            mBinding.internetArchiveSublabel.text = "Not connected"
//            mBinding.iconNextInternetArchive.setIconResource(R.drawable.outline_add_link_24)
//            mBinding.iconNextInternetArchive.setOnClickListener {
//                setFragmentResult(RESULT_REQUEST_KEY, bundleOf(RESULT_BUNDLE_KEY to RESULT_VAL_INTERNET_ARCHIVE))
//            }
//        }
//
//        if (!playServicesAvailable()) {
//            mBinding.gdriveRow.hide()
//        } else {
//            if (Space.has(Space.Type.GDRIVE)) {
//                mBinding.gdriveSublabel.text = "Connected"
//                mBinding.iconNextGdrive.setIconResource(R.drawable.outline_link_off_24)
//                mBinding.iconNextGdrive.setOnClickListener {
//                    removeGoogleSpace()
//                }
//            } else {
//                mBinding.gdriveSublabel.text = "Not connected"
//                mBinding.iconNextGdrive.setIconResource(R.drawable.outline_add_link_24)
//                mBinding.iconNextGdrive.setOnClickListener {
//                    setFragmentResult(RESULT_REQUEST_KEY, bundleOf(RESULT_BUNDLE_KEY to RESULT_VAL_GDRIVE))
//                }
//            }
//        }
//    }

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
        const val RESULT_REQUEST_KEY = "space_setup_fragment_result"
        const val RESULT_BUNDLE_KEY = "space_setup_result_key"
        const val RESULT_VAL_WEBDAV = "webdav"
        const val RESULT_VAL_INTERNET_ARCHIVE = "internet_archive"
        const val RESULT_VAL_GDRIVE = "gdrive"
    }

    override fun backendClicked(backend: Backend) {
        Timber.d("backendClicked")
        // Backend.current = backend
        setFragmentResult(RESULT_REQUEST_KEY, bundleOf(RESULT_BUNDLE_KEY to backend.friendlyName))
    }

    override fun addBackendClicked() {
        Timber.d("addBackendClicked")
        startActivity(Intent(requireContext(), BackendSetupActivity::class.java))
    }

    override fun getSelectedBackend(): Backend? {
        Timber.d("getSelectedBackend")
        return Backend.current
    }
}