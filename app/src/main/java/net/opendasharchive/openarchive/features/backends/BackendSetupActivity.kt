package net.opendasharchive.openarchive.features.backends

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityBackendSetupBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.internetarchive.presentation.InternetArchiveFragment
import net.opendasharchive.openarchive.features.main.MainActivity
import net.opendasharchive.openarchive.services.gdrive.GDriveFragment
import net.opendasharchive.openarchive.services.veilid.VeilidFragment
import net.opendasharchive.openarchive.services.webdav.WebDavFragment
import timber.log.Timber

class BackendSetupActivity : BaseActivity() {

    private lateinit var mBinding: ActivityBackendSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityBackendSetupBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Servers"

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        initBackendSetupFragmentBindings()
        initBackendSetupSuccessFragmentBindings()
    }

    override fun onSupportNavigateUp(): Boolean {
        Timber.d("onSupportNavigateUp")
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun initBackendSetupSuccessFragmentBindings() {
        supportFragmentManager.setFragmentResultListener(BackendSetupSuccessFragment.RESP_DONE, this) { _, _ ->
            finishAffinity()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun showSpaceFragment(fragment: Fragment) {
//        progress2()
        supportFragmentManager.commit {
            setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            replace(mBinding.spaceSetupFragment.id, fragment)
        }
    }

    private fun initBackendSetupFragmentBindings() {
        supportFragmentManager.setFragmentResultListener(BackendSetupFragment.RESULT_REQUEST_KEY, this) { _, bundle ->
            when (bundle.getString(BackendSetupFragment.RESULT_BUNDLE_KEY)) {
                Backend.Type.INTERNET_ARCHIVE.friendlyName -> {
                    showSpaceFragment(InternetArchiveFragment.newInstance())
                }

                Backend.Type.GDRIVE.friendlyName -> {
                    showSpaceFragment(GDriveFragment())
                }

                Backend.Type.VEILID.friendlyName -> {
                    showSpaceFragment(VeilidFragment())
                }

                Backend.Type.WEBDAV.friendlyName -> {
                    showSpaceFragment(WebDavFragment.newInstance())
                }
            }
        }

        supportFragmentManager.setFragmentResultListener("created", this) { _, _ ->
//            progress3()
            supportFragmentManager.commit {
                setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                replace(
                    mBinding.spaceSetupFragment.id,
                    BackendSetupSuccessFragment.newInstance("Created!")
                )
            }
        }

        supportFragmentManager.setFragmentResultListener("cancel", this) { _, _ ->
//            progress1()
            supportFragmentManager.commit {
                setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                replace(mBinding.spaceSetupFragment.id, BackendSetupFragment())
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        supportFragmentManager.fragments.firstOrNull()?.onActivityResult(requestCode, resultCode, data)
    }

//    private fun progress1() {
//        Util.setBackgroundTint(mBinding.progressBlock.dot1, R.color.colorSpaceSetupProgressOn)
//        Util.setBackgroundTint(mBinding.progressBlock.bar1, R.color.colorSpaceSetupProgressOff)
//        Util.setBackgroundTint(mBinding.progressBlock.dot2, R.color.colorSpaceSetupProgressOff)
//        Util.setBackgroundTint(mBinding.progressBlock.bar2, R.color.colorSpaceSetupProgressOff)
//        Util.setBackgroundTint(mBinding.progressBlock.dot3, R.color.colorSpaceSetupProgressOff)
//    }
//
//    private fun progress2() {
//        Util.setBackgroundTint(mBinding.progressBlock.dot1, R.color.colorSpaceSetupProgressOn)
//        Util.setBackgroundTint(mBinding.progressBlock.bar1, R.color.colorSpaceSetupProgressOn)
//        Util.setBackgroundTint(mBinding.progressBlock.dot2, R.color.colorSpaceSetupProgressOn)
//        Util.setBackgroundTint(mBinding.progressBlock.bar2, R.color.colorSpaceSetupProgressOff)
//        Util.setBackgroundTint(mBinding.progressBlock.dot3, R.color.colorSpaceSetupProgressOff)
//    }
//
//    private fun progress3() {
//        Util.setBackgroundTint(mBinding.progressBlock.dot1, R.color.colorSpaceSetupProgressOn)
//        Util.setBackgroundTint(mBinding.progressBlock.bar1, R.color.colorSpaceSetupProgressOn)
//        Util.setBackgroundTint(mBinding.progressBlock.dot2, R.color.colorSpaceSetupProgressOn)
//        Util.setBackgroundTint(mBinding.progressBlock.bar2, R.color.colorSpaceSetupProgressOn)
//        Util.setBackgroundTint(mBinding.progressBlock.dot3, R.color.colorSpaceSetupProgressOn)
//    }
}
