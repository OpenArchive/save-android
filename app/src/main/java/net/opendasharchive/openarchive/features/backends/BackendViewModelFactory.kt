package net.opendasharchive.openarchive.features.backends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.opendasharchive.openarchive.features.backends.BackendViewModel.Companion.Filter

class BackendViewModelFactory(private val filter: Filter) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BackendViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BackendViewModel(filter) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}