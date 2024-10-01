package net.opendasharchive.openarchive

import TorViewModel
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.multidex.MultiDex
import com.orm.SugarApp
import net.opendasharchive.openarchive.core.di.coreModule
import net.opendasharchive.openarchive.core.di.featuresModule
import net.opendasharchive.openarchive.upload.MediaUploadManager
import net.opendasharchive.openarchive.util.Analytics
import net.opendasharchive.openarchive.util.Prefs
import net.opendasharchive.openarchive.util.ProofModeHelper
import net.opendasharchive.openarchive.util.Theme
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import org.koin.java.KoinJavaComponent.get as getKoin

class SaveApp : SugarApp() {

    lateinit var torViewModel: TorViewModel
        private set

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        MediaUploadManager.initialize(this)

        startKoin {
            androidContext(this@SaveApp)
            modules(coreModule, featuresModule)
        }

        if (BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }

        Analytics.init(this)

        Prefs.load(this)

//        val intent = Intent(this, SnowbirdService::class.java)
//        startForegroundService(intent)

        ProofModeHelper.init(this) {
            // Check for any queued uploads and restart, only after ProofMode is correctly initialized.
            // UploadService.startUploadService(this)
            MediaUploadManager.initialize(this)
        }

        initializeTorViewModel()

        createNotificationChannel()

        Theme.set(Prefs.theme)

        Timber.d("Starting app $packageName ")
    }

    private fun initializeTorViewModel() {
        val torViewModel: TorViewModel = getKoin(TorViewModel::class.java)
        torViewModel.updateTorServiceState()
    }

    private fun createNotificationChannel() {
        val name = "Tor Service"
        val descriptionText = "Keeps the Tor service running"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(TOR_SERVICE_CHANNEL, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createSnowbirdNotificationChannel() {
        val name = "Snowbird Service"
        val descriptionText = "Keeps the Snowbird server running"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(SNOWBIRD_SERVICE_CHANNEL, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val SNOWBIRD_SERVICE_ID = 2601
        const val SNOWBIRD_SERVICE_CHANNEL = "snowbird_service_channel"

        const val TOR_SERVICE_ID = 2602
        const val TOR_SERVICE_CHANNEL = "tor_service_channel"
    }
}
