package net.opendasharchive.openarchive.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit

class MediaUploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val mediaUri = inputData.getString(KEY_MEDIA_URI)
            ?: return@withContext Result.failure()

        val uploadResult = uploadMedia(Uri.parse(mediaUri))

        if (uploadResult) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    private suspend fun uploadMedia(mediaUri: Uri): Boolean {
        Timber.d("Starting upload")

        return try {
            // Simulating network delay
            withContext(Dispatchers.IO) {
                Thread.sleep(2000)
            }

            Timber.d("Upload successful")

            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        const val KEY_MEDIA_URI = "KEY_MEDIA_URI"
    }
}

// Singleton MediaUploadManager
object MediaUploadManager {
    private lateinit var applicationContext: Context

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    fun scheduleMediaUpload(mediaUri: Uri) {
        if (!::applicationContext.isInitialized) {
            throw IllegalStateException("MediaUploadManager is not initialized. Call initialize() first.")
        }

        val uploadWorkRequest = OneTimeWorkRequestBuilder<MediaUploadWorker>()
            .setInputData(workDataOf(MediaUploadWorker.KEY_MEDIA_URI to mediaUri.toString()))
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
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "media_upload_${System.currentTimeMillis()}",
                ExistingWorkPolicy.REPLACE,
                uploadWorkRequest
            )
    }


    fun getWorkInfo(workName: String): LiveData<WorkInfo> {
        return WorkManager.getInstance(applicationContext)
            .getWorkInfosForUniqueWorkLiveData(workName)
            .map { workInfoList ->
                workInfoList.firstOrNull() ?: WorkInfo(
                    id = UUID.randomUUID(),
                    state = WorkInfo.State.CANCELLED,
                    outputData = Data.EMPTY,
                    tags = emptySet(),
                    runAttemptCount = 0
                )
            }
    }

    fun observeAllUploads(): LiveData<List<WorkInfo>> {
        val workManager = WorkManager.getInstance(applicationContext)
        return workManager.getWorkInfosByTagLiveData("media_upload")
    }
}