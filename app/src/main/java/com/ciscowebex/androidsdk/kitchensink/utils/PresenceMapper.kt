package com.ciscowebex.androidsdk.kitchensink.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.people.PresenceStatus

fun stateToDrawable(context: Context, status: PresenceStatus): Drawable? {
    return when(status) {
        PresenceStatus.Active -> AppCompatResources.getDrawable(context, R.drawable.ic_presence_active)
        PresenceStatus.Inactive -> AppCompatResources.getDrawable(context, R.drawable.ic_presence_inactive)
        PresenceStatus.Dnd -> AppCompatResources.getDrawable(context, R.drawable.ic_presence_dnd)
        PresenceStatus.Quiet -> AppCompatResources.getDrawable(context, R.drawable.ic_presence_quiet)
        PresenceStatus.Busy -> AppCompatResources.getDrawable(context, R.drawable.ic_presence_busy)
        PresenceStatus.OutOfOffice -> AppCompatResources.getDrawable(context, R.drawable.ic_presence_ooo)
        PresenceStatus.Call -> AppCompatResources.getDrawable(context, R.drawable.ic_presence_call)
        PresenceStatus.Meeting -> AppCompatResources.getDrawable(context, R.drawable.ic_presence_meeting)
        PresenceStatus.Presenting -> AppCompatResources.getDrawable(context, R.drawable.ic_presence_sharing_screen)
        PresenceStatus.CalendarItem -> AppCompatResources.getDrawable(context, R.drawable.ic_presence_calendar)
        else -> null
    }
}

fun stateToString(context: Context, status: PresenceStatus): String {
    return when(status) {
        PresenceStatus.Active -> context.getString(R.string.presence_active)
        PresenceStatus.Pending -> context.getString(R.string.presence_pending)
        PresenceStatus.Inactive -> context.getString(R.string.presence_inactive)
        PresenceStatus.Dnd -> context.getString(R.string.presence_dnd)
        PresenceStatus.Quiet -> context.getString(R.string.presence_quiet)
        PresenceStatus.Busy -> context.getString(R.string.presence_busy)
        PresenceStatus.OutOfOffice -> context.getString(R.string.presence_ooo)
        PresenceStatus.Call -> context.getString(R.string.presence_call)
        PresenceStatus.Meeting -> context.getString(R.string.presence_meeting)
        PresenceStatus.Presenting -> context.getString(R.string.presence_presenting)
        PresenceStatus.CalendarItem -> context.getString(R.string.presence_calendar)
        else -> context.getString(R.string.presence_unknown)
    }
}