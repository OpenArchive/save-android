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
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.esafirm.imagepicker.features.ImagePickerLauncher
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityTabBarBinding
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.features.backends.BackendSetupActivity
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.media.Picker
import net.opendasharchive.openarchive.features.media.Picker.pickMedia
import net.opendasharchive.openarchive.features.settings.SettingsFragment
import net.opendasharchive.openarchive.util.Prefs
import org.aviran.cookiebar2.CookieBar
import timber.log.Timber

class TabBarActivity : BaseActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private enum class Screen {
        MEDIA, SETTINGS
    }

    private lateinit var binding: ActivityTabBarBinding
    private lateinit var networkRequest: NetworkRequest
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var mediaPickerLauncher: ImagePickerLauncher
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private var wifiIssueIndicator: MenuItem? = null
    private var visibleScreen = Screen.MEDIA
    private lateinit var mMediaPickerLauncher: ImagePickerLauncher
    private lateinit var mFilePickerLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private val newFolderResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                Timber.d("Got media")
                // refreshProjects(it.data?.getLongExtra(AddFolderActivity.EXTRA_FOLDER_ID, -1))
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

        updateNavBar()

        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(onWifiStatusChanged, IntentFilter(Prefs.UPLOAD_WIFI_ONLY))

        wifiIssueIndicator?.setVisible(false)

//        val folder = Folder(description = "Photographer Collab", backend = Backend.getById(1)!!)
//        folder.save()

//        val folder = Folder.getById(1)
//        Folder.current = folder

//        val rusty = Rusty()
//        val greeting = rusty.rust_greeting("Bob")
//        Timber.d("Greeting = $greeting")
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
                CookieBar.build(this@TabBarActivity)
                    .setTitle("Hi, there!")
                    .setMessage("You have chosen to only upload media over wifi and there is currently no connection.\n\nIf you'd like to disable this, please go to the Settings screen. If not, your uploads will resume once you are connected to wifi again.")     // i.e. R.string.message
                    .setCookiePosition(CookieBar.BOTTOM)
                    .setIcon(R.mipmap.ic_launcher_round)
                    .setAnimationIn(R.anim.slide_down, R.anim.slide_up)
                    .setAnimationOut(R.anim.slide_up, R.anim.slide_down)
                    .setDuration(5000)
                    .show()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
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
            showFileSourceActionSheet()
        }
    }

    private fun addBackend() {
        newFolderResultLauncher.launch(Intent(this, BackendSetupActivity::class.java))
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

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Timber.d("Selected file URI: $it")
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                pickMedia(this, mMediaPickerLauncher)
            } else {
                Timber.d("le sigh")
            }
        }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                startActivity(Intent(this, CameraCaptureActivity::class.java))
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Timber.d("really?")
                // Provide an additional rationale to the user if the permission was not granted
                // and the user would benefit from additional context for the use of the permission.
            }

            else -> {
                // Ask for the permission
                Timber.d("Asking permission for camera")
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showFileSourceActionSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_file_source, null)

        view.findViewById<Button>(R.id.btnImages).setOnClickListener {
            pickMedia(this, mMediaPickerLauncher)
            bottomSheetDialog.dismiss()
        }

        view.findViewById<Button>(R.id.camera_button).setOnClickListener {
            requestCameraPermission()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
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