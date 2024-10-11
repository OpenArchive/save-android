package net.opendasharchive.openarchive.services.snowbird

import androidx.fragment.app.Fragment
import net.opendasharchive.openarchive.util.FullScreenOverlayManager
import org.koin.androidx.viewmodel.ext.android.viewModel

open class BaseSnowbirdFragment : Fragment() {
    val snowbirdGroupViewModel: SnowbirdGroupViewModel by viewModel()
    val snowbirdRepoViewModel: SnowbirdRepoViewModel by viewModel()

    fun handleProcessingStatus(isProcessing: Boolean) {
        if (isProcessing) {
            FullScreenOverlayManager.show(this@BaseSnowbirdFragment)
        } else {
            FullScreenOverlayManager.hide()
        }
    }
}