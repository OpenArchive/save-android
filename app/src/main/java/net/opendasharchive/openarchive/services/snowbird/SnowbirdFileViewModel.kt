package net.opendasharchive.openarchive.services.snowbird

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.db.FileUploadResult
import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.db.SnowbirdFileItem
import net.opendasharchive.openarchive.util.BaseViewModel
import net.opendasharchive.openarchive.util.trackProcessingWithTimeout
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class SnowbirdFileViewModel(
    private val application: Application,
    private val repository: ISnowbirdFileRepository
) : BaseViewModel(application) {

    sealed class State {
        data object Idle : State()
        data object Loading : State()
        data class DownloadSuccess(val uri: Uri) : State()
        data class FetchSuccess(val media: List<SnowbirdFileItem>, var isRefresh: Boolean) : State()
        data class UploadSuccess(val result: FileUploadResult) : State()
        data class Error(val error: SnowbirdError) : State()
    }

    private val _mediaState = MutableStateFlow<State>(State.Idle)
    val mediaState: StateFlow<State> = _mediaState.asStateFlow()

    fun downloadFile(groupKey: String, repoKey: String, filename: String) {
        viewModelScope.launch {
            _mediaState.value = State.Loading
            try {
                val result = processingTracker.trackProcessingWithTimeout(30_000, "download_file") {
                    repository.downloadFile(groupKey, repoKey, filename)
                }

                _mediaState.value = when (result) {
                    is SnowbirdResult.Success -> onDownload(result.value, filename)
                    is SnowbirdResult.Failure -> State.Error(result.error)
                }
            } catch (e: TimeoutCancellationException) {
                _mediaState.value = State.Error(SnowbirdError.TimedOut)
            }
        }
    }

    fun fetchFiles(groupKey: String, repoKey: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _mediaState.value = State.Loading
            try {
                val result = processingTracker.trackProcessingWithTimeout(60_000, "fetch_files") {
                    repository.fetchFiles(groupKey, repoKey, forceRefresh)
                }

                _mediaState.value = when (result) {
                    is SnowbirdResult.Success -> State.FetchSuccess(result.value, forceRefresh)
                    is SnowbirdResult.Failure -> State.Error(result.error)
                }
            } catch (e: TimeoutCancellationException) {
                _mediaState.value = State.Error(SnowbirdError.TimedOut)
            }
        }
    }

    // Example reponse:
    //    {
    //        "updated_collection_hash": "7dkgeko3oeyyr5xympsg2mhbicb2k2ba4wqen6lpt6qs7mgza7vq"
    //    }

    fun uploadFile(groupKey: String, repoKey: String, uri: Uri) {
        viewModelScope.launch {
            _mediaState.value = State.Loading
            try {
                val result = processingTracker.trackProcessingWithTimeout(30_000, "upload_file") {
                    repository.uploadFile(groupKey, repoKey, uri)
                }

                _mediaState.value = when (result) {
                    is SnowbirdResult.Success -> State.UploadSuccess(result.value)
                    is SnowbirdResult.Failure -> State.Error(result.error)
                }
            } catch (e: TimeoutCancellationException) {
                _mediaState.value = State.Error(SnowbirdError.TimedOut)
            }
        }
    }

    private fun onDownload(bytes: ByteArray, filename: String): State {
        Timber.d("Downloaded ${bytes.size} bytes")
        return saveByteArrayToFile(application.applicationContext, bytes, filename).fold(
            onSuccess = { uri -> State.DownloadSuccess(uri) },
            onFailure = { error -> State.Error(SnowbirdError.GeneralError("Error saving file: ${error.message}")) }
        )
    }

    private fun saveByteArrayToFile(context: Context, byteArray: ByteArray, filename: String): Result<Uri> {
        val directory = File(context.filesDir, "images")

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, filename)

        FileOutputStream(file).use { fos ->
            fos.write(byteArray)
        }

        return Result.success(FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file)
        )
    }
}