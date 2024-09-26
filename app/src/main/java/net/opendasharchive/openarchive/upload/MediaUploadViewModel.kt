package net.opendasharchive.openarchive.upload

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.db.Media

class MediaUploadViewModel(private val repository: MediaUploadRepository) : ViewModel() {
    private val _uploadItems = MediatorLiveData<List<MediaUploadItem>>()
    val uploadItems: LiveData<List<MediaUploadItem>> = _uploadItems

    init {
        _uploadItems.addSource(repository.observeUploads()) { workInfoList ->
            updateUploadItems(workInfoList)
        }
    }

    private fun updateUploadItems(workInfoList: List<WorkInfo>) {
        val currentList = _uploadItems.value ?: emptyList()
        val updatedList = currentList.toMutableList()

        workInfoList.forEach { workInfo ->
            val index = updatedList.indexOfFirst { it.id == workInfo.id.toString() }
            if (index != -1) {
                updatedList[index] = updatedList[index].copy(workInfo = workInfo)
            } else {
                updatedList.add(MediaUploadItem(workInfo.id.toString(), "File ${workInfo.id}", workInfo))
            }
        }

        _uploadItems.value = updatedList
    }

    fun scheduleUpload(media: Media) {
        viewModelScope.launch {
            repository.scheduleUpload(media)
        }
    }
}