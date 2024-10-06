package net.opendasharchive.openarchive.services.snowbird

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.features.main.ApiResponse
import net.opendasharchive.openarchive.features.main.UnixSocketClient

class SnowbirdViewModel : ViewModel() {

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

    private val _error = MutableStateFlow<ApiError?>(null)
    val error: StateFlow<ApiError?> = _error.asStateFlow()

    val client = UnixSocketClient(SnowbirdService.DEFAULT_SOCKET_PATH)
    val api = SnowbirdAPI(client)

    fun fetchGroup(groupId: String) {
        viewModelScope.launch {
            when (val response = api.getGroup(groupId)) {
                is ApiResponse.SingleResponse -> _group.value = response.data
                is ApiResponse.ErrorResponse -> _error.value = response.error
                else -> _error.value = ApiError.UnexpectedError("Unexpected response type")
            }
        }
    }

    fun fetchGroups() {
        viewModelScope.launch {
            when (val response = api.getGroups()) {
                is ApiResponse.ListResponse -> _groups.value = response.data
                is ApiResponse.ErrorResponse -> _error.value = response.error
                else -> _error.value = ApiError.UnexpectedError("Unexpected response type")
            }
        }
    }

    fun createGroup(group: SnowbirdGroup) {
        viewModelScope.launch {
            when (val response = api.createGroup(group)) {
                is ApiResponse.SingleResponse -> {
                    _group.value = response.data
                    _groups.value += response.data
                }
                is ApiResponse.ErrorResponse -> _error.value = response.error
                else -> _error.value = ApiError.UnexpectedError("Unexpected response type")
            }
        }
    }
}