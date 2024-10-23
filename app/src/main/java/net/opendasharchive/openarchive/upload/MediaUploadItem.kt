package net.opendasharchive.openarchive.upload

import androidx.work.WorkInfo

data class MediaUploadItem(
    val mediaId: String,
    val state: WorkInfo.State,
    val fileName: String,
    val workInfo: WorkInfo?,
    val progress: Int
)