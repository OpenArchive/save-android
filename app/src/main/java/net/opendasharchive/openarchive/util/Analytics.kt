package net.opendasharchive.openarchive.util

import android.annotation.SuppressLint
import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import net.opendasharchive.openarchive.R
import org.json.JSONObject

@SuppressLint("StaticFieldLeak")
object Analytics {

    private var mixpanel: MixpanelAPI? = null

    fun init(context: Context) {
        val token = context.getString(R.string.mixpanel_key)
        mixpanel = MixpanelAPI.getInstance(context, token, false)
    }

    fun log(eventName: String, props: JSONObject? = null) {
        mixpanel?.track(eventName, props)
    }
}