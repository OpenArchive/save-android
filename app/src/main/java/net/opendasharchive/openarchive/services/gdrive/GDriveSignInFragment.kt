package net.opendasharchive.openarchive.services.gdrive

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.setFragmentResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
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
import net.opendasharchive.openarchive.features.folders.BrowseFoldersActivity
import net.opendasharchive.openarchive.services.CommonServiceFragment
import timber.log.Timber

class GDriveSignInFragment : CommonServiceFragment() {

    private lateinit var binding: FragmentGdriveSignInBinding

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            MainScope().launch {
                setFragmentResult(RESP_CREATED, bundleOf())
            }
        }
    }

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
            //authenticate()
            signIn()
            binding.btAuthenticate.isEnabled = false
        }

        return binding.root
    }

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // The Task returned from this call is always completed, no need to attach a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        } else {
            Timber.d("Sign in failed")
        }
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(Scopes.DRIVE_FILE))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        CoroutineScope(Dispatchers.IO).launch {
            val backend = Backend(Backend.Type.GDRIVE)
            val account = completedTask.getResult(ApiException::class.java)

            backend.displayname = account.email ?: ""

            if (GDriveConduit.permissionsGranted(requireContext())) {
                backend.save()

                openFolderBroswerActivityForBackend(backend)
            } else {
                authFailed(
                    getString(
                        R.string.gdrive_auth_insufficient_permissions,
                        getString(R.string.app_name),
                        getString(R.string.gdrive)
                    )
                )
            }
        }
    }

    private fun openFolderBroswerActivityForBackend(backend: Backend) {
        val intent = Intent(requireActivity(), BrowseFoldersActivity::class.java)
        intent.putExtra("BACKEND_ID", backend.id)
        resultLauncher.launch(intent)
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
}