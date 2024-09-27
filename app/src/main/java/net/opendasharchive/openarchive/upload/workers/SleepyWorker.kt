package net.opendasharchive.openarchive.upload.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.random.Random

class SleepyWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_SLEEP_DURATION = "sleep_duration"
        const val KEY_UPLOAD_RESULT = "upload_result"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Timber.d("Starting work")

        val sleepDuration = inputData.getLong(KEY_SLEEP_DURATION, 5000)

        // Simulate work by sleeping
        //
        try {
            Thread.sleep(sleepDuration)
        } catch (e: InterruptedException) {
            Result.failure()
        }

        // Simulate 90% success rate
        //
        if (Random.nextDouble() < 0.9) {
            Timber.d("Success")
            Result.success(workDataOf(KEY_UPLOAD_RESULT to "Upload successful"))
        } else {
            Timber.d("Failed")
            Result.failure(workDataOf(KEY_UPLOAD_RESULT to "Upload failed"))
        }
    }
}