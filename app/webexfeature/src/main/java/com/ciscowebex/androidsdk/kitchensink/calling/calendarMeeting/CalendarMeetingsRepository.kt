package com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting

import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.calendarMeeting.CalendarMeeting
import io.reactivex.Observable
import io.reactivex.Single
import java.util.Date

class CalendarMeetingsRepository(private val webex: Webex) {
    fun listCalendarMeetings(fromDate: Date?, toDate: Date?): Observable<List<CalendarMeeting>> {
        return Single.create<List<CalendarMeeting>> { emitter ->
            webex.calendarMeetings.list(fromDate, toDate, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data ?: emptyList())
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }

    fun getCalendarMeetingById(meetingId : String): Observable<CalendarMeeting> {
        return Single.create<CalendarMeeting> { emitter ->
            webex.calendarMeetings.getById(meetingId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    result.data?.let { emitter.onSuccess(it) } ?: emitter.onError(Throwable("No calendar meeting found!"))
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }

    fun isMoveMeetingSupported(meetingId: String): Boolean {
        return webex.calendarMeetings.isMoveMeetingSupported(meetingId)
    }
}