package net.opendasharchive.openarchive.features.onboarding

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
import net.opendasharchive.openarchive.databinding.ActivitySpaceSetupBinding
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.internetarchive.presentation.InternetArchiveFragment
import net.opendasharchive.openarchive.features.main.MainActivity
import net.opendasharchive.openarchive.features.settings.SpaceSetupFragment
import net.opendasharchive.openarchive.features.settings.SpaceSetupSuccessFragment
import net.opendasharchive.openarchive.services.gdrive.GDriveFragment
import net.opendasharchive.openarchive.services.internetarchive.Util
import net.opendasharchive.openarchive.services.webdav.WebDavFragment
import timber.log.Timber

class ServerSetupActivity : BaseActivity() {

    companion object {
        const val FRAGMENT_TAG = "ssa_fragment"
    }

    private lateinit var mBinding: ActivitySpaceSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivitySpaceSetupBinding.inflate(layoutInflater)
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

        initSpaceSetupFragmentBindings()
        initWebDavFragmentBindings()
        initSpaceSetupSuccessFragmentBindings()
        initInternetArchiveFragmentBindings()
        initGDriveFragmentBindings()
    }

    override fun onSupportNavigateUp(): Boolean {
        Timber.d("onSupportNavigateUp")
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun initSpaceSetupSuccessFragmentBindings() {
        supportFragmentManager.setFragmentResultListener(SpaceSetupSuccessFragment.RESP_DONE, this) { _, _ ->
            finishAffinity()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun initWebDavFragmentBindings() {
        supportFragmentManager.setFragmentResultListener(WebDavFragment.RESP_SAVED, this) { _, _ ->
            progress3()
            supportFragmentManager.commit {
                setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                replace(
                    mBinding.spaceSetupFragment.id,
                    SpaceSetupSuccessFragment.newInstance(getString(R.string.you_have_successfully_connected_to_a_private_server))
                )
            }
        }

        supportFragmentManager.setFragmentResultListener(WebDavFragment.RESP_CANCEL, this) { _, _ ->
            progress1()
            supportFragmentManager.commit {
                setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                replace(mBinding.spaceSetupFragment.id, SpaceSetupFragment())
            }
        }
    }

    private fun showSpaceFragment(fragment: Fragment) {
        progress2()
        supportFragmentManager.commit {
            setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            replace(mBinding.spaceSetupFragment.id, fragment)
        }
    }

    private fun initSpaceSetupFragmentBindings() {
        supportFragmentManager.setFragmentResultListener(SpaceSetupFragment.RESULT_REQUEST_KEY, this) { _, bundle ->
            when (bundle.getString(SpaceSetupFragment.RESULT_BUNDLE_KEY)) {
                SpaceSetupFragment.RESULT_VAL_INTERNET_ARCHIVE -> {
                    showSpaceFragment(InternetArchiveFragment.newInstance())
                }

                SpaceSetupFragment.RESULT_VAL_WEBDAV -> {
                    showSpaceFragment(WebDavFragment.newInstance())
                }

                SpaceSetupFragment.RESULT_VAL_GDRIVE -> {
                    showSpaceFragment(GDriveFragment())
                }
            }
        }
    }

    private fun initInternetArchiveFragmentBindings() {
        supportFragmentManager.setFragmentResultListener(InternetArchiveFragment.RESP_SAVED, this) { _, _ ->
            progress3()
            supportFragmentManager.commit {
                setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                replace(
                    mBinding.spaceSetupFragment.id,
                    SpaceSetupSuccessFragment.newInstance(getString(R.string.you_have_successfully_connected_to_the_internet_archive)))
            }
        }

        supportFragmentManager.setFragmentResultListener(InternetArchiveFragment.RESP_CANCEL,this) { _, _ ->
            progress1()
            supportFragmentManager.commit {
                setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                replace(mBinding.spaceSetupFragment.id, SpaceSetupFragment())
            }
        }
    }

    private fun initGDriveFragmentBindings() {
        supportFragmentManager.setFragmentResultListener(GDriveFragment.RESP_CANCEL, this) { _, _ ->
            progress1()
            supportFragmentManager.commit {
                setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                replace(mBinding.spaceSetupFragment.id, SpaceSetupFragment())
            }
        }

        supportFragmentManager.setFragmentResultListener(GDriveFragment.RESP_AUTHENTICATED, this) { _, _ ->
            progress3()
            supportFragmentManager.commit {
                setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                replace(
                    mBinding.spaceSetupFragment.id,
                    SpaceSetupSuccessFragment.newInstance(getString(R.string.you_have_successfully_connected_to_gdrive)))
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        supportFragmentManager.fragments.firstOrNull()?.onActivityResult(requestCode, resultCode, data)
//        supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)?.onActivityResult(requestCode, resultCode, data)
    }

    private fun progress1() {
        Util.setBackgroundTint(mBinding.progressBlock.dot1, R.color.colorSpaceSetupProgressOn)
        Util.setBackgroundTint(mBinding.progressBlock.bar1, R.color.colorSpaceSetupProgressOff)
        Util.setBackgroundTint(mBinding.progressBlock.dot2, R.color.colorSpaceSetupProgressOff)
        Util.setBackgroundTint(mBinding.progressBlock.bar2, R.color.colorSpaceSetupProgressOff)
        Util.setBackgroundTint(mBinding.progressBlock.dot3, R.color.colorSpaceSetupProgressOff)
    }

    private fun progress2() {
        Util.setBackgroundTint(mBinding.progressBlock.dot1, R.color.colorSpaceSetupProgressOn)
        Util.setBackgroundTint(mBinding.progressBlock.bar1, R.color.colorSpaceSetupProgressOn)
        Util.setBackgroundTint(mBinding.progressBlock.dot2, R.color.colorSpaceSetupProgressOn)
        Util.setBackgroundTint(mBinding.progressBlock.bar2, R.color.colorSpaceSetupProgressOff)
        Util.setBackgroundTint(mBinding.progressBlock.dot3, R.color.colorSpaceSetupProgressOff)
    }

    private fun progress3() {
        Util.setBackgroundTint(mBinding.progressBlock.dot1, R.color.colorSpaceSetupProgressOn)
        Util.setBackgroundTint(mBinding.progressBlock.bar1, R.color.colorSpaceSetupProgressOn)
        Util.setBackgroundTint(mBinding.progressBlock.dot2, R.color.colorSpaceSetupProgressOn)
        Util.setBackgroundTint(mBinding.progressBlock.bar2, R.color.colorSpaceSetupProgressOn)
        Util.setBackgroundTint(mBinding.progressBlock.dot3, R.color.colorSpaceSetupProgressOn)
    }
}
