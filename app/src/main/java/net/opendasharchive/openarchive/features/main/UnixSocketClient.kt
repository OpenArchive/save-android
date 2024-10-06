package net.opendasharchive.openarchive.features.main

import android.net.LocalSocket
import android.net.LocalSocketAddress
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.opendasharchive.openarchive.services.snowbird.ApiError
import net.opendasharchive.openarchive.util.Jsonable
import java.io.BufferedReader
import java.io.IOException
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

sealed class ApiResponse<out T> {
    data class SingleResponse<T>(val data: T) : ApiResponse<T>()
    data class ListResponse<T>(val data: List<T>) : ApiResponse<T>()
    data class ErrorResponse(val error: ApiError) : ApiResponse<Nothing>()
}

data class ClientRequest(
    val endpoint: String,
    val method: HttpMethod,
    val body: Jsonable? = null
)

data class ClientResponse(
    val responseCode: Int,
    val responseBody: String
)

class UnixSocketClient(val socketPath: String) {
    val json = Json { ignoreUnknownKeys = true }

    inline fun <reified T> Gson.fromJson(json: String): T = fromJson(json, object : TypeToken<T>() {}.type)

    suspend inline fun <reified T : Any> sendRequest(endpoint: String, method: HttpMethod, body: Jsonable? = null): ApiResponse<T> = withContext(Dispatchers.IO) {
        try {
            LocalSocket().use { socket ->
                socket.connect(LocalSocketAddress(socketPath, LocalSocketAddress.Namespace.FILESYSTEM))

                val (statusCode, responseBody) = getResponse(
                    socket, ClientRequest(
                        endpoint, method, body
                    )
                )

                when (statusCode.toInt()) {
                    in 200..299 -> parseSuccessResponse<T>(responseBody, method)
                    401 -> ApiResponse.ErrorResponse(ApiError.Unauthorized)
                    404 -> ApiResponse.ErrorResponse(ApiError.ResourceNotFound)
                    in 400..499 -> ApiResponse.ErrorResponse(ApiError.ClientError("Client error: $statusCode"))
                    in 500..599 -> ApiResponse.ErrorResponse(ApiError.ServerError("Server error: $statusCode"))
                    else -> ApiResponse.ErrorResponse(ApiError.UnexpectedError("Unexpected status code: $statusCode"))
                }
            }
        } catch (e: IOException) {
            ApiResponse.ErrorResponse(ApiError.NetworkError(e.localizedMessage.orEmpty()))
        } catch (e: Exception) {
            ApiResponse.ErrorResponse(ApiError.UnexpectedError("Unexpected error: ${e.localizedMessage}"))
        }
    }

    inline fun <reified T> parseSuccessResponse(responseBody: String, method: HttpMethod): ApiResponse<T> {
        return when {
            method == HttpMethod.GET && responseBody.startsWith("[") -> {
                val listData = json.decodeFromString<List<T>>(responseBody)
                ApiResponse.ListResponse(listData)
            }
            else -> {
                val singleData = json.decodeFromString<T>(responseBody)
                ApiResponse.SingleResponse(singleData)
            }
        }
    }

    fun getResponse(socket: LocalSocket, request: ClientRequest): ClientResponse {
        socket.connect(LocalSocketAddress(socketPath, LocalSocketAddress.Namespace.FILESYSTEM))
        val output = PrintWriter(socket.outputStream.bufferedWriter())
        val input = BufferedReader(InputStreamReader(socket.inputStream))

        // Send request
        output.apply {
            println("${request.method} ${request.endpoint} HTTP/1.1")
            println("Content-Type: application/json")
            if (request.body != null) {
                val content = json.encodeToString(request.body)
                println("Content-Length: ${content.length}")
                println()
                println(content)
            } else {
                println()
            }
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

        return ClientResponse(statusCode.toInt(), input.readText())
    }
}