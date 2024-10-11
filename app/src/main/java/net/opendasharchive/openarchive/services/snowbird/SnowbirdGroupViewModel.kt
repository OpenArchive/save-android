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
import net.opendasharchive.openarchive.db.SnowbirdGroup
import net.opendasharchive.openarchive.util.BaseViewModel
import net.opendasharchive.openarchive.util.trackProcessingWithTimeout
import timber.log.Timber

class SnowbirdGroupViewModel(private val repository: ISnowbirdGroupRepository) : BaseViewModel() {

    val status: StateFlow<SnowbirdServiceStatus> = SnowbirdBridge.getInstance().status
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SnowbirdServiceStatus.BackendInitializing
        )

    data class GroupState(val groups: List<SnowbirdGroup>, val updateCount: Int = 0)

    private val _groupState = MutableStateFlow(GroupState(emptyList()))
    val groupState: StateFlow<GroupState> = _groupState

    private val _group = MutableStateFlow<SnowbirdGroup?>(null)
    val group: StateFlow<SnowbirdGroup?> = _group.asStateFlow()

    private val _groups = MutableStateFlow<List<SnowbirdGroup>>(emptyList())
    val groups: StateFlow<List<SnowbirdGroup>> = _groups.asStateFlow()

    private val _error = MutableStateFlow<SnowbirdError?>(null)
    val error: StateFlow<SnowbirdError?> = _error.asStateFlow()

    var currentError: SnowbirdError?
        get() = _error.value
        set(value) {
            _error.value = value
            Timber.d("Error set to $value")
        }

    fun fetchGroup(groupId: String) {
        viewModelScope.launch {
            try {
                processingTracker.trackProcessingWithTimeout(10_000, "fetch_group") {
                    when (val result = repository.fetchGroup(groupId)) {
                        is SnowbirdResult.Success -> _group.value = result.value
                        is SnowbirdResult.Failure -> currentError = result.error
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = SnowbirdError.TimedOut
            }
        }
    }

    fun fetchGroups(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                processingTracker.trackProcessingWithTimeout(10_000, "fetch_groups") {
                    when (val result = repository.fetchGroups(forceRefresh)) {
                        is SnowbirdResult.Success -> {
                            _groupState.value = GroupState(result.value, _groupState.value.updateCount + 1)
                        }
                        is SnowbirdResult.Failure -> currentError = result.error
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = SnowbirdError.TimedOut
            }
        }
    }

    fun createGroup(groupName: String) {
        viewModelScope.launch {
            try {
                processingTracker.trackProcessingWithTimeout(10_000, "create_group") {
                    when (val result = repository.createGroup(groupName)) {
                        is SnowbirdResult.Success -> {
                            _groups.value += result.value
                            _group.value = result.value
                        }
                        is SnowbirdResult.Failure -> currentError = result.error
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = SnowbirdError.TimedOut
            }
        }
    }
}