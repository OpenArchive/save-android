package net.opendasharchive.openarchive.extensions

import android.view.View
import androidx.appcompat.app.AlertDialog

fun View.getMeasurments(): Pair<Int, Int> {
    measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    val width = measuredWidth
    val height = measuredHeight
    return width to height
}

fun View.alertUserOfError(e: Error) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(context)

    builder
        .setTitle("Oops")
        .setMessage(e.localizedMessage)
        .setPositiveButton("OK") { dialog, which ->
        }

    val dialog: AlertDialog = builder.create()

    dialog.show()
}