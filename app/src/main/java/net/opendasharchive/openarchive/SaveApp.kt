package net.opendasharchive.openarchive

import android.content.Context
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig
import com.orm.SugarApp
import info.guardianproject.netcipher.proxy.OrbotHelper
import net.opendasharchive.openarchive.core.di.coreModule
import net.opendasharchive.openarchive.core.di.featuresModule
import net.opendasharchive.openarchive.util.Prefs
import net.opendasharchive.openarchive.util.Theme
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber


class SaveApp : SugarApp() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SaveApp)
            modules(coreModule, featuresModule)
        }

        val config = ImagePipelineConfig.newBuilder(this)
            .setProgressiveJpegConfig(SimpleProgressiveJpegConfig())
            .setResizeAndRotateEnabledForNetwork(true)
            .setDownsampleEnabled(true)
            .build()

        Fresco.initialize(this, config)
        Prefs.load(this)

        if (Prefs.useTor) initNetCipher()

        Theme.set(Prefs.theme)

        CleanInsightsManager.init(this)

        // enable timber logging library for debug builds
        if (BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
            Timber.tag("SAVE")
        }
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
