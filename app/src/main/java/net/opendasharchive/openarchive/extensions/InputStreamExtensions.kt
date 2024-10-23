package net.opendasharchive.openarchive.extensions

import java.io.InputStream

fun InputStream.readChunks(chunkSize: Int = 1024 * 1024): List<ByteArray> {
    val chunks = mutableListOf<ByteArray>()
    val buffer = ByteArray(chunkSize)

    var bytesRead: Int
    while (read(buffer).also { bytesRead = it } != -1) {
        if (bytesRead == chunkSize) {
            chunks.add(buffer.clone())
        } else {
            chunks.add(buffer.copyOf(bytesRead))
        }
    }
    return chunks
}