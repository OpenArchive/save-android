package net.opendasharchive.openarchive.services.snowbird

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.features.main.ApiResponse
import net.opendasharchive.openarchive.util.BaseViewModel
import net.opendasharchive.openarchive.util.trackProcessingWithTimeout
import timber.log.Timber

class SnowbirdViewModel(val api: SnowbirdAPI) : BaseViewModel() {

    val status: StateFlow<SnowbirdServiceStatus> = SnowbirdBridge.getInstance().status
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SnowbirdServiceStatus.BackendInitializing
        )

    private val _group = MutableStateFlow<SnowbirdGroup?>(null)
    val group: StateFlow<SnowbirdGroup?> = _group.asStateFlow()

    private val _groups = MutableStateFlow<List<SnowbirdGroup>>(emptyList())
    val groups: StateFlow<List<SnowbirdGroup>> = _groups.asStateFlow()

    private val _users = MutableStateFlow<List<SnowbirdUser>>(emptyList())
    val users: StateFlow<List<SnowbirdUser>> = _users.asStateFlow()

    private val _error = MutableStateFlow<ApiError?>(null)
    val error: StateFlow<ApiError?> = _error.asStateFlow()

    fun fetchGroup(groupId: String) {
        viewModelScope.launch {
            try {
                processingTracker.trackProcessingWithTimeout(10_000, "fetch_group") {
                    when (val response = api.fetchGroup(groupId)) {
                        is ApiResponse.SingleResponse -> _group.value = response.data
                        is ApiResponse.ErrorResponse -> _error.value = response.error
                        else -> _error.value = ApiError.UnexpectedError("Unexpected response type")
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = ApiError.TimedOut
            }
        }
    }

    fun fetchGroups() {
        viewModelScope.launch {
            try {
                processingTracker.trackProcessingWithTimeout(10_000, "fetch_groups") {
                    when (val response = api.fetchGroups()) {
                        is ApiResponse.ListResponse -> _groups.value = response.data
                        is ApiResponse.ErrorResponse -> _error.value = response.error
                        else -> _error.value = ApiError.UnexpectedError("Unexpected response type")
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = ApiError.TimedOut
            }
        }
    }

    fun fetchUsers(groupId: String) {
        viewModelScope.launch {
            try {
                processingTracker.trackProcessingWithTimeout(10_000, "fetch_users") {
                    when (val response = api.fetchUsers(groupId)) {
                        is ApiResponse.ListResponse -> _users.value = response.data
                        is ApiResponse.ErrorResponse -> _error.value = response.error
                        else -> _error.value = ApiError.UnexpectedError("Unexpected response type")
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = ApiError.TimedOut
            }
        }
    }

    fun createGroup(groupName: String) {
        viewModelScope.launch {
            try {
                processingTracker.trackProcessingWithTimeout(10_000, "create_group") {
                    when (val response = api.createGroup(groupName)) {
                        is ApiResponse.SingleResponse -> {
                            Timber.d("response = $response")
                            _group.value = response.data
                            _groups.value += response.data
                        }

                        is ApiResponse.ErrorResponse -> {
                            Timber.d("response = $response")
                            _error.value = response.error
                        }

                        else -> {
                            Timber.d("error response = $response")
                            _error.value = ApiError.UnexpectedError("Unexpected response type")
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = ApiError.TimedOut
            }
        }
    }
}