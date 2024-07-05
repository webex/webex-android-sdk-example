package com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.calendarMeeting.CalendarMeeting
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.calling.CallActivity
import com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting.details.CalendarMeetingDetailsActivity
import com.ciscowebex.androidsdk.kitchensink.databinding.FragmentMeetingListBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemCalendarMeetingsBinding
import org.koin.android.ext.android.inject
import java.util.Date

class CalendarMeetingListFragment : Fragment() {
    private lateinit var binding : FragmentMeetingListBinding
    private lateinit var calendarMeetingListAdapter : CalendarMeetingListAdapter

    private val meetingsViewModel: CalendarMeetingsViewModel by inject()

    private var isFABOpen = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentMeetingListBinding.inflate(inflater, container, false).also { binding = it }.apply {
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
            lifecycleOwner = this@CalendarMeetingListFragment
            setUpObservers()
            setFilterMeetingListeners()
        }.root
    }

    override fun onPause() {
        super.onPause()
        closeFABMenu()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        meetingsViewModel.getCalendarMeetingsList()
    }

    private fun setFilterMeetingListeners() {
        binding.filterMeetingsFAB.setOnClickListener {
            if(!isFABOpen) showFABMenu() else closeFABMenu()
        }
        binding.tvToday.setOnClickListener {
            meetingsViewModel.onFilterItemClick(CalendarMeetingsViewModel.FilterMeetingsBy.Today)
            closeFABMenu()
        }
        binding.tvTomorrow.setOnClickListener {
            meetingsViewModel.onFilterItemClick(CalendarMeetingsViewModel.FilterMeetingsBy.Tomorrow)
            closeFABMenu()
        }
        binding.tvUpcomingMeetings.setOnClickListener {
            meetingsViewModel.onFilterItemClick(CalendarMeetingsViewModel.FilterMeetingsBy.UpcomingMeetings)
            closeFABMenu()
        }
        binding.tvPastMeetings.setOnClickListener {
            meetingsViewModel.onFilterItemClick(CalendarMeetingsViewModel.FilterMeetingsBy.PastMeetings)
            closeFABMenu()
        }
        binding.tvAllMeetings.setOnClickListener {
            meetingsViewModel.onFilterItemClick(CalendarMeetingsViewModel.FilterMeetingsBy.AllMeetings)
            closeFABMenu()
        }
        binding.tvOngoing.setOnClickListener {
            meetingsViewModel.onFilterItemClick(CalendarMeetingsViewModel.FilterMeetingsBy.Ongoing)
            closeFABMenu()
        }
    }

    private fun showFABMenu() {
        isFABOpen = true
        binding.tvUpcomingMeetings.animate().alpha(1F).duration = 250
        binding.tvUpcomingMeetings.animate().translationY(-resources.getDimension(R.dimen.filter_meetings_pos1))
        binding.tvPastMeetings.animate().alpha(1F).duration = 250
        binding.tvPastMeetings.animate().translationY(-resources.getDimension(R.dimen.filter_meetings_pos2))
        binding.tvTomorrow.animate().alpha(1F).duration = 250
        binding.tvTomorrow.animate().translationY(-resources.getDimension(R.dimen.filter_meetings_pos3))
        binding.tvToday.animate().alpha(1F).duration = 250
        binding.tvToday.animate().translationY(-resources.getDimension(R.dimen.filter_meetings_pos4))
        binding.tvAllMeetings.animate().alpha(1F).duration = 250
        binding.tvAllMeetings.animate().translationY(-resources.getDimension(R.dimen.filter_meetings_pos5))
        binding.tvOngoing.animate().alpha(1F).duration = 250
        binding.tvOngoing.animate().translationY(-resources.getDimension(R.dimen.filter_meetings_pos6))
    }

    private fun closeFABMenu() {
        isFABOpen = false
        binding.tvUpcomingMeetings.animate().translationY(0F)
        binding.tvUpcomingMeetings.animate().alpha(0F).duration = 300
        binding.tvPastMeetings.animate().translationY(0F)
        binding.tvPastMeetings.animate().alpha(0F).duration = 300
        binding.tvTomorrow.animate().translationY(0F)
        binding.tvTomorrow.animate().alpha(0F).duration = 300
        binding.tvToday.animate().translationY(0F)
        binding.tvToday.animate().alpha(0F).duration = 300
        binding.tvAllMeetings.animate().translationY(0F)
        binding.tvAllMeetings.animate().alpha(0F).duration = 300
        binding.tvOngoing.animate().translationY(0F)
        binding.tvOngoing.animate().alpha(0F).duration = 300
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
        context?.let {
            startActivity(CallActivity.getOutgoingIntent(it, meetingId, false, moveMeeting))
        }
    }

    private fun joinByMeetingLink(meetingLink: String, moveMeeting: Boolean) {
        context?.let {
            startActivity(CallActivity.getOutgoingIntent(it, meetingLink, false, moveMeeting))
        }
    }

    private fun joinBySipUrl(sipUrl: String, moveMeeting: Boolean) {
        context?.let {
            startActivity(CallActivity.getOutgoingIntent(it, sipUrl, false, moveMeeting))
        }
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
            return MeetingListViewHolder(
                meetingJoinOptionsDialogFragment,
                supportFragmentManager,
                ListItemCalendarMeetingsBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                ),
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
        private val binding: ListItemCalendarMeetingsBinding,
        private val onListItemClicked : (listItemPosition: Int) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onListItemClicked(adapterPosition)
            }
        }

        fun bind(meetingModel: CalendarMeetingModel, meetingsViewModel: CalendarMeetingsViewModel) {
            binding.meeting = meetingModel.calendarMeeting
            val currentTime = Date().time
            val showJoinButton = ((meetingModel.calendarMeeting.startTime?.time ?: 0L) <= currentTime && (meetingModel.calendarMeeting.endTime?.time ?: 0L) >= currentTime) || meetingModel.calendarMeeting.canJoin
            val isMoveMeetingPossible = meetingModel.calendarMeeting.isOngoingMeeting && meetingsViewModel.isMoveMeetingSupported(meetingModel.calendarMeeting.id ?: "")
            binding.btnJoinMeeting.visibility = if (showJoinButton) View.VISIBLE else View.GONE
            binding.btnMoveMeeting.visibility = if (isMoveMeetingPossible) View.VISIBLE else View.GONE
            binding.tvTime.text = meetingModel.date
            binding.btnJoinMeeting.setOnClickListener {
                meetingJoinOptionsDialogFragment.meeting = meetingModel.calendarMeeting
                meetingJoinOptionsDialogFragment.show(supportFragmentManager, "Calendar meeting join options")
            }
            binding.btnMoveMeeting.setOnClickListener {
                meetingJoinOptionsDialogFragment.meeting = meetingModel.calendarMeeting
                meetingJoinOptionsDialogFragment.moveMeeting = true
                meetingJoinOptionsDialogFragment.show(supportFragmentManager, "Calendar meeting join options")
            }
            binding.executePendingBindings()
        }
    }
}