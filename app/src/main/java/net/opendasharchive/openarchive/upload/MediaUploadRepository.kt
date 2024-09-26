package net.opendasharchive.openarchive.upload

import net.opendasharchive.openarchive.db.Media

class MediaUploadRepository(private val manager: MediaUploadManager) {
    fun scheduleUpload(media: Media) = manager.scheduleUpload(media)
    fun observeUploads() = manager.observeUploads()
}