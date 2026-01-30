package com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting

import com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting.details.CalendarMeetingDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val calendarMeetingsModule = module {
    viewModel { CalendarMeetingsViewModel(get(), get()) }
    viewModel { CalendarMeetingDetailsViewModel(get()) }

    single { CalendarMeetingsRepository(get()) }
}