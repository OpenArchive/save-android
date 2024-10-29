package net.opendasharchive.openarchive.util

import kotlin.reflect.KProperty

class PropertyTagDelegate {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String =
        property.name.replace(Regex("([A-Z])"), "_$1").lowercase().removePrefix("_")

    companion object {
        fun tag() = PropertyTagDelegate()
    }
}