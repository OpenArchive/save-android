package net.opendasharchive.openarchive.features.backends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.opendasharchive.openarchive.db.Backend
import timber.log.Timber

sealed class SaveStatus {
    object Idle : SaveStatus()
    object Loading : SaveStatus()
    object Success : SaveStatus()
    data class Error(val message: String) : SaveStatus()
}

class BackendViewModel : ViewModel() {
    private val _backend = MutableStateFlow<Backend?>(null)
    val backend: StateFlow<Backend?> = _backend.asStateFlow()

    private val _saveStatus = MutableStateFlow<SaveStatus>(SaveStatus.Idle)
    val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    fun saveNewBackend(newBackend: Backend) {
        viewModelScope.launch {
            _saveStatus.value = SaveStatus.Loading
            try {
                withContext(Dispatchers.IO) {
                    newBackend.save()
                }
                _backend.value = newBackend
                _saveStatus.value = SaveStatus.Success
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    suspend fun updateBackend(update: (Backend) -> Unit) {
        _backend.update { currentBackend ->
            currentBackend?.apply {
                update(this)
                withContext(Dispatchers.IO) {
                    save()
                }
            }
        }
    }

    fun upsertBackend(newBackend: Backend) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val existingBackend = Backend.getById(newBackend.id)

                    if (existingBackend != null) {
                        existingBackend.updateFrom(newBackend)
                        existingBackend.save()
                    } else {
                        newBackend.save()
                    }
                }
                _backend.value = newBackend
            } catch (e: Exception) {
                Timber.d("Error = $e")
            }
        }
    }
}