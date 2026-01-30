package com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ciscowebex.androidsdk.calendarMeeting.CalendarMeeting
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.calling.CallActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CalendarMeetingJoinActionBottomSheet(
    val joinByMeetingIdClickListener: (String, Boolean) -> Unit,
    val joinByMeetingLinkClickListener: (String, Boolean) -> Unit,
    val joinByMeetingNumberClickListener: (String, Boolean) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var tvJoinByMeetingId: TextView
    private lateinit var tvJoinByMeetingLink: TextView
    private lateinit var tvJoinByMeetingNumber: TextView
    private lateinit var tvCancel: TextView
    var meeting : CalendarMeeting? = null
    var moveMeeting: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_calendar_meeting_join_options, container, false)
        
        tvJoinByMeetingId = view.findViewById(R.id.tv_join_by_meetingId)
        tvJoinByMeetingLink = view.findViewById(R.id.tv_join_by_meetingLink)
        tvJoinByMeetingNumber = view.findViewById(R.id.tv_join_by_meetingNumber)
        tvCancel = view.findViewById(R.id.tv_cancel)
        
        // Control joining options visibility
        if (meeting?.sipUrl.isNullOrEmpty()) {
            tvJoinByMeetingNumber.visibility = View.GONE
        }

        if (meeting?.link.isNullOrEmpty()) {
            tvJoinByMeetingLink.visibility = View.GONE
        }

        tvJoinByMeetingId.setOnClickListener {
            dismiss()
            joinByMeetingIdClickListener(meeting?.id ?: "", moveMeeting)
        }

        tvJoinByMeetingLink.setOnClickListener {
            dismiss()
            joinByMeetingLinkClickListener(meeting?.link ?: "", moveMeeting)
        }

        tvJoinByMeetingNumber.setOnClickListener {
            dismiss()
            joinByMeetingNumberClickListener(meeting?.sipUrl ?: "", moveMeeting)
        }

        tvCancel.setOnClickListener { dismiss() }
        
        return view
    }

}