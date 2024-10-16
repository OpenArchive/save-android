package net.opendasharchive.openarchive.db

import kotlinx.serialization.Serializable

@Serializable
data class SnowbirdMediaList(
    var media: List<SnowbirdMediaItem>
) : SerializableMarker

@Serializable
data class SnowbirdMediaItem(
    val uri: String,
    val aspectRatio: Float
): SerializableMarker