package net.opendasharchive.openarchive.features.main.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import net.opendasharchive.openarchive.R

class ProgressDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            builder.setView(inflater.inflate(R.layout.layout_progress_dialog, null))
                .setCancelable(false) // Prevent dismissing by tapping outside

            builder.create().apply {
                // Make the dialog background transparent
                window?.setBackgroundDrawableResource(android.R.color.transparent)
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val TAG = "ProgressDialog"
    }
}