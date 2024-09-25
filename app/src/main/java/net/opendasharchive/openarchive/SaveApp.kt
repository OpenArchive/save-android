package net.opendasharchive.openarchive

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

        Theme.set(Prefs.theme)

        Timber.d("Starting app $packageName ")
    }
}
