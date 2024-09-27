package net.opendasharchive.openarchive.upload

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import net.opendasharchive.openarchive.db.Media

class MediaUploadRepository(private val manager: MediaUploadManager) {
    fun scheduleUpload(media: Media) = manager.scheduleUpload(media)
    fun observeUploads() = manager.observeUploads()
    fun getMediaItems(): Flow<List<Media>> = flow {
        emit(Media.getAll())
    }.flowOn(Dispatchers.IO)
}