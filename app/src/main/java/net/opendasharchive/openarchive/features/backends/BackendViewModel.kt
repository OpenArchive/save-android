package net.opendasharchive.openarchive.features.backends

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.opendasharchive.openarchive.db.Backend

class BackendViewModel : ViewModel() {
    private val _backend = MutableStateFlow(Backend())
    val backend: StateFlow<Backend> = _backend.asStateFlow()

    fun updateBackend(update: (Backend) -> Backend) {
        _backend.update(update)
    }
}