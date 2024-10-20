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
import java.io.InputStream
import java.io.InputStreamReader

enum class HttpMethod(val value: String) {
    GET("GET"), POST("POST"), PUT("PUT"), DELETE("DELETE"), PATCH("PATCH"),
    HEAD("HEAD"), OPTIONS("OPTIONS"), TRACE("TRACE");

    override fun toString() = value

    companion object {
        fun fromString(method: String) = entries.find { it.value.equals(method, ignoreCase = true) }
    }
}

sealed class ClientResponse<out T> {
    data class SuccessResponse<T>(val data: T) : ClientResponse<T>()
    data class ErrorResponse(val error: ApiError) : ClientResponse<Nothing>()
}

class UnixSocketClient(val socketPath: String = "/data/user/0/net.opendasharchive.openarchive.debug/files/rust_server.sock") {
    val json = Json { ignoreUnknownKeys = true }

    suspend inline fun <reified REQUEST : SerializableMarker, reified RESPONSE : SerializableMarker> sendRequest(
        endpoint: String,
        method: HttpMethod,
        body: REQUEST? = null
    ): ClientResponse<RESPONSE> = withContext(Dispatchers.IO) {
        Timber.d("$method $endpoint")
        sendRequestInternal(endpoint, method, body, { json.encodeToString(it) }, { json.decodeFromString<RESPONSE>(it) })
    }

    fun <REQUEST : SerializableMarker, RESPONSE : Any> sendRequestInternal(
        endpoint: String,
        method: HttpMethod,
        body: REQUEST?,
        serialize: (REQUEST) -> String,
        deserialize: (String) -> RESPONSE
    ): ClientResponse<RESPONSE> {
        return try {
            LocalSocket().use { socket ->
                socket.connect(LocalSocketAddress(socketPath, LocalSocketAddress.Namespace.FILESYSTEM))

                val (responseCode, _, responseBody) = sendJsonRequestAndGetResponse(socket, endpoint, method, body, serialize)

                Timber.d("response body = $responseBody")

                when (responseCode) {
                    in 200..299 -> parseSuccessResponse(responseBody, deserialize)
                    in 400..499 -> ClientResponse.ErrorResponse(ApiError.ClientError("Client error: $responseCode"))
                    in 500..599 -> ClientResponse.ErrorResponse(ApiError.ServerError("Server error: $responseCode"))
                    else -> ClientResponse.ErrorResponse(ApiError.UnexpectedError("Unexpected status code: $responseCode"))
                }
            }
        } catch (e: Exception) {
            ClientResponse.ErrorResponse(ApiError.UnexpectedError(e.localizedMessage ?: "Unknown error"))
        }
    }

    private fun <REQUEST : SerializableMarker> sendJsonRequestAndGetResponse(
        socket: LocalSocket,
        endpoint: String,
        method: HttpMethod,
        body: REQUEST?,
        serialize: (REQUEST) -> String
    ): Triple<Int, Map<String, String>, String> {
        val output = socket.outputStream
        val jsonBody = body?.let { serialize(it) } ?: ""

        val requestHeaders = buildString {
            append("$method $endpoint HTTP/1.1\r\n")
            append("Content-Type: application/json\r\n")
            append("Content-Length: ${jsonBody.length}\r\n")
            append("\r\n")
        }

        output.write(requestHeaders.toByteArray())
        output.write(jsonBody.toByteArray())
        output.flush()

        return readResponse(socket.inputStream)
    }

    fun readResponse(inputStream: InputStream): Triple<Int, Map<String, String>, String> {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val statusLine = reader.readLine()
        val (_, statusCode, _) = statusLine.split(" ", limit = 3)

        val headers = mutableMapOf<String, String>()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            if (line.isNullOrBlank()) break
            val (key, value) = line!!.split(": ", limit = 2)
            headers[key] = value
        }

        val responseBody = reader.readText()
        return Triple(statusCode.toInt(), headers, responseBody)
    }

    fun <T> parseSuccessResponse(responseBody: String, deserialize: (String) -> T): ClientResponse<T> {
        return try {
            val obj = deserialize(responseBody)
            ClientResponse.SuccessResponse(obj)
        } catch (e: Exception) {
            Timber.e("error = $e")
            ClientResponse.ErrorResponse(ApiError.UnexpectedError(e.localizedMessage ?: "Unknown error"))
        }
    }
}