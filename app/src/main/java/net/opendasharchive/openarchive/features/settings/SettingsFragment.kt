package net.opendasharchive.openarchive.features.settings

import android.content.Intent
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.extensions.apply
import net.opendasharchive.openarchive.extensions.getVersionName
import net.opendasharchive.openarchive.features.backends.BackendSetupActivity
import net.opendasharchive.openarchive.features.main.WebViewActivity
import net.opendasharchive.openarchive.services.tor.TorViewModel
import net.opendasharchive.openarchive.util.Analytics
import net.opendasharchive.openarchive.util.AnalyticsTags
import net.opendasharchive.openarchive.util.AppSettings
import net.opendasharchive.openarchive.util.AppSettingsTags
import net.opendasharchive.openarchive.util.AppTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SettingsFragment : PreferenceFragmentCompat() {

    private val torViewModel: TorViewModel by viewModel { parametersOf(requireActivity().application) }
    private val settings: AppSettings by inject()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.prefs_general, rootKey)

        findPreference<Preference>(AppSettingsTags.LOCK_WITH_PASSCODE)?.setOnPreferenceChangeListener { _, newValue ->
            settings.lockWithPasscode = newValue as Boolean
            Analytics.log(
                AnalyticsTags.Settings.USE_PASSCODE,
                value = newValue)
            true
        }

        getPrefByKey<Preference>(R.string.pref_media_servers)?.setOnPreferenceClickListener {
            startActivity(Intent(context, BackendSetupActivity::class.java))
            true
        }

        findPreference<Preference>(AppSettingsTags.USE_TOR)?.setOnPreferenceChangeListener { _, newValue ->
            settings.useTor = (newValue as Boolean)
            torViewModel.updateTorServiceState()
            Analytics.log(
                AnalyticsTags.Settings.USE_TOR,
                value = newValue)
            true
        }

        findPreference<Preference>(AppSettingsTags.ProofMode.USE_PROOFMODE)?.setOnPreferenceClickListener {
            startActivity(Intent(context, ProofModeSettingsActivity::class.java))
            true
        }

        findPreference<Preference>(AppSettingsTags.UPLOAD_WIFI_ONLY)?.setOnPreferenceChangeListener { _, newValue ->
            val intent = Intent(AppSettingsTags.UPLOAD_WIFI_ONLY).apply{putExtra("value", newValue as Boolean)}
            // Replace with LiveData
            // LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            Analytics.log(
                AnalyticsTags.Settings.REQUIRE_WIFI,
                value = newValue)
            true
        }

        findPreference<Preference>(AppSettingsTags.MEDIA_UPLOAD_POLICY)?.setOnPreferenceChangeListener { _, newValue ->
            settings.mediaUploadPolicy = newValue as String
            true
        }

        findPreference<Preference>(AppSettingsTags.PRIVACY_POLICY)?.setOnPreferenceClickListener {
            val intent = WebViewActivity.newIntent(requireContext(), "https://open-archive.org/privacy")
            startActivity(intent)
            true
        }

        findPreference<ListPreference>(AppSettingsTags.APP_THEME)?.apply {
            entries = AppTheme.getEntries(requireContext())
            entryValues = AppTheme.getEntryValues(requireContext())
            value = context.getString(settings.theme.valueResId)
            summaryProvider = ListPreferenceSummaryProvider()

            setOnPreferenceChangeListener { _, newValue ->
                val newTheme = AppTheme.fromPreferenceValue(requireContext(), newValue as String)
                settings.theme = newTheme
                newTheme.apply()
                true
            }
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