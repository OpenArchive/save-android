package net.opendasharchive.openarchive.features.settings

import android.content.Intent
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.features.backends.BackendSetupActivity
import net.opendasharchive.openarchive.features.main.WebViewActivity
import net.opendasharchive.openarchive.util.Prefs
import net.opendasharchive.openarchive.util.Theme
import net.opendasharchive.openarchive.util.extensions.getVersionName

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs_general, rootKey)

        findPreference<Preference>(Prefs.LOCK_WITH_PASSCODE)?.setOnPreferenceChangeListener { _, newValue ->
            Prefs.lockWithPasscode = newValue as Boolean
            true
        }

        getPrefByKey<Preference>(R.string.pref_media_servers)?.setOnPreferenceClickListener {
            startActivity(Intent(context, BackendSetupActivity::class.java))
            true
        }

        findPreference<Preference>(Prefs.USE_TOR)?.setOnPreferenceChangeListener { _, newValue ->
            val activity = activity ?: return@setOnPreferenceChangeListener true
            true
        }

        findPreference<Preference>(Prefs.USE_PROOFMODE)?.setOnPreferenceClickListener {
            startActivity(Intent(context, ProofModeSettingsActivity::class.java))
            true
        }

        findPreference<Preference>(Prefs.UPLOAD_WIFI_ONLY)?.setOnPreferenceChangeListener { _, newValue ->
            val intent = Intent(Prefs.UPLOAD_WIFI_ONLY).apply{putExtra("value", newValue as Boolean)}
            // Replace with shared ViewModel + LiveData
            // LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            true
        }

        findPreference<Preference>(Prefs.MEDIA_UPLOAD_POLICY)?.setOnPreferenceChangeListener { _, newValue ->
            Prefs.mediaUploadPolicy = newValue as String
            true
        }

        findPreference<Preference>(Prefs.PRIVACY_POLICY)?.setOnPreferenceClickListener {
            val intent = WebViewActivity.newIntent(requireContext(), "https://open-archive.org/privacy")
            startActivity(intent)
            true
        }

        val themePreference = findPreference<ListPreference>(Prefs.THEME)
        themePreference?.summaryProvider = ListPreferenceSummaryProvider()
        themePreference?.setOnPreferenceChangeListener { _, newValue ->
            Theme.set(Theme.get(newValue as? String))
            true
        }

        activity?.let { activity ->
            findPreference<Preference>("app_version")?.setSummary(
                activity.packageManager.getVersionName(
                    activity.packageName
                )
            )
        }
    }

    private fun <T: Preference> getPrefByKey(key: Int): T? {
        return findPreference(getString(key))
    }
}

private class ListPreferenceSummaryProvider : Preference.SummaryProvider<ListPreference> {
    override fun provideSummary(preference: ListPreference): CharSequence {
        return when {
            preference.entry == null -> "Not set"
            else -> "${preference.entry}"
        }
    }
}