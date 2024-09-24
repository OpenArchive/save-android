package net.opendasharchive.openarchive.extensions

import java.net.URI

fun String.uriToPath(): String {
    val uri = URI(this)
    return uri.path
}

