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
import com.ciscowebex.androidsdk.kitchensink.databinding.FragmentMembershipReadStatusBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemMembershipReadStatusBinding
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogWithMessage
import org.koin.android.ext.android.inject

class MembershipReadStatusFragment : Fragment() {

    lateinit var binding: FragmentMembershipReadStatusBinding

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

        return FragmentMembershipReadStatusBinding.inflate(inflater, container, false)
                .also { binding = it }
                .apply {


                    val membershipsReadStatusAdapter = MembershipReadStatusAdapter()
                    recyclerView.adapter = membershipsReadStatusAdapter
                    recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

                    membershipReadStatusViewModel.membershipsReadStatus.observe(this@MembershipReadStatusFragment.viewLifecycleOwner, Observer {
                        membershipsReadStatusAdapter.membershipsReadStatus.clear()
                        membershipsReadStatusAdapter.membershipsReadStatus.addAll(it)
                        membershipsReadStatusAdapter.notifyDataSetChanged()
                        binding.progressBar.visibility = View.GONE
                    })

                    membershipReadStatusViewModel.membershipReadStatusError.observe(this@MembershipReadStatusFragment.viewLifecycleOwner, Observer {
                        showDialogWithMessage(requireContext(), R.string.error_occurred, it)
                        binding.progressBar.visibility = View.GONE
                    })
                    membershipReadStatusViewModel.membershipEventLiveData.observe(this@MembershipReadStatusFragment.viewLifecycleOwner, Observer {
                        if (it.second?.spaceId == spaceId) {
                            when (it.first) {
                                WebexRepository.MembershipEvent.MessageSeen -> {
                                    getList()
                                }

                            }
                        }
                    })
                }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.visibility = View.VISIBLE
        getList()
    }

    fun getList() {
        membershipReadStatusViewModel.getMembershipsWithReadStatus(spaceId)
    }

}

class MembershipReadStatusAdapter : RecyclerView.Adapter<MembershipReadStatusViewHolder>() {
    var membershipsReadStatus: MutableList<MembershipReadStatusModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembershipReadStatusViewHolder {
        return MembershipReadStatusViewHolder(ListItemMembershipReadStatusBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = membershipsReadStatus.size

    override fun onBindViewHolder(holder: MembershipReadStatusViewHolder, position: Int) {
        holder.bind(membershipsReadStatus[position])
    }

}

class MembershipReadStatusViewHolder(private val binding: ListItemMembershipReadStatusBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(membershipReadStatus: MembershipReadStatusModel) {
        binding.membershipReadStatus = membershipReadStatus
        binding.executePendingBindings()
    }
}