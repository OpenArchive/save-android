package net.opendasharchive.openarchive.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.preference.PreferenceManager
import org.witness.proofmode.ProofMode
import org.witness.proofmode.ProofModeConstants

object Prefs {
    const val LOCK_WITH_PASSCODE = "lock_with_passcode"
    const val DID_COMPLETE_ONBOARDING = "did_complete_onboarding"
    const val UPLOAD_WIFI_ONLY = "upload_wifi_only"
    const val NEARBY_USE_BLUETOOTH = "nearby_use_bluetooth"
    const val NEARBY_USE_WIFI = "nearby_use_wifi"
    const val USE_TOR = "use_tor"
    const val PROHIBIT_SCREENSHOTS = "prohibit_screenshots"
    const val USE_PROOFMODE = "use_proofmode"
    const val USE_PROOFMODE_KEY_ENCRYPTION = "proofmode_key_encryption"
    // private const val USE_NEXTCLOUD_CHUNKING = "upload_nextcloud_chunks"
    const val THEME = "theme"
    const val CURRENT_FOLDER_ID = "current_folder"
    const val CURRENT_SPACE_ID = "current_space"
    const val FLAG_HINT_SHOWN = "ft.flag"
    const val BATCH_HINT_SHOWN = "ft.batch"
    const val DONT_SHOW_UPLOAD_HINT = "ft.upload"
    const val IA_HINT_SHOWN = "ft.ia"
    const val ADD_FOLDER_HINT_SHOWN = "ft.add_folder"
    const val LICENSE_URL = "archive_pref_share_license_url"
    const val PROOFMODE_ENCRYPTED_PASSPHRASE = "proof_mode_encrypted_passphrase"

    var manager: SharedPreferences? = null

    fun load(context: Context) {
        if (manager == null) manager = PreferenceManager.getDefaultSharedPreferences(context)
    }

    @SuppressLint("ApplySharedPref")
    fun store() {
        manager?.edit()?.commit()
    }

    var didCompleteOnboarding: Boolean
        get() = manager?.getBoolean(DID_COMPLETE_ONBOARDING, false) ?: false
        set(value) {
            manager?.edit()?.putBoolean(DID_COMPLETE_ONBOARDING, value)?.apply()
        }

    var uploadWifiOnly: Boolean
        get() = manager?.getBoolean(UPLOAD_WIFI_ONLY, false) ?: false
        set(value) {
            manager?.edit()?.putBoolean(UPLOAD_WIFI_ONLY, value)?.apply()
        }

    var nearbyUseBluetooth: Boolean
        get() = manager?.getBoolean(NEARBY_USE_BLUETOOTH, false) ?: false
        set(value) {
            manager?.edit()?.putBoolean(NEARBY_USE_BLUETOOTH, value)?.apply()
        }

    var nearbyUseWifi: Boolean
        get() = manager?.getBoolean(NEARBY_USE_WIFI, false) ?: false
        set(value) {
            manager?.edit()?.putBoolean(NEARBY_USE_WIFI, value)?.apply()
        }

    val useProofMode: Boolean
        get() = manager?.getBoolean(USE_PROOFMODE, false) ?: false

    var useTor: Boolean
        get() = manager?.getBoolean(USE_TOR, false) ?: false
        set(value) {
            manager?.edit()?.putBoolean(USE_TOR, value)?.apply()
        }

    var currentBackendId: Long
        get() = manager?.getLong(CURRENT_SPACE_ID, -1) ?: -1
        set(value) {
            manager?.edit()?.putLong(CURRENT_SPACE_ID, value)?.apply()
        }

    var currentFolderId: Long
        get() = manager?.getLong(CURRENT_FOLDER_ID, -1) ?: -1
        set(value) {
            manager?.edit()?.putLong(CURRENT_FOLDER_ID, value)?.apply()
        }

    var flagHintShown: Boolean
        get() = manager?.getBoolean(FLAG_HINT_SHOWN, false) ?: false
        set(value) {
            manager?.edit()?.putBoolean(FLAG_HINT_SHOWN, value)?.apply()
        }

    var batchHintShown: Boolean
        get() = manager?.getBoolean(BATCH_HINT_SHOWN, false) ?: false
        set(value) {
            manager?.edit()?.putBoolean(BATCH_HINT_SHOWN, value)?.apply()
        }

    var dontShowUploadHint: Boolean
        get() = manager?.getBoolean(DONT_SHOW_UPLOAD_HINT, false) ?: false
        set(value) {
            manager?.edit()?.putBoolean(DONT_SHOW_UPLOAD_HINT, value)?.apply()
        }

    var iaHintShown: Boolean
        get() = manager?.getBoolean(IA_HINT_SHOWN, false) ?: false
        set(value) {
            manager?.edit()?.putBoolean(IA_HINT_SHOWN, value)?.apply()
        }

    var addFolderHintShown: Boolean
        get() = manager?.getBoolean(ADD_FOLDER_HINT_SHOWN, false) ?: false
        set(value) {
            manager?.edit()?.putBoolean(ADD_FOLDER_HINT_SHOWN, value)?.apply()
        }

    var licenseUrl: String?
        get() = manager?.getString(LICENSE_URL, null)
        set(value) {
            manager?.edit()?.putString(LICENSE_URL, value)?.apply()
        }

    var lockWithPasscode: Boolean
        get() = manager?.getBoolean(LOCK_WITH_PASSCODE, false) ?: false
        set(value) {
            manager?.edit()?.putBoolean(LOCK_WITH_PASSCODE, value)?.apply()
        }

    var proofModeLocation: Boolean
        get() = manager?.getBoolean(ProofMode.PREF_OPTION_LOCATION, false) ?: false
        set(value) {
            manager?.edit()?.putBoolean(ProofMode.PREF_OPTION_LOCATION, value)?.apply()
        }

    var proofModeNetwork: Boolean
        get() = manager?.getBoolean(ProofMode.PREF_OPTION_NETWORK, false) ?: false
        set(value) {
            manager?.edit()?.putBoolean(ProofMode.PREF_OPTION_NETWORK, value)?.apply()
        }

    var useProofModeKeyEncryption: Boolean
        get() = manager?.getBoolean(USE_PROOFMODE_KEY_ENCRYPTION, false) ?: false
        set(value) {
            manager?.edit()?.putBoolean(USE_PROOFMODE_KEY_ENCRYPTION, value)?.apply()
        }

    var proofModeEncryptedPassphrase: ByteArray?
        get() {
            val passphrase = manager?.getString(PROOFMODE_ENCRYPTED_PASSPHRASE, null) ?: return null

            return Base64.decode(passphrase, Base64.DEFAULT)
        }
        set(value) {
            val passphrase =
                if (value == null) null else Base64.encodeToString(value, Base64.DEFAULT)

            manager?.edit()?.putString(PROOFMODE_ENCRYPTED_PASSPHRASE, passphrase)?.apply()
        }

    /**
     * Only set this right before initializing `MediaWatcher`!
     * This needs to be the unencrypted passphrase for `MediaWatcher` to read.
     * But we don't want to store this, so overwrite right after!
     */
    var temporaryUnencryptedProofModePassphrase: String?
        get() = manager?.getString(ProofModeConstants.PREFS_KEY_PASSPHRASE, null) ?: ProofModeConstants.PREFS_KEY_PASSPHRASE_DEFAULT
        set(value) {
            manager?.edit()?.putString(ProofModeConstants.PREFS_KEY_PASSPHRASE, value)?.apply()
        }

    val theme: Theme
        get() = Theme.get(manager?.getString(THEME, null))

    var prohibitScreenshots: Boolean
        get() = manager?.getBoolean(PROHIBIT_SCREENSHOTS, false) ?: false
        set(value) {
            manager?.edit()?.putBoolean(PROHIBIT_SCREENSHOTS, value)?.apply()
        }
}
