package net.opendasharchive.openarchive.features.backends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.opendasharchive.openarchive.features.backends.BackendListViewModel.Companion.Filter

class BackendListViewModelFactory(private val filter: Filter) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BackendListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BackendListViewModel(filter) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}