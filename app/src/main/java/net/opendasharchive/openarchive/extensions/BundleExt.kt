package net.opendasharchive.openarchive.extensions

import android.os.Bundle
import androidx.core.os.bundleOf
import net.opendasharchive.openarchive.db.Backend

private const val ARG_VAL_NEW_BACKEND = -1L

private const val ARG_BACKEND = "backend"

fun bundleWithBackendId(backendId: Long) = bundleOf(ARG_BACKEND to backendId)

fun bundleWithNewSpace() = bundleOf(ARG_BACKEND to ARG_VAL_NEW_BACKEND)

fun Bundle?.getBackend(type: Backend.Type): Pair<Backend, Boolean> {
    val mBackendId = this?.getLong(ARG_BACKEND, ARG_VAL_NEW_BACKEND) ?: ARG_VAL_NEW_BACKEND

    val isNewBackend = ARG_VAL_NEW_BACKEND == mBackendId

    return if (isNewBackend) {
        Pair(Backend(type), true)
    } else {
        Backend.getById(mBackendId)?.let { Pair(it, false) } ?: Pair(Backend(type), true)
    }
}
