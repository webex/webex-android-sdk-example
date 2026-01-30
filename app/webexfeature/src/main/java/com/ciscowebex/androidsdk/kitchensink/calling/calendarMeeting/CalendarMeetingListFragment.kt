package com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting

import android.Manifest
import android.os.Bundle
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.calendarMeeting.CalendarMeeting
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.calling.CallActivity
import com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting.details.CalendarMeetingDetailsActivity
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.TextView
import org.koin.android.ext.android.inject
import java.util.Date

class CalendarMeetingListFragment : Fragment() {
    private lateinit var calendarMeetingListAdapter : CalendarMeetingListAdapter

    private val meetingsViewModel: CalendarMeetingsViewModel by inject()

    private var isFABOpen = false

    // View references
    private lateinit var meetingListRecyclerView: RecyclerView
    private lateinit var filterMeetingsFAB: FloatingActionButton
    private lateinit var tvToday: TextView
    private lateinit var tvTomorrow: TextView
    private lateinit var tvUpcomingMeetings: TextView
    private lateinit var tvPastMeetings: TextView
    private lateinit var tvAllMeetings: TextView
    private lateinit var tvOngoing: TextView

    // No local pre-checks; rely on SDK PERMISSION_REQUIRED and KS observers

    private val callingPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val allGranted = grants.values.all { it }
            val webexVM = (activity as? BaseActivity)?.webexViewModel
            if (allGranted) {
                webexVM?.retryPendingDialIfAny()
                webexVM?.retryPendingAnswerIfAny()
            } else {
                Toast.makeText(requireContext(), getString(R.string.permission_error), Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_meeting_list, container, false)
    }

    override fun onPause() {
        super.onPause()
        closeFABMenu()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        meetingListRecyclerView = view.findViewById(R.id.meetingListRecyclerView)
        filterMeetingsFAB = view.findViewById(R.id.filterMeetingsFAB)
        tvToday = view.findViewById(R.id.tv_today)
        tvTomorrow = view.findViewById(R.id.tv_tomorrow)
        tvUpcomingMeetings = view.findViewById(R.id.tv_upcoming_meetings)
        tvPastMeetings = view.findViewById(R.id.tv_past_meetings)
        tvAllMeetings = view.findViewById(R.id.tv_all_meetings)
        tvOngoing = view.findViewById(R.id.tv_ongoing)
        
        // Set up adapter
        val meetingJoinOptionsDialogFragment = CalendarMeetingJoinActionBottomSheet(
            {meetingId, moveMeeting -> joinByMeetingId(meetingId, moveMeeting)},
            {meetingLink, moveMeeting -> joinByMeetingLink(meetingLink, moveMeeting)},
            {sipUrl, moveMeeting -> joinBySipUrl(sipUrl, moveMeeting)}
        )
        calendarMeetingListAdapter = CalendarMeetingListAdapter(meetingJoinOptionsDialogFragment, requireActivity().supportFragmentManager, meetingsViewModel) { listItemPosition ->
            context?.let {
                val meetingItem = calendarMeetingListAdapter.meetings[listItemPosition]
                it.startActivity(CalendarMeetingDetailsActivity.getIntent(it, meetingItem.calendarMeeting.id ?: ""))
            }
        }
        meetingListRecyclerView.adapter = calendarMeetingListAdapter
        meetingListRecyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        
        setUpObservers()
        setFilterMeetingListeners()

        // Fragment-local fallback observer to launch permission prompts
        (activity as? BaseActivity)?.webexViewModel?.callingLiveData?.observe(viewLifecycleOwner) { live ->
            val missing = live?.missingPermissions
            if (!missing.isNullOrEmpty()) {
                val normalized = normalizePermissionsForApi(missing.toSet()).toTypedArray()
                callingPermissionLauncher.launch(normalized)
            }
        }
        
        meetingsViewModel.getCalendarMeetingsList()
    }

    private fun setFilterMeetingListeners() {
        filterMeetingsFAB.setOnClickListener {
            if(!isFABOpen) showFABMenu() else closeFABMenu()
        }
        tvToday.setOnClickListener {
            meetingsViewModel.onFilterItemClick(CalendarMeetingsViewModel.FilterMeetingsBy.Today)
            closeFABMenu()
        }
        tvTomorrow.setOnClickListener {
            meetingsViewModel.onFilterItemClick(CalendarMeetingsViewModel.FilterMeetingsBy.Tomorrow)
            closeFABMenu()
        }
        tvUpcomingMeetings.setOnClickListener {
            meetingsViewModel.onFilterItemClick(CalendarMeetingsViewModel.FilterMeetingsBy.UpcomingMeetings)
            closeFABMenu()
        }
        tvPastMeetings.setOnClickListener {
            meetingsViewModel.onFilterItemClick(CalendarMeetingsViewModel.FilterMeetingsBy.PastMeetings)
            closeFABMenu()
        }
        tvAllMeetings.setOnClickListener {
            meetingsViewModel.onFilterItemClick(CalendarMeetingsViewModel.FilterMeetingsBy.AllMeetings)
            closeFABMenu()
        }
        tvOngoing.setOnClickListener {
            meetingsViewModel.onFilterItemClick(CalendarMeetingsViewModel.FilterMeetingsBy.Ongoing)
            closeFABMenu()
        }
    }

    private fun showFABMenu() {
        isFABOpen = true
        tvUpcomingMeetings.animate().alpha(1F).duration = 250
        tvUpcomingMeetings.animate().translationY(-resources.getDimension(R.dimen.filter_meetings_pos1))
        tvPastMeetings.animate().alpha(1F).duration = 250
        tvPastMeetings.animate().translationY(-resources.getDimension(R.dimen.filter_meetings_pos2))
        tvTomorrow.animate().alpha(1F).duration = 250
        tvTomorrow.animate().translationY(-resources.getDimension(R.dimen.filter_meetings_pos3))
        tvToday.animate().alpha(1F).duration = 250
        tvToday.animate().translationY(-resources.getDimension(R.dimen.filter_meetings_pos4))
        tvAllMeetings.animate().alpha(1F).duration = 250
        tvAllMeetings.animate().translationY(-resources.getDimension(R.dimen.filter_meetings_pos5))
        tvOngoing.animate().alpha(1F).duration = 250
        tvOngoing.animate().translationY(-resources.getDimension(R.dimen.filter_meetings_pos6))
    }

    private fun closeFABMenu() {
        isFABOpen = false
        tvUpcomingMeetings.animate().translationY(0F)
        tvUpcomingMeetings.animate().alpha(0F).duration = 300
        tvPastMeetings.animate().translationY(0F)
        tvPastMeetings.animate().alpha(0F).duration = 300
        tvTomorrow.animate().translationY(0F)
        tvTomorrow.animate().alpha(0F).duration = 300
        tvToday.animate().translationY(0F)
        tvToday.animate().alpha(0F).duration = 300
        tvAllMeetings.animate().translationY(0F)
        tvAllMeetings.animate().alpha(0F).duration = 300
        tvOngoing.animate().translationY(0F)
        tvOngoing.animate().alpha(0F).duration = 300
    }

    private fun setUpObservers() {
        meetingsViewModel.meetings.observe(this@CalendarMeetingListFragment.viewLifecycleOwner, Observer { meetings ->
            calendarMeetingListAdapter.meetings.clear()
            calendarMeetingListAdapter.meetings.addAll(meetings.map { CalendarMeetingModel(it) })
            calendarMeetingListAdapter.notifyDataSetChanged()
        })

        meetingsViewModel.getCalendarMeetingEvent()?.observe(this@CalendarMeetingListFragment.viewLifecycleOwner, Observer { pair ->
            when(pair.first) {
                WebexRepository.CalendarMeetingEvent.Created -> {
                    val newMeeting = pair.second as CalendarMeeting
                    val meetingModels = calendarMeetingListAdapter.meetings
                    val index = meetingModels.indexOfFirst { it.calendarMeeting.startTime?.time?: 0 > newMeeting.startTime?.time?: 0 }

                    if (index == -1) {
                        calendarMeetingListAdapter.meetings.add(CalendarMeetingModel(newMeeting))
                        calendarMeetingListAdapter.notifyItemInserted(calendarMeetingListAdapter.meetings.size - 1)
                    } else {
                        calendarMeetingListAdapter.meetings.add(index, CalendarMeetingModel(newMeeting))
                        calendarMeetingListAdapter.notifyItemInserted(index)
                    }
                }
                WebexRepository.CalendarMeetingEvent.Updated -> {
                    val meeting = pair.second as CalendarMeeting
                    val index = calendarMeetingListAdapter.getPositionById(meeting.id?: "")
                    if (!calendarMeetingListAdapter.meetings.isNullOrEmpty() && index != -1) {
                        calendarMeetingListAdapter.meetings[index] = CalendarMeetingModel(meeting)
                        calendarMeetingListAdapter.notifyItemChanged(index)
                    }
                }
                WebexRepository.CalendarMeetingEvent.Deleted -> {
                    val meetingId = pair.second as String
                    val index = calendarMeetingListAdapter.getPositionById(meetingId)
                    if (!calendarMeetingListAdapter.meetings.isNullOrEmpty() && index != -1) {
                        val meeting = calendarMeetingListAdapter.meetings[index]
                        calendarMeetingListAdapter.meetings.remove(meeting)
                        calendarMeetingListAdapter.notifyItemRemoved(index)
                    }
                }
            }
        })
    }

    private fun joinByMeetingId(meetingId: String, moveMeeting: Boolean) {
        context?.let { startActivity(CallActivity.getOutgoingIntent(it, meetingId, false, moveMeeting)) }
    }

    private fun joinByMeetingLink(meetingLink: String, moveMeeting: Boolean) {
        context?.let { startActivity(CallActivity.getOutgoingIntent(it, meetingLink, false, moveMeeting)) }
    }

    private fun joinBySipUrl(sipUrl: String, moveMeeting: Boolean) {
        context?.let { startActivity(CallActivity.getOutgoingIntent(it, sipUrl, false, moveMeeting)) }
    }

    private fun normalizePermissionsForApi(perms: Set<String>): Set<String> {
        if (Build.VERSION.SDK_INT >= 31) {
            val mapped = perms.map {
                if (it == Manifest.permission.BLUETOOTH) Manifest.permission.BLUETOOTH_CONNECT else it
            }
            return mapped.toSet()
        }
        return perms
    }

    class CalendarMeetingListAdapter(
        private val meetingJoinOptionsDialogFragment: CalendarMeetingJoinActionBottomSheet,
        private val supportFragmentManager: FragmentManager,
        private val meetingsViewModel: CalendarMeetingsViewModel,
        private val onListItemClicked : (listItemPosition: Int) -> Unit,
    ) : RecyclerView.Adapter<MeetingListViewHolder>() {
        var meetings: MutableList<CalendarMeetingModel> = mutableListOf()

        fun getPositionById(meetingId: String): Int {
            return meetings.indexOfFirst { it.calendarMeeting.id == meetingId }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingListViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_calendar_meetings, parent, false)
            return MeetingListViewHolder(
                meetingJoinOptionsDialogFragment,
                supportFragmentManager,
                view,
                onListItemClicked
            )
        }

        override fun getItemCount(): Int = meetings.size

        override fun onBindViewHolder(holder: MeetingListViewHolder, position: Int) {
            holder.bind(meetings[position], meetingsViewModel)
        }
    }

    class MeetingListViewHolder(
        private val meetingJoinOptionsDialogFragment: CalendarMeetingJoinActionBottomSheet,
        private val supportFragmentManager: FragmentManager,
        itemView: View,
        private val onListItemClicked : (listItemPosition: Int) -> Unit,
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val tvMeetingTitle: TextView = itemView.findViewById(R.id.tv_meeting_title)
        private val btnJoinMeeting: Button = itemView.findViewById(R.id.btn_join_meeting)
        private val btnMoveMeeting: Button = itemView.findViewById(R.id.btn_move_meeting)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)

        init {
            itemView.setOnClickListener {
                onListItemClicked(adapterPosition)
            }
        }

        fun bind(meetingModel: CalendarMeetingModel, meetingsViewModel: CalendarMeetingsViewModel) {
            // Set the meeting title
            tvMeetingTitle.text = meetingModel.calendarMeeting.subject ?: ""
            
            val currentTime = Date().time
            val showJoinButton = ((meetingModel.calendarMeeting.startTime?.time ?: 0L) <= currentTime && (meetingModel.calendarMeeting.endTime?.time ?: 0L) >= currentTime) || meetingModel.calendarMeeting.canJoin
            val isMoveMeetingPossible = meetingModel.calendarMeeting.isOngoingMeeting && meetingsViewModel.isMoveMeetingSupported(meetingModel.calendarMeeting.id ?: "")
            btnJoinMeeting.visibility = if (showJoinButton) View.VISIBLE else View.GONE
            btnMoveMeeting.visibility = if (isMoveMeetingPossible) View.VISIBLE else View.GONE
            tvTime.text = meetingModel.date
            btnJoinMeeting.setOnClickListener {
                meetingJoinOptionsDialogFragment.meeting = meetingModel.calendarMeeting
                meetingJoinOptionsDialogFragment.show(supportFragmentManager, "Calendar meeting join options")
            }
            btnMoveMeeting.setOnClickListener {
                meetingJoinOptionsDialogFragment.meeting = meetingModel.calendarMeeting
                meetingJoinOptionsDialogFragment.moveMeeting = true
                meetingJoinOptionsDialogFragment.show(supportFragmentManager, "Calendar meeting join options")
            }
        }
    }
}