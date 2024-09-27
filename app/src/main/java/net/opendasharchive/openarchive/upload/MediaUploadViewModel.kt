package net.opendasharchive.openarchive.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.upload.WorkTags.MEDIA_ID_PREFIX

data class MediaWithState(val media: Media, val state: WorkInfo.State?)

//data class MediaUploadItem(
//    val mediaId: String,
//    val state: WorkInfo.State,
//    val progress: Int
//)

class MediaUploadViewModel(private val repository: MediaUploadRepository) : ViewModel() {
    private val _mediaStates = MutableStateFlow<Map<String, WorkInfo.State>>(emptyMap())
    val mediaStates: StateFlow<Map<String, WorkInfo.State>> = _mediaStates.asStateFlow()

    private val _mediaItems = MutableStateFlow<List<Media>>(emptyList())
    val mediaItems: StateFlow<List<Media>> = _mediaItems.asStateFlow()

//    private val _uploadItems = MutableStateFlow<List<MediaUploadItem>>(emptyList())
//    val uploadItems: StateFlow<List<MediaUploadItem>> = _uploadItems.asStateFlow()

    val combinedMediaData: StateFlow<List<MediaWithState>> = combine(mediaItems, mediaStates) { items, states ->
        items.map { media ->
            MediaWithState(media, states[media.id.toString()] ?: WorkInfo.State.ENQUEUED)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            repository.observeUploads().collect { workInfoList ->
                val newStates = workInfoList.mapNotNull { workInfo ->
                    val mediaId = workInfo.tags
                        .find { it.startsWith(MEDIA_ID_PREFIX) }
                        ?.removePrefix(MEDIA_ID_PREFIX)
                    mediaId?.let { it to workInfo.state }
                }.toMap()
                _mediaStates.update { it + newStates }
                // updateUploadItems(workInfoList)
            }
        }

        viewModelScope.launch {
            repository.getMediaItems().collect { items ->
                _mediaItems.value = items
            }
        }
    }

//    private fun updateUploadItems(workInfoList: List<WorkInfo>) {
//        val newUploadItems = workInfoList.mapNotNull { workInfo ->
//            val mediaId = workInfo.tags.find { it.startsWith(MEDIA_ID_PREFIX) }?.removePrefix(MEDIA_ID_PREFIX) ?: return@mapNotNull null
//            MediaUploadItem(
//                fileName = workInfo.,
//                mediaId = mediaId,
//                state = workInfo.state,
//                progress = workInfo.progress.getInt("progress", 0)
//            )
//        }
//        _uploadItems.value = newUploadItems
//    }

    fun scheduleUpload(media: Media) {
        viewModelScope.launch {
            repository.scheduleUpload(media)
        }
    }
}