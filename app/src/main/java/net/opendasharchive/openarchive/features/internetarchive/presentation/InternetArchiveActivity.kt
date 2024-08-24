package net.opendasharchive.openarchive.features.internetarchive.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.internetarchive.presentation.components.getBackend

class InternetArchiveActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val (space, isNewSpace) = intent.extras.getBackend(Backend.Type.INTERNET_ARCHIVE)

        setContent {
            InternetArchiveScreen(space, isNewSpace) {
                finish()
            }
        }
    }

//    private fun finish(result: IAResult) {
//        when (result) {
//            IAResult.Saved -> {
//                startActivity(Intent(this, MainActivity::class.java))
//            }
//
//            IAResult.Deleted -> Backend.navigate(this)
//            else -> Unit
//        }
//    }
}