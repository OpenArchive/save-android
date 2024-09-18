package net.opendasharchive.openarchive.services.snowbird

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.features.main.TabBarActivity
import timber.log.Timber
import java.io.File

class SnowbirdService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 2600
        private const val NOTIFICATION_CHANNEL_ID = "SnowbirdServerChannel"
        lateinit var DEFAULT_SOCKET_PATH: String
            private set
    }

    private var serverJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        DEFAULT_SOCKET_PATH = File(filesDir, "rust_server.sock").absolutePath
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

    private fun createNotificationChannel() {
        val name = "Snowbird Service"
        val descriptionText = "Keeps the Snowbird server running"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(contentText: String): Notification {
        val pendingIntent: PendingIntent =
            Intent(this, TabBarActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE)
            }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Rust Server")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.snowbird)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun startServer(socketPath: String) {
        serverJob = serviceScope.launch {
            val result = SnowbirdBridge.startServer(socketPath)
            Timber.d("Snowbird Service: $result")
        }
    }

    private fun stopServer() {
        serverJob?.cancel()
    }
}