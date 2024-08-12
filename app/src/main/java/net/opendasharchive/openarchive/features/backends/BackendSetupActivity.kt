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
        supportActionBar?.title = "All Servers"

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

    private fun showSpaceFragment(fragment: Fragment, title: String) {
        supportActionBar?.title = title
        supportFragmentManager.commit {
            setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            replace(mBinding.spaceSetupFragment.id, fragment)
        }
    }

    private fun initBackendSetupFragmentBindings() {
        supportFragmentManager.setFragmentResultListener(BackendSetupFragment.RESULT_REQUEST_KEY, this) { _, bundle ->
            when (bundle.getString(BackendSetupFragment.RESULT_BUNDLE_KEY)) {
                Backend.Type.INTERNET_ARCHIVE.friendlyName -> {
                    showSpaceFragment(InternetArchiveFragment.newInstance(), getString(R.string.internet_archive))
                }

                Backend.Type.GDRIVE.friendlyName -> {
                    showSpaceFragment(GDriveFragment(), getString(R.string.gdrive))
                }

                Backend.Type.VEILID.friendlyName -> {
                    showSpaceFragment(VeilidFragment(), getString(R.string.veilid))
                }

                Backend.Type.WEBDAV.friendlyName -> {
                    showSpaceFragment(WebDavFragment.newInstance(), getString(R.string.private_server))
                }
            }
        }

        supportFragmentManager.setFragmentResultListener("created", this) { _, bundle ->
            supportFragmentManager.commit {
                val message = when (Backend.current?.friendlyName) {
                    Backend.Type.INTERNET_ARCHIVE.friendlyName -> {
                        getString(R.string.you_have_successfully_connected_to_the_internet_archive)
                    }

                    Backend.Type.GDRIVE.friendlyName -> {
                        getString(R.string.you_have_successfully_connected_to_gdrive)
                    }

                    Backend.Type.VEILID.friendlyName -> {
                        getString(R.string.you_have_successfully_connected_to_veilid)
                    }

                    Backend.Type.WEBDAV.friendlyName -> {
                        getString(R.string.you_have_successfully_connected_to_a_private_server)
                    }

                    Backend.Type.FILECOIN.friendlyName -> {
                        getString(R.string.you_have_successfully_connected_to_filecoin)
                    }

                    else -> { "Unknown Backend" }
                }
                setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                replace(
                    mBinding.spaceSetupFragment.id,
                    BackendSetupSuccessFragment.newInstance(message)
                )
            }
        }

        supportFragmentManager.setFragmentResultListener("cancel", this) { _, _ ->
            supportActionBar?.title = "All servers"
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
}
