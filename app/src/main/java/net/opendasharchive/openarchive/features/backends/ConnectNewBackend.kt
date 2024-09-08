package net.opendasharchive.openarchive.features.backends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentConnectNewBackendBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.util.SpacingItemDecoration

class ConnectNewBackend : Fragment() {
    private lateinit var viewBinding: FragmentConnectNewBackendBinding

    private val adapter = BackendAdapter { backend, action ->
        when (action) {
            ItemAction.SELECTED -> showAuthScreenFor(backend)
            else -> Unit
        }
    }
    private val viewModel: BackendViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentConnectNewBackendBinding.inflate(inflater)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createBackendList()
    }

    private fun createBackendList() {
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.list_item_spacing)
        viewBinding.backendList.addItemDecoration(SpacingItemDecoration(spacingInPixels))

        viewBinding.backendList.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.backendList.adapter = adapter

        viewModel.backends.observe(viewLifecycleOwner) { backends ->
            adapter.submitList(backends)
        }
    }

    private fun showAuthScreenFor(backend: Backend) {
        val nextScreen = when (backend.type) {
            Backend.Type.GDRIVE.id -> ConnectNewBackendDirections.navigationSegueToGdrive()
            Backend.Type.INTERNET_ARCHIVE.id -> ConnectNewBackendDirections.navigationSegueToInternetArchive()
            Backend.Type.WEBDAV.id -> ConnectNewBackendDirections.navigationSegueToPrivateServer()
            Backend.Type.SNOWBIRD.id -> ConnectNewBackendDirections.navigationSegueToSnowbird()
            else -> ConnectNewBackendDirections.navigationSegueToSnowbird()
        }

        findNavController().navigate(nextScreen)
    }


//        val powerMenu = PowerMenu.Builder(this).build()
//        powerMenu.showAsDropDown(view)

    //    private fun handleGoogle() {
//        val hasPerms = GDriveConduit.permissionsGranted(this)
//        Timber.d("Permissions granted already? $hasPerms")
//
//        if (hasPerms) {
//            Timber.d("has perms")
//            removeGoogle()
//        } else {
//            Timber.d("no perms")
//            backendAuthLauncher.launch(Intent(this, GDriveActivity::class.java))
//        }
//    }
//
//    private fun completeSignOut() {
//        val googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
//
//        googleSignInClient.revokeAccess().addOnCompleteListener { result ->
//            Timber.d("result = $result")
//
//            if (result.isSuccessful) {
//                googleSignInClient.signOut()
//            }
//
//            // Regardless of result, we need to remove GDrive from local config.
//            //
//            Backend.get(Backend.Type.GDRIVE).firstOrNull() { backend ->
//                backend.delete()
//            }
//        }
//    }
//
//    private fun removeGoogle() {
//        AlertHelper.show(this,
//            R.string.are_you_sure_you_want_to_remove_this_server_from_the_app,
//            R.string.remove_from_app,
//            buttons = listOf(
//            AlertHelper.positiveButton(R.string.remove) { _, _ ->
//                completeSignOut()
//            },
//            AlertHelper.negativeButton()))
//    }

}