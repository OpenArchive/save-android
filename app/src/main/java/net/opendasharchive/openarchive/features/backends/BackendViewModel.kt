package net.opendasharchive.openarchive.features.backends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.opendasharchive.openarchive.db.Backend

class BackendViewModel(private val filter: Filter = Filter.ALL) : ViewModel() {
    companion object {
        enum class Filter() {
            ALL, CONNECTED
        }
    }

    private val _backends = MutableLiveData<List<Backend>>()
    val backends: LiveData<List<Backend>> = _backends

    init {
        _backends.value = if (filter == Filter.ALL) {
            Backend.ALL_BACKENDS
        } else {
            Backend.getAll().asSequence().toList()
        }
    }
}