package net.opendasharchive.openarchive

import android.content.Context
import androidx.multidex.MultiDex
import com.orm.SugarApp
import info.guardianproject.netcipher.proxy.OrbotHelper
import net.opendasharchive.openarchive.core.di.coreModule
import net.opendasharchive.openarchive.core.di.featuresModule
import net.opendasharchive.openarchive.upload.UploadService
import net.opendasharchive.openarchive.util.Analytics
import net.opendasharchive.openarchive.util.Prefs
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

//        val config = ImagePipelineConfig.newBuilder(this)
//            .setProgressiveJpegConfig(SimpleProgressiveJpegConfig())
//            .setResizeAndRotateEnabledForNetwork(true)
//            .setDownsampleEnabled(true)
//            .build()

//        Fresco.initialize(this, config)

        Analytics.init(this)

        Prefs.load(this)

//        val intent = Intent(this, SnowbirdService::class.java)
//        startForegroundService(intent)

        UploadService.startUploadService(this)

        if (Prefs.useTor) initNetCipher()

        Theme.set(Prefs.theme)

        Timber.d("Starting app $packageName ")
    }

    private fun initNetCipher() {
        Timber.d( "Initializing NetCipher client")
        val oh = OrbotHelper.get(this)

        if (BuildConfig.DEBUG) {
            oh.skipOrbotValidation()
        }

//        oh.init()
    }
}
