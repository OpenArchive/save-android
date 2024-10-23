package net.opendasharchive.openarchive.features.main

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.animation.DecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import net.opendasharchive.openarchive.databinding.ActivityCameraCaptureBinding
import net.opendasharchive.openarchive.features.core.BaseActivity
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class CameraCaptureActivity : BaseActivity() {
    private lateinit var binding: ActivityCameraCaptureBinding
    private lateinit var cameraProvider: ProcessCameraProvider
    private var imageCapture: ImageCapture? = null
    private var uris = mutableSetOf<Uri>()

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Camera"

        startCamera()

        binding.imageCaptureButton.setOnClickListener {
            val resultIntent = Intent().apply {
                putParcelableArrayListExtra("URI_SET", ArrayList(uris))
            }

            takePhoto()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Timber.d("Back pressed")

//                cameraProvider.unbindAll()

//                handleSelectedImages(ArrayList(uris))

                val resultIntent = Intent().apply {
                    putParcelableArrayListExtra("URI_SET", ArrayList(uris))
                }
                setResult(Activity.RESULT_OK, resultIntent)

                finish()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraProvider.unbindAll()
    }

//    fun handleSelectedImages(uris: List<Uri>) {
//        if (uris.isNotEmpty()) {
//            val mediaList = Picker.import(this@CameraCaptureActivity, Folder.current, uris)
//
//            mediaList.forEach { media ->
//                media.status = Media.Status.Local
//                media.selected = false
//                media.save()
//            }
//        } else {
//            // No images were selected
//            Timber.d("No images selected")
//        }
//    }

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)
            } catch(e: Exception) {
                Timber.e(e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun getOutputFileOptions(): ImageCapture.OutputFileOptions {
        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Save")
            }
        }

        // Create output options object which contains file + metadata
        return ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                     MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                     contentValues)
            .build()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val outputOptions = getOutputFileOptions()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Timber.d("Photo capture failed: ${exc.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { uri ->
                        Timber.d("Photo capture succeeded: $uri")
                        uris.add(uri)
                    } ?: {
                        Timber.d("Errr")
                    }
                }
            }
        )

        showCameraEffect()
    }

    private fun showCameraEffect() {
        binding.cameraEffect.alpha = 0.5f
        binding.cameraEffect
            .animate()
            .setDuration(400)
            .alpha(0f)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }
}