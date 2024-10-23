package net.opendasharchive.openarchive.extensions

fun <T> MutableSet<T>.toggle(item: T): Boolean {
    return if (contains(item)) {
        remove(item)
        false
    } else {
        add(item)
        true
    }
}