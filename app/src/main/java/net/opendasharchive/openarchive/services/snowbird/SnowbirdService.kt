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
import java.nio.file.Files
import kotlin.io.path.Path

class SnowbirdService : Service() {

    companion object {
        var DEFAULT_BACKEND_DIRECTORY = ""
            private set

        var DEFAULT_SOCKET_PATH = ""
            private set
    }

    private var serverJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        val backendBaseDirectory = filesDir
        DEFAULT_BACKEND_DIRECTORY = backendBaseDirectory.absolutePath

        val serverSocketFile = File(filesDir, "rust_server.sock")
        DEFAULT_SOCKET_PATH = serverSocketFile.absolutePath

        val path = Path(serverSocketFile.absolutePath)

        try {
            Files.delete(path)
        } catch (e: Exception) {
            // ignore
        } finally {
            Files.createFile(path)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(SaveApp.SNOWBIRD_SERVICE_ID, createNotification("Snowbird Server is running"))
        startServer(DEFAULT_BACKEND_DIRECTORY, DEFAULT_SOCKET_PATH)
        return START_STICKY
    }

    override fun onDestroy() {
        stopServer()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(text: String): Notification {
        val pendingIntent: PendingIntent = Intent(this, TabBarActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        return NotificationCompat.Builder(this, SaveApp.SNOWBIRD_SERVICE_CHANNEL)
            .setContentTitle("Snowbird Service")
            .setContentText(text)
            .setSmallIcon(R.drawable.baseline_scatter_plot_24)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun startServer(baseDirectory: String, socketPath: String) {
        serverJob = serviceScope.launch {
            Timber.d("Starting Snowbird Service")
            val result = SnowbirdBridge.getInstance().startServer(applicationContext, baseDirectory, socketPath)
            Timber.d("Snowbird Service: $result")
        }
    }

    private fun stopServer() {
        serverJob?.cancel()
    }
}