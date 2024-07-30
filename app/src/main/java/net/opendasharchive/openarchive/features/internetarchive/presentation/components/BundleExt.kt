package net.opendasharchive.openarchive.features.internetarchive.presentation.components

import android.os.Bundle
import androidx.core.os.bundleOf
import net.opendasharchive.openarchive.db.Backend

@Deprecated("only for use with fragments and activities")
private const val ARG_VAL_NEW_SPACE = -1L

@Deprecated("only for use with fragments and activities")
private const val ARG_SPACE = "space"

enum class IAResult(
    val value: String
) {
    Saved("ia_fragment_resp_saved"), Deleted("ia_fragment_resp_deleted"), Cancelled("ia_fragment_resp_cancel"),
}

@Deprecated("only for use with fragments and activities")
fun bundleWithBackendId(backendId: Long) = bundleOf(ARG_SPACE to backendId)

@Deprecated("only for use with fragments and activities")
fun bundleWithNewSpace() = bundleOf(ARG_SPACE to ARG_VAL_NEW_SPACE)

@Deprecated("only for use with fragments and activities")
fun Bundle?.getSpace(type: Backend.Type): Pair<Backend, Boolean> {
    val mBackendId = this?.getLong(ARG_SPACE, ARG_VAL_NEW_SPACE) ?: ARG_VAL_NEW_SPACE

    val isNewSpace = ARG_VAL_NEW_SPACE == mBackendId

    return if (isNewSpace) {
        Pair(Backend(type), true)
    } else {
        Backend.get(mBackendId)?.let { Pair(it, false) } ?: Pair(Backend(type), true)
    }
}
