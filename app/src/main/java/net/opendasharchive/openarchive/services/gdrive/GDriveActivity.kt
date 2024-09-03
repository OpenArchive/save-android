package net.opendasharchive.openarchive.services.gdrive

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.commit
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityGdriveBinding
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.folders.CreateNewFolderActivity
import net.opendasharchive.openarchive.services.CommonServiceFragment.Companion.RESP_CANCEL
import net.opendasharchive.openarchive.services.CommonServiceFragment.Companion.RESP_CREATED
import timber.log.Timber

class GDriveActivity : BaseActivity() {

    private lateinit var binding: ActivityGdriveBinding

    private val newFolderCreator = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Timber.d("Received result: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            finishAffinity()
            startActivity(Intent(this@GDriveActivity, CreateNewFolderActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGdriveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.gdrive)

        showSignInScreen()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSignInScreen() {
        supportFragmentManager.commit {
            setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            replace(binding.gdriveFragment.id, GDriveSignInFragment())
            supportFragmentManager.setFragmentResultListener(RESP_CREATED, this@GDriveActivity) { _, _ ->
                Timber.d("Signed in.")
                finishAffinity()
                newFolderCreator.launch(Intent(this@GDriveActivity, CreateNewFolderActivity::class.java))
            }

            supportFragmentManager.setFragmentResultListener(RESP_CANCEL, this@GDriveActivity) { _, _ ->
                Timber.d("Cancelled.")
                finish()
            }
        }
    }
}