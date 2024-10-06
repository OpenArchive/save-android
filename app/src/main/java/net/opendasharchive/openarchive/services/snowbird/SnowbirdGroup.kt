package net.opendasharchive.openarchive.services.snowbird

import com.orm.SugarRecord
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.opendasharchive.openarchive.util.Jsonable
import org.json.JSONObject

@Serializable
data class SnowbirdGroup(
    val groupId: String? = null,
    val key: String? = null,
    val name: String? = null,
) : SugarRecord(), Jsonable {

    override fun toJson(): JSONObject {
        val jsonString = Json.encodeToString(this)
        return JSONObject(jsonString)
    }

    companion object {
        fun fromJson(jsonObject: JSONObject): SnowbirdGroup {
            val jsonString = jsonObject.toString()
            return Json.decodeFromString(jsonString)
        }
    }
}
