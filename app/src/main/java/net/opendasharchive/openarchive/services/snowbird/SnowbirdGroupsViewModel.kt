package net.opendasharchive.openarchive.services.snowbird

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.features.main.ApiResponse
import net.opendasharchive.openarchive.features.main.UnixSocketClient
import timber.log.Timber

class SnowbirdGroupsViewModel : ViewModel() {
    companion object {
        private val MOCK_GROUPS = listOf(
            SnowbirdGroup("Veilid Group 1"),
            SnowbirdGroup("Veilid Group 2"),
            SnowbirdGroup("Veilid Group 3")
        )
    }

    private val _groups = MutableLiveData<List<SnowbirdGroup>>()
    val groups: LiveData<List<SnowbirdGroup>> = _groups

    val isLoading = MutableLiveData(false)

    val client = UnixSocketClient(SnowbirdService.DEFAULT_SOCKET_PATH)
    val api = SnowbirdAPI(client)

    init {
        // _groups.value = MOCK_GROUPS

        isLoading.value = true

        CoroutineScope(Dispatchers.IO).launch {
            when (val response = api.getGroups()) {
                is ApiResponse.Success -> {
                    val data = response.data
                    Timber.d("Received data: $data")
                    _groups.postValue(data)
                }
                is ApiResponse.Error -> {
                    Timber.d("Error: ${response.message}")
                }
            }

            isLoading.postValue(false)
        }
    }

    fun getItemAtPosition(position: Int): SnowbirdGroup? {
        return _groups.value?.getOrNull(position)
    }
}