package net.opendasharchive.openarchive.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import net.opendasharchive.openarchive.R
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object Utility {

    fun getMimeType(context: Context, uri: Uri?): String? {
        val cR = context.contentResolver
        return cR.getType(uri!!)
    }

    fun getUriDisplayName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return null

        var result: String? = null

        // Get the column indexes of the data in the Cursor,
        // move to the first row in the Cursor, get the data, and display it.
        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx >= 0 && cursor.moveToFirst()) {
            result = cursor.getString(idx)
        }

        cursor.close()

        return result
    }

    fun getOutputMediaFileByCache(context: Context, fileName: String): File? {
        val dir = context.cacheDir
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return null
            }
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        return File(dir, "$timeStamp.$fileName")
    }

    fun getContextMenu(context: Context, anchorView: View): PowerMenu {
        return PowerMenu.Builder(context)
            .addItem(PowerMenuItem("Remove", true, iconRes = R.drawable.outline_delete_24))
            .setAnimation(MenuAnimation.DROP_DOWN)
            .setMenuRadius(10f)
            .setMenuShadow(10f)
            .setAnimation(MenuAnimation.FADE)
            .setTextColor(ContextCompat.getColor(context, R.color.c23_grey))
            .setTextGravity(Gravity.START)
            .setTextSize(18)
            .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            .setSelectedTextColor(Color.WHITE)
            .setMenuColor(Color.WHITE)
            .setSelectedMenuColor(ContextCompat.getColor(context, R.color.colorOnBackground))
            .build()

            // .showAtCenter(anchorView, 0, 18) // Center of screen
    }

    fun showMaterialWarning(context: Context, message: String? = null, positiveButtonText: String = "Ok", completion: (() -> Unit)? = null) {
        showMaterialMessage(context, "Oops", message, positiveButtonText, completion)
    }

    fun showMaterialMessage(context: Context, title: String = "Oops", message: String? = null, positiveButtonText: String = "Ok", completion: (() -> Unit)? = null) {
        Handler(Looper.getMainLooper()).post {
            MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText) { _, _ ->
                    completion?.invoke()
                }
                .show()
        }
    }

    fun showMaterialPrompt(context: Context, title: String, message: String? = null, positiveButtonText: String, negativeButtonText: String, completion: (Boolean) -> Unit) {
        Handler(Looper.getMainLooper()).post {
            MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText) { _, _ ->
                    completion.invoke(true)
                }
                .setNegativeButton(negativeButtonText) { _, _ ->
                    completion.invoke(false)
                }
                .show()
        }
    }

    fun writeStreamToFile(input: InputStream?, file: File?): Boolean {
        @Suppress("NAME_SHADOWING")
        val input = input ?: return false

        @Suppress("NAME_SHADOWING")
        val file = file ?: return false

        var success = false
        var output: FileOutputStream? = null

        try {
            output = FileOutputStream(file)
            val buffer = ByteArray(4 * 1024) // or other buffer size
            var read: Int

            while (input.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
            }
            output.flush()

            success = true
        }
        catch (e: FileNotFoundException) {
            Timber.e(e)
        }
        catch (e: IOException) {
            Timber.e(e)
        }
        finally {
            try {
                output?.close()
            }
            catch (e: IOException) {
                Timber.e(e)
            }

            try {
                input.close()
            }
            catch (e: IOException) {
                Timber.e(e)
            }
        }

        return success
    }

    fun openStore(context: Context, appId: String) {
        var i = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${appId}"))

        val capableApps = context.packageManager.queryIntentActivities(i, 0)

        // If there are no app stores installed, send to the web.
        if (capableApps.size < 1) {
            i = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${appId}"))
        }

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        context.startActivity(i)
    }
}