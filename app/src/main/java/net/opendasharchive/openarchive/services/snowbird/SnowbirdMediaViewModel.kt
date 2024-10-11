package net.opendasharchive.openarchive.services.snowbird

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.db.SnowbirdRepo
import net.opendasharchive.openarchive.util.BaseViewModel
import net.opendasharchive.openarchive.util.trackProcessingWithTimeout

class SnowbirdMediaViewModel(private val repository: ISnowbirdRepoRepository) : BaseViewModel() {

    val status: StateFlow<SnowbirdServiceStatus> = SnowbirdBridge.getInstance().status
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SnowbirdServiceStatus.BackendInitializing
        )

    data class MediaState(val repos: List<SnowbirdRepo>, val updateCount: Int = 0)

    private val _mediaState = MutableStateFlow(MediaState(emptyList()))
    val mediaState: StateFlow<MediaState> = _mediaState

    fun fetchMedia(repoId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                processingTracker.trackProcessingWithTimeout(10_000, "fetch_media") {
                    when (val result = repository.fetchRepos(repoId, forceRefresh)) {
                        is SnowbirdResult.Success -> _mediaState.value = MediaState(result.value, _mediaState.value.updateCount + 1)
                        is SnowbirdResult.Failure -> currentError = result.error
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = SnowbirdError.TimedOut
            }
        }
    }
}