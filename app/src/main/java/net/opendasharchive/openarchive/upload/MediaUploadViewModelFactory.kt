package net.opendasharchive.openarchive.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MediaUploadViewModelFactory(private val repository: MediaUploadRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MediaUploadViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MediaUploadViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}