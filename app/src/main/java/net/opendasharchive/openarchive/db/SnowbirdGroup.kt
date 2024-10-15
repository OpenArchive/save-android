package net.opendasharchive.openarchive.db

import com.orm.SugarRecord
import kotlinx.serialization.Serializable

@Serializable
data class SnowbirdGroupList(
    var groups: List<SnowbirdGroup>
) : SerializableMarker

@Serializable
data class SnowbirdGroup(
    var key: String = "",
    var name: String? = null,
    var uri: String? = null
) : SugarRecord(), SerializableMarker {
    companion object {
        fun getAll(): List<SnowbirdGroup> {
            return findAll(SnowbirdGroup::class.java).asSequence().toList()
        }

        fun get(key: String): SnowbirdGroup? {
            val whereClause = "key = ?"
            val whereArgs = mutableListOf(key)

            return find(
                SnowbirdGroup::class.java, whereClause, whereArgs.toTypedArray(),
                null,
                null,
                null).firstOrNull()
        }
    }
}

fun SnowbirdGroup.shortHash(): String {
    return key.take(10)
}