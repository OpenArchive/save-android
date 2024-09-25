package net.opendasharchive.openarchive.upload

import androidx.work.WorkInfo

data class MediaUploadItem(
    val id: String,
    val fileName: String,
    val workInfo: WorkInfo?
)