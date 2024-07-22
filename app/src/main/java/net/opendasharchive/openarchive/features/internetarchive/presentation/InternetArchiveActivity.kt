package net.opendasharchive.openarchive.features.internetarchive.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import net.opendasharchive.openarchive.db.Space
import net.opendasharchive.openarchive.features.internetarchive.presentation.components.IAResult
import net.opendasharchive.openarchive.features.internetarchive.presentation.components.getSpace
import net.opendasharchive.openarchive.features.main.MainActivity

class InternetArchiveActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val (space, isNewSpace) = intent.extras.getSpace(Space.Type.INTERNET_ARCHIVE)

        setContent {
            InternetArchiveScreen(space, isNewSpace) {
                finish(it)
            }
        }
    }

    private fun finish(result: IAResult) {
        when (result) {
            IAResult.Saved -> {
                startActivity(Intent(this, MainActivity::class.java))
            }

            IAResult.Deleted -> Space.navigate(this)
            else -> Unit
        }
    }
}