package net.opendasharchive.openarchive.features.media

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.View
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.esafirm.imagepicker.features.ImagePickerConfig
import com.esafirm.imagepicker.features.ImagePickerLauncher
import com.esafirm.imagepicker.features.ImagePickerMode
import com.esafirm.imagepicker.features.ImagePickerSavePath
import com.esafirm.imagepicker.features.ReturnMode
import com.esafirm.imagepicker.features.registerImagePicker
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.util.Utility
import net.opendasharchive.openarchive.util.extensions.makeSnackBar
import org.witness.proofmode.crypto.HashUtils
import java.io.File
import java.util.Date

object Picker {

    fun register(activity: ComponentActivity, root: View, folder: () -> Folder?, completed: (List<Media>) -> Unit): Pair<ImagePickerLauncher, ActivityResultLauncher<Intent>> {
        val mpl = activity.registerImagePicker { result ->
            val bar = root.makeSnackBar(activity.getString(R.string.importing_media))
            (bar.view as? Snackbar.SnackbarLayout)?.addView(ProgressBar(activity))
            bar.show()

            CoroutineScope(Dispatchers.IO).launch {
                val media = import(activity, folder(), result.map { it.uri })

                MainScope().launch {
                    bar.dismiss()

                    completed(media)
                }
            }
        }

        val fpl = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != AppCompatActivity.RESULT_OK) return@registerForActivityResult

            val uri = result.data?.data ?: return@registerForActivityResult

            val bar = root.makeSnackBar(activity.getString(R.string.importing_media))
            (bar.view as? Snackbar.SnackbarLayout)?.addView(ProgressBar(activity))
            bar.show()

            CoroutineScope(Dispatchers.IO).launch {
                val files = import(activity, folder(), listOf(uri))

                MainScope().launch {
                    bar.dismiss()

                    completed(files)
                }
            }
        }

        return Pair(mpl, fpl)
    }

    fun pickMedia(activity: Activity, launcher: ImagePickerLauncher) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (needAskForPermission(activity, arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO))
            ) {
                return
            }
        }

        val config = ImagePickerConfig {
            mode = ImagePickerMode.MULTIPLE
            isShowCamera = false
            returnMode = ReturnMode.NONE
            isFolderMode = true
            isIncludeVideo = true
            arrowColor = Color.WHITE
            limit = 99
            theme = R.style.ImagePickerTheme
            savePath = ImagePickerSavePath(Environment.getExternalStorageDirectory().path, false)
        }

        launcher.launch(config)
    }

    fun canPickFiles(context: Context): Boolean {
        return mFilePickerIntent.resolveActivity(context.packageManager) != null
    }

    fun pickFiles(launcher: ActivityResultLauncher<Intent>) {
        launcher.launch(mFilePickerIntent)
    }

    private val mFilePickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/*"
    }

    private fun needAskForPermission(activity: Activity, permissions: Array<String>): Boolean {
        var needAsk = false

        for (permission in permissions) {
            needAsk = ContextCompat.checkSelfPermission(
                activity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

            if (needAsk) break
        }

        if (!needAsk) return false

        ActivityCompat.requestPermissions(activity, permissions, 2)

        return true
    }

    private fun import(context: Context, folder: Folder?, uris: List<Uri>): ArrayList<Media> {
        val result = ArrayList<Media>()

        for (uri in uris) {
            val media = import(context, folder, uri)
            if (media != null) result.add(media)
        }

        return result
    }

    fun import(context: Context, folder: Folder?, uri: Uri): Media? {
        @Suppress("NAME_SHADOWING")
        val folder = folder ?: return null

        val title = Utility.getUriDisplayName(context, uri) ?: ""
        val file = Utility.getOutputMediaFileByCache(context, title)

        if (!Utility.writeStreamToFile(context.contentResolver.openInputStream(uri), file)) {
            return null
        }

        // create media
        val media = Media()

        val coll = folder.openCollection

        media.collectionId = coll.id

        val fileSource = uri.path?.let { File(it) }
        var createDate = Date()

        if (fileSource?.exists() == true) {
            createDate = Date(fileSource.lastModified())
            media.contentLength = fileSource.length()
        }
        else {
            media.contentLength = file?.length() ?: 0
        }

        media.originalFilePath = Uri.fromFile(file).toString()
        media.mimeType = Utility.getMimeType(context, uri) ?: ""
        media.createDate = createDate
        media.updateDate = media.createDate
        media.status = Media.Status.Local
        media.mediaHashString =
            HashUtils.getSHA256FromFileContent(context.contentResolver.openInputStream(uri))
        media.folderId = folder.id
        media.title = title
        media.save()

        return media
    }
}