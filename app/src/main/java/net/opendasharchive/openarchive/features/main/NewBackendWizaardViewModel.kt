package net.opendasharchive.openarchive.features.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.util.OAEvent

class NewBackendWizaardViewModel : ViewModel() {
    private val _backend = MutableLiveData<Backend>()
    val backend: LiveData<Backend> = _backend

    private val _navigationEvent = MutableLiveData<OAEvent<Int>>()
    val navigationEvent: LiveData<OAEvent<Int>> = _navigationEvent

    fun updateDataAndNavigate(updatedBackend: Backend) {
        _backend.value = updatedBackend
        determineNextStep()
    }

    private fun determineNextStep() {
        val backend = _backend.value ?: return
        val nextDestination = when {
            backend.hasValidType() -> R.id.help_text_header
            else -> R.id.help_text_header
        }
        _navigationEvent.value = OAEvent(nextDestination)
    }
}