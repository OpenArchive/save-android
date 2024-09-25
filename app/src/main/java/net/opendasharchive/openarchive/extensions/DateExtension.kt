package net.opendasharchive.openarchive.extensions

import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

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