package net.opendasharchive.openarchive.features.backends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.opendasharchive.openarchive.db.Backend
import timber.log.Timber

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
            Backend.getAll().toList()
        }
    }

    fun deleteBackend(backend: Backend) {
        Timber.d("Deleting backend ID ${backend.id}")
        backend.delete()
        _backends.value = _backends.value?.filter { it.id != backend.id }
    }

}