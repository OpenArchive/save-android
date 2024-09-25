package net.opendasharchive.openarchive.services.gdrive

import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentGdriveSignInBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.backends.BackendViewModel
import net.opendasharchive.openarchive.services.CommonServiceFragment
import net.opendasharchive.openarchive.util.Analytics
import net.opendasharchive.openarchive.util.Utility
import timber.log.Timber

class GDriveSignInFragment : CommonServiceFragment() {

    private lateinit var binding: FragmentGdriveSignInBinding
    private lateinit var gso: GoogleSignInOptions
    private lateinit var googleSignInClient: GoogleSignInClient
    private val backendViewModel: BackendViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGdriveSignInBinding.inflate(inflater)

        binding.disclaimer1.text = HtmlCompat.fromHtml(
            getString(
                R.string.gdrive_disclaimer_1,
                getString(R.string.app_name),
                getString(R.string.google_name),
                getString(R.string.gdrive_sudp_name),
            ), HtmlCompat.FROM_HTML_MODE_COMPACT
        )
        binding.disclaimer1.movementMethod = LinkMovementMethod.getInstance()
        binding.disclaimer2.text = getString(
            R.string.gdrive_disclaimer_2,
            getString(R.string.google_name),
            getString(R.string.gdrive),
            getString(R.string.app_name),
        )
        binding.error.visibility = View.GONE

        binding.btAuthenticate.setOnClickListener {
            binding.error.visibility = View.GONE
            binding.btAuthenticate.isEnabled = false
            signIn()
        }

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(Scopes.DRIVE_FILE))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getGoogleAccounts().forEach { account ->
            Timber.d("Account = $account")
        }
    }

    private fun getGoogleAccounts(): Array<Account> {
        return accountManager.getAccountsByType("com.google")
    }

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            }

            Activity.RESULT_CANCELED -> {
                Timber.d("Sign in canceled")
                Toast.makeText(requireContext(), "Sign in canceled", Toast.LENGTH_LONG).show()
                setFragmentResult(RESP_CANCEL, bundleOf())
            }
        }
    }

    private fun switchAccounts() {
        googleSignInClient.signOut().addOnCompleteListener {
            signIn()
        }
    }

    private fun signIn() {
        if (GDriveConduit.permissionsGranted(requireContext())) {
            promptUserToSignOut()
        } else {
            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        MainScope().launch {
            val backend = Backend(Backend.Type.GDRIVE)
            val account = completedTask.getResult(ApiException::class.java)

            backend.nickname = account.email ?: ""
            backend.username = account.email ?: ""

            if (GDriveConduit.permissionsGranted(requireContext())) {
                saveNewBackend(backend)

                CoroutineScope(Dispatchers.IO).launch {
                    syncGDrive(requireContext(), backend)

                    MainScope().launch {
                        Utility.showMaterialMessage(
                            requireContext(),
                            title = "Success!",
                            message = "You have successfully authenticated! Now let's continue setting up your media server.") {
                            findNavController().navigate(GDriveSignInFragmentDirections.navigateToBackendMetadataScreen())
                        }
                    }
                }
            } else {
                Timber.d("Permissions not granted")
                showPermissionsWarning()
            }
        }
    }

    private fun saveNewBackend(backend: Backend) {
        viewLifecycleOwner.lifecycleScope.launch {
            backendViewModel.saveNewBackend(backend)
        }

        Analytics.log(Analytics.BACKEND_CONNECTED, mapOf("type" to backend.name))
    }

    private fun syncGDrive(context: Context, backend: Backend): Int {
        val folders = GDriveConduit.listFoldersInRoot(GDriveConduit.getDrive(context), backend)

        folders.forEach { folder ->
            if (folder.doesNotExist()) {
                Timber.d("Syncing ${folder.name}")
                folder.save()
            }
        }

        return folders.size
    }

    private fun authFailed(errorMessage: String?) {
        MainScope().launch {
            errorMessage?.let {
                binding.error.text = errorMessage
                binding.error.visibility = View.VISIBLE
            }
            binding.btAuthenticate.isEnabled = true
        }
    }

    private fun promptUserToSignOut() {
        Utility.showMaterialPrompt(
            context = requireContext(),
            title = "Hi, there!",
            message = "You are already signed into a Google account. If you want to sign into a different account you will need to sign out first. Do that now?",
            positiveButtonText = "Yes",
            negativeButtonText = "No") { affirm ->
            if (affirm) {
                switchAccounts()
            } else {
                binding.btAuthenticate.isEnabled = true
            }
        }
    }

    private fun showPermissionsWarning() {
        Utility.showMaterialPrompt(
            context = requireContext(),
            title = "Oops",
            message = "We will need permissions to access your GDrive in order to store your media there. Would you like to grant permissions?",
            positiveButtonText = "Yes",
            negativeButtonText = "No") { affirm ->
            if (affirm) {
                signIn()
            } else {
                binding.btAuthenticate.isEnabled = true
            }
        }
    }
}