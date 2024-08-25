package net.opendasharchive.openarchive.services.gdrive

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.commit
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityGdriveBinding
import net.opendasharchive.openarchive.features.core.BaseActivity
import timber.log.Timber

class GDriveActivity : BaseActivity() {

    companion object {
        const val REQUEST_CODE_GOOGLE_AUTH = 21701
    }

    private lateinit var binding: ActivityGdriveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGdriveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.gdrive)

        val hasPerms = GDriveConduit.permissionsGranted(this)
        Timber.d("Permissions granted already? $hasPerms")

        if (hasPerms) {
            Timber.d("has perms")
            supportFragmentManager.commit() {
                setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                replace(binding.gdriveFragment.id, GDriveSignOutFragment())
            }
        } else {
            Timber.d("no perms")
            supportFragmentManager.commit() {
                setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                replace(binding.gdriveFragment.id, GDriveSignInFragment())
            }
        }
    }

    // boilerplate to make back button in app bar work
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}