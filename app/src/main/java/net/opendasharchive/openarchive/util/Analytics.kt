package net.opendasharchive.openarchive.util

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import net.opendasharchive.openarchive.R
import org.json.JSONObject
import timber.log.Timber

interface IAnalytics {
    fun log(eventName: String, params: Map<String, Any> = emptyMap())
}

class Analytics(
    private val context: Context
) : IAnalytics {

    private companion object {
        const val SCREEN_PARAM_KEY = "screen"
        const val VALUE_PARAM_KEY = "value"
    }

    private val mixpanel: MixpanelAPI by lazy {
        val token = context.getString(R.string.mixpanel_key)
        MixpanelAPI.getInstance(context, token, true)
    }

    override fun log(eventName: String, params: Map<String, Any>) {
        Timber.d("Event: $eventName, Params: $params")
        mixpanel.track(eventName, JSONObject(params))
    }

    fun cleanup() {
        mixpanel.flush()
    }
}