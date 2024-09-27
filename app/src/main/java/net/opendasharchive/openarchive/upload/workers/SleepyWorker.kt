package net.opendasharchive.openarchive.upload.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlin.random.Random

class SleepyWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        const val KEY_SLEEP_DURATION = "sleep_duration"
        const val KEY_UPLOAD_RESULT = "upload_result"
    }

    override fun doWork(): Result {
        val sleepDuration = inputData.getLong(KEY_SLEEP_DURATION, 5000)

        // Simulate work by sleeping
        //
        try {
            Thread.sleep(sleepDuration)
        } catch (e: InterruptedException) {
            return Result.failure()
        }

        // Simulate 90% success rate
        //
        return if (Random.nextDouble() < 0.9) {
            Result.success(workDataOf(KEY_UPLOAD_RESULT to "Upload successful"))
        } else {
            Result.failure(workDataOf(KEY_UPLOAD_RESULT to "Upload failed"))
        }
    }
}