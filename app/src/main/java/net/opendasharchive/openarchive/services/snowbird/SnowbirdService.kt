package net.opendasharchive.openarchive.services.snowbird

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.SaveApp
import net.opendasharchive.openarchive.features.main.TabBarActivity
import timber.log.Timber
import java.io.File

class SnowbirdService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 2600
        lateinit var DEFAULT_SOCKET_PATH: String
            private set
    }

    private var serverJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        val socketFile = File(filesDir, "rust_server.sock")
        DEFAULT_SOCKET_PATH = socketFile.absolutePath
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification("Snowbird Server is running"))
        startServer(DEFAULT_SOCKET_PATH)
        return START_STICKY
    }

    override fun onDestroy() {
        stopServer()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(contentText: String): Notification {
        val pendingIntent: PendingIntent =
            Intent(this, TabBarActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE)
            }

        return NotificationCompat.Builder(this, SaveApp.SNOWBIRD_SERVICE_CHANNEL)
            .setContentTitle("Snowbird Service")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.snowbird)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun startServer(socketPath: String) {
        serverJob = serviceScope.launch {
            Timber.d("Starting Snowbird Service")
            val result = SnowbirdBridge.startServer(socketPath)
            Timber.d("Snowbird Service: $result")
        }
    }

    private fun stopServer() {
        serverJob?.cancel()
    }
}