package com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ciscowebex.androidsdk.calendarMeeting.CalendarMeeting
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetCalendarMeetingJoinOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CalendarMeetingJoinActionBottomSheet(
    val joinByMeetingIdClickListener: (String) -> Unit,
    val joinByMeetingLinkClickListener: (String) -> Unit,
    val joinByMeetingNumberClickListener: (String) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetCalendarMeetingJoinOptionsBinding
    var meeting : CalendarMeeting? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return BottomSheetCalendarMeetingJoinOptionsBinding.inflate(inflater, container, false)
            .also { binding = it }.apply {
                // Control joining options visibility
                if (meeting?.sipUrl.isNullOrEmpty()) {
                    tvJoinByMeetingNumber.visibility = View.GONE
                }

                if (meeting?.link.isNullOrEmpty()) {
                    tvJoinByMeetingLink.visibility = View.GONE
                }

                tvJoinByMeetingId.setOnClickListener {
                    dismiss()
                    joinByMeetingIdClickListener(meeting?.id ?: "")
                }

                tvJoinByMeetingLink.setOnClickListener {
                    dismiss()
                    joinByMeetingLinkClickListener(meeting?.link ?: "")
                }

                tvJoinByMeetingNumber.setOnClickListener {
                    dismiss()
                    joinByMeetingNumberClickListener(meeting?.sipUrl ?: "")
                }

            tvCancel.setOnClickListener { dismiss() }
        }.root
    }

}