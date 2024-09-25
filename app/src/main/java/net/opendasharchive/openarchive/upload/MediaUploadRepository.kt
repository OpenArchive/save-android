package net.opendasharchive.openarchive.upload

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf

class MediaUploadRepository(private val workManager: WorkManager) {
    fun observeUploads(): LiveData<List<WorkInfo>> {
        return workManager.getWorkInfosByTagLiveData("media_upload")
    }

    fun scheduleUpload(fileUri: Uri) {
        val uploadWork = OneTimeWorkRequestBuilder<MediaUploadWorker>()
            .setInputData(workDataOf(MediaUploadWorker.KEY_MEDIA_URI to fileUri.toString()))
            .addTag("media_upload")
            .build()
        workManager.enqueue(uploadWork)
    }
}