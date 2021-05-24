package com.ciscowebex.androidsdk.kitchensink.calling

import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceModel
import com.ciscowebex.androidsdk.phone.Call
import com.ciscowebex.androidsdk.phone.CallSchedule
import java.text.SimpleDateFormat
import java.util.Date

data class MeetingInfoModel(val _call: Call, val meetingId: String, val startTime: Date, val endTime: Date, val link: String, val subject: String): IncomingCallInfoModel(_call) {
    val startTimeString: String =  SimpleDateFormat("hh:mm a").format(startTime)
    val endTimeString: String =  SimpleDateFormat("hh:mm a").format(endTime)
    val timeString: String = "$startTimeString - $endTimeString"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MeetingInfoModel

        return meetingId == other.meetingId
    }

    override fun hashCode(): Int {
        var result = meetingId.hashCode()
        result = 31 * result + startTime.hashCode()
        result = 31 * result + endTime.hashCode()
        result = 31 * result + link.hashCode()
        result = 31 * result + subject.hashCode()
        return result
    }

    companion object {
        fun convertToMeetingInfoModel(call: Call, schedule: CallSchedule): MeetingInfoModel {
            return MeetingInfoModel(call, schedule.getId().orEmpty(), schedule.getStart() ?: Date(), schedule.getEnd() ?: Date(),
                    schedule.getMeetingLink().orEmpty(), schedule.getSubject().orEmpty())
        }
    }
}