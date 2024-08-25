package net.opendasharchive.openarchive.features.internetarchive.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.db.BackendResult
import net.opendasharchive.openarchive.extensions.getBackend
import net.opendasharchive.openarchive.features.main.TabBarActivity

class InternetArchiveActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val (space, isNewSpace) = intent.extras.getBackend(Backend.Type.INTERNET_ARCHIVE)

        setContent {
            InternetArchiveScreen(space, isNewSpace) { result ->
                finish(result)
            }
        }
    }

    private fun finish(result: BackendResult) {
        when (result) {
            BackendResult.Created -> {
                startActivity(Intent(this, TabBarActivity::class.java))
            }

            BackendResult.Cancelled -> finish()

            BackendResult.Deleted -> Backend.navigate(this)

            else -> Unit
        }
    }
}