package net.opendasharchive.openarchive.services.tor

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.SaveApp
import net.opendasharchive.openarchive.features.main.TabBarActivity
import org.torproject.jni.TorService
import timber.log.Timber

class TorForegroundService : TorService() {
    private val _torStatus = MutableStateFlow<TorStatus>(TorStatus.DISCONNECTED)
    val torStatus: StateFlow<TorStatus> = _torStatus.asStateFlow()

    inner class TorServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d("intent = $intent")
            when (intent.action) {
                ACTION_ERROR -> {
                    val errorText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    Timber.d("error = $errorText")
                    _torStatus.value = TorStatus.ERROR
                }
                ACTION_STATUS -> {
                    val status = intent.getStringExtra(EXTRA_STATUS)
                    Timber.d("Tor status = $status")

                    if (status == STATUS_ON) {
                        updateNotification("Connected")
                        _torStatus.value = TorStatus.CONNECTED
                    } else if (status == STATUS_OFF || status == STATUS_STOPPING) {
                        // It might be the case the OFF precedes STOPPING, so let's
                        // bundle it up together here.
                        //
                        if (torStatus.value != TorStatus.DISCONNECTED) {
                            _torStatus.value = TorStatus.DISCONNECTED
                        }
                    }
                }
                else -> Timber.d("Got rogue action: ${intent.action}")
            }
        }
    }

    private var receiver = TorServiceBroadcastReceiver()

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        registerBroadcastRecivers(receiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(SaveApp.TOR_SERVICE_ID, createNotification("Tor is starting"), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(SaveApp.TOR_SERVICE_ID, createNotification("Tor is starting"))
        }
        return START_STICKY
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

        return NotificationCompat.Builder(this, SaveApp.TOR_SERVICE_CHANNEL)
            .setContentTitle("Tor Service")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_tor)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun registerBroadcastRecivers(receiver: BroadcastReceiver) {
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(receiver, IntentFilter(ACTION_STATUS))
    }

    fun updateNotification(status: String) {
        val notification = createNotification(status)
        notificationManager.notify(SaveApp.TOR_SERVICE_ID, notification)
    }
}