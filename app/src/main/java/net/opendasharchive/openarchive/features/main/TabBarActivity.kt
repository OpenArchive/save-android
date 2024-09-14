package net.opendasharchive.openarchive.features.main

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.esafirm.imagepicker.features.ImagePickerLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityTabBarBinding
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.features.backends.BackendSetupActivity
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.main.ui.OABottomSheetDialogFragment
import net.opendasharchive.openarchive.features.media.Picker
import net.opendasharchive.openarchive.features.media.Picker.pickMedia
import net.opendasharchive.openarchive.features.settings.SettingsFragment
import net.opendasharchive.openarchive.upload.BroadcastManager.Action
import net.opendasharchive.openarchive.util.Prefs
import net.opendasharchive.openarchive.util.Utility
import org.aviran.cookiebar2.CookieBar
import timber.log.Timber

class TabBarActivity : BaseActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    enum class Screen(val value: Int) {
        MEDIA(1),
        SETTINGS(2);

        companion object {
            fun fromInt(value: Int) = entries.first { it.value == value }
        }
    }

    private lateinit var binding: ActivityTabBarBinding
    private lateinit var networkRequest: NetworkRequest
    private lateinit var connectivityManager: ConnectivityManager
    private var wifiIssueIndicator: MenuItem? = null
    private var visibleScreen = Screen.MEDIA
    private lateinit var mMediaPickerLauncher: ImagePickerLauncher
    private lateinit var mFilePickerLauncher: ActivityResultLauncher<Intent>

    private val newFolderResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                Timber.d("Got new folder")
            }
        }

    private val cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            Timber.d("step 10")
            when (it.resultCode) {
                RESULT_OK -> processIntentResult(it.data)
                else -> Timber.d("Failed with code ${it.resultCode}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTabBarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = null

        val launchers = Picker.register(this, binding.root, { Folder.current }, { media ->
            Timber.d("media = $media")

            val i = Intent(Action.Add.id)

            LocalBroadcastManager.getInstance(this).sendBroadcastSync(i)

//            if (media.isNotEmpty()) {
//                preview()
//            }
        })

        mMediaPickerLauncher = launchers.first
        mFilePickerLauncher = launchers.second

        binding.bottomBar.addButton.setOnClickListener {
            didClickMediaButton()
        }

        binding.bottomBar.myMediaButton.setOnClickListener {
            if (visibleScreen != Screen.MEDIA) {
                visibleScreen = Screen.MEDIA
                supportFragmentManager.commit() {
                    setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    replace(binding.navHostFragment.id, MainMediaFragment())
                }
                updateNavBar()
            }
        }

        binding.bottomBar.settingsButton.setOnClickListener {
            if (visibleScreen != Screen.SETTINGS) {
                visibleScreen = Screen.SETTINGS
                supportFragmentManager.commit() {
                    setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    replace(binding.navHostFragment.id, SettingsFragment())
                }
                updateNavBar()
            }
        }

        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(onWifiStatusChanged, IntentFilter(Prefs.UPLOAD_WIFI_ONLY))

        wifiIssueIndicator?.setVisible(false)
    }

    override fun onResume() {
        super.onResume()

        updateNavBar()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        wifiIssueIndicator = menu.findItem(R.id.menu_no_wifi)

        setWifiIndicator(Prefs.uploadWifiOnly)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_no_wifi -> {
                showWifiStatusDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val previousScreen = savedInstanceState.getInt("VISIBLE_SCREEN")

        if (previousScreen != 0) {
            visibleScreen = Screen.fromInt(previousScreen)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt("VISIBLE_SCREEN", visibleScreen.value)
    }

    override fun onStart() {
        super.onStart()

        networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    private fun showWifiStatusDialog() {
        CookieBar.build(this@TabBarActivity)
            .setTitle("Hi, there!")
            .setMessage("You have chosen to only upload media over wifi and there is currently no connection.\n\nIf you'd like to disable this, please go to the Settings screen. If not, your uploads will resume once you are connected to wifi again.")     // i.e. R.string.message
            .setCookiePosition(CookieBar.BOTTOM)
            .setIcon(R.mipmap.ic_launcher_round)
            .setAnimationIn(R.anim.slide_down, R.anim.slide_up)
            .setAnimationOut(R.anim.slide_up, R.anim.slide_down)
            .setDuration(5000)
            .show()
    }

    private fun updateNavBar() {
        if (visibleScreen == Screen.MEDIA) {
            binding.bottomBar.myMediaButton.setIconResource(R.drawable.perm_media_24px)
            binding.bottomBar.settingsButton.setIconResource(R.drawable.ic_settings)
        } else {
            binding.bottomBar.myMediaButton.setIconResource(R.drawable.outline_perm_media_24)
            binding.bottomBar.settingsButton.setIconResource(R.drawable.ic_settings_filled)
        }
    }

    private fun didClickMediaButton() {
        if (Folder.current == null) {
            addBackend()
        } else {
            showBottomActionSheet()
        }
    }

    private fun addBackend() {
        Utility.showMaterialPrompt(
            this,
            "Question",
            "You don't have any media servers or folders. Would you like to set one up now?",
            "YES",
            "NO") { affirm ->

            if (affirm) {
                newFolderResultLauncher.launch(Intent(this, BackendSetupActivity::class.java))
            }
        }
    }

    private fun setWifiIndicator(uploadOnWifiOnly: Boolean) {
        if (uploadOnWifiOnly) {
            val network = connectivityManager.activeNetwork

            if (network != null) {
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return
                val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

                Timber.d("Is Wifi available? $hasWifi")

                lifecycleScope.launch(Dispatchers.Main) {
                    if (hasWifi) {
                        wifiIssueIndicator?.setVisible(false)
                    } else if (Prefs.uploadWifiOnly) {
                        wifiIssueIndicator?.setVisible(true)
                    }
                }
            } else {
                wifiIssueIndicator?.setVisible(true)
            }
        } else {
            wifiIssueIndicator?.setVisible(false)
        }
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
                handleSelectedImages(uris)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                pickMedia(this, mMediaPickerLauncher)
            } else {
                Timber.d("External storage permission denied")
            }
        }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            cameraResultLauncher.launch(Intent(this, CameraCaptureActivity::class.java))
        } else {
            Timber.d("Camera permission denied")
        }
    }

    private val pickMultipleMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
        handleSelectedImages(uris)
    }

    private val legacyPickMultipleMedia = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        handleSelectedImages(uris)
    }

    private fun handleSelectedImages(uris: List<Uri>) {
        if (uris.isNotEmpty()) {
            for (uri in uris) {
                val mimeType = contentResolver.getType(uri)
                when {
                    mimeType?.startsWith("image/") == true -> handleImage(uri)
                    mimeType?.startsWith("video/") == true -> handleVideo(uri)
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

    private fun handleImage(uri: Uri) {
        Picker.import(this@TabBarActivity, Folder.current, uri)?.let { media ->
            media.status = Media.Status.Local
            media.selected = false
            media.save()
        }
    }

    private fun handleVideo(uri: Uri) {
        Picker.import(this@TabBarActivity, Folder.current, uri)?.let { media ->
            media.status = Media.Status.Local
            media.selected = false
            media.save()
        }
    }

    private fun launchLegacyPicker() {
        legacyPickMultipleMedia.launch("image/*")
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

    private fun showRationaleDialog() {
        Utility.showMaterialMessage(
            this,
            title = "Note",
            message = "The app is asking for access so that you can add photos directly from the camera. This is indeed optional though.",
            positiveButtonText = "TRY AGAIN") {

            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun showBottomActionSheet() {
        val bottomSheetFragment = OABottomSheetDialogFragment.newInstance()

        bottomSheetFragment.onMediaSourceSelected = { source ->
            when (source) {
                OABottomSheetDialogFragment.MediaSource.Camera -> requestCameraPermission()
                OABottomSheetDialogFragment.MediaSource.Images -> launchImagePicker()
            }
        }

        bottomSheetFragment.show(supportFragmentManager, OABottomSheetDialogFragment.TAG)
    }

//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
//        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
//    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            Timber.d("networkCallback: wifi changed $networkCapabilities")
            super.onCapabilitiesChanged(network, networkCapabilities)
            setWifiIndicator(Prefs.uploadWifiOnly)
        }
    }

    private val onWifiStatusChanged: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Prefs.UPLOAD_WIFI_ONLY) {
                Timber.d("onReceive: wifi changed")
                setWifiIndicator(Prefs.uploadWifiOnly)
            }
        }
    }
}