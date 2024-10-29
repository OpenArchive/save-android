package net.opendasharchive.openarchive.extensions

import android.content.SharedPreferences
import net.opendasharchive.openarchive.util.PreferenceDelegate

fun PreferenceDelegate.Companion.long(
    manager: SharedPreferences?,
    key: String,
    defaultValue: Long,
    onChange: ((Long) -> Unit)? = null
) = PreferenceDelegate(
    manager = manager,
    key = key,
    defaultValue = defaultValue,
    getter = SharedPreferences::getLong,
    setter = { k, v -> putLong(k, v) },
    onChange = onChange  // Pass through the onChange parameter
)