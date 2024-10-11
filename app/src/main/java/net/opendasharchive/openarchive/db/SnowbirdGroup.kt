package net.opendasharchive.openarchive.db

import com.orm.SugarRecord
import kotlinx.serialization.Serializable

@Serializable
data class SnowbirdGroupList(
    var groups: List<SnowbirdGroup>
) : SugarRecord(), SerializableMarker

@Serializable
data class SnowbirdGroup(
    var key: String = "",
    var name: String? = null
) : SugarRecord(), SerializableMarker {
    companion object {
        fun getAll(): List<SnowbirdGroup> {
            return findAll(SnowbirdGroup::class.java).asSequence().toList()
        }
    }
}

fun SnowbirdGroup.shortHash(): String {
    return key.take(10)
}