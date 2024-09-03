package net.opendasharchive.openarchive.services.webdav

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityWebdavBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.services.SaveClient
import net.opendasharchive.openarchive.util.Utility.showMaterialWarning
import net.opendasharchive.openarchive.util.extensions.makeSnackBar
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.Delegates

open class ReadyToAuthTextWatcher : TextWatcher {
    override fun afterTextChanged(s: Editable?) {}
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}

class WebDavActivity : BaseActivity() {

    private lateinit var binding: ActivityWebdavBinding
    private var backendId by Delegates.notNull<Long>()
    private lateinit var backend: Backend

    companion object {
        // factory method parameters (bundle args)
        const val ARG_SPACE = "space"
        const val ARG_VAL_NEW_BACKEND = -1L

        // other internal constants
        const val REMOTE_PHP_ADDRESS = "/remote.php/webdav/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebdavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.private_server)

        backendId = intent.getLongExtra(EXTRA_DATA_BACKEND, ARG_VAL_NEW_BACKEND)

        setup()
    }

    private fun setup() {
        binding.server.requestFocus()
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

        if (ARG_VAL_NEW_BACKEND != backendId) {
            // setup views for editing and existing space

            backend = Backend.getById(backendId) ?: Backend(Backend.Type.WEBDAV)

            binding.header.visibility = View.GONE
            binding.server.isEnabled = false
            binding.username.isEnabled = false
            binding.password.isEnabled = false

            binding.server.setText(backend.host)
            binding.name.setText(backend.name)
            binding.username.setText(backend.username)
            binding.password.setText(backend.password)

            binding.name.addTextChangedListener(object : ReadyToAuthTextWatcher() {
                override fun afterTextChanged(s: Editable?) {
                    if (s == null) return

                    backend.name = s.toString()
                    backend.save()
                }
            })
        } else {
            // setup views for creating a new space
            //
            backend = Backend(Backend.Type.WEBDAV)
        }

        binding.authenticationButton.setOnClickListener { attemptLogin() }

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

        backend.name = binding.name.text?.toString() ?: ""

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

        val other = Backend.get(Backend.Type.WEBDAV, backend.host, backend.username)

        if (other.isNotEmpty() && other[0].id != backend.id) {
            return showMaterialWarning(context = this, message = getString(R.string.you_already_have_a_server_with_these_credentials))
        }

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        // mSnackbar.show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                testConnection()
                backend.save()
                signalSuccess()
            } catch (exception: IOException) {
                runOnUiThread {
                    if (exception.message?.startsWith("401") == true) {
                        showMaterialWarning(context = this@WebDavActivity, message = getString(R.string.error_incorrect_username_or_password)) {
                            resetAfterBadTest()
                        }
                    } else {
                        showMaterialWarning(context = this@WebDavActivity, message = exception.localizedMessage ?: getString(R.string.error)) {
                            resetAfterBadTest()
                        }
                    }
                }
            }
        }
    }

    private fun resetAfterBadTest() {
        binding.server.text = null
        binding.server.requestFocus()
    }

    private fun signalSuccess() {
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
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

        val client = SaveClient.get(this, backend.username, backend.password)

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

    private fun showError(text: CharSequence, onForm: Boolean = false) {
        runOnUiThread {
            if (onForm) {
                binding.password.error = text
                binding.password.requestFocus()
            } else {
                val snackbar = binding.root.makeSnackBar(text, Snackbar.LENGTH_LONG)
                snackbar.show()

                binding.server.requestFocus()
            }
        }
    }
}