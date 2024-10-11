package net.opendasharchive.openarchive.features.main

import android.net.LocalSocket
import android.net.LocalSocketAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.opendasharchive.openarchive.db.ApiError
import net.opendasharchive.openarchive.db.SerializableMarker
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter

enum class HttpMethod(val value: String) {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    PATCH("PATCH"),
    HEAD("HEAD"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(method: String): HttpMethod? {
            return entries.find { it.value.equals(method, ignoreCase = true) }
        }
    }
}

data class NetworkResponse(
    val responseCode: Int,
    val responseBody: String
)

sealed class ClientResponse<out T> {
    data class SuccessResponse<T>(val data: T) : ClientResponse<T>()
    data class ErrorResponse(val error: ApiError) : ClientResponse<Nothing>()
}

sealed class ApiResponse<out T> {
    data class SingleResponse<T>(val data: T) : ApiResponse<T>()
    data class ListResponse<T>(val data: List<T>) : ApiResponse<T>()
    data class ErrorResponse(val error: ApiError) : ApiResponse<Nothing>()
}

data class ClientRequest(
    val endpoint: String,
    val method: HttpMethod,
    val body: SerializableMarker? = null
)

class UnixSocketClient() {
    val socketPath = "/data/user/0/net.opendasharchive.openarchive.debug/files/rust_server.sock"
    val json = Json { ignoreUnknownKeys = true }

    suspend inline fun <reified REQUEST: SerializableMarker, reified RESPONSE: SerializableMarker> sendRequest(
        endpoint: String,
        method: HttpMethod,
        body: REQUEST
    ): ClientResponse<RESPONSE> = sendRequestInternal(endpoint, method, body)

    suspend inline fun <reified RESPONSE: SerializableMarker> sendRequest(
        endpoint: String,
        method: HttpMethod
    ): ClientResponse<RESPONSE> = sendRequestInternal(endpoint, method, null)

    suspend inline fun <reified REQUEST: SerializableMarker, reified RESPONSE: SerializableMarker> sendRequestInternal(
        endpoint: String,
        method: HttpMethod,
        body: REQUEST?
    ): ClientResponse<RESPONSE> = withContext(Dispatchers.IO) {
        Timber.d("Endpoint = $endpoint")

        try {
            LocalSocket().use { socket ->
                socket.connect(LocalSocketAddress(socketPath, LocalSocketAddress.Namespace.FILESYSTEM))
                val request = ClientRequest(endpoint, method, body)

                val (responseCode, responseBody) = getResponse(socket, request)

                Timber.d("resposne body = $responseBody")

                when (responseCode) {
                    in 200..299 -> parseSuccessResponse<RESPONSE>(responseBody)
                    in 400..499 -> ClientResponse.ErrorResponse(ApiError.ClientError("Client error: $responseCode"))
                    in 500..599 -> ClientResponse.ErrorResponse(ApiError.ServerError("Server error: $responseCode"))
                    else -> ClientResponse.ErrorResponse(ApiError.UnexpectedError("Unexpected status code: $responseCode"))
                }
            }
        } catch (e: Exception) {
            ClientResponse.ErrorResponse(ApiError.UnexpectedError("Unexpected error: ${e.localizedMessage}"))
        }
    }

    inline fun <reified RESPONSE> parseSuccessResponse(responseBody: String): ClientResponse<RESPONSE> {
        val obj = json.decodeFromString<RESPONSE>(responseBody)
        return ClientResponse.SuccessResponse(obj)

//        return when {
//            method == HttpMethod.GET && obj.isListLike() -> {
//                Timber.d("list = $responseBody")
//                ApiResponse.ListResponse(listData)
//            }
//            else -> {
//                val singleData = json.decodeFromString<RESPONSE>(responseBody)
//                Timber.d("single = $responseBody")
//                ApiResponse.SingleResponse(singleData)
//            }
//        }
    }

    fun getResponse(socket: LocalSocket, request: ClientRequest): NetworkResponse {
        val output = PrintWriter(socket.outputStream.bufferedWriter())
        val input = BufferedReader(InputStreamReader(socket.inputStream))

        // Send request
        output.apply {
            println("${request.method} ${request.endpoint} HTTP/1.1")
            println("Content-Type: application/json")
            request.body?.let {
                val content = json.encodeToString(it)
                println("Content-Length: ${content.length}")
                println()
                println(content)
            }
            println()
            flush()
        }

        // Read response
        val statusLine = input.readLine()
        val (_, statusCode, _) = statusLine.split(" ", limit = 3)
        val headers = mutableMapOf<String, String>()
        var line: String?
        // Read headers
        while (input.readLine().also { line = it } != null) {
            if (line.isNullOrBlank()) break
            val (key, value) = line!!.split(": ", limit = 2)
            headers[key] = value
        }

        return NetworkResponse(statusCode.toInt(), input.readText())
    }
}