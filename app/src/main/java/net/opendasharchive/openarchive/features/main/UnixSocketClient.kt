package net.opendasharchive.openarchive.features.main

import android.net.LocalSocket
import android.net.LocalSocketAddress
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import net.opendasharchive.openarchive.util.Jsonable
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
            return values().find { it.value.equals(method, ignoreCase = true) }
        }
    }
}

sealed class ApiResponse<out T> {
    data class SingleResponse<T>(val data: T) : ApiResponse<T>()
    data class ListResponse<T>(val data: List<T>) : ApiResponse<T>()
    data class Error(val code: Int, val message: String) : ApiResponse<Nothing>()
}

class UnixSocketClient(val socketPath: String) {
    val json = Json { ignoreUnknownKeys = true }

    inline fun <reified T> Gson.fromJson(json: String): T = fromJson(json, object : TypeToken<T>() {}.type)

    suspend inline fun <reified T : Any> sendRequest(endpoint: String, method: HttpMethod = HttpMethod.GET, body: Jsonable? = null): ApiResponse<T> = withContext(Dispatchers.IO) {
        try {
            LocalSocket().use { socket ->
                socket.connect(LocalSocketAddress(socketPath, LocalSocketAddress.Namespace.FILESYSTEM))

                val output = PrintWriter(socket.outputStream.bufferedWriter())
                val input = BufferedReader(InputStreamReader(socket.inputStream))

                val content = body?.toJson()

                // Send request
                output.apply {
                    println("$method $endpoint HTTP/1.1")
                    println("Content-Type: application/json")
                    if (body != null) {
                        println("Content-Length: ${content.toString().length}")
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

                // Read body
                val responseBody = input.readText()

                if (statusCode.toInt() in 200..299) {
                    when (method) {
                        HttpMethod.GET -> {
                            val listData = parseListResponse<T>(responseBody)
                            ApiResponse.ListResponse(listData)
                        }
                        HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH -> {
                            val singleData = parseSingleResponse<T>(responseBody)
                            ApiResponse.SingleResponse(singleData)
                        }
                        else -> throw IllegalArgumentException("Unsupported HTTP method")
                    }
                } else {
                    ApiResponse.Error(statusCode.toInt(), responseBody)
                }
            }
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error occurred")
        }
    }

    inline fun <reified T> parseSingleResponse(json: String): T {
        val gson = Gson()

        return gson.fromJson<T>(json)
    }

    // Allows us to process responses of the form { "foo": [ "bar1", "bar2", ... , "bar3"] }
    //
    inline fun <reified T> parseListResponse(json: String): List<T> {
        val gson = Gson()
        val jsonElement = JsonParser.parseString(json)
        val jsonObject = jsonElement.asJsonObject

        val entry = jsonObject.entrySet().first()
        val jsonArray = entry.value.asJsonArray

        val listType = TypeToken.getParameterized(List::class.java, T::class.java).type

        return gson.fromJson(jsonArray, listType)
    }
}