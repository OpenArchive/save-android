package net.opendasharchive.openarchive.services.webdav

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentWebDavBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.services.CommonServiceFragment
import net.opendasharchive.openarchive.services.SaveClient
import net.opendasharchive.openarchive.services.internetarchive.Util
import net.opendasharchive.openarchive.util.extensions.makeSnackBar
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.suspendCoroutine

class WebDavFragment : CommonServiceFragment() {
    private var mBackendId: Long? = null
    private lateinit var mBackend: Backend

    private lateinit var mSnackbar: Snackbar
    private lateinit var mBinding: FragmentWebDavBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBackendId = arguments?.getLong(ARG_SPACE) ?: ARG_VAL_NEW_SPACE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        mBinding = FragmentWebDavBinding.inflate(inflater)

        mBackendId = arguments?.getLong(ARG_SPACE) ?: ARG_VAL_NEW_SPACE

        if (ARG_VAL_NEW_SPACE != mBackendId) {
            // setup views for editing and existing space

            mBackend = Backend.get(mBackendId!!) ?: Backend(Backend.Type.WEBDAV)

            mBinding.header.visibility = View.GONE
//            mBinding.buttonBar.visibility = View.GONE

            mBinding.server.isEnabled = false
            mBinding.username.isEnabled = false
            mBinding.password.isEnabled = false

            mBinding.server.setText(mBackend.host)
            mBinding.name.setText(mBackend.name)
            mBinding.username.setText(mBackend.username)
            mBinding.password.setText(mBackend.password)

            mBinding.name.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(name: Editable?) {
                    if (name == null) return

                    mBackend.name = name.toString()
                    mBackend.save()
                }
            })

//            mBinding.btRemove.setOnClickListener {
//                removeProject()
//            }

        } else {
            // setup views for creating a new space

            mBackend = Backend(Backend.Type.WEBDAV)
//            mBinding.btRemove.visibility = View.GONE
        }

        mBinding.btAuthenticate.setOnClickListener { attemptLogin() }

//        mBinding.btCancel.setOnClickListener {
//            setFragmentResult(RESP_CANCEL, bundleOf())
//        }

        mBinding.server.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                mBinding.server.setText(fixSpaceUrl(mBinding.server.text)?.toString())
            }
        }

        mBinding.password.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                attemptLogin()
            }

            false
        }

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSnackbar = mBinding.root.makeSnackBar(getString(R.string.login_activity_logging_message))
    }

    private fun fixSpaceUrl(url: CharSequence?): Uri? {
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

        mBackend.host = fixSpaceUrl(mBinding.server.text)?.toString() ?: ""
        mBinding.server.setText(mBackend.host)

        mBackend.username = mBinding.username.text?.toString() ?: ""
        mBackend.password = mBinding.password.text?.toString() ?: ""

//        mSpace.useChunking = mBinding.swChunking.isChecked

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
            return showError(getString(R.string.you_already_have_a_server_with_these_credentials))
        }

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        mSnackbar.show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                testConnection()
                mBackend.save()
                Backend.current = mBackend

                setFragmentResult(RESP_SAVED, bundleOf())
            } catch (exception: IOException) {
                if (exception.message?.startsWith("401") == true) {
                    showError(getString(R.string.error_incorrect_username_or_password), true)
                } else {
                    showError(exception.localizedMessage ?: getString(R.string.error))
                }
            }
        }
    }

    private suspend fun testConnection() {
        val url = mBackend.hostUrl ?: throw IOException("400 Bad Request")

        val client = SaveClient.get(requireContext(), mBackend.username, mBackend.password)

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
        requireActivity().runOnUiThread {
            mSnackbar.dismiss()

            if (onForm) {
                mBinding.password.error = text
                mBinding.password.requestFocus()
            } else {
                mSnackbar = mBinding.root.makeSnackBar(text, Snackbar.LENGTH_LONG)
                mSnackbar.show()

                mBinding.server.requestFocus()
            }
        }
    }

    override fun onStop() {
        super.onStop()

        // make sure the snack-bar is gone when this fragment isn't on display anymore
        mSnackbar.dismiss()
        // also hide keyboard when fragment isn't on display anymore
        Util.hideSoftKeyboard(requireActivity())
    }

//    private fun removeProject() {
//        AlertHelper.show(
//            requireContext(),
//            R.string.are_you_sure_you_want_to_remove_this_server_from_the_app,
//            R.string.remove_from_app,
//            buttons = listOf(
//                AlertHelper.positiveButton(R.string.remove) { _, _ ->
//                    mSpace.delete()
//                    setFragmentResult(RESP_DELETED, bundleOf())
//                }, AlertHelper.negativeButton()
//            )
//        )
//    }

    companion object {
        // events emitted by this fragment
        const val RESP_SAVED = "web_dav_fragment_resp_saved"
        const val RESP_DELETED = "web_dav_fragment_resp_deleted"
        const val RESP_CANCEL = "web_dav_fragment_resp_cancel"

        // factory method parameters (bundle args)
        const val ARG_SPACE = "space"
        const val ARG_VAL_NEW_SPACE = -1L

        // other internal constants
        const val REMOTE_PHP_ADDRESS = "/remote.php/webdav/"

        @JvmStatic
        fun newInstance(backendId: Long) = WebDavFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_SPACE, backendId)
            }
        }

        @JvmStatic
        fun newInstance() = newInstance(ARG_VAL_NEW_SPACE)
    }
}