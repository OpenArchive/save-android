package net.opendasharchive.openarchive.db

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface IMediaRepository {
    val saveFlow: SharedFlow<Media>
    suspend fun save(media: Media)
}

class MediaRepository : IMediaRepository {
    private val _saveFlow = MutableSharedFlow<Media>()
    override val saveFlow: SharedFlow<Media> = _saveFlow.asSharedFlow()

    override suspend fun save(media: Media) {
        media.save()
        _saveFlow.emit(media)
    }
}
