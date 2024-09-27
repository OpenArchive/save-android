package net.opendasharchive.openarchive.extensions

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

fun Date.isToday(): Boolean {
    val today = LocalDate.now()
    val dateToCheck = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    return dateToCheck == today
}

fun Date.isYesterday(): Boolean {
    val yesterday = LocalDate.now().minusDays(1)
    val dateToCheck = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    return dateToCheck == yesterday
}

fun Date?.friendlyString(): String {
    val dateTimeFormatter = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

    if (this == null) {
        return "Uploading..."
    }

    if (this.isYesterday()) {
        return "Today at " + timeFormatter.format(this)
    }

    if (this.isYesterday()) {
        return "Yesterday at " + timeFormatter.format(this)
    }

    return dateTimeFormatter.format(this)
}