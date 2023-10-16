package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.databinding.DialogMembershipDetailsBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.FragmentMembershipBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemMembershipClientBinding
import com.ciscowebex.androidsdk.kitchensink.person.PersonDialogFragment
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.getCurrentDate
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogWithMessage
import com.ciscowebex.androidsdk.kitchensink.utils.stateToDrawable
import com.ciscowebex.androidsdk.kitchensink.utils.stateToString
import com.ciscowebex.androidsdk.people.PresenceStatus
import org.koin.android.ext.android.inject

class MembershipFragment : Fragment() {

    lateinit var binding: FragmentMembershipBinding

    private val membershipViewModel: MembershipViewModel by inject()
    private var spaceId: String? = null

    companion object {
        fun newInstance(spaceId: String): MembershipFragment {
            val args = Bundle()
            args.putString(Constants.Bundle.SPACE_ID, spaceId)

            val fragment = MembershipFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        spaceId = arguments?.getString(Constants.Bundle.SPACE_ID)

        return FragmentMembershipBinding.inflate(inflater, container, false)
                .also { binding = it }
                .apply {
                    lifecycleOwner = this@MembershipFragment
                    val spaceMembershipActionBottomSheetFragment = SpaceMembershipActionBottomSheetFragment(
                            { membershipId -> membershipViewModel.getMembership(membershipId) },
                            { membershipId -> membershipViewModel.updateMembershipWith(membershipId, true) },
                            { membershipId -> membershipViewModel.updateMembershipWith(membershipId, false) },
                            { personId -> showPersonDetails(personId) },
                            { membershipId, position ->
                                showDialogWithMessage(requireContext(), getString(R.string.delete_membership), getString(R.string.confirm_delete_space_membership_action),
                                        onPositiveButtonClick = { dialog, _ ->
                                            dialog.dismiss()
                                            membershipViewModel.deleteMembership(position, membershipId)
                                        },
                                        onNegativeButtonClick = { dialog, _ ->
                                            dialog.dismiss()
                                        })
                            })

                    val membershipClientAdapter = MembershipClientAdapter(spaceMembershipActionBottomSheetFragment, spaceId)
                    membershipsRecyclerView.adapter = membershipClientAdapter
                    membershipsRecyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

                    membershipViewModel.presenceChangeLiveData.observe(this@MembershipFragment.viewLifecycleOwner) {
                        val position =
                            membershipClientAdapter.getPositionByPersonId(it.getContactId())
                        if (position >= 0) {
                            membershipClientAdapter.memberships[position].apply {
                                presenceStatusDrawable = stateToDrawable(
                                    this@MembershipFragment.requireContext(),
                                    it.getStatus()
                                )
                                presenceStatusText = stateToString(
                                    this@MembershipFragment.requireContext(),
                                    it.getStatus()
                                )

                                if (PresenceStatus.Inactive == it.getStatus()) {
                                    if (it.getLastActiveTime() > 0) {
                                        presenceStatusText =
                                            presenceStatusText + " | last seen: " + getCurrentDate(
                                                it.getLastActiveTime()
                                            )
                                    }
                                } else {
                                    if (it.getExpiresTime() > 0) {
                                        presenceStatusText =
                                            presenceStatusText + " | till: " + getCurrentDate(it.getExpiresTime())
                                    }
                                }

                                if (!it.getCustomStatus().isNullOrEmpty()) {
                                    presenceStatusText =
                                        presenceStatusText + " | " + it.getCustomStatus()
                                }
                            }
                            membershipClientAdapter.notifyItemChanged(position)
                        }
                    }

                    membershipViewModel.memberships.observe(this@MembershipFragment.viewLifecycleOwner, Observer { model ->
                        model?.let {
                            binding.progressLayout.visibility = View.GONE
                            membershipClientAdapter.memberships.clear()
                            membershipClientAdapter.memberships.addAll(it)
                            membershipClientAdapter.notifyDataSetChanged()

                            val personList:List<String> = membershipClientAdapter.memberships.map { it.personId }
                            membershipViewModel.startWatchingPresence(personList)
                        }
                    })

                    membershipViewModel.membershipDetail.observe(this@MembershipFragment.viewLifecycleOwner, Observer { model ->
                        model?.let {
                            getMembers()
                            displayMembershipDetail(it)
                        }
                    })

                    membershipViewModel.membershipError.observe(this@MembershipFragment.viewLifecycleOwner, Observer { error ->
                        error?.let {
                            showErrorDialog(it)
                        }
                    })

                    membershipViewModel.membershipEventLiveData.observe(this@MembershipFragment.viewLifecycleOwner, Observer {
                        if(it.second?.spaceId == spaceId) {
                            when (it.first) {
                                WebexRepository.MembershipEvent.Created -> {
                                    membershipClientAdapter.memberships.add(0, MembershipModel.convertToMembershipModel(it.second))
                                    membershipClientAdapter.notifyItemInserted(0)

                                    val personList:List<String> = membershipClientAdapter.memberships.map { data -> data.personId }
                                    membershipViewModel.startWatchingPresence(personList)
                                }
                                WebexRepository.MembershipEvent.Updated -> {

                                    val position = membershipClientAdapter.getPositionById(it.second?.id.orEmpty())
                                    if (!membershipClientAdapter.memberships.isNullOrEmpty() && position != -1) {
                                        membershipClientAdapter.memberships[position] = MembershipModel.convertToMembershipModel(it.second)
                                        membershipClientAdapter.notifyItemChanged(position)
                                    }
                                    Log.d(tag, "MembershipEvent - Update -> MembershipID : ${it.second?.id} , PersonID : ${it.second?.personId} ")

                                }
                                WebexRepository.MembershipEvent.Deleted -> {
                                    val position = membershipClientAdapter.getPositionById(it.second?.id.orEmpty())
                                    if (!membershipClientAdapter.memberships.isNullOrEmpty() && position != -1) {
                                        membershipClientAdapter.memberships.removeAt(position)
                                        membershipClientAdapter.notifyItemRemoved(position)
                                    }
                                    Log.d(tag, "MembershipEvent - Delete -> MembershipID : ${it.second?.id} , PersonID : ${it.second?.personId} ")
                                }
                            }
                        }
                    })
                }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getMembers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        membershipViewModel.stopWatchingPresence()
    }

    private fun getMembers() {
        binding.progressLayout.visibility = View.VISIBLE
        val maxMemberships = resources.getInteger(R.integer.membership_list_size)
        membershipViewModel.getMembersIn(spaceId, maxMemberships)
    }

    private fun displayMembershipDetail(membershipModel: MembershipModel) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.members_details)

