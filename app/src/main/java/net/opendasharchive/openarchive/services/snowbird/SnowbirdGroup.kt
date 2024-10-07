package net.opendasharchive.openarchive.services.snowbird

import com.orm.SugarRecord
import kotlinx.serialization.Serializable

@Serializable
data class SnowbirdGroup(
    val groupId: String? = null,
    val key: String? = null,
    val name: String? = null,
) : SugarRecord(), SerializableMarker

fun SnowbirdGroup.shortHash(): String {
    return "hash"
}