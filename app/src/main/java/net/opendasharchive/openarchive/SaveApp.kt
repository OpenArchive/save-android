package net.opendasharchive.openarchive

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.multidex.MultiDex
import coil.Coil
import coil.ImageLoader
import coil.util.Logger
import com.orm.SugarApp
import net.opendasharchive.openarchive.core.di.coreModule
import net.opendasharchive.openarchive.core.di.featuresModule
import net.opendasharchive.openarchive.extensions.getViewModel
import net.opendasharchive.openarchive.services.tor.TorViewModel
import net.opendasharchive.openarchive.upload.MediaUploadManager
import net.opendasharchive.openarchive.util.Analytics
import net.opendasharchive.openarchive.util.Prefs
import net.opendasharchive.openarchive.util.ProofModeHelper
import net.opendasharchive.openarchive.util.Theme
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class SaveApp : SugarApp() {

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

        val imageLoader = ImageLoader.Builder(this)
            .logger(object : Logger {
                override var level = Log.VERBOSE

                override fun log(tag: String, priority: Int, message: String?, throwable: Throwable?) {
                    Timber.tag("Coil").log(priority, throwable, message)
                }
            })
            .build()

        Coil.setImageLoader(imageLoader)

        Analytics.init(this)

        Prefs.load(this)

        ProofModeHelper.init(this) {
            // Check for any queued uploads and restart, only after ProofMode is correctly initialized.
            // UploadService.startUploadService(this)
            MediaUploadManager.initialize(this)
        }

        initializeTorViewModel()

        createTorNotificationChannel()
        createSnowbirdNotificationChannel()

        Theme.set(Prefs.theme)

        Timber.d("Starting app $packageName ")
    }

    private fun initializeTorViewModel() {
        val torViewModel: TorViewModel = getViewModel(this)
        torViewModel.updateTorServiceState()
    }

    private fun createTorNotificationChannel() {
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
