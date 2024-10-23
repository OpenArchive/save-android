package net.opendasharchive.openarchive.upload

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.upload.WorkTags.MEDIA_ID_PREFIX
import net.opendasharchive.openarchive.upload.workers.MediaUploadWorker
import net.opendasharchive.openarchive.upload.workers.SleepyWorker
import java.util.concurrent.TimeUnit

object WorkTags {
    const val MEDIA_ID_PREFIX = "media:"
    const val MEDIA_UPLOAD = "media_upload"
}

object MediaUploadManager {
    private lateinit var applicationContext: Context

    private inline fun <reified T : ListenableWorker> getWorkRequest(media: Media) =
        OneTimeWorkRequestBuilder<T>()
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
            .addTag("${MEDIA_ID_PREFIX}${media.id}")
            .build()

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    fun observeUploads(): Flow<List<WorkInfo>> {
        val workManager = WorkManager.getInstance(applicationContext)
        return workManager.getWorkInfosByTagLiveData(WorkTags.MEDIA_UPLOAD).asFlow()
    }

    fun scheduleUpload(media: Media) {
        if (!::applicationContext.isInitialized) {
            throw IllegalStateException("MediaUploadManager is not initialized. Call initialize() first.")
        }

        val uploadWorkRequest = getWorkRequest<SleepyWorker>(media)

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "${WorkTags.MEDIA_UPLOAD}_${System.currentTimeMillis()}",
                ExistingWorkPolicy.REPLACE,
                uploadWorkRequest
            )
    }
}