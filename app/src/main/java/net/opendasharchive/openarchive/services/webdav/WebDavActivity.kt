package net.opendasharchive.openarchive.services.webdav

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityWebdavBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.services.SaveClient
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

    private lateinit var mBinding: ActivityWebdavBinding
    private var mBackendId by Delegates.notNull<Long>()
    private lateinit var mBackend: Backend

    companion object {
        // factory method parameters (bundle args)
        const val ARG_SPACE = "space"
        const val ARG_VAL_NEW_SPACE = -1L

        // other internal constants
        const val REMOTE_PHP_ADDRESS = "/remote.php/webdav/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityWebdavBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.private_server)

        mBackendId = intent.getLongExtra(EXTRA_DATA_SPACE, ARG_VAL_NEW_SPACE)

        setup()
    }

    private fun setup() {
        mBinding.server.addTextChangedListener(object : ReadyToAuthTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                enableIfReady()
            }
        })

        mBinding.username.addTextChangedListener(object : ReadyToAuthTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                enableIfReady()
            }
        })

        mBinding.password.addTextChangedListener(object : ReadyToAuthTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                enableIfReady()
            }
        })

        if (ARG_VAL_NEW_SPACE != mBackendId) {
            // setup views for editing and existing space

            mBackend = Backend.get(mBackendId) ?: Backend(Backend.Type.WEBDAV)

            mBinding.header.visibility = View.GONE
            mBinding.server.isEnabled = false
            mBinding.username.isEnabled = false
            mBinding.password.isEnabled = false

            mBinding.server.setText(mBackend.host)
            mBinding.name.setText(mBackend.name)
            mBinding.username.setText(mBackend.username)
            mBinding.password.setText(mBackend.password)

            mBinding.name.addTextChangedListener(object : ReadyToAuthTextWatcher() {
                override fun afterTextChanged(s: Editable?) {
                    if (s == null) return

                    mBackend.name = s.toString()
                    mBackend.save()
                }
            })
        } else {
            // setup views for creating a new space
            //
            mBackend = Backend(Backend.Type.WEBDAV)
        }

        mBinding.authenticationButton.setOnClickListener { attemptLogin() }

        mBinding.server.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                mBinding.server.setText(fixUrl(mBinding.server.text)?.toString())
            }
        }

        mBinding.password.setOnEditorActionListener { _, actionId, _ ->
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
        mBinding.username.error = null
        mBinding.password.error = null

        // Store values at the time of the login attempt.
        var errorView: View? = null

        mBackend.name = mBinding.name.text?.toString() ?: ""

        mBackend.host = fixUrl(mBinding.server.text)?.toString() ?: ""
        mBinding.server.setText(mBackend.host)

        mBackend.username = mBinding.username.text?.toString() ?: ""
        mBackend.password = mBinding.password.text?.toString() ?: ""

        if (mBackend.host.isEmpty()) {
            mBinding.server.error = getString(R.string.error_field_required)
            errorView = mBinding.server
        } else if (mBackend.username.isEmpty()) {
            mBinding.username.error = getString(R.string.error_field_required)
            errorView = mBinding.username
        } else if (mBackend.password.isEmpty()) {
            mBinding.password.error = getString(R.string.error_field_required)
            errorView = mBinding.password
        }

        if (errorView != null) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            errorView.requestFocus()

            return
        }

        val other = Backend.get(Backend.Type.WEBDAV, mBackend.host, mBackend.username)

        if (other.isNotEmpty() && other[0].id != mBackend.id) {
            // return showError(getString(R.string.you_already_have_a_server_with_these_credentials))
        }

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        // mSnackbar.show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                testConnection()
                mBackend.save()
                Backend.current = mBackend

                // setFragmentResult(RESP_CREATED, bundleOf())
            } catch (exception: IOException) {
//                if (exception.message?.startsWith("401") == true) {
//                    showError(getString(R.string.error_incorrect_username_or_password), true)
//                } else {
//                    showError(exception.localizedMessage ?: getString(R.string.error))
//                }
            }
        }
    }

    private fun enableIfReady() {
        val isIncomplete = mBinding.server.text.isNullOrEmpty()
                || mBinding.username.text.isNullOrEmpty()
                || mBinding.password.text.isNullOrEmpty()

        mBinding.authenticationButton.isEnabled = !isIncomplete
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
        val url = mBackend.hostUrl ?: throw IOException("400 Bad Request")

        val client = SaveClient.get(this, mBackend.username, mBackend.password)

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