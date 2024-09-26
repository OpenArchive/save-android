package net.opendasharchive.openarchive.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.services.Conduit
import timber.log.Timber

class MediaUploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val mediaId = inputData.getLong(KEY_MEDIA_ID, 0)

        val media = Media.get(mediaId)
            ?: return@withContext Result.failure()

        val uploadResult = uploadMedia(media)

        if (uploadResult) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    private suspend fun uploadMedia(media: Media): Boolean {
        Timber.d("Starting upload ${media.originalFilePath}")

        return try {
            val conduit = Conduit.get(media, applicationContext) ?: return false

            conduit.upload()

            Timber.d("Upload successful")

            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        const val KEY_MEDIA_ID = "KEY_MEDIA_ID"
    }
}
