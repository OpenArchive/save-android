//package net.opendasharchive.openarchive.services.webdav
//
//import android.net.Uri
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.inputmethod.EditorInfo
//import androidx.core.os.bundleOf
//import androidx.fragment.app.setFragmentResult
//import com.google.android.material.snackbar.Snackbar
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import net.opendasharchive.openarchive.R
//import net.opendasharchive.openarchive.databinding.FragmentWebDavBinding
//import net.opendasharchive.openarchive.db.Backend
//import net.opendasharchive.openarchive.services.CommonServiceFragment
//import net.opendasharchive.openarchive.services.SaveClient
//import net.opendasharchive.openarchive.services.internetarchive.Util
//import net.opendasharchive.openarchive.services.webdav.WebDavActivity.Companion.ARG_SPACE
//import net.opendasharchive.openarchive.services.webdav.WebDavActivity.Companion.ARG_VAL_NEW_SPACE
//import net.opendasharchive.openarchive.services.webdav.WebDavActivity.Companion.REMOTE_PHP_ADDRESS
//import net.opendasharchive.openarchive.util.extensions.makeSnackBar
//import okhttp3.Call
//import okhttp3.Callback
//import okhttp3.Request
//import okhttp3.Response
//import java.io.IOException
//import kotlin.coroutines.suspendCoroutine
//
//class WebDavFragment : CommonServiceFragment() {
//    private var mBackendId: Long? = null
//    private lateinit var mBackend: Backend
//
////    private lateinit var mSnackbar: Snackbar
//    private lateinit var mBinding: FragmentWebDavBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        mBackendId = arguments?.getLong(ARG_SPACE) ?: ARG_VAL_NEW_SPACE
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        // Inflate the layout for this fragment
//        mBinding = FragmentWebDavBinding.inflate(inflater)
//
//
//
//        return mBinding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        mSnackbar = mBinding.root.makeSnackBar(getString(R.string.login_activity_logging_message))
//    }
//
//
//
//
//
//
//
//
//    private fun showError(text: CharSequence, onForm: Boolean = false) {
//        requireActivity().runOnUiThread {
//            mSnackbar.dismiss()
//
//            if (onForm) {
//                mBinding.password.error = text
//                mBinding.password.requestFocus()
//            } else {
//                mSnackbar = mBinding.root.makeSnackBar(text, Snackbar.LENGTH_LONG)
//                mSnackbar.show()
//
//                mBinding.server.requestFocus()
//            }
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//
//        // make sure the snack-bar is gone when this fragment isn't on display anymore
//        mSnackbar.dismiss()
//        // also hide keyboard when fragment isn't on display anymore
//        Util.hideSoftKeyboard(requireActivity())
//    }
//
////    private fun removeProject() {
////        AlertHelper.show(
////            requireContext(),
////            R.string.are_you_sure_you_want_to_remove_this_server_from_the_app,
////            R.string.remove_from_app,
////            buttons = listOf(
////                AlertHelper.positiveButton(R.string.remove) { _, _ ->
////                    mSpace.delete()
////                    setFragmentResult(RESP_DELETED, bundleOf())
////                }, AlertHelper.negativeButton()
////            )
////        )
////    }
//
//
//}