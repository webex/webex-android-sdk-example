package com.ciscowebex.androidsdk.kitchensink.utils

import java.text.SimpleDateFormat
import java.util.*

fun getStartOfDay(date: Date): Date {
    val cal = getCalInstance()
    cal.time = date
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    return cal.time
}

fun getEndOfDay(date: Date): Date {
    val cal = getCalInstance()
    cal.time = date
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)

    return cal.time
}

fun getCalInstance(): Calendar {
    return Calendar.getInstance(Locale.getDefault())
}

fun formatCallDurationTime(duration: Long): CharSequence {
    val h = (duration / 3600000).toInt()
    val m = (duration - h * 3600000).toInt() / 60000
    val s = (duration - h * 3600000 - m * 60000).toInt() / 1000
    val hh = if (h > 0) {
        (if (h < 10) "0$h" else "$h") + ":"
    } else {
        ""
    }
    return hh + (if (m < 10) "0$m" else "$m") + ":" + if (s < 10) "0$s" else "$s"
}

fun getCurrentDate(timeMillis: Long): String {
    val simpleDateFormat = SimpleDateFormat("MM-dd, HH:mm a", Locale.getDefault())
    val dateString: String = simpleDateFormat.format(timeMillis)
    return dateString
}