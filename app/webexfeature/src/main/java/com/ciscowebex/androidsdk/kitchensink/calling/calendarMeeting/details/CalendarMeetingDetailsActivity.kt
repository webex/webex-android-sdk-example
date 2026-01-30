package com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.calendarMeeting.CalendarMeeting
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting.CalendarMeetingModel
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

    private lateinit var tvDescription: TextView
    private lateinit var tvName: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvLink: TextView
    private lateinit var tvLocation: TextView
    private lateinit var inviteesRecyclerView: RecyclerView
    private lateinit var tvInviteeCount: TextView
    private val meetingDetailsViewModel : CalendarMeetingDetailsViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_meeting_details)
        
        // Initialize views
        tvDescription = findViewById(R.id.tvDescription)
        tvName = findViewById(R.id.tvName)
        tvDate = findViewById(R.id.tvDate)
        tvLink = findViewById(R.id.tvLink)
        tvLocation = findViewById(R.id.tvLocation)
        inviteesRecyclerView = findViewById(R.id.inviteesRecyclerView)
        tvInviteeCount = findViewById(R.id.tvInviteeCount)
        
        tvDescription.movementMethod = ScrollingMovementMethod()
        setUpObservers()
        
        val calendarMeetingId = intent.getStringExtra(Constants.Intent.CALENDAR_MEETING_ID)
        meetingDetailsViewModel.getCalendarMeetingById(calendarMeetingId.orEmpty())
    }

    private fun setUpObservers() {
        meetingDetailsViewModel.meeting.observe(this@CalendarMeetingDetailsActivity, Observer { calendarMeeting ->
            if (calendarMeeting != null) {
                val meetingModel = CalendarMeetingModel(calendarMeeting)
                tvName.text = calendarMeeting.subject
                tvDate.text = meetingModel.date
                tvLink.text = calendarMeeting.link
                tvLocation.text = calendarMeeting.location
                tvDescription.text = calendarMeeting.description
                
                if (!calendarMeeting.invitees.isNullOrEmpty()) {
                    val rvAdapter = InviteesAdapter()
                    val invitees = calendarMeeting.invitees as MutableList<CalendarMeeting.Invitee>
                    rvAdapter.invitees = invitees
                    inviteesRecyclerView.adapter = rvAdapter
                    tvInviteeCount.text = "(${invitees.size})"

                } else {
                    inviteesRecyclerView.visibility = View.GONE
                }
            }
        })
    }

    class InviteesAdapter : RecyclerView.Adapter<InviteesViewHolder>() {
        var invitees = mutableListOf<CalendarMeeting.Invitee>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteesViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_meeting_invitee, parent, false)
            return InviteesViewHolder(view)
        }

        override fun onBindViewHolder(holder: InviteesViewHolder, position: Int) {
            holder.bind(invitees[position])
        }

        override fun getItemCount(): Int {
            return invitees.size
        }

    }

    class InviteesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvInviteeName: TextView = itemView.findViewById(R.id.tvInviteeName)
        private val tvResponse: TextView = itemView.findViewById(R.id.tvResponse)

        fun bind(invitee: CalendarMeeting.Invitee) {
            tvInviteeName.text = invitee.displayName ?: ""
            tvResponse.text = invitee.response?.name ?: ""
        }

    }
}