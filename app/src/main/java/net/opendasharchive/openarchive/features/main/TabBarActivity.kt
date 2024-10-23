package net.opendasharchive.openarchive.features.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityTabBarBinding
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.features.backends.BackendSetupActivity
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.settings.SettingsFragment
import net.opendasharchive.openarchive.services.snowbird.SnowbirdBridge
import net.opendasharchive.openarchive.services.snowbird.service.SnowbirdService
import net.opendasharchive.openarchive.upload.MediaUploadStatusViewModel
import net.opendasharchive.openarchive.util.NetworkConnectivityViewModel
import net.opendasharchive.openarchive.util.Utility
import org.aviran.cookiebar2.CookieBar
import org.koin.androidx.viewmodel.ext.android.viewModel
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
    private var wifiIssueIndicator: MenuItem? = null
    private var visibleScreen = Screen.MEDIA

    private val mediaUploadStatusViewModel: MediaUploadStatusViewModel by viewModel()
    private val networkConnectivityViewModel: NetworkConnectivityViewModel by viewModels()

    private val _uriStateFlow = MutableStateFlow<Uri?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Timber.d("Able to post notifications")
        } else {
            Timber.d("Need to explain")
        }
    }

    private val newFolderResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                Timber.d("Got new folder")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTabBarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = null

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

        networkConnectivityViewModel.networkStatusLiveData.observe(this) { gotWifi ->
            Timber.d("Got wifi? $gotWifi")
        }

        checkNotificationPermissions()

        SnowbirdBridge.getInstance().initialize()

        val intent = Intent(this, SnowbirdService::class.java)
        startForegroundService(intent)

        handleIntent(intent)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                _uriStateFlow.collect { uri ->
                    uri?.let { processUri(it) }
                }
            }
        }

//        val mediaUri = Uri.parse("file:///data/user/0/net.opendasharchive.openarchive.debug/cache/20240924_165908.riot3.jpg")
//        MediaUploadManager.scheduleMediaUpload(mediaUri)
    }

    override fun onResume() {
        super.onResume()

        updateNavBar()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        wifiIssueIndicator = menu.findItem(R.id.menu_no_wifi)

//        setWifiIndicator(Prefs.uploadWifiOnly)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
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

//    override fun onStart() {
//        super.onStart()
//
//        networkRequest = NetworkRequest.Builder()
//            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
//            .build()
//
//        connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager
//        connectivityManager.requestNetwork(networkRequest, networkCallback)
//    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            if (uri?.scheme == "save-veilid") {
                _uriStateFlow.update { uri }
            }
        }
    }

    private fun processUri(uri: Uri) {
        val path = uri.path
        val queryParams = uri.queryParameterNames.associateWith { uri.getQueryParameter(it) }
        Timber.d("Got path = $path and params = $queryParams")
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

    private fun checkNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Timber.d("We have notifications permissions")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showNotificationPermissionRationale()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun showNotificationPermissionRationale() {
        Utility.showMaterialWarning(this, "Accept!") {
            Timber.d("thing")
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

//    private fun setWifiIndicator(uploadOnWifiOnly: Boolean) {
//        if (uploadOnWifiOnly) {
//            val network = connectivityManager.activeNetwork
//
//            if (network != null) {
//                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return
//                val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
//
//                Timber.d("Is Wifi available? $hasWifi")
//
//                lifecycleScope.launch(Dispatchers.Main) {
//                    if (hasWifi) {
//                        wifiIssueIndicator?.setVisible(false)
//                    } else if (Prefs.uploadWifiOnly) {
//                        wifiIssueIndicator?.setVisible(true)
//                    }
//                }
//            } else {
//                wifiIssueIndicator?.setVisible(true)
//            }
//        } else {
//            wifiIssueIndicator?.setVisible(false)
//        }
//    }

//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
//        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
//    }

//    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
//        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
//            Timber.d("networkCallback: wifi changed $networkCapabilities")
//            super.onCapabilitiesChanged(network, networkCapabilities)
//            setWifiIndicator(Prefs.uploadWifiOnly)
//        }
//    }

//    private val onWifiStatusChanged: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            if (intent.action == Prefs.UPLOAD_WIFI_ONLY) {
//                Timber.d("onReceive: wifi changed")
//                setWifiIndicator(Prefs.uploadWifiOnly)
//            }
//        }
//    }
}