package net.opendasharchive.openarchive

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.Toast
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
import org.torproject.jni.TorService
import org.torproject.jni.TorService.LocalBinder
import timber.log.Timber

class SaveApp : SugarApp() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

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

        if (Prefs.useTor) {
            startTor()
        }

        Theme.set(Prefs.theme)

        Timber.d("Starting app $packageName ")
    }

    private fun startTor() {
        bindService(Intent(this, TorService::class.java), object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val torService = (service as LocalBinder).service

                while (torService.torControlConnection == null) {
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }

                Toast.makeText(this@SaveApp, "Got Tor control connection", Toast.LENGTH_LONG).show()
            }

            override fun onServiceDisconnected(name: ComponentName) {
            }
        }, BIND_AUTO_CREATE)
    }
}