        DialogMembershipDetailsBinding.inflate(layoutInflater)
                .apply {
                    membership = membershipModel

                    builder.setView(this.root)
                    builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                        dialog.dismiss()
                    }

                    builder.show()
                }
    }

    private fun showErrorDialog(errorMessage: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.error_occurred)
        val message = TextView(requireContext())
        message.setPadding(10, 10, 10, 10)
        message.text = errorMessage

        builder.setView(message)

        builder.setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun showPersonDetails(personId: String) {
        PersonDialogFragment.newInstance(personId).show(childFragmentManager, getString(R.string.person_detail))
    }
}

class MembershipClientAdapter(private val spaceMembershipActionBottomSheetFragment: SpaceMembershipActionBottomSheetFragment, private val spaceId: String?) : RecyclerView.Adapter<MembershipClientViewHolder>() {
    var memberships: MutableList<MembershipModel> = mutableListOf()

    fun getPositionById(membershipId: String): Int {
        return memberships.indexOfFirst { it.membershipId == membershipId }
    }

    fun getPositionByPersonId(personId: String): Int {
        return memberships.indexOfFirst { it.personId == personId }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembershipClientViewHolder {
        return MembershipClientViewHolder(ListItemMembershipClientBinding.inflate(LayoutInflater.from(parent.context), parent, false), spaceMembershipActionBottomSheetFragment, spaceId)
    }

    override fun getItemCount(): Int = memberships.size

    override fun onBindViewHolder(holder: MembershipClientViewHolder, position: Int) {
        holder.bind(memberships[position])
    }
}

class MembershipClientViewHolder(private val binding: ListItemMembershipClientBinding, private val spaceMembershipActionBottomSheetFragment: SpaceMembershipActionBottomSheetFragment, private val spaceId: String?) : RecyclerView.ViewHolder(binding.root) {
    fun bind(membership: MembershipModel) {
        binding.membership = membership

        if (!spaceId.isNullOrEmpty()) {
            binding.membershipContainer.setOnLongClickListener { view ->
                spaceMembershipActionBottomSheetFragment.membershipId = membership.membershipId
                spaceMembershipActionBottomSheetFragment.personId = membership.personId
                spaceMembershipActionBottomSheetFragment.position = adapterPosition

                val activity = view.context as AppCompatActivity
                activity.supportFragmentManager.let { spaceMembershipActionBottomSheetFragment.show(it, "Membership Options") }

                true
            }
        }

        binding.executePendingBindings()
    }
}