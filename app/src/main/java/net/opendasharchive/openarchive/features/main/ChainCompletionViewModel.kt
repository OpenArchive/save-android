package net.opendasharchive.openarchive.features.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChainCompletionViewModel : ViewModel() {
    private val _isChainCompleted = MutableLiveData<Boolean>()
    val isChainCompleted: LiveData<Boolean> = _isChainCompleted

    fun signalCompletion() {
        _isChainCompleted.value = true
    }
}