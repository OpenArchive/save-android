package net.opendasharchive.openarchive.util

import android.content.SharedPreferences
import kotlin.reflect.KProperty

// PreferenceDelegate.kt
class PreferenceDelegate<T>(
    private val manager: SharedPreferences?,
    private val key: String,
    private val defaultValue: T,
    private val getter: SharedPreferences.(String, T) -> T,
    private val setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor,
    private val onChange: ((T) -> Unit)? = null
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
        manager?.getter(key, defaultValue) ?: defaultValue

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        manager?.edit()?.setter(key, value)?.apply()
    }

    companion object {
        fun theme(
            manager: SharedPreferences?,
            key: String,
            defaultValue: AppTheme = AppTheme.SYSTEM
        ) = PreferenceDelegate(
            manager = manager,
            key = key,
            defaultValue = defaultValue,
            getter = { k, d -> AppTheme.fromMode(getInt(k, d.mode)) },
            setter = { k, v -> putInt(k, v.mode) }
        )

        fun string(
            manager: SharedPreferences?,
            key: String,
            defaultValue: String = ""
        ) = PreferenceDelegate(
            manager = manager,
            key = key,
            defaultValue = defaultValue,
            getter = SharedPreferences::getString,
            setter = SharedPreferences.Editor::putString
        )

        fun boolean(
            manager: SharedPreferences?,
            key: String,
            defaultValue: Boolean = false
        ) = PreferenceDelegate(
            manager = manager,
            key = key,
            defaultValue = defaultValue,
            getter = SharedPreferences::getBoolean,
            setter = SharedPreferences.Editor::putBoolean
        )

        fun int(
            manager: SharedPreferences?,
            key: String,
            defaultValue: Int = 0
        ) = PreferenceDelegate(
            manager = manager,
            key = key,
            defaultValue = defaultValue,
            getter = SharedPreferences::getInt,
            setter = SharedPreferences.Editor::putInt
        )

        fun long(
            manager: SharedPreferences?,
            key: String,
            defaultValue: Long = 0L
        ) = PreferenceDelegate(
            manager = manager,
            key = key,
            defaultValue = defaultValue,
            getter = SharedPreferences::getLong,
            setter = SharedPreferences.Editor::putLong
        )

        fun float(
            manager: SharedPreferences?,
            key: String,
            defaultValue: Float = 0f
        ) = PreferenceDelegate(
            manager = manager,
            key = key,
            defaultValue = defaultValue,
            getter = SharedPreferences::getFloat,
            setter = SharedPreferences.Editor::putFloat
        )

        fun stringSet(
            manager: SharedPreferences?,
            key: String,
            defaultValue: Set<String> = emptySet()
        ) = PreferenceDelegate(
            manager = manager,
            key = key,
            defaultValue = defaultValue,
            getter = SharedPreferences::getStringSet,
            setter = SharedPreferences.Editor::putStringSet
        )
    }
}
