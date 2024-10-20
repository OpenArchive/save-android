package net.opendasharchive.openarchive.features.main

import android.net.LocalSocket
import android.net.LocalSocketAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import net.opendasharchive.openarchive.db.ApiError
import net.opendasharchive.openarchive.db.SerializableMarker
import timber.log.Timber
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.coroutines.cancellation.CancellationException

suspend fun UnixSocketClient.downloadFile(endpoint: String): ClientResponse<ByteArray> = withContext(Dispatchers.IO) {
    try {
        LocalSocket().use { socket ->
            socket.connect(LocalSocketAddress(socketPath, LocalSocketAddress.Namespace.FILESYSTEM))

            val requestHeaders = buildString {
                append("${HttpMethod.GET} $endpoint HTTP/1.1\r\n")
                append("Accept: image/*\r\n")
                append("\r\n")
            }

            socket.outputStream.apply {
                write(requestHeaders.toByteArray())
                flush()
            }

            val (responseCode, _, bytes) = readBinaryResponseWithCancellation(socket.inputStream)

            Timber.d("File download response code: $responseCode")

            when (responseCode) {
                in 200..299 -> ClientResponse.SuccessResponse(bytes)
                in 400..499 -> ClientResponse.ErrorResponse(ApiError.ClientError("Client error: $responseCode"))
                in 500..599 -> ClientResponse.ErrorResponse(ApiError.ServerError("Server error: $responseCode"))
                else -> ClientResponse.ErrorResponse(ApiError.UnexpectedError("Unexpected status code: $responseCode"))
            }
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to download file")
        ClientResponse.ErrorResponse(ApiError.UnexpectedError(e.localizedMessage ?: "Unknown error"))
    }
}

suspend inline fun <reified RESPONSE : SerializableMarker> UnixSocketClient.uploadFile(
    endpoint: String,
    imageData: ByteArray
): ClientResponse<RESPONSE> = withContext(Dispatchers.IO) {
    try {
        LocalSocket().use { socket ->
            socket.connect(LocalSocketAddress(socketPath, LocalSocketAddress.Namespace.FILESYSTEM))

            val requestHeaders = buildString {
                append("${HttpMethod.POST} $endpoint HTTP/1.1\r\n")
                append("Content-Type: application/octet-stream\r\n")
                append("Content-Length: ${imageData.size}\r\n")
                append("\r\n")
            }

            socket.outputStream.apply {
                write(requestHeaders.toByteArray())
                write(imageData)
                flush()
            }

            val (responseCode, _, responseBody) = readResponse(socket.inputStream)

            Timber.d("Image upload response code: $responseCode")

            when (responseCode) {
                in 200..299 -> parseSuccessResponse(responseBody) { json.decodeFromString<RESPONSE>(it) }
                in 400..499 -> ClientResponse.ErrorResponse(ApiError.ClientError("Client error: $responseCode"))
                in 500..599 -> ClientResponse.ErrorResponse(ApiError.ServerError("Server error: $responseCode"))
                else -> ClientResponse.ErrorResponse(ApiError.UnexpectedError("Unexpected status code: $responseCode"))
            }
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to upload image")
        ClientResponse.ErrorResponse(ApiError.UnexpectedError(e.localizedMessage ?: "Unknown error"))
    }
}

suspend inline fun <reified RESPONSE : SerializableMarker> UnixSocketClient.uploadFile(
    endpoint: String,
    inputStream: InputStream,
): ClientResponse<RESPONSE> {
    inputStream.use { stream ->
        return uploadFile<RESPONSE>(endpoint, stream.readBytes())
    }
}

private suspend fun readBinaryResponseWithCancellation(
    inputStream: InputStream,
    onProgress: ((Long) -> Unit)? = null
): Triple<Int, Map<String, String>, ByteArray> = withContext(Dispatchers.IO) {
    val reader = BufferedReader(InputStreamReader(inputStream))

    // Read status line
    val statusLine = reader.readLine() ?: throw IOException("Empty response")
    val (_, statusCode, _) = statusLine.split(" ", limit = 3)

    // Read headers
    val headers = mutableMapOf<String, String>()
    var line: String?
    while (reader.readLine().also { line = it } != null) {
        if (line.isNullOrBlank()) break
        val (key, value) = line!!.split(": ", limit = 2)
        headers[key] = value
    }

    val outputStream = ByteArrayOutputStream()
    var totalBytesRead = 0L

    val isChunked = headers["Transfer-Encoding"]?.equals("chunked", ignoreCase = true) ?: false
    val contentLength = headers["Content-Length"]?.toLongOrNull()

    try {
        if (isChunked) {
            // Handle chunked transfer encoding
            while (isActive) {
                ensureActive()
                val chunkSizeLine = reader.readLine() ?: break
                val chunkSize = chunkSizeLine.trim().toInt(16)
                if (chunkSize == 0) break

                val buffer = ByteArray(chunkSize)
                var bytesRead = 0
                while (bytesRead < chunkSize) {
                    ensureActive()
                    val count = inputStream.read(buffer, bytesRead, chunkSize - bytesRead)
                    if (count == -1) break
                    bytesRead += count
                }
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                onProgress?.invoke(totalBytesRead)

                reader.readLine() // Read the CRLF after the chunk
            }
        } else if (contentLength != null) {
            // Handle Content-Length specified
            val buffer = ByteArray(8192) // 8KB buffer
            var bytesRead = 0
            while (totalBytesRead < contentLength && inputStream.read(buffer).also { bytesRead = it } != -1) {
                ensureActive()
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                onProgress?.invoke(totalBytesRead)
            }
        } else {
            // Handle case where neither chunked nor Content-Length is specified
            val buffer = ByteArray(8192) // 8KB buffer
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                ensureActive()
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                onProgress?.invoke(totalBytesRead)
            }
        }
    } catch (e: CancellationException) {
        throw e
    } finally {
        inputStream.close()
    }

    Triple(statusCode.toInt(), headers, outputStream.toByteArray())
}