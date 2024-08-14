package net.opendasharchive.openarchive.features.main

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.esafirm.imagepicker.features.ImagePickerLauncher
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityMainBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.features.backends.BackendSetupActivity
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.folders.AddFolderActivity
import net.opendasharchive.openarchive.features.media.AddMediaDialogFragment
import net.opendasharchive.openarchive.features.media.Picker
import net.opendasharchive.openarchive.features.onboarding.Onboarding23Activity
import net.opendasharchive.openarchive.upload.UploadService
import net.opendasharchive.openarchive.util.Prefs
import net.opendasharchive.openarchive.util.ProofModeHelper
import org.aviran.cookiebar2.CookieBar
import org.torproject.jni.TorService
import timber.log.Timber


class MainActivity : BaseActivity() {

//    private lateinit var biometricPrompt: BiometricPrompt
//    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private var mMenuDelete: MenuItem? = null
    private var mMenuNoWifiIndicator: MenuItem? = null
    private var mSnackBar: Snackbar? = null

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mMediaPickerLauncher: ImagePickerLauncher
    private lateinit var mFilePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var networkRequest: NetworkRequest
    private lateinit var connectivityManager: ConnectivityManager

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

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Timber.d("Selected file URI: $it")
        }
    }

    private fun preview() {
//         val folderId = getSelectedFolder()?.id ?: return
//
//        PreviewActivity.start(this, folderId)
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

//    private fun initBiometricPrompt() {
//        val executor = ContextCompat.getMainExecutor(this)
//
//        biometricPrompt = BiometricPrompt(this, executor,
//            object : BiometricPrompt.AuthenticationCallback() {
//                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
//                    super.onAuthenticationError(errorCode, errString)
//                    showMessage("Authentication error: $errString")
//                    finish()
//                }
//
//                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
//                    super.onAuthenticationSucceeded(result)
//                    showMessage("Authentication succeeded!")
//                    splashScreen = installSplashScreen()
//                    dismissSplashScreen()
//                }
//
//                override fun onAuthenticationFailed() {
//                    super.onAuthenticationFailed()
//                    showMessage("Authentication failed")
//                    finish()
//                }
//            })
//
//        promptInfo = BiometricPrompt.PromptInfo.Builder()
//            .setTitle("Authentication")
//            .setSubtitle("Sign in using your biometric credential")
//            .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_STRONG)
//            .build()
//
//        biometricPrompt.authenticate(promptInfo)
//    }
//
//    private fun didSetupBiometricAuthentication(): Boolean {
//        val biometricManager = BiometricManager.from(this)
//
//        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
//            BiometricManager.BIOMETRIC_SUCCESS -> {
//                initBiometricPrompt()
//                return true
//            }
//            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
//                showMessage("No biometric features available on this device.")
//                return false
//            }
//            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
//                showMessage("Biometric features are currently unavailable.")
//                return false
//            }
//            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
//                showMessage("No biometric credentials are enrolled.")
//                return false
//            }
//            else -> {
//                showMessage("Error setting up biometric authentication.")
//                return false
//            }
//        }
//    }

    private fun showMessage(message: String) {
        Timber.d(message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

//        val filecoinManager = FilecoinJ2V8Manager(this)
//        filecoinManager.initialize()
//
//        try {
//            val walletAddress = filecoinManager.createWallet()
//            Log.d("Filecoin", "Created wallet: $walletAddress")
//
//            val balance = filecoinManager.getBalance(walletAddress)
//            Log.d("Filecoin", "Wallet balance: $balance")
//
////            val dealCid = filecoinManager.createDeal("t01234", "QmExampleCID...", 1.0, 2880)
////            Timber.d("Filecoin", "Created deal: $dealCid")
//        } catch (e: Exception) {
//            Log.e("Filecoin", "Error: ${e.localizedMessage}")
//        }

//        if (Prefs.lockWithPasscode && didSetupBiometricAuthentication()) {
//            Timber.d("Doing biometrics")
//        } else {
//            Timber.d("Not doing biometrics")
//        }

        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(onWifiStatusChanged, IntentFilter(Prefs.UPLOAD_WIFI_ONLY))

        val launchers = Picker.register(this, mBinding.root, { mPagerAdapter.getFolder(mCurrentItem) }, { media ->
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

        val folders = Backend.current?.folders ?: emptyList()
        mPagerAdapter.updateData(folders)

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

        mBinding.bottomBar.myMediaButton.setOnClickListener {
            mCurrentItem = mLastMediaItem
        }

        // add_button on the bottom bar
        //
        mBinding.bottomBar.addButton.setOnClickListener { addMedia() }

        // settings_button on bottom bar
        //
        mBinding.bottomBar.settingsButton.setOnClickListener {
            mCurrentItem = mPagerAdapter.settingsIndex
        }

        if (Picker.canPickFiles(this)) {
            mBinding.bottomBar.addButton.setOnLongClickListener {
                val addMediaDialogFragment = AddMediaDialogFragment()
                addMediaDialogFragment.show(supportFragmentManager, addMediaDialogFragment.tag)

                true
            }

            supportFragmentManager.setFragmentResultListener(AddMediaDialogFragment.RESP_PHOTO_GALLERY, this) { _, _ ->
                addMedia()
            }

            supportFragmentManager.setFragmentResultListener(AddMediaDialogFragment.RESP_FILES, this) { _, _ ->
                addMedia(typeFiles = true)
            }
        }

        mMenuNoWifiIndicator?.setVisible(false)
    }

    private fun showCurrentPage() {
        val folder = mPagerAdapter.getFolder(mCurrentItem)

        if (folder != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                mPagerAdapter.notifyFolderChanged(folder)
            }, 100)
        }
    }

    private fun addMedia(typeFiles: Boolean = false) {
        if (Backend.current != null) {
            if (typeFiles && Picker.canPickFiles(this)) {
                Picker.pickFiles(mFilePickerLauncher)
            } else {
                pickMedia()
            }
        } else {
//            if (!Prefs.addFolderHintShown) {
//                AlertHelper.show(
//                    this,
//                    R.string.before_adding_media_create_a_new_folder_first,
//                    R.string.to_get_started_please_create_a_folder,
//                    R.drawable.ic_folder,
//                    buttons = listOf(
//                        AlertHelper.positiveButton(R.string.add_a_folder) { _, _ ->
//                            Prefs.addFolderHintShown = true
//
//                            addFolder()
//                        },
//                        AlertHelper.negativeButton(R.string.lbl_Cancel)
//                    )
//                )
            addBackend()
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onStart() {
        super.onStart()

        // Folder.current = Folder.getById(1)

        Timber.d("Current backend = ${Backend.current?.friendlyName}")
        Timber.d("Current folder = ${Folder.current?.description}")

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

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val status = intent.getStringExtra(TorService.EXTRA_STATUS)

                Toast.makeText(context, status, Toast.LENGTH_SHORT).show()

                if (status == TorService.STATUS_ON) {
                    CoroutineScope(Dispatchers.IO).launch {
                        // connectToRestEndpoint()
                    }
                }
            }
        }, IntentFilter(TorService.ACTION_STATUS), RECEIVER_NOT_EXPORTED)

        class StartTor(val appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
            override fun doWork(): Result {
                Timber.d("StartTor")
                bindService(Intent(appContext, TorService::class.java), object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName, service: IBinder) {
                        val torService: TorService = (service as TorService.LocalBinder).service

                        while (torService.torControlConnection == null) {
                            try {
                                Timber.d("Sleeping")
                                Thread.sleep(500)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        }

                        Toast.makeText(
                            this@MainActivity,
                            "Got Tor control connection",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }

                    override fun onServiceDisconnected(name: ComponentName) {
                        // Things...
                    }
                }, BIND_AUTO_CREATE)

                return Result.success()
            }
        }

        class UploadMedia(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
            override fun doWork(): Result {
                Timber.d("UploadMedia")
                val imageUriInput =
                    inputData.getString("IMAGE_URI") ?: return Result.failure()

                return Result.success()
            }
        }

        val startTorRequest = OneTimeWorkRequestBuilder<StartTor>()
            .build()

        val uploadMediaRequest = OneTimeWorkRequestBuilder<UploadMedia>()
            .addTag("media_upload")
            .setInputData(workDataOf(
                "IMAGE_URI" to "http://..."
            ))
            .build()

        WorkManager.getInstance(this)
            .enqueue(uploadMediaRequest)
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
                        mMenuNoWifiIndicator?.setVisible(false)
                    } else if (Prefs.uploadWifiOnly) {
                        mMenuNoWifiIndicator?.setVisible(true)
                    }
                }
            } else {
                mMenuNoWifiIndicator?.setVisible(true)
            }
        } else {
            mMenuNoWifiIndicator?.setVisible(false)
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            Timber.d("networkCallback: wifi changed $networkCapabilities")
            super.onCapabilitiesChanged(network, networkCapabilities)
            setWifiIndicator(Prefs.uploadWifiOnly)
        }
    }

//    suspend fun connectToRestEndpoint() {
//        val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress("localhost", 9050))
//
//        val client = OkHttpClient.Builder()
//            .connectTimeout(3000L, TimeUnit.MILLISECONDS)
//            .proxy(proxy)
//            .build()
//
//        val request = Request.Builder()
//            .url("https://jsonplaceholder.typicode.com/todos/1")
//            .build()
//
//        try {
//            val response = withContext(Dispatchers.IO) {
//                client.newCall(request).execute()
//            }
//
//            val result = if (response.isSuccessful) {
//                response.body?.string()
//            } else {
//                null
//            }
//
//            Timber.d("result: $result")
//        } catch (e: Exception) {
//            Timber.e(e)
//        }
//    }

    override fun onResume() {
        super.onResume()

        val results = Media.getAll()
        results.forEach { media ->
            Timber.d("Media = $media")
        }

        // showCurrentPage()

        mCurrentItem = mLastItem

        if (!Prefs.didCompleteOnboarding) {
            startActivity(Intent(this, Onboarding23Activity::class.java))
        }

        importSharedMedia(intent)
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
                    .setAnimationIn(R.anim.slide_down, R.anim.slide_up)
                    .setAnimationOut(R.anim.slide_up, R.anim.slide_down)
                    .setDuration(5000)
                    .show()

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun updateAfterDelete(done: Boolean) {
        mMenuDelete?.isVisible = !done
    }

    private fun addFolder() {
        mNewFolderResultLauncher.launch(Intent(this, AddFolderActivity::class.java))
    }

    private fun addBackend() {
        mNewFolderResultLauncher.launch(Intent(this, BackendSetupActivity::class.java))
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
            mBinding.bottomBar.myMediaButton.setIconResource(R.drawable.outline_perm_media_24)
            mBinding.bottomBar.settingsButton.setIconResource(R.drawable.ic_settings_filled)
        } else {
            mBinding.bottomBar.myMediaButton.setIconResource(R.drawable.perm_media_24px)
            mBinding.bottomBar.settingsButton.setIconResource(R.drawable.ic_settings)
        }
    }

//    @SuppressLint("NotifyDataSetChanged")
//    override fun folderClicked(folder: Folder) {
//        mCurrentItem = mPagerAdapter.folders.indexOf(folder)
//
//        mBinding.root.closeDrawer(mBinding.folderBar)
//
//        // make sure that even when navigating to settings and picking a folder there
//        // the dataset will get update correctly
//        mFolderAdapter.notifyDataSetChanged()
//    }

//    override fun getSelectedFolder(): Folder? {
//        return mPagerAdapter.getFolder(mCurrentItem)
//    }
//
//    override fun addSpaceClicked() {
//        // mBinding.root.closeDrawer(mBinding.folderBar)
//
//        startActivity(Intent(this, BackendSetupActivity::class.java))
//    }
//
//    override fun getSelectedSpace(): Backend? {
//        return Backend.current
//    }

//    private fun refreshPages(setFolderId: Long? = null) {
//        val folders = Backend.current?.folders ?: emptyList()
//
//        mPagerAdapter.updateData(folders)
//
//        mBinding.pager.isSaveFromParentEnabled = false
//        mBinding.pager.adapter = mPagerAdapter
//
//        setFolderId?.let {
//            mCurrentItem = mPagerAdapter.getProjectIndexById(it, default = 0)
//        }
//
//        mFolderAdapter.update(folders)
//
//        showCurrentPage()
//    }

//    private fun refreshSpace() {
//        mBinding.spaceName.text = "Servers" // currentSpace.friendlyName
//
//        mSpaceAdapter.update(Backend.getAll().asSequence().toList())
//
//        refreshPages()
//    }
}
