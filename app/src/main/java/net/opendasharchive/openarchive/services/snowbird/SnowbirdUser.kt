package net.opendasharchive.openarchive.services.snowbird

import com.orm.SugarRecord
import kotlinx.serialization.Serializable

@Serializable
data class SnowbirdUser(
    val name: String,
) : SugarRecord(), SerializableMarker