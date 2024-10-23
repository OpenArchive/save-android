package net.opendasharchive.openarchive.features.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.opendasharchive.openarchive.db.Backend
import timber.log.Timber

class FolderListViewModelFactory(private val backend: Backend?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FolderListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FolderListViewModel(backend) as T
        }

        Timber.d("is not Assignable")
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}