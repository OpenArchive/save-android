package net.opendasharchive.openarchive.services.gdrive

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityGdriveBinding
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.main.ChainCompletionViewModel

class GDriveActivity : BaseActivity() {

    private lateinit var binding: ActivityGdriveBinding
    private val chainViewModel: ChainCompletionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGdriveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.gdrive)

        chainViewModel.isChainCompleted.observe(this) { isCompleted ->
            if (isCompleted) {
                finish()
            }
        }

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

//        supportFragmentManager.commit() {
//            setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
//            replace(binding.gdriveFragment.id, GDriveSignInFragment())
//            supportFragmentManager.setFragmentResultListener(RESP_CREATED, this@GDriveActivity) { _, _ ->
//                Timber.d("Signed in.")
//                finish()
//            }
//        }
    }
}