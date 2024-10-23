package net.opendasharchive.openarchive.extensions

import kotlin.reflect.full.memberProperties

fun Any?.extractFirstList(): List<*>? {
    if (this == null) return null

    return when (this) {
        is List<*> -> this
        is Array<*> -> this.toList()
        is Collection<*> -> this.toList()
        else -> {
            val properties = this::class.memberProperties
            if (properties.size == 1) {
                val singleProperty = properties.first()
                val value = singleProperty.getter.call(this)
                value.extractFirstList() // Recursive call for nested structures
            } else {
                null
            }
        }
    }
}

fun Any?.isListLike(): Boolean {
    if (this == null) return false

    return when (this) {
        is Collection<*>, is Array<*> -> true
        is Map<*, *> -> false
        else -> {
            val properties = this::class.memberProperties

            if (properties.size == 1) {
                val singleProperty = properties.first()
                val value = singleProperty.getter.call(this)

                when (value) {
                    is Collection<*>, is Array<*> -> true
                    else -> value.isListLike() // Recursive check for nested structures
                }
            } else {
                false
            }
        }
    }
}