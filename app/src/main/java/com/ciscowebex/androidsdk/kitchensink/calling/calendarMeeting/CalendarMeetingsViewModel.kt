package com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.kitchensink.BaseViewModel
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.calendarMeeting.CalendarMeeting
import com.ciscowebex.androidsdk.kitchensink.utils.getEndOfDay
import com.ciscowebex.androidsdk.kitchensink.utils.getStartOfDay
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.Date

class CalendarMeetingsViewModel(private val repo: CalendarMeetingsRepository, private val webexRepository: WebexRepository) : BaseViewModel() {
    private val _meetings = MutableLiveData<List<CalendarMeeting>>()
    val meetings: LiveData<List<CalendarMeeting>> = _meetings

    var _calendarMeetingEventLiveData = MutableLiveData<Pair<WebexRepository.CalendarMeetingEvent, Any>>()

    init {
        webexRepository._calendarMeetingEventLiveData = _calendarMeetingEventLiveData
    }

    override fun onCleared() {
        webexRepository.removeCalendarMeetingObserver()
    }

    fun getCalendarMeetingEvent() = webexRepository._calendarMeetingEventLiveData

    fun getCalendarMeetingsList(fromDate: Date? = null, toDate: Date? = null, isOngoing: Boolean = false) {
        repo.listCalendarMeetings(fromDate, toDate).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ meetingsList ->
                if(isOngoing) {
                    val ongoingMeetings = meetingsList.filter { it.isOngoingMeeting }
                    _meetings.postValue(ongoingMeetings)
                    return@subscribe
                }
                _meetings.postValue(meetingsList)
            }, { _meetings.postValue(emptyList()) }).autoDispose()
    }

    fun isMoveMeetingSupported(meetingId: String): Boolean {
        return repo.isMoveMeetingSupported(meetingId)
    }

    fun onFilterItemClick(filterByOption: FilterMeetingsBy) {
        when (filterByOption) {
            FilterMeetingsBy.Today -> {
                val startTimeOfToday = getStartOfDay(Date())
                val endTimeOfToday = getEndOfDay(Date())
                getCalendarMeetingsList(startTimeOfToday, endTimeOfToday)
            }
            FilterMeetingsBy.Tomorrow -> {
                val tomorrow = Date().time + 86400000 // Till next day example
                val startTimeOfTomorrow = getStartOfDay(Date(tomorrow))
                val endTimeOfTomorrow = getEndOfDay(Date(tomorrow))
                getCalendarMeetingsList(startTimeOfTomorrow, endTimeOfTomorrow)
            }
            FilterMeetingsBy.PastMeetings -> { getCalendarMeetingsList(null, Date()) }
            FilterMeetingsBy.UpcomingMeetings -> {
                getCalendarMeetingsList(Date())
            }
            FilterMeetingsBy.Ongoing -> { getCalendarMeetingsList(null, null, true) }
            FilterMeetingsBy.AllMeetings -> {
                getCalendarMeetingsList()
            }
        }
    }

    enum class FilterMeetingsBy {
        Today,
        Tomorrow,
        PastMeetings,
        UpcomingMeetings,
        AllMeetings,
        Ongoing
    }
}
