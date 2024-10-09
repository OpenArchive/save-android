package net.opendasharchive.openarchive.db

import com.orm.SugarRecord
import kotlinx.serialization.Serializable

@Serializable
data class SnowbirdGroupList(
    var groups: List<SnowbirdGroup>
) : SugarRecord(), SerializableMarker

@Serializable
data class SnowbirdGroup(
    var key: String,
    var name: String?
) : SugarRecord(), SerializableMarker

data class SugarySnowbirdGroup(
    var foo: String?,
    var bar: String?
) : SugarRecord()

fun SnowbirdGroup.shortHash(): String {
    return key.take(10)
}