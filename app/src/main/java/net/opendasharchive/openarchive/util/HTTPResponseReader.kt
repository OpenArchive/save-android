package net.opendasharchive.openarchive.util

import android.net.LocalSocket
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream

class HttpResponseReader(private val socket: LocalSocket) {
    private val input = BufferedInputStream(socket.getInputStream())

    fun readResponse(): Response {
        val statusLine = readLine()
        val (_, statusCode, _) = statusLine.split(" ", limit = 3)
        val headers = mutableMapOf<String, String>()

        // Read headers
        while (true) {
            val line = readLine()
            if (line.isBlank()) break
            val (key, value) = line.split(": ", limit = 2)
            headers[key] = value
        }

        // Read body
        val body = readBody(headers)

        return Response(statusCode.toInt(), headers, body)
    }

    private fun readLine(): String {
        val buffer = ByteArrayOutputStream()
        while (true) {
            val byte = input.read()
            if (byte == -1 || byte == '\n'.code) break
            if (byte != '\r'.code) {
                buffer.write(byte)
            }
        }
        return String(buffer.toByteArray())
    }

    private fun readBody(headers: Map<String, String>): ByteArray {
        val contentLength = headers["Content-Length"]?.toIntOrNull()
        val transferEncoding = headers["Transfer-Encoding"]

        return when {
            contentLength != null -> readFixedLengthBody(contentLength)
            transferEncoding?.lowercase() == "chunked" -> readChunkedBody()
            else -> ByteArray(0) // No body or unsupported encoding
        }
    }

    private fun readFixedLengthBody(length: Int): ByteArray {
        val body = ByteArray(length)
        var bytesRead = 0
        while (bytesRead < length) {
            val count = input.read(body, bytesRead, length - bytesRead)
            if (count == -1) break
            bytesRead += count
        }
        return body
    }

    private fun readChunkedBody(): ByteArray {
        val body = ByteArrayOutputStream()
        while (true) {
            val chunkSizeLine = readLine()
            val chunkSize = chunkSizeLine.trim().toInt(16)
            if (chunkSize == 0) break
            val chunk = ByteArray(chunkSize)
            input.read(chunk)
            body.write(chunk)
            readLine() // Read trailing CRLF
        }
        readLine() // Read final CRLF
        return body.toByteArray()
    }

    data class Response(
        val statusCode: Int,
        val headers: Map<String, String>,
        val body: ByteArray
    ) {
        fun bodyAsString(): String = String(body)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Response

            if (statusCode != other.statusCode) return false
            if (headers != other.headers) return false
            if (!body.contentEquals(other.body)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = statusCode
            result = 31 * result + headers.hashCode()
            result = 31 * result + body.contentHashCode()
            return result
        }
    }
}