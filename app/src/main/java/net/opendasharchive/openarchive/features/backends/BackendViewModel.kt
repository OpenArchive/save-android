package net.opendasharchive.openarchive.features.backends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.opendasharchive.openarchive.db.Backend

class BackendViewModel(private val filter: Filter = Filter.ALL) : ViewModel() {
    companion object {
        private val ALL_BACKENDS = listOf(
            Backend(Backend.Type.INTERNET_ARCHIVE),
            Backend(Backend.Type.WEBDAV),
            Backend(Backend.Type.GDRIVE),
        )

        enum class Filter() {
            ALL, CONNECTED
        }
    }

    private val _backends = MutableLiveData<List<Backend>>()
    val backends: LiveData<List<Backend>> = _backends

    init {
        _backends.value = if (filter == Filter.ALL) {
            ALL_BACKENDS // .sortedBy { it.friendlyName }
        } else {
            Backend.getAll().asSequence().toList() // .sortedBy { it.friendlyName }
        }
    }
}