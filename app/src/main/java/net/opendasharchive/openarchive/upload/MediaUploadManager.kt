package net.opendasharchive.openarchive.upload

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.BackoffPolicy
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import net.opendasharchive.openarchive.db.Media
import java.util.concurrent.TimeUnit

object WorkTags {
    const val MEDIA_UPLOAD = "media_upload"
}

object MediaUploadManager {
    private lateinit var applicationContext: Context

    fun initialize(context: Context) {
        applicationContext = context.applicationContext

        val config = Configuration.Builder()
            .setMaxSchedulerLimit(1)
            .build()

        WorkManager.initialize(context, config)
    }

    fun scheduleUpload(media: Media) {
        if (!::applicationContext.isInitialized) {
            throw IllegalStateException("MediaUploadManager is not initialized. Call initialize() first.")
        }

        val uploadWorkRequest = OneTimeWorkRequestBuilder<MediaUploadWorker>()
            .setInputData(workDataOf(MediaUploadWorker.KEY_MEDIA_ID to media.id))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag(WorkTags.MEDIA_UPLOAD)
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "${WorkTags.MEDIA_UPLOAD}_${System.currentTimeMillis()}",
                ExistingWorkPolicy.REPLACE,
                uploadWorkRequest
            )
    }

    fun observeUploads(): LiveData<List<WorkInfo>> {
        val workManager = WorkManager.getInstance(applicationContext)
        return workManager.getWorkInfosByTagLiveData(WorkTags.MEDIA_UPLOAD)
    }
}