package net.opendasharchive.openarchive.services.snowbird

import com.orm.SugarRecord
import kotlinx.serialization.Serializable

@Serializable
data class SnowbirdGroup(
    var groupId: String = "",
    var key: String = "",
    var name: String = "",
) : SugarRecord(), SerializableMarker

data class SugarySnowbirdGroup(
    var foo: String = "",
    var bar: String = "",
) : SugarRecord()

fun SnowbirdGroup.shortHash(): String {
    return key.take(10)
}