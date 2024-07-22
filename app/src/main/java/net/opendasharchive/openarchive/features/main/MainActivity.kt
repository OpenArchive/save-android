package net.opendasharchive.openarchive.features.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.esafirm.imagepicker.features.ImagePickerLauncher
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.adapters.FolderAdapter
import net.opendasharchive.openarchive.adapters.FolderAdapterListener
import net.opendasharchive.openarchive.adapters.SpaceAdapter
import net.opendasharchive.openarchive.adapters.SpaceAdapterListener
import net.opendasharchive.openarchive.databinding.ActivityMainBinding
import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.db.Project
import net.opendasharchive.openarchive.db.Space
import net.opendasharchive.openarchive.extensions.getMeasurments
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.folders.AddFolderActivity
import net.opendasharchive.openarchive.features.media.AddMediaDialogFragment
import net.opendasharchive.openarchive.features.media.Picker
import net.opendasharchive.openarchive.features.media.PreviewActivity
import net.opendasharchive.openarchive.features.onboarding.Onboarding23Activity
import net.opendasharchive.openarchive.features.onboarding.ServerSetupActivity
import net.opendasharchive.openarchive.upload.UploadService
import net.opendasharchive.openarchive.util.AlertHelper
import net.opendasharchive.openarchive.util.Prefs
import net.opendasharchive.openarchive.util.ProofModeHelper
import net.opendasharchive.openarchive.util.extensions.Position
import net.opendasharchive.openarchive.util.extensions.hide
import net.opendasharchive.openarchive.util.extensions.scaleAndTintDrawable
import net.opendasharchive.openarchive.util.extensions.scaled
import net.opendasharchive.openarchive.util.extensions.setDrawable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.aviran.cookiebar2.CookieBar
import timber.log.Timber
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


class MainActivity : BaseActivity(), FolderAdapterListener, SpaceAdapterListener {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private var mMenuDelete: MenuItem? = null
    private var mMenuNoWifiIndicator: MenuItem? = null
    private var mSnackBar: Snackbar? = null

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mMediaPickerLauncher: ImagePickerLauncher
    private lateinit var mFilePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var networkRequest: NetworkRequest
    private lateinit var connectivityManager: ConnectivityManager

    // These are for the side panel. Refactor all this.
    //
    private lateinit var mSpaceAdapter: SpaceAdapter
    private lateinit var mFolderAdapter: FolderAdapter

    // And this is for the pager.
    //
    private lateinit var mPagerAdapter: PagerAdapter

    private var mLastItem: Int = 0
    private var mLastMediaItem: Int = 0
    private var serverListOffset: Float = 0F
    private var serverListCurOffset: Float = 0F

    private val observer = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            Timber.d("onItemRangeInserted")
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            super.onItemRangeChanged(positionStart, itemCount)
            Timber.d("onItemRangeChanged")

//            if (SaveApp.hasUserAuthenticated) {
//                dismissSplashScreen()
//            }
        }
    }

    private var mCurrentItem
        get() = mBinding.pager.currentItem
        set(value) {
            mBinding.pager.currentItem = value
            updateBottomNavbar(value)
        }

    private val mNewFolderResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            if (it.resultCode == RESULT_OK) {
//                refreshProjects(it.data?.getLongExtra(AddFolderActivity.EXTRA_FOLDER_ID, -1))
//            }
        }

    private fun dismissSplashScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
        }, 3000)
    }

    @Suppress("DEPRECATION")
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                // Hide the navigation bar
                controller.hide(WindowInsets.Type.navigationBars())
                // Ensure the status bar stays visible
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // For API level < 30
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Timber.d("Selected file URI: $it")
        }
    }

    private fun preview() {
         val projectId = getSelectedProject()?.id ?: return

        PreviewActivity.start(this, projectId)
    }

//    private fun showFileSourceActionSheet() {
//        val bottomSheetDialog = BottomSheetDialog(this)
//        val view = layoutInflater.inflate(R.layout.bottom_sheet_file_source, null)
//
//        view.findViewById<Button>(R.id.btnDocuments).setOnClickListener {
//            openFilePicker("application/*")
//            bottomSheetDialog.dismiss()
//        }
//
//        view.findViewById<Button>(R.id.btnImages).setOnClickListener {
//            openFilePicker("image/*")
//            bottomSheetDialog.dismiss()
//        }
//
//        view.findViewById<Button>(R.id.btnDownloads).setOnClickListener {
//            openFilePicker("*/*")
//            bottomSheetDialog.dismiss()
//        }
//
//        bottomSheetDialog.setContentView(view)
//        bottomSheetDialog.show()
//    }

    private fun openFilePicker(mimeType: String) {
        getContent.launch(mimeType)
    }

    private val onWifiStatusChanged: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Prefs.UPLOAD_WIFI_ONLY) {
                Timber.d("onReceive: wifi changed")
                setWifiIndicator(Prefs.uploadWifiOnly)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(onWifiStatusChanged)
    }

    private fun initBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    showMessage("Authentication error: $errString")
                    finish()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
//                    showMessage("Authentication succeeded!")
//                    splashScreen = installSplashScreen()
//                    dismissSplashScreen()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showMessage("Authentication failed")
                    finish()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authentication")
            .setSubtitle("Sign in using your biometric credential")
            .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun didSetupBiometricAuthentication(): Boolean {
        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                initBiometricPrompt()
                return true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                showMessage("No biometric features available on this device.")
                return false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                showMessage("Biometric features are currently unavailable.")
                return false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                showMessage("No biometric credentials are enrolled.")
                return false
            }
            else -> {
                showMessage("Error setting up biometric authentication.")
                return false
            }
        }
    }

    private fun showMessage(message: String) {
        Timber.d(message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        Timber.d("onRestoreInstanceState")
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        Timber.d("onSaveInstanceState")
//
//        if (SaveApp.hasThemeChanged) {
//            SaveApp.hasThemeChanged = false
//            outState.putBoolean(AppSettings.HAS_USER_AUTHENTICATED, true)
//        }
//    }

//    lateinit var splashScreen: androidx.core.splashscreen.SplashScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        if (Prefs.lockWithPasscode && didSetupBiometricAuthentication()) {
            Timber.d("Doing biometrics")
        } else {
            Timber.d("Not doing biometrics")
//            splashScreen = installSplashScreen()
//            dismissSplashScreen(after = 3000)
        }

        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(onWifiStatusChanged, IntentFilter(Prefs.UPLOAD_WIFI_ONLY))

        val launchers = Picker.register(this, mBinding.root, { getSelectedProject() }, { media ->
            showCurrentPage()

            if (media.isNotEmpty()) {
                preview()
            }
        })

        mMediaPickerLauncher = launchers.first
        mFilePickerLauncher = launchers.second

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = null

//        mSnackBar = mBinding.root.makeSnackBar(getString(R.string.importing_media))
//        (mSnackBar?.view as? SnackbarLayout)?.addView(ProgressBar(this))

//        mBinding.uploadEditButton.setOnClickListener {
//            startActivity(Intent(this, UploadManagerActivity::class.java))
//        }

        mPagerAdapter = PagerAdapter(supportFragmentManager, lifecycle)
        mPagerAdapter.registerAdapterDataObserver(observer)
        mBinding.pager.adapter = mPagerAdapter

        mBinding.pager.isUserInputEnabled = false
        mBinding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                mLastItem = position

                if (position < mPagerAdapter.settingsIndex) {
                    mLastMediaItem = position
                }

                updateBottomNavbar(position)

                showCurrentPage()
            }
        })

        mBinding.spaceName.setOnClickListener {
            var newAlpha = 0F

            if (serverListCurOffset != serverListOffset) {
                serverListCurOffset = serverListOffset
                mBinding.spaceName.setDrawable(R.drawable.ic_expand_more, Position.End, 0.75)
            } else {
                newAlpha = 1F
                serverListCurOffset = 0F
                mBinding.spaceName.setDrawable(R.drawable.ic_expand_less, Position.End, 0.75)
            }

            mBinding.spaces.visibility = View.VISIBLE
            mBinding.currentSpaceName.visibility = View.VISIBLE
            mBinding.newFolder.visibility = View.VISIBLE
            mBinding.folders.visibility = View.VISIBLE

            mBinding.spaces.animate().translationY(serverListCurOffset).alpha(newAlpha).withEndAction {
                run {
                    if (newAlpha == 0F) {
                        mBinding.spaces.hide(false)
                    }
                }
            }
            mBinding.currentSpaceName.animate().alpha(1 - newAlpha)
            mBinding.newFolder.animate().alpha(1 - newAlpha)
            mBinding.folders.animate().alpha(1 - newAlpha)
        }

        mBinding.currentSpaceName.text = Space.current?.friendlyName
        mBinding.currentSpaceName.setDrawable(Space.current?.getAvatar(applicationContext)?.scaled(32, applicationContext),
            Position.Start, tint = true)
        mBinding.currentSpaceName.compoundDrawablePadding =
            applicationContext.resources.getDimension(R.dimen.padding_small).roundToInt()

        mBinding.newFolder.scaleAndTintDrawable(Position.Start, 0.75)
        mBinding.newFolder.setOnClickListener {
            addFolder()
        }

        mBinding.myMediaButton.setOnClickListener {
            mCurrentItem = mLastMediaItem
        }
        mBinding.myMediaLabel.setOnClickListener {
            // perform click + play ripple animation
            mBinding.myMediaButton.isPressed = true
            mBinding.myMediaButton.isPressed = false
            mBinding.myMediaButton.performClick()
        }

        // add_button on the bottom bar
        //
        mBinding.addButton.setOnClickListener { addClicked() }

        // settings_button on bottom bar
        //
        mBinding.settingsButton.setOnClickListener {
            mCurrentItem = mPagerAdapter.settingsIndex
        }
        mBinding.settingsLabel.setOnClickListener {
            // perform click + play ripple animation
            mBinding.settingsButton.isPressed = true
            mBinding.settingsButton.isPressed = false
            mBinding.settingsButton.performClick()
        }

        if (Picker.canPickFiles(this)) {
            mBinding.addButton.setOnLongClickListener {
                val addMediaDialogFragment = AddMediaDialogFragment()
                addMediaDialogFragment.show(supportFragmentManager, addMediaDialogFragment.tag)

                true
            }

            supportFragmentManager.setFragmentResultListener(AddMediaDialogFragment.RESP_PHOTO_GALLERY, this) { _, _ ->
                addClicked()
            }

            supportFragmentManager.setFragmentResultListener(AddMediaDialogFragment.RESP_FILES, this) { _, _ ->
                addClicked(typeFiles = true)
            }
        }

        mMenuNoWifiIndicator?.setVisible(false)
    }

    private fun showCurrentPage() {
        val project = getSelectedProject()

        if (project != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                mPagerAdapter.notifyProjectChanged(project)
            }, 100)
        }
    }

    private fun addClicked(typeFiles: Boolean = false) {
        if (getSelectedProject() != null) {
            if (typeFiles && Picker.canPickFiles(this)) {
                Picker.pickFiles(mFilePickerLauncher)
            } else {
                pickMedia()
            }
        } else {
            if (!Prefs.addFolderHintShown) {
                AlertHelper.show(
                    this,
                    R.string.before_adding_media_create_a_new_folder_first,
                    R.string.to_get_started_please_create_a_folder,
                    R.drawable.ic_folder,
                    buttons = listOf(
                        AlertHelper.positiveButton(R.string.add_a_folder) { _, _ ->
                            Prefs.addFolderHintShown = true

                            addFolder()
                        },
                        AlertHelper.negativeButton(R.string.lbl_Cancel)
                    )
                )
            } else {
                addFolder()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onStart() {
        super.onStart()

        mSpaceAdapter = SpaceAdapter(this)
        mBinding.spaces.layoutManager = LinearLayoutManager(this)
        mBinding.spaces.adapter = mSpaceAdapter

        mFolderAdapter = FolderAdapter(this)
        mBinding.folders.layoutManager = LinearLayoutManager(this)
        mBinding.folders.adapter = mFolderAdapter

        networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)

        ProofModeHelper.init(this) {
            // Check for any queued uploads and restart, only after ProofMode is correctly initialized.
            UploadService.startUploadService(this)
        }

        // requestNotificationPermission()

//        registerReceiver(object : BroadcastReceiver() {
//            override fun onReceive(context: Context, intent: Intent) {
//                val status = intent.getStringExtra(TorService.EXTRA_STATUS)
//
//                Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
//
////                if (status == TorService.STATUS_ON) {
////                    CoroutineScope(Dispatchers.IO).launch {
////                        connectToRestEndpoint()
////                    }
////                }
//            }
//        }, IntentFilter(TorService.ACTION_STATUS), RECEIVER_NOT_EXPORTED)

//        class StartTor(val appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
//
//            override fun doWork(): Result {
//                Timber.d("StartTor")
//                bindService(Intent(appContext, TorService::class.java), object : ServiceConnection {
//                    override fun onServiceConnected(name: ComponentName, service: IBinder) {
//                        val torService: TorService = (service as TorService.LocalBinder).service
//
//                        while (torService.torControlConnection == null) {
//                            try {
//                                Timber.d("Sleeping")
//                                Thread.sleep(500)
//                            } catch (e: InterruptedException) {
//                                e.printStackTrace()
//                            }
//                        }
//
//                        Toast.makeText(
//                            this@MainActivity,
//                            "Got Tor control connection",
//                            Toast.LENGTH_LONG
//                        )
//                            .show()
//                    }
//
//                    override fun onServiceDisconnected(name: ComponentName) {
//                        // Things...
//                    }
//                }, BIND_AUTO_CREATE)
//
//                return Result.success()
//            }
//        }

//        class UploadMedia(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
//            override fun doWork(): Result {
//                Timber.d("UploadMedia")
//                val imageUriInput =
//                    inputData.getString("IMAGE_URI") ?: return Result.failure()
//
//                return Result.success()
//            }
//        }
//
//        val startTorRequest = OneTimeWorkRequestBuilder<StartTor>()
//            .build()
//
//        val uploadMediaRequest = OneTimeWorkRequestBuilder<UploadMedia>()
//            .addTag("media_upload")
//            .setInputData(workDataOf(
//                "IMAGE_URI" to "http://..."
//            ))
//            .build()
//
//        WorkManager.getInstance(this)
//            .enqueue(uploadMediaRequest)
    }

    private fun setWifiIndicator(uploadOnWifiOnly: Boolean) {
        if (uploadOnWifiOnly) {
            val network = connectivityManager.activeNetwork ?: return
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return
            val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

            Timber.d("Is Wifi available? $hasWifi")

            lifecycleScope.launch(Dispatchers.Main) {
                if (hasWifi) {
                    mMenuNoWifiIndicator?.setVisible(false)
                } else if (Prefs.uploadWifiOnly) {
                    mMenuNoWifiIndicator?.setVisible(true)
                }
            }
        } else {
            mMenuNoWifiIndicator?.setVisible(false)
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            Timber.d("networkCallback: wifi changed")
            super.onCapabilitiesChanged(network, networkCapabilities)
            setWifiIndicator(Prefs.uploadWifiOnly)
        }
    }

    suspend fun connectToRestEndpoint() {
        val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress("localhost", 9050))

        val client = OkHttpClient.Builder()
            .connectTimeout(3000L, TimeUnit.MILLISECONDS)
            .proxy(proxy)
            .build()

        val request = Request.Builder()
            .url("https://jsonplaceholder.typicode.com/todos/1")
            .build()

        try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val result = if (response.isSuccessful) {
                response.body?.string()
            } else {
                null
            }

            Timber.d("result: $result")
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onResume() {
        super.onResume()

//        window.navigationBarColor = getColor(R.color.colorPrimary)
//
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        WindowInsetsControllerCompat(window, findViewById(android.R.id.content)).let { controller ->
//            controller.hide(WindowInsetsCompat.Type.systemBars())
//            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//        }

        val results = Media.getAll()
        results.forEach { media ->
            Timber.d("Media = $media")
        }

        refreshSpace()

        mCurrentItem = mLastItem

        if (!Prefs.didCompleteOnboarding) {
            startActivity(Intent(this, Onboarding23Activity::class.java))
        }

        importSharedMedia(intent)

        if (serverListOffset == 0F) {
            val dims = mBinding.spaces.getMeasurments()
            serverListOffset = -dims.second.toFloat()
            serverListCurOffset = serverListOffset
            mBinding.spaces.visibility = View.GONE
            mBinding.spaces.animate().translationY(serverListOffset)
            mBinding.spaceName.setDrawable(R.drawable.ic_expand_more, Position.End, 0.75)
        }
    }

//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    private fun requestNotificationPermission() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//        }
//    }

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//
//        importSharedMedia(intent)
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        mMenuDelete = menu.findItem(R.id.menu_delete)
        mMenuNoWifiIndicator = menu.findItem(R.id.menu_no_wifi)

        setWifiIndicator(Prefs.uploadWifiOnly)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_no_wifi -> {
                CookieBar.build(this@MainActivity)
                    .setTitle("Hi, there!")
                    .setMessage("You have chosen to only upload media over wifi and there is currently no connection.\n\nIf you'd like to disable this, please go to the Settings screen. If not, your uploads will resume once you are connected to wifi again.")     // i.e. R.string.message
                    .setCookiePosition(CookieBar.BOTTOM)
                    .setIcon(R.mipmap.ic_launcher_round)
                    .setDuration(5000)
                    .show()

                true
            }

//            R.id.menu_folders -> {
//                // https://stackoverflow.com/questions/21796209/how-to-create-a-custom-navigation-drawer-in-android
//
//                if (mBinding.root.isDrawerOpen(mBinding.folderBar)) {
//                    mBinding.root.closeDrawer(mBinding.folderBar)
//                } else {
//                    mBinding.root.openDrawer(mBinding.folderBar)
//                }
//                true
//            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun updateAfterDelete(done: Boolean) {
        mMenuDelete?.isVisible = !done
    }

    private fun addFolder() {
        mNewFolderResultLauncher.launch(Intent(this, AddFolderActivity::class.java))

        mBinding.root.closeDrawer(mBinding.folderBar)
    }

    private fun importSharedMedia(data: Intent?) {
        if (data?.action != Intent.ACTION_SEND) return

        val uri = data.data ?: if ((data.clipData?.itemCount
                ?: 0) > 0
        ) data.clipData?.getItemAt(0)?.uri else null
        val path = uri?.path ?: return

        if (path.contains(packageName)) return

        mSnackBar?.show()

        lifecycleScope.launch(Dispatchers.IO) {
//            val media = Picker.import(this@MainActivity, getSelectedProject(), uri)
//
//            lifecycleScope.launch(Dispatchers.Main) {
//                mSnackBar?.dismiss()
//                intent = null
//
//                if (media != null) {
//                    preview()
//                }
//            }
        }
    }

    private fun pickMedia() {
        Picker.pickMedia(this, mMediaPickerLauncher)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            2 -> pickMedia()
        }
    }

//    private fun showAlertIcon() {
//        mBinding.alertIcon.show()
//        TooltipCompat.setTooltipText(
//            mBinding.alertIcon,
//            getString(R.string.unsecured_internet_connection)
//        )
//    }

    private fun updateBottomNavbar(position: Int) {
        if (position == mPagerAdapter.settingsIndex) {
            mBinding.myMediaButton.setIconResource(R.drawable.outline_perm_media_24)
            mBinding.settingsButton.setIconResource(R.drawable.ic_settings_filled)
        } else {
            mBinding.myMediaButton.setIconResource(R.drawable.perm_media_24px)
            mBinding.settingsButton.setIconResource(R.drawable.ic_settings)
        }
    }

    override fun spaceClicked(space: Space) {
        Space.current = space

        refreshSpace()

        mBinding.root.closeDrawer(mBinding.folderBar)

//        mBinding.spacesCard.disableAnimation {
//            mBinding.spacesCard.hide()
//        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun projectClicked(project: Project) {
        mCurrentItem = mPagerAdapter.projects.indexOf(project)

        mBinding.root.closeDrawer(mBinding.folderBar)

//        mBinding.folderBar.disableAnimation {
//            mBinding.folderBar.hide()
//        }

        // make sure that even when navigating to settings and picking a folder there
        // the dataset will get update correctly
        mFolderAdapter.notifyDataSetChanged()
    }

    override fun getSelectedProject(): Project? {
        return mPagerAdapter.getProject(mCurrentItem)
    }

    override fun addSpaceClicked() {
        // mBinding.root.closeDrawer(mBinding.folderBar)

        startActivity(Intent(this, ServerSetupActivity::class.java))
    }

    override fun getSelectedSpace(): Space? {
        return Space.current
    }

    private fun refreshPages(setProjectId: Long? = null) {
        val projects = Space.current?.projects ?: emptyList()

        mPagerAdapter.updateData(projects)

        mBinding.pager.isSaveFromParentEnabled = false
        mBinding.pager.adapter = mPagerAdapter

        setProjectId?.let {
            mCurrentItem = mPagerAdapter.getProjectIndexById(it, default = 0)
        }

        mFolderAdapter.update(projects)

        showCurrentPage()
    }

    private fun refreshSpace() {
        mBinding.spaceName.text = "Servers" // currentSpace.friendlyName

        mSpaceAdapter.update(Space.getAll().asSequence().toList())

        refreshPages()
    }
}
