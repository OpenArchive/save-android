package net.opendasharchive.openarchive.util

import android.annotation.SuppressLint
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.opendasharchive.openarchive.extensions.long
import net.opendasharchive.openarchive.util.AppSettingsTags.CURRENT_FOLDER_ID

class AppSettings(private val manager: SharedPreferences?) {
    private val _currentFolderIdFlow = MutableStateFlow(-1L)
    val currentFolderIdFlow = _currentFolderIdFlow.asStateFlow()

    var addFolderHintShown by PreferenceDelegate.boolean(
        manager = manager,
        key = AppSettingsTags.Hints.ADD_FOLDER_HINT_SHOWN,
        defaultValue = false
    )

    var flagHintShown by PreferenceDelegate.boolean(
        manager = manager,
        key = AppSettingsTags.Hints.FLAG_HINT_SHOWN,
        defaultValue = false
    )

    var iaHintShown by PreferenceDelegate.boolean(
        manager = manager,
        key = AppSettingsTags.Hints.IA_HINT_SHOWN,
        defaultValue = false
    )

    var batchHintShown by PreferenceDelegate.boolean(
        manager = manager,
        key = AppSettingsTags.Hints.BATCH_HINT_SHOWN,
        defaultValue = false
    )

    var mediaUploadPolicy by PreferenceDelegate.string(
        manager = manager,
        key = AppSettingsTags.MEDIA_UPLOAD_POLICY,
        defaultValue = "upload_media_automatically"
    )

    var currentFolderId by PreferenceDelegate.long(
        manager = manager,
        key = CURRENT_FOLDER_ID,
        defaultValue = -1L,
        onChange = { newValue ->
            _currentFolderIdFlow.value = newValue
        }
    )

    var didCompleteOnboarding by PreferenceDelegate.boolean(
        manager = manager,
        key = AppSettingsTags.DID_COMPLETE_ONBOARDING,
        defaultValue = false
    )

    var lockWithPasscode by PreferenceDelegate.boolean(
        manager = manager,
        key = AppSettingsTags.LOCK_WITH_PASSCODE,
        defaultValue = false
    )

    var theme by PreferenceDelegate.theme(
        manager = manager,
        key = AppSettingsTags.APP_THEME,
        defaultValue = AppTheme.SYSTEM
    )

    var uploadWifiOnly by PreferenceDelegate.boolean(
        manager = manager,
        key = AppSettingsTags.UPLOAD_WIFI_ONLY,
        defaultValue = false
    )

    var useProofMode by PreferenceDelegate.boolean(
        manager = manager,
        key = AppSettingsTags.ProofMode.USE_PROOFMODE,
        defaultValue = false
    )

    var useProofModeEncryptionKey by PreferenceDelegate.boolean(
        manager = manager,
        key = AppSettingsTags.ProofMode.USE_KEY_ENCRYPTION,
        defaultValue = false
    )

    var useTor by PreferenceDelegate.boolean(
        manager = manager,
        key = AppSettingsTags.USE_TOR,
        defaultValue = false
    )

    @SuppressLint("ApplySharedPref")
    fun store() {
        manager?.edit()?.commit()
    }
}