package net.opendasharchive.openarchive.services.web3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.databinding.FragmentWeb3Binding
import net.opendasharchive.openarchive.extensions.assetToFile
import timber.log.Timber
import java.io.File

class Web3FileUploadFragment : Web3BaseFragment() {

    private lateinit var viewBinding: FragmentWeb3Binding
    private lateinit var webView: WebView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentWeb3Binding.inflate(inflater)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.uploadButton.setOnClickListener {
            val file = requireContext().assetToFile("idgaf.jpg")
            Timber.d("file = $file")
            uploadFile(file)
        }

        // Collect states
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.initializationState.collect { state ->
                when (state) {
                    is InitializationState.NotInitialized -> showNotInitializedUI()
                    is InitializationState.Initializing -> showInitializingUI()
                    is InitializationState.Initialized -> handleInitialized(state.spaceDid)
                    is InitializationState.Error -> handleInitError(state.error)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uploadState.collect { state ->
                when (state) {
                    is UploadState.Idle -> hideProgress()
                    is UploadState.Preparing -> showPreparingUI()
                    is UploadState.Uploading -> updateProgress(state.percentage)
                    is UploadState.Success -> handleSuccess(state.cid)
                    is UploadState.Error -> handleError(state.error)
                }
            }
        }
    }

    fun uploadFile(file: File) {
        viewModel.uploadFile(file)
    }

    // UI handling methods to be implemented
    private fun showNotInitializedUI() {}
    private fun showInitializingUI() {}
    private fun handleInitialized(spaceDid: String) {}
    private fun handleInitError(error: Exception) {}
    private fun hideProgress() {}
    private fun showPreparingUI() {}
    private fun updateProgress(percentage: Int) {}
    private fun handleSuccess(cid: String) {}
    private fun handleError(error: Exception) {}
}