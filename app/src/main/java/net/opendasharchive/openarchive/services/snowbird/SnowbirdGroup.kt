package net.opendasharchive.openarchive.services.snowbird

import com.google.gson.Gson
import com.orm.SugarRecord
import kotlinx.serialization.Serializable
import net.opendasharchive.openarchive.util.Jsonable
import org.json.JSONObject

@Serializable
data class SnowbirdGroup(val name: String) : SugarRecord(), Jsonable {
    override fun toJson(): JSONObject {
        val gson = Gson()
        val jsonString = gson.toJson(this)
        return JSONObject(jsonString)
    }
}
