package net.opendasharchive.openarchive.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import net.opendasharchive.openarchive.features.main.TabBarActivity
import org.witness.proofmode.crypto.pgp.PgpUtils
import org.witness.proofmode.service.MediaWatcher
import timber.log.Timber
import java.io.File

object ProofModeHelper {

    private var initialized = false

    fun init(context: Context, completed: () -> Unit) {
        if (initialized) return completed()

        // Disable ProofMode GPS data tracking by default.
        if (Prefs.proofModeLocation) Prefs.proofModeLocation = false

        finishInit(context, completed)

//        val encryptedPassphrase = Prefs.proofModeEncryptedPassphrase
//
//        if (encryptedPassphrase?.isNotEmpty() == true) {
//            // Sometimes this gets out of sync because of the restarts.
//            Prefs.useProofModeKeyEncryption = true
//
//            val key = Hbks.loadKey()
//
//            if (key != null) {
//                Hbks.decrypt(encryptedPassphrase, Hbks.loadKey(), context) { plaintext, e ->
//                    // User failed or denied authentication. Stop app in that case.
//                    if (e is UserNotAuthenticatedException) {
//                        Runtime.getRuntime().exit(0)
//                    }
//                    else {
//                        finishInit(context, completed, plaintext)
//                    }
//                }
//            }
//            else {
//                // Oh, oh. User removed passphrase lock.
//                Prefs.proofModeEncryptedPassphrase = null
//                Prefs.useProofModeKeyEncryption = false
//
//                removePgpKey(context)
//
//                finishInit(context, completed)
//            }
//        }
//        else {
//            // Sometimes this gets out of sync because of the restarts.
//            Prefs.useProofModeKeyEncryption = false
//
//            finishInit(context, completed)
//        }
    }

    private fun finishInit(context: Context, completed: () -> Unit, passphrase: String? = null) {
        // Store unencrypted passphrase so MediaWatcher can read it.
        Prefs.temporaryUnencryptedProofModePassphrase = passphrase

        // Load or create PGP key using the decrypted passphrase OR the default passphrase.
        PgpUtils.getInstance(context,
            Prefs.temporaryUnencryptedProofModePassphrase)

        // Initialize MediaWatcher with the correct passphrase.
        MediaWatcher.getInstance(context)

        // Remove again to avoid leaking unencrypted passphrase.
        Prefs.temporaryUnencryptedProofModePassphrase = null

        initialized = true

        completed()
    }

    fun removePgpKey(context: Context) {
        for (file in arrayOf(File(context.filesDir, "pkr.asc"), File(context.filesDir, "pub.asc"))) {
            try {
                file.delete()
            }
            catch (e: Exception) {
                Timber.d(e)
            }
        }
    }

    fun restartApp(activity: Activity) {
        val i = Intent(activity, TabBarActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(i)

        activity.finish()

        Prefs.store()

        Runtime.getRuntime().exit(0)
    }
}