package net.opendasharchive.openarchive.services.snowbird

import com.google.gson.Gson
import net.opendasharchive.openarchive.util.Jsonable
import org.json.JSONObject

data class SnowbirdStatus(val status: String, val version: String): Jsonable {
    override fun toJson(): JSONObject {
        val gson = Gson()
        val jsonString = gson.toJson(this)
        return JSONObject(jsonString)
    }
}