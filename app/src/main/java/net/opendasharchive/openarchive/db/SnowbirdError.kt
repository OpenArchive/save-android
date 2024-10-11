package net.opendasharchive.openarchive.db

import kotlinx.serialization.Serializable

@Serializable
sealed class SnowbirdError: SerializableMarker {
    @Serializable
    data class NetworkingError(val code: Int, val message: String) : SnowbirdError()

    @Serializable
    data class GeneralError(val message: String) : SnowbirdError()

    @Serializable
    data object TimedOut : SnowbirdError()

    @Serializable
    data object None : SnowbirdError()

    val friendlyMessage: String
        get() = when (this) {
            is NetworkingError -> "NetworkingError Error $code: $message"
            is GeneralError -> message
            is TimedOut -> "The current operation took too long."
            is None -> "No error"
        }
}