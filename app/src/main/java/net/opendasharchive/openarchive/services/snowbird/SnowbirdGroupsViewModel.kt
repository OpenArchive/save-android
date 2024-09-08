package net.opendasharchive.openarchive.services.snowbird

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

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

    init {
        _groups.value = MOCK_GROUPS
    }

    fun getItemAtPosition(position: Int): SnowbirdGroup? {
        return _groups.value?.getOrNull(position)
    }
}