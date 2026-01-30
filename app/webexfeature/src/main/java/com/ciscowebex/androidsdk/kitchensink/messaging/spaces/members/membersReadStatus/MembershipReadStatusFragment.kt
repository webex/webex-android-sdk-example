package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.membersReadStatus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import android.widget.TextView
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogWithMessage
import org.koin.android.ext.android.inject

class MembershipReadStatusFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: View

    private val membershipReadStatusViewModel: MembershipReadStatusViewModel by inject()
    private var spaceId: String? = null

    companion object {
        fun newInstance(spaceId: String): MembershipReadStatusFragment {
            val args = Bundle()
            args.putString(Constants.Bundle.SPACE_ID, spaceId)
            val fragment = MembershipReadStatusFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        spaceId = arguments?.getString(Constants.Bundle.SPACE_ID)

        val view = inflater.inflate(R.layout.fragment_membership_read_status, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        progressBar = view.findViewById(R.id.progress_bar)

        val membershipsReadStatusAdapter = MembershipReadStatusAdapter()
        recyclerView.adapter = membershipsReadStatusAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        membershipReadStatusViewModel.membershipsReadStatus.observe(this@MembershipReadStatusFragment.viewLifecycleOwner, Observer {
            membershipsReadStatusAdapter.membershipsReadStatus.clear()
            membershipsReadStatusAdapter.membershipsReadStatus.addAll(it)
            membershipsReadStatusAdapter.notifyDataSetChanged()
            progressBar.visibility = View.GONE
        })

        membershipReadStatusViewModel.membershipReadStatusError.observe(this@MembershipReadStatusFragment.viewLifecycleOwner, Observer {
            showDialogWithMessage(requireContext(), R.string.error_occurred, it)
            progressBar.visibility = View.GONE
        })
        membershipReadStatusViewModel.membershipEventLiveData.observe(this@MembershipReadStatusFragment.viewLifecycleOwner, Observer {
            if (it.second?.spaceId == spaceId) {
                when (it.first) {
                    WebexRepository.MembershipEvent.MessageSeen -> {
                        getList()
                    }
                    else -> {}

                }
            }
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar.visibility = View.VISIBLE
        getList()
    }

    fun getList() {
        membershipReadStatusViewModel.getMembershipsWithReadStatus(spaceId)
    }

}

class MembershipReadStatusAdapter : RecyclerView.Adapter<MembershipReadStatusViewHolder>() {
    var membershipsReadStatus: MutableList<MembershipReadStatusModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembershipReadStatusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_membership_read_status, parent, false)
        return MembershipReadStatusViewHolder(view)
    }

    override fun getItemCount(): Int = membershipsReadStatus.size

    override fun onBindViewHolder(holder: MembershipReadStatusViewHolder, position: Int) {
        holder.bind(membershipsReadStatus[position])
    }

}

class MembershipReadStatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val membershipIdTextView: TextView = itemView.findViewById(R.id.membershipIdTextView)
    private val membershipPersonIdTextView: TextView = itemView.findViewById(R.id.membershipPersonIdTextView)
    private val membershipPersonDisplayNameTextView: TextView = itemView.findViewById(R.id.membershipPersonDisplayNameTextView)
    private val membershipPersonEmailTextView: TextView = itemView.findViewById(R.id.membershipPersonEmailTextView)
    private val membershipIsModeratorTextView: TextView = itemView.findViewById(R.id.membershipIsModeratorTextView)
    private val membershipLastSeenDateTextView: TextView = itemView.findViewById(R.id.membershipLastSeenDateTextView)
    
    fun bind(membershipReadStatus: MembershipReadStatusModel) {
        val member = membershipReadStatus.member
        membershipIdTextView.text = member.membershipId ?: ""
        membershipPersonIdTextView.text = member.personId ?: ""
        membershipPersonDisplayNameTextView.text = member.personDisplayName ?: ""
        membershipPersonEmailTextView.text = member.personEmail ?: ""
        membershipIsModeratorTextView.text = member.isModerator.toString()
        membershipLastSeenDateTextView.text = membershipReadStatus.lastSeenDate.toString()
    }
}