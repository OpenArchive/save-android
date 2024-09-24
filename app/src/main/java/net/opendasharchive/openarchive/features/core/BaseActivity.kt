package net.opendasharchive.openarchive.features.core

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.esafirm.imagepicker.features.ImagePickerLauncher
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.features.main.CameraCaptureActivity
import net.opendasharchive.openarchive.features.main.ui.OABottomSheetDialogFragment
import net.opendasharchive.openarchive.features.media.Picker
import net.opendasharchive.openarchive.features.media.Picker.pickMedia
import net.opendasharchive.openarchive.upload.BroadcastManager.Action
import net.opendasharchive.openarchive.util.Prefs
import net.opendasharchive.openarchive.util.Utility
import timber.log.Timber

abstract class BaseActivity: AppCompatActivity() {

    private lateinit var mMediaPickerLauncher: ImagePickerLauncher
    private lateinit var mFilePickerLauncher: ActivityResultLauncher<Intent>

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            cameraResultLauncher.launch(Intent(this, CameraCaptureActivity::class.java))
        } else {
            Timber.d("Camera permission denied")
        }
    }

    private val cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_OK -> processIntentResult(it.data)
                else -> Timber.d("Failed with code ${it.resultCode}")
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            val obscuredTouch = event.flags and MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED != 0
            if (obscuredTouch) return false
        }

        return super.dispatchTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val launchers = Picker.register(this, { Folder.current }, { media ->
            Timber.d("media = $media")

            val i = Intent(Action.Add.id)

            LocalBroadcastManager.getInstance(this).sendBroadcastSync(i)

//            if (media.isNotEmpty()) {
//                preview()
//            }
        })

        mMediaPickerLauncher = launchers.first
        mFilePickerLauncher = launchers.second
    }

    override fun onResume() {
        super.onResume()

        // updating this in onResume (previously was in onCreate) to make sure setting changes get
        // applied instantly instead after the next app restart
        //
        // Commenting out since we are not currently support this feature.
        //
//        updateScreenshotPrevention()
    }

    override fun onSupportNavigateUp(): Boolean {
        Timber.d("onSupportNavigateUp")
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private val getMultipleContents = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        handleSelectedFiles(uris)
    }

    private val pickMultipleMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
        handleSelectedFiles(uris)
    }

    private val legacyPickMultipleMedia = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        handleSelectedFiles(uris)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                pickMedia(this, mMediaPickerLauncher)
            } else {
                Timber.d("External storage permission denied")
            }
        }

    // Media types are split out in case we ever need custom
    // handling by media type.
    //
    private fun handleAudio(uri: Uri) {
        handleMedia(uri)
    }

    private fun handleImage(uri: Uri) {
        handleMedia(uri)
    }

    private fun handleVideo(uri: Uri) {
        handleMedia(uri)
    }

    private fun handleMedia(uri: Uri) {
        Picker.import(this@BaseActivity, Folder.current, uri)?.let { media ->
            media.status = if (Prefs.mediaUploadPolicy == "upload_media_automatically") Media.Status.Queued else Media.Status.Local
            media.selected = false
            media.save()
        }
    }

    private fun handleSelectedFiles(uris: List<Uri>) {
        if (uris.isNotEmpty()) {
            for (uri in uris) {
                val mimeType = contentResolver.getType(uri)
                when {
                    mimeType?.startsWith("image/") == true -> handleImage(uri)
                    mimeType?.startsWith("video/") == true -> handleVideo(uri)
                    mimeType?.startsWith("audio/") == true -> handleAudio(uri)
                    else -> {
                        Timber.d("Unknown type picked: $mimeType")
                    }
                }
            }
        } else {
            // No images were selected
            Timber.d("No images selected")
        }
    }

    private fun openFilePicker() {
        getMultipleContents.launch("*/*")
    }

    private fun launchImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                launchLegacyPicker()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun launchLegacyPicker() {
        legacyPickMultipleMedia.launch("image/*")
    }

    private fun processIntentResult(result: Intent?) {
        Timber.d("Got camera results")

        result?.let { intent ->
            val returnedUris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra("URI_SET", Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra<Uri>("URI_SET")
            }

            returnedUris?.let { uris ->
                handleSelectedFiles(uris)
            }
        }
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Timber.d("shouldShowRequestPermissionRationale")

                showRationaleDialog()
            }

            else -> {
                Timber.d("Asking permission for camera")
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    fun showBottomActionSheet() {
        val bottomSheetFragment = OABottomSheetDialogFragment.newInstance()

        bottomSheetFragment.onMediaSourceSelected = { source ->
            when (source) {
                OABottomSheetDialogFragment.MediaSource.Camera -> requestCameraPermission()
                OABottomSheetDialogFragment.MediaSource.Images -> launchImagePicker()
                OABottomSheetDialogFragment.MediaSource.Storage -> openFilePicker()
            }
        }

        bottomSheetFragment.show(supportFragmentManager, OABottomSheetDialogFragment.TAG)
    }

    fun showRationaleDialog() {
        Utility.showMaterialMessage(
            this,
            title = "Note",
            message = "The app is asking for access so that you can add photos directly from the camera. This is indeed optional though.",
            positiveButtonText = "TRY AGAIN") {

            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun updateScreenshotPrevention() {
        if (Prefs.prohibitScreenshots) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}