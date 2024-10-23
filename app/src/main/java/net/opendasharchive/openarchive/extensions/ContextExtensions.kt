package net.opendasharchive.openarchive.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import net.opendasharchive.openarchive.R
import java.io.File

fun Context.assetToFile(assetName: String): File {
    val file = File(filesDir, assetName)

    if (!file.exists()) {
        assets.open(assetName).use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    return file
}

fun Context.openBrowser(link: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
    }
    catch (e: ActivityNotFoundException) {
        Toast.makeText(this, getString(R.string.no_webbrowser_found_error),
            Toast.LENGTH_LONG).show()
    }
}