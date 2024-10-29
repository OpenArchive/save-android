package net.opendasharchive.openarchive.util

object AppSettingsTags {
    object Hints {
        val ADD_FOLDER_HINT_SHOWN by tag()
        val BATCH_HINT_SHOWN by tag()
        val IA_HINT_SHOWN by tag()
        val FLAG_HINT_SHOWN by tag()
    }

    object ProofMode {
        val ENCRYPTED_PASSPHRASE by tag()
        val USE_KEY_ENCRYPTION by tag()
        val USE_PROOFMODE by tag()
    }

    val CURRENT_FOLDER_ID by tag()
    val DID_COMPLETE_ONBOARDING by tag()
    val LOCK_WITH_PASSCODE by tag()
    val MEDIA_UPLOAD_POLICY by tag()
    val PRIVACY_POLICY by tag()

    val APP_THEME by tag()
    val UPLOAD_WIFI_ONLY by tag()
    val USE_TOR by tag()
}