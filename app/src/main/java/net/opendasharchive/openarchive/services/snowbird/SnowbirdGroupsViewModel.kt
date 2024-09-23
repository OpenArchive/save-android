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

    private val _groups = MutableLiveData<List<SnowbirdGroup>>()
    val groups: LiveData<List<SnowbirdGroup>> = _groups

    val isLoading = MutableLiveData(false)

    val client = UnixSocketClient(SnowbirdService.DEFAULT_SOCKET_PATH)
    val api = SnowbirdAPI(client)

    init {
        isLoading.value = true

        CoroutineScope(Dispatchers.IO).launch {
            when (val response = api.getGroups()) {
                is ApiResponse.ListResponse -> {
                    val data = response.data
                    Timber.d("Received data: $data")
                    _groups.postValue(data)
                }
                is ApiResponse.Error -> {
                    Timber.d("Error: ${response.message}")
                }
                else -> Unit
            }

            isLoading.postValue(false)
        }
    }

    fun getItemAtPosition(position: Int): SnowbirdGroup? {
        return _groups.value?.getOrNull(position)
    }
}