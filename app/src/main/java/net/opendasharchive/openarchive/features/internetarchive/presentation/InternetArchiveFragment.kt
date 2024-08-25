package net.opendasharchive.openarchive.features.internetarchive.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.db.BackendResult
import net.opendasharchive.openarchive.features.backends.BackendSetupFragment
import net.opendasharchive.openarchive.extensions.bundleWithBackendId
import net.opendasharchive.openarchive.extensions.bundleWithNewSpace
import net.opendasharchive.openarchive.extensions.getBackend
import net.opendasharchive.openarchive.services.CommonServiceFragment
import timber.log.Timber

class InternetArchiveFragment : CommonServiceFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val (backend, isNewBackend) = arguments.getBackend(Backend.Type.INTERNET_ARCHIVE)

        return ComposeView(requireContext()).apply {
            setContent {
                InternetArchiveScreen(backend, isNewBackend) { result ->
                    finish(result)
                }
            }
        }
    }

    private fun finish(result: BackendResult) {
        Timber.d("IA result vbalue = $result")
        setFragmentResult(
            BackendSetupFragment.BACKEND_RESULT_REQUEST_KEY,
            bundleOf(BackendSetupFragment.BACKEND_RESULT_BUNDLE_TYPE_KEY to Backend.Type.INTERNET_ARCHIVE))
    }

    companion object {
        @JvmStatic
        fun newInstance(args: Bundle) = InternetArchiveFragment().apply {
            arguments = args
        }

        @JvmStatic
        fun newInstance(backendId: Long) = newInstance(args = bundleWithBackendId(backendId))

        @JvmStatic
        fun newInstance() = newInstance(args = bundleWithNewSpace())
    }
}
