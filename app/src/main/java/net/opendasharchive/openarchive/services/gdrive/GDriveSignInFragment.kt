package net.opendasharchive.openarchive.services.gdrive

import android.accounts.Account
import android.app.Activity
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.setFragmentResult
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
import net.opendasharchive.openarchive.services.CommonServiceFragment
import net.opendasharchive.openarchive.util.Utility
import timber.log.Timber

class GDriveSignInFragment : CommonServiceFragment() {

    private lateinit var binding: FragmentGdriveSignInBinding

    private lateinit var gso: GoogleSignInOptions

    private lateinit var googleSignInClient: GoogleSignInClient

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
                Timber.d("Sign in failed")
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

    private fun promptUserToSignOut() {
        Utility.showMaterialPrompt(
            context = requireContext(),
            title = "Hi, there!",
            message = "You are already signed into a Google account. If you want to sign into s different account you will need to sign out first. Do that now?",
            positiveButtonText = "Yes",
            negativeButtonText = "No") { affirm ->
            if (affirm) {
                switchAccounts()
            } else {
                setFragmentResult(RESP_CANCEL, bundleOf())
            }
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        CoroutineScope(Dispatchers.IO).launch {
            val backend = Backend(Backend.Type.GDRIVE)
            val account = completedTask.getResult(ApiException::class.java)

            backend.displayname = account.email ?: ""

            if (GDriveConduit.permissionsGranted(requireContext())) {
                if (backend.exists()) {
                    Timber.d("Account already exists")
                    setFragmentResult(RESP_CANCEL, bundleOf())
                } else {
                    backend.save()
                    Timber.d("Account created")
                    setFragmentResult(RESP_CREATED, bundleOf())
                }
            } else {
                Timber.d("Permissions not granted")
                setFragmentResult(RESP_CANCEL, bundleOf())
            }
        }
    }

//    private fun openFolderBroswerActivityForBackend(backend: Backend) {
//        val intent = Intent(requireActivity(), BrowseFoldersActivity::class.java)
//        intent.putExtra("BACKEND_ID", backend.id)
//        resultLauncher.launch(intent)
//    }

    private fun authFailed(errorMessage: String?) {
        MainScope().launch {
            errorMessage?.let {
                binding.error.text = errorMessage
                binding.error.visibility = View.VISIBLE
            }
            binding.btAuthenticate.isEnabled = true
        }
    }
}