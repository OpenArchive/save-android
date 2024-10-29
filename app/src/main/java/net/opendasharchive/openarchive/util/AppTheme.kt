package net.opendasharchive.openarchive.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import net.opendasharchive.openarchive.R

enum class AppTheme(
    val mode: Int,
    @StringRes val labelResId: Int,
    @StringRes val valueResId: Int
) {
    SYSTEM(
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        R.string.prefs_theme_system,
        R.string.prefs_theme_val_system
    ),
    LIGHT(
        AppCompatDelegate.MODE_NIGHT_NO,
        R.string.prefs_theme_light,
        R.string.prefs_theme_val_light
    ),
    DARK(
        AppCompatDelegate.MODE_NIGHT_YES,
        R.string.prefs_theme_dark,
        R.string.prefs_theme_val_dark
    );

    companion object {
        fun fromMode(mode: Int): AppTheme =
            entries.firstOrNull { it.mode == mode } ?: SYSTEM

        fun fromPreferenceValue(context: Context, value: String?): AppTheme =
            entries.firstOrNull { theme ->
                context.getString(theme.valueResId) == value
            } ?: SYSTEM

        fun getEntryValues(context: Context): Array<String> =
            entries.map { context.getString(it.valueResId) }.toTypedArray()

        fun getEntries(context: Context): Array<String> =
            entries.map { context.getString(it.labelResId) }.toTypedArray()
    }
}