package net.opendasharchive.openarchive.services.snowbird

sealed class ApiResponse<out T, out E> {
    data class SingleResponse<T>(val data: T) : ApiResponse<T, Nothing>()
    data class ListResponse<T>(val data: List<T>) : ApiResponse<T, Nothing>()
    data class ErrorResponse<E>(val error: E) : ApiResponse<Nothing, E>()
}