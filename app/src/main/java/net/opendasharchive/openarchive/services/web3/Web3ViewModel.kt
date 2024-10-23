package net.opendasharchive.openarchive.services.web3

import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

sealed class InitializationState {
    data object NotInitialized : InitializationState()
    data object Initializing : InitializationState()
    data class Initialized(val spaceDid: String) : InitializationState()
    data class Error(val error: Exception) : InitializationState()
}

sealed class UploadState {
    data object Idle : UploadState()
    data object Preparing : UploadState()
    data class Uploading(val percentage: Int) : UploadState()
    data class Success(val cid: String) : UploadState()
    data class Error(val error: Exception) : UploadState()
}

class Web3ViewModel : ViewModel() {
    private var web3Client: Web3Client? = null

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    private val _initializationState = MutableStateFlow<InitializationState>(InitializationState.NotInitialized)
    val initializationState: StateFlow<InitializationState> = _initializationState

    fun initializeClient(webView: WebView, privateKey: String, space: Web3Space) {
        if (web3Client != null) return

        viewModelScope.launch {
            _initializationState.value = InitializationState.Initializing

            try {
                web3Client = Web3Client(webView, privateKey, space)
                val spaceInfo = web3Client?.initialize()
                Timber.d("spaceInfo = $spaceInfo")
                _initializationState.value = InitializationState.Initialized(space.did)
            } catch (e: Exception) {
                _initializationState.value = InitializationState.Error(e)
                Timber.d("error = ${e.localizedMessage}")
                web3Client = null
            }
        }
    }

    fun uploadFile(file: File) {
        val client = web3Client ?: run {
            _uploadState.value = UploadState.Error(Exception("Client not initialized"))
            Timber.d("Client not initialized")
            return
        }

        viewModelScope.launch {
            client.uploadFile(file).collect { progress ->
                _uploadState.value = when (progress) {
                    is UploadProgress.Preparing -> UploadState.Preparing
                    is UploadProgress.Uploading -> UploadState.Uploading(progress.percentage)
                    is UploadProgress.Success -> UploadState.Success(progress.cid)
                    is UploadProgress.Error -> UploadState.Error(progress.exception)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        web3Client = null
    }
}
