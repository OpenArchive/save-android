package net.opendasharchive.openarchive.db

import kotlinx.serialization.Serializable

@Serializable
data class SnowbirdMediaDTO(
    var foo: String
) : SerializableMarker