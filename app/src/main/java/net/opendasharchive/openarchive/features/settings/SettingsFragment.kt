package net.opendasharchive.openarchive.features.settings

import android.content.Intent
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.SaveApp
import net.opendasharchive.openarchive.features.backends.BackendSetupActivity
import net.opendasharchive.openarchive.features.folders.CreateNewFolderActivity
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

        findPreference<Preference>("servers")?.setOnPreferenceClickListener {
            startActivity(Intent(context, BackendSetupActivity::class.java))
            true
        }

        findPreference<Preference>("folders")?.setOnPreferenceClickListener {
            startActivity(Intent(context, CreateNewFolderActivity::class.java))
            true
        }

        findPreference<Preference>(Prefs.USE_TOR)?.setOnPreferenceChangeListener { _, newValue ->
            val activity = activity ?: return@setOnPreferenceChangeListener true

            if (newValue as Boolean) {
            }

            true
        }

        findPreference<Preference>(Prefs.USE_PROOFMODE)?.setOnPreferenceClickListener {
            startActivity(Intent(context, ProofModeSettingsActivity::class.java))
            true
        }

        findPreference<Preference>(Prefs.UPLOAD_WIFI_ONLY)?.setOnPreferenceChangeListener { _, newValue ->
            val intent = Intent(Prefs.UPLOAD_WIFI_ONLY).apply{putExtra("value", newValue as Boolean)}
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            true
        }

        val themePreference = findPreference<ListPreference>(Prefs.THEME)
        themePreference?.summaryProvider = ListPreferenceSummaryProvider()
        themePreference?.setOnPreferenceChangeListener { _, newValue ->
            Theme.set(Theme.get(newValue as? String))
            SaveApp.hasThemeChanged = true
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
}

private class ListPreferenceSummaryProvider : Preference.SummaryProvider<ListPreference> {
    override fun provideSummary(preference: ListPreference): CharSequence {
        return when {
            preference.entry == null -> "Not set"
            else -> "${preference.entry}"
        }
    }
}

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//
//        mBinding = FragmentSettingsBinding.inflate(inflater, container, false)
//
//        mBinding.btGeneral.setDrawable(R.drawable.ic_account_circle, Position.Start, 0.6)
//        mBinding.btGeneral.compoundDrawablePadding =
//            resources.getDimension(R.dimen.padding_small).roundToInt()
//        mBinding.btGeneral.setOnClickListener {
//            val context = context ?: return@setOnClickListener
//
//            startActivity(Intent(context, GeneralSettingsActivity::class.java))
//        }
//
//        mBinding.btSpace.compoundDrawablePadding =
//            resources.getDimension(R.dimen.padding_small).roundToInt()
//        mBinding.btSpace.setOnClickListener {
//            startSpaceAuthActivity()
//        }
//
//        mBinding.btFolders.setDrawable(R.drawable.ic_folder, Position.Start, 0.6)
//        mBinding.btFolders.compoundDrawablePadding =
//            resources.getDimension(R.dimen.padding_small).roundToInt()
//        mBinding.btFolders.setOnClickListener {
//            val context = context ?: return@setOnClickListener
//            startActivity(Intent(context, FoldersActivity::class.java))
//        }
//
//        mBinding.btAbout.text = getString(R.string.action_about, getString(R.string.app_name))
//        mBinding.btAbout.styleAsLink()
//        mBinding.btAbout.setOnClickListener {
//            context?.openBrowser("https://open-archive.org/save")
//        }
//
//        mBinding.btPrivacy.styleAsLink()
//        mBinding.btPrivacy.setOnClickListener {
//            context?.openBrowser("https://open-archive.org/privacy")
//        }
//
//        val activity = activity
//
//        if (activity != null) {
//            mBinding.version.text = getString(
//                R.string.version__,
//                activity.packageManager.getVersionName(activity.packageName)
//            )
//        }
//
//        return mBinding.root
//    }

//    override fun onResume() {
//        super.onResume()
//
//        updateSpace()
//    }

//    private fun updateSpace() {
//        val context = context ?: return
//        val space = Space.current
//
//        if (space != null) {
//            mBinding.btSpace.text = space.friendlyName
//
//            mBinding.btSpace.setDrawable(
//                space.getAvatar(context)?.scaled(24, context),
//                Position.Start, tint = true
//            )
//        } else {
//            mBinding.btSpace.visibility = View.GONE
//        }
//    }

//    private fun startSpaceAuthActivity() {
//        val space = Space.current ?: return
//
//        val clazz = when (space.tType) {
//            Space.Type.INTERNET_ARCHIVE -> InternetArchiveActivity::class.java
//            Space.Type.GDRIVE -> GDriveActivity::class.java
//            else -> WebDavActivity::class.java
//        }
//
//        val intent = Intent(context, clazz)
//        intent.putExtra(BaseActivity.EXTRA_DATA_SPACE, space.id)
//
//        startActivity(intent)
//    }
