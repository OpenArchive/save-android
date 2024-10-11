package net.opendasharchive.openarchive.util

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.opendasharchive.openarchive.db.SnowbirdError
import timber.log.Timber

open class BaseViewModel : ViewModel() {
    protected val processingTracker = ProcessingTracker()
    val isProcessing: StateFlow<Boolean> = processingTracker.isProcessing

    protected val _error = MutableStateFlow<SnowbirdError?>(null)
    val error: StateFlow<SnowbirdError?> = _error.asStateFlow()

    var currentError: SnowbirdError?
        get() = _error.value
        set(value) {
            _error.value = value
            Timber.d("Error set to $value")
        }
}