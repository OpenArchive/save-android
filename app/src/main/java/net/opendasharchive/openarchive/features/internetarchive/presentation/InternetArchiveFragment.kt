package net.opendasharchive.openarchive.features.internetarchive.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.internetarchive.presentation.components.IAResult
import net.opendasharchive.openarchive.features.internetarchive.presentation.components.bundleWithBackendId
import net.opendasharchive.openarchive.features.internetarchive.presentation.components.bundleWithNewSpace
import net.opendasharchive.openarchive.features.internetarchive.presentation.components.getSpace
import net.opendasharchive.openarchive.services.CommonServiceFragment

class InternetArchiveFragment : CommonServiceFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val (space, isNewSpace) = arguments.getSpace(Backend.Type.INTERNET_ARCHIVE)

        return ComposeView(requireContext()).apply {
            setContent {
                InternetArchiveScreen(space, isNewSpace) { result ->
                    finish(result)
                }
            }
        }
    }

    private fun finish(result: IAResult) {
        setFragmentResult(result.value, bundleOf())
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
