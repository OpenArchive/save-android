package net.opendasharchive.openarchive.services.veilid

import android.os.Bundle
import android.view.MenuItem
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityGdriveBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.core.BaseActivity

class VeilidActivity : BaseActivity() {
    private lateinit var viewBinding: ActivityGdriveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var backend: Backend? = null

        if (intent.hasExtra(EXTRA_DATA_SPACE)) {
            backend = Backend.get(intent.getLongExtra(EXTRA_DATA_SPACE, -1L))
        }

        viewBinding = ActivityGdriveBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.btRemove.setOnClickListener {
            // if (backend != null) removeSpace(backend)
        }

        setSupportActionBar(viewBinding.toolbar)
        supportActionBar?.title = getString(R.string.gdrive)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewBinding.gdriveId.setText(backend?.displayname ?: "")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}