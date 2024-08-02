package net.opendasharchive.openarchive.services.webdav

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.commit
import net.opendasharchive.openarchive.databinding.ActivityWebdavBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.main.MainActivity
import net.opendasharchive.openarchive.services.CommonServiceFragment.Companion.RESP_CREATED
import net.opendasharchive.openarchive.services.CommonServiceFragment.Companion.RESP_DELETED
import kotlin.properties.Delegates

class WebDavActivity : BaseActivity() {

    private lateinit var mBinding: ActivityWebdavBinding
    private var mBackendId by Delegates.notNull<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityWebdavBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mBackendId = intent.getLongExtra(EXTRA_DATA_SPACE, WebDavFragment.ARG_VAL_NEW_SPACE)

        if (mBackendId != WebDavFragment.ARG_VAL_NEW_SPACE) {
            supportFragmentManager.commit {
                replace(mBinding.webDavFragment.id, WebDavFragment.newInstance(mBackendId))
            }
        }

        supportFragmentManager.setFragmentResultListener(RESP_CREATED, this) { _, _ ->
            finishAffinity()
            startActivity(Intent(this, MainActivity::class.java))
        }
        supportFragmentManager.setFragmentResultListener(RESP_DELETED, this) { _, _ ->
            Backend.navigate(this)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle appbar back button tap
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}