package net.opendasharchive.openarchive.services.snowbird

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.db.SnowbirdRepo
import net.opendasharchive.openarchive.util.BaseViewModel
import net.opendasharchive.openarchive.util.trackProcessingWithTimeout
import timber.log.Timber

class SnowbirdRepoViewModel(private val repository: ISnowbirdRepoRepository) : BaseViewModel() {

    val status: StateFlow<SnowbirdServiceStatus> = SnowbirdBridge.getInstance().status
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SnowbirdServiceStatus.BackendInitializing
        )

    private val _repos = MutableStateFlow<List<SnowbirdRepo>>(emptyList())
    val repos: StateFlow<List<SnowbirdRepo>> = _repos.asStateFlow()

    private val _error = MutableStateFlow<SnowbirdError?>(null)
    val error: StateFlow<SnowbirdError?> = _error.asStateFlow()

    var currentError: SnowbirdError?
        get() = _error.value
        set(value) {
            _error.value = value
            Timber.d("Error set to $value")
        }

    fun fetchRepos(groupId: String) {
        viewModelScope.launch {
            try {
                processingTracker.trackProcessingWithTimeout(10_000, "fetch_repos") {
                    when (val result = repository.fetchRepos(groupId)) {
                        is SnowbirdResult.Success -> _repos.value = result.value
                        is SnowbirdResult.Failure -> currentError = result.error
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = SnowbirdError.TimedOut
            }
        }
    }
}