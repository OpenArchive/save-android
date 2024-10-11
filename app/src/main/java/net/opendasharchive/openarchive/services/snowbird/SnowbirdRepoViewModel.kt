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

class SnowbirdRepoViewModel(private val repository: ISnowbirdRepoRepository) : BaseViewModel() {

    val status: StateFlow<SnowbirdServiceStatus> = SnowbirdBridge.getInstance().status
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SnowbirdServiceStatus.BackendInitializing
        )

    data class RepoState(val repos: List<SnowbirdRepo>, val updateCount: Int = 0)

    private val _repoState = MutableStateFlow(RepoState(emptyList()))
    val repoState: StateFlow<RepoState> = _repoState

    private val _repos = MutableStateFlow<List<SnowbirdRepo>>(emptyList())
    val repos: StateFlow<List<SnowbirdRepo>> = _repos.asStateFlow()

    fun fetchRepos(groupId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                processingTracker.trackProcessingWithTimeout(10_000, "fetch_repos") {
                    when (val result = repository.fetchRepos(groupId, forceRefresh)) {
                        is SnowbirdResult.Success -> _repoState.value = RepoState(result.value, _repoState.value.updateCount + 1)
                        is SnowbirdResult.Failure -> currentError = result.error
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = SnowbirdError.TimedOut
            }
        }
    }
}