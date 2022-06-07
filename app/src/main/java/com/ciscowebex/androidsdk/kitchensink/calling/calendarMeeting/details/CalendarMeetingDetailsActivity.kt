package com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.calendarMeeting.CalendarMeeting
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting.CalendarMeetingModel
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityCalendarMeetingDetailsBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemMeetingInviteeBinding
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import org.koin.android.ext.android.inject

class CalendarMeetingDetailsActivity : BaseActivity() {
    companion object {
        fun getIntent(context: Context, meetingId: String): Intent {
            val intent = Intent(context, CalendarMeetingDetailsActivity::class.java)
            intent.putExtra(Constants.Intent.CALENDAR_MEETING_ID, meetingId)
            return intent
        }
    }

    private lateinit var binding: ActivityCalendarMeetingDetailsBinding
    private val meetingDetailsViewModel : CalendarMeetingDetailsViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val calendarMeetingId = intent.getStringExtra(Constants.Intent.CALENDAR_MEETING_ID)
        DataBindingUtil.setContentView<ActivityCalendarMeetingDetailsBinding>(
            this,
            R.layout.activity_calendar_meeting_details
        )
            .apply {
                binding = this
                tvDescription.movementMethod = ScrollingMovementMethod()
                setUpObservers()
            }
        meetingDetailsViewModel.getCalendarMeetingById(calendarMeetingId.orEmpty())
    }

    private fun setUpObservers() {
        meetingDetailsViewModel.meeting.observe(this@CalendarMeetingDetailsActivity, Observer { calendarMeeting ->
            if (calendarMeeting != null) {
                binding.meetingModel = CalendarMeetingModel((calendarMeeting))
                if (!calendarMeeting.invitees.isNullOrEmpty()) {
                    val rvAdapter = InviteesAdapter()
                    val invitees = calendarMeeting.invitees as MutableList<CalendarMeeting.Invitee>
                    rvAdapter.invitees = invitees
                    binding.inviteesRecyclerView.adapter = rvAdapter
                    binding.tvInviteeCount.text = "(${invitees.size})"

                } else {
                    binding.inviteesRecyclerView.visibility = View.GONE
                }
            }
        })
    }

    class InviteesAdapter : RecyclerView.Adapter<InviteesViewHolder>() {
        var invitees = mutableListOf<CalendarMeeting.Invitee>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteesViewHolder {
            return InviteesViewHolder(
                ListItemMeetingInviteeBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: InviteesViewHolder, position: Int) {
            holder.bind(invitees[position])
        }

        override fun getItemCount(): Int {
            return invitees.size
        }

    }

    class InviteesViewHolder(val binding: ListItemMeetingInviteeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(invitee: CalendarMeeting.Invitee) {
            binding.invitee = invitee
            binding.executePendingBindings()
        }

    }
}