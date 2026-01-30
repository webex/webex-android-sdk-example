package com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting

import com.ciscowebex.androidsdk.calendarMeeting.CalendarMeeting
import java.text.SimpleDateFormat
import java.util.Date

class CalendarMeetingModel constructor(val calendarMeeting: CalendarMeeting) {
    var date = "${getFormattedTime(calendarMeeting.startTime)}  -  ${getFormattedTime(calendarMeeting.endTime)}"

    private fun getFormattedTime(date: Date?): String {
        return if (date != null) {
            val timeStampFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a")
            timeStampFormat.format(date)
        } else ""
    }
}