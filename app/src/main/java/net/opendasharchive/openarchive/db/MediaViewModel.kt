package net.opendasharchive.openarchive.db

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class MediaViewModel(private val mediaRepository: IMediaRepository) : ViewModel() {
    val saveFlow: SharedFlow<Media> = mediaRepository.saveFlow

    fun saveMedia(media: Media) {
        viewModelScope.launch {
            mediaRepository.save(media)
        }
    }
}