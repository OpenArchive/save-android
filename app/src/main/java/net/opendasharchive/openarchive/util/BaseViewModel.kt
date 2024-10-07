package net.opendasharchive.openarchive.util

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

open class BaseViewModel : ViewModel() {
    protected val processingTracker = ProcessingTracker()
    val isProcessing: StateFlow<Boolean> = processingTracker.isProcessing
}