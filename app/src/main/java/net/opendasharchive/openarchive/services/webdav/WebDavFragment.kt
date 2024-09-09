package net.opendasharchive.openarchive.services.webdav

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentWebdavBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.folders.NewFolderDataViewModel
import net.opendasharchive.openarchive.features.folders.NewFolderNavigationAction
import net.opendasharchive.openarchive.features.folders.NewFolderNavigationViewModel
import net.opendasharchive.openarchive.services.SaveClient
import net.opendasharchive.openarchive.util.Utility.showMaterialWarning
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.suspendCoroutine

open class ReadyToAuthTextWatcher : TextWatcher {
    override fun afterTextChanged(s: Editable?) {}
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}

class WebDavFragment : Fragment() {

    private lateinit var binding: FragmentWebdavBinding
    private lateinit var backend: Backend
    private val newFolderDataViewModel: NewFolderDataViewModel by activityViewModels()
    private val newFolderNavigationViewModel: NewFolderNavigationViewModel by activityViewModels()

    companion object {
        // factory method parameters (bundle args)
        const val ARG_VAL_NEW_BACKEND = -1L

        // other internal constants
        const val REMOTE_PHP_ADDRESS = "/remote.php/webdav/"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWebdavBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    private fun setup() {
        binding.server.requestFocus()

        setupSignInButtonEnablers()

//        if (ARG_VAL_NEW_BACKEND != backendId) {
//            // setup views for editing and existing space
//
//            backend = Backend.getById(backendId) ?: Backend(Backend.Type.WEBDAV)
//
//            binding.header.visibility = View.GONE
//            binding.server.isEnabled = false
//            binding.username.isEnabled = false
//            binding.password.isEnabled = false
//
//            binding.server.setText(backend.host)
//            binding.username.setText(backend.username)
//            binding.password.setText(backend.password)
//        } else {
            // setup views for creating a new space
            //
            backend = Backend(Backend.Type.WEBDAV)
//        }

        binding.authenticationButton.setOnClickListener {
            attemptLogin()
        }

        binding.server.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.server.setText(fixUrl(binding.server.text)?.toString())
            }
        }

        binding.password.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                attemptLogin()
            }

            false
        }

        newFolderNavigationViewModel.observeNavigation(viewLifecycleOwner) { action ->
            if (action == NewFolderNavigationAction.UserAuthenticated) {
                findNavController().navigate(WebDavFragmentDirections.navigationSegueToBackendMetadata())
            }
        }
    }

    private fun setupSignInButtonEnablers() {
        binding.server.addTextChangedListener(object : ReadyToAuthTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                enableIfReady()
            }
        })

        binding.username.addTextChangedListener(object : ReadyToAuthTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                enableIfReady()
            }
        })

        binding.password.addTextChangedListener(object : ReadyToAuthTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                enableIfReady()
            }
        })
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        // Reset errors.
        binding.username.error = null
        binding.password.error = null

        // Store values at the time of the login attempt.
        var errorView: View? = null

//        backend.name = binding.name.text?.toString() ?: ""

        backend.host = fixUrl(binding.server.text)?.toString() ?: ""
        binding.server.setText(backend.host)

        backend.username = binding.username.text?.toString() ?: ""
        backend.password = binding.password.text?.toString() ?: ""

        if (backend.host.isEmpty()) {
            binding.server.error = getString(R.string.error_field_required)
            errorView = binding.server
        } else if (backend.username.isEmpty()) {
            binding.username.error = getString(R.string.error_field_required)
            errorView = binding.username
        } else if (backend.password.isEmpty()) {
            binding.password.error = getString(R.string.error_field_required)
            errorView = binding.password
        }

        if (errorView != null) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            errorView.requestFocus()
            return
        }

        if (alreadyHasBackend()) {
            return showMaterialWarning(
                context = requireContext(),
                message = getString(R.string.you_already_have_a_server_with_these_credentials))
        }

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        // mSnackbar.show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                testConnection()
                signalSuccess()
            } catch (exception: IOException) {
                activity?.runOnUiThread {
                    if (exception.message?.startsWith("401") == true) {
                        showMaterialWarning(context = requireContext(), message = getString(R.string.error_incorrect_username_or_password)) {
                            resetAfterBadTest()
                        }
                    } else {
                        showMaterialWarning(context = requireContext(), message = exception.localizedMessage ?: getString(R.string.error)) {
                            resetAfterBadTest()
                        }
                    }
                }
            }
        }
    }

    private fun alreadyHasBackend(): Boolean {
        val other = Backend.get(Backend.Type.WEBDAV, backend.host, backend.username)

        return (other.isNotEmpty() && other[0].id != backend.id)
    }

    private fun resetAfterBadTest() {
        binding.server.requestFocus()
    }

    private fun signalSuccess() {
        newFolderDataViewModel.updateFolder { folder ->
            folder.copy(backend = backend)
        }

        newFolderNavigationViewModel.triggerNavigation(NewFolderNavigationAction.UserAuthenticated)
    }

    private fun enableIfReady() {
        val isComplete = !binding.server.text.isNullOrEmpty()
                && !binding.username.text.isNullOrEmpty()
                && !binding.password.text.isNullOrEmpty()

        binding.authenticationButton.isEnabled = isComplete
    }

    private fun fixUrl(url: CharSequence?): Uri? {
        if (url.isNullOrBlank()) return null

        val uri = Uri.parse(url.toString())
        val builder = uri.buildUpon()

        if (uri.scheme != "https") {
            builder.scheme("https")
        }

        if (uri.authority.isNullOrBlank()) {
            builder.authority(uri.path)
            builder.path(REMOTE_PHP_ADDRESS)
        } else if (uri.path.isNullOrBlank() || uri.path == "/") {
            builder.path(REMOTE_PHP_ADDRESS)
        }

        return builder.build()
    }

    private suspend fun testConnection() {
        val url = backend.hostUrl ?: throw IOException("400 Bad Request")

        val client = SaveClient.get(requireContext(), backend.username, backend.password)

        val request =
            Request.Builder().url(url).method("GET", null).addHeader("OCS-APIRequest", "true")
                .addHeader("Accept", "application/json").build()

        return suspendCoroutine {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    it.resumeWith(Result.failure(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    val code = response.code
                    val message = response.message

                    response.close()

                    if (code != 200 && code != 204) {
                        return it.resumeWith(Result.failure(IOException("$code $message")))
                    }

                    it.resumeWith(Result.success(Unit))
                }
            })
        }
    }
}