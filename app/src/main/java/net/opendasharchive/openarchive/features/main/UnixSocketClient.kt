package net.opendasharchive.openarchive.features.main

import android.net.LocalSocket
import android.net.LocalSocketAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter

sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val code: Int, val message: String) : ApiResponse<Nothing>()
}

class UnixSocketClient(val socketPath: String) {
    val json = Json { ignoreUnknownKeys = true }

    suspend inline fun <reified T> sendRequest(
        endpoint: String,
        method: String = "GET",
        body: JSONObject? = null
    ): ApiResponse<T> = withContext(Dispatchers.IO) {
        try {
            LocalSocket().use { socket ->
                socket.connect(LocalSocketAddress(socketPath, LocalSocketAddress.Namespace.FILESYSTEM))

                val output = PrintWriter(socket.outputStream.bufferedWriter())
                val input = BufferedReader(InputStreamReader(socket.inputStream))

                // Send request
                output.apply {
                    println("$method $endpoint HTTP/1.1")
                    println("Content-Type: application/json")
                    if (body != null) {
                        println("Content-Length: ${body.toString().length}")
                        println()
                        println(body.toString())
                    } else {
                        println()
                    }
                    flush()
                }

                // Read response
                val statusLine = input.readLine()
                val (httpVersion, statusCode, reasonPhrase) = statusLine.split(" ", limit = 3)
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
                    val data = parseResponse<T>(responseBody)
                    ApiResponse.Success(data)
                } else {
                    ApiResponse.Error(statusCode.toInt(), responseBody)
                }
            }
        } catch (e: Exception) {
            ApiResponse.Error(-1, e.message ?: "Unknown error occurred")
        }
    }

    inline fun <reified T> parseResponse(responseBody: String): T {
        return json.decodeFromString(responseBody)
    }
}