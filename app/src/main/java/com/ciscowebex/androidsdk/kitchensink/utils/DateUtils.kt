package com.ciscowebex.androidsdk.kitchensink.utils

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