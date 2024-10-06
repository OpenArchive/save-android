package net.opendasharchive.openarchive.services.snowbird

import kotlinx.serialization.Serializable

@Serializable
sealed class ApiError {
    @Serializable
    data class HttpError(val code: Int, val message: String) : ApiError()

    @Serializable
    data class NetworkError(val message: String) : ApiError()

    @Serializable
    data class ServerError(val message: String) : ApiError()

    @Serializable
    data class ClientError(val message: String) : ApiError()

    @Serializable
    data class UnexpectedError(val message: String) : ApiError()

    @Serializable
    data object Unauthorized : ApiError()

    @Serializable
    data object ResourceNotFound : ApiError()

    @Serializable
    data object None : ApiError()

    val friendlyMessage: String
        get() = when (this) {
            is HttpError -> "HTTP Error $code: $message"
            is NetworkError -> "Network Error: $message"
            is ServerError -> "Server Error: $message"
            is ClientError -> message
            is UnexpectedError -> "An unexpected error occurred: $message"
            Unauthorized -> "Unauthorized: Please log in and try again"
            ResourceNotFound -> "The requested resource was not found"
            None -> "No error"
        }
}