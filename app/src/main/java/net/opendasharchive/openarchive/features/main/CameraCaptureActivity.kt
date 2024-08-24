package net.opendasharchive.openarchive.features.main

import android.os.Bundle
import android.util.Base64
import androidx.activity.OnBackPressedCallback
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import net.opendasharchive.openarchive.databinding.ActivityCameraCaptureBinding
import net.opendasharchive.openarchive.features.core.BaseActivity
import timber.log.Timber

class CameraCaptureActivity : BaseActivity() {
    private lateinit var binding: ActivityCameraCaptureBinding
    private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Camera"

        startCamera()

        binding.imageCaptureButton.setOnClickListener {
            takePhoto()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

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

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)

                // Convert byte array to Base64 string
                val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)

                Timber.d("Got image as base64 length ${base64Image.length}")

                // uploadImageToServer(base64Image)

                image.close()
            }

            override fun onError(exception: ImageCaptureException) {
                Timber.e("Photo capture failed: ${exception.message}", exception)
            }
        })
    }
}