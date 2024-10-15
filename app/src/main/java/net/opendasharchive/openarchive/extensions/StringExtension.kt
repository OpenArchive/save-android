package net.opendasharchive.openarchive.extensions

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import net.opendasharchive.openarchive.util.Utility
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Generates a QR code bitmap from a given string.
 *
 * @param size The width and height of the resulting bitmap.
 * @param quietZone The size of the quiet zone around the QR code (optional, default is 4).
 * @return A Bitmap containing the generated QR code.
 */
fun String.asQRCode(size: Int = 512, quietZone: Int = 4): Bitmap {
    val hints = hashMapOf<EncodeHintType, Any>().apply {
        put(EncodeHintType.MARGIN, quietZone)
    }

    val bits = QRCodeWriter().encode(this, BarcodeFormat.QR_CODE, size, size, hints)

    return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also { bitmap ->
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
            }
        }
    }
}

fun String.toSnowbirdUri(scheme: String = "save-veilid"): Uri {
    return Utility.buildUri(
        scheme = scheme,
        host = this
    )
}

fun String.uriToPath(): String {
    val uri = URI(this)
    return uri.path
}

fun String.urlEncode(): String = URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
