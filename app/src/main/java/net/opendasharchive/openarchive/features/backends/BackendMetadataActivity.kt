package net.opendasharchive.openarchive.features.backends

import android.content.Intent
import android.os.Bundle
import net.opendasharchive.openarchive.databinding.ActivityBackendMetaBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.settings.CcSelector

class BackendMetadataActivity : BaseActivity() {
    private lateinit var binding: ActivityBackendMetaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBackendMetaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Server Details"

        setup()
    }

    fun setup() {
        val backend = Backend()

        CcSelector.init(binding.cc, license = "https://creativecommons.org/licenses/by-sa/4.0")

        binding.authenticationButton.setOnClickListener {
            binding.nickname.text?.let { nickname ->
                if (nickname.isNotEmpty()) {
                    backend.name = nickname.toString()
                }
            }

            backend.license = CcSelector.get(binding.cc)

            backend.save()

            signalSuccess()
        }
    }

    private fun signalSuccess() {
        setResult(RESULT_OK, Intent())
        finish()
    }
}