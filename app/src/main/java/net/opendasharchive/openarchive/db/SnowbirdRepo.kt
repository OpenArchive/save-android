package net.opendasharchive.openarchive.db

import com.google.gson.Gson
import com.orm.SugarRecord
import kotlinx.serialization.Serializable
import net.opendasharchive.openarchive.util.Jsonable
import org.json.JSONObject

class SnowbirdRepo {
    @Serializable
    data class SnowbirdRepo(
        val repoId: String,
        val key: String
    ) : SugarRecord(), Jsonable {

        override fun toJson(): JSONObject {
            val gson = Gson()
            val jsonString = gson.toJson(this)
            return JSONObject(jsonString)
        }
    }
}