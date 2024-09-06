package net.opendasharchive.openarchive.features.backends

import android.content.Intent
import android.os.Bundle
import net.opendasharchive.openarchive.databinding.ActivityBackendMetaBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.settings.CcSelector
import net.opendasharchive.openarchive.util.Analytics

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

            Analytics.log(Analytics.NEW_BACKEND_ADDED, mutableMapOf("type" to backend.name))

            backend.save()

            // Mixing "name" and "type" here, but it should make more sense
            // for the humans reading the logs.
            //
            Analytics.log(Analytics.NEW_BACKEND_ADDED, mutableMapOf("type" to backend.name))

            signalSuccess()
        }
    }

    private fun signalSuccess() {
        setResult(RESULT_OK, Intent())
        finish()
    }
}