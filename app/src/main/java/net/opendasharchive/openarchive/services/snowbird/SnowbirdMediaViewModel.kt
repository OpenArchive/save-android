package net.opendasharchive.openarchive.services.snowbird

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.db.SnowbirdMediaItem
import net.opendasharchive.openarchive.util.BaseViewModel
import net.opendasharchive.openarchive.util.trackProcessingWithTimeout

class SnowbirdMediaViewModel(private val repository: ISnowbirdMediaRepository) : BaseViewModel() {

    sealed class MediaState {
        data object Idle : MediaState()
        data object Loading : MediaState()
        data class Success(val media: List<SnowbirdMediaItem>) : MediaState()
        data class Error(val error: SnowbirdError) : MediaState()
    }

    private val _mediaState = MutableStateFlow<MediaState>(MediaState.Idle)
    val mediaState: StateFlow<MediaState> = _mediaState.asStateFlow()

    fun fetchMedia(groupKey: String, repoKey: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _mediaState.value = MediaState.Loading
            try {
                val result = processingTracker.trackProcessingWithTimeout(30_000, "fetch_media") {
                    repository.fetchMedia(groupKey, repoKey, forceRefresh)
                }

                _mediaState.value = when (result) {
                    is SnowbirdResult.Success -> MediaState.Success(result.value)
                    is SnowbirdResult.Failure -> MediaState.Error(result.error)
                }
            } catch (e: TimeoutCancellationException) {
                _mediaState.value = MediaState.Error(SnowbirdError.TimedOut)
            }
        }
    }
}