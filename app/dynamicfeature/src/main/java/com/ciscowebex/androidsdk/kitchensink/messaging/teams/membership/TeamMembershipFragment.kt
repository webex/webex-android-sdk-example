package com.ciscowebex.androidsdk.kitchensink.messaging.teams.membership

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.DialogTeamMembershipDetailsBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.FragmentMembershipBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemTeamMembershipClientBinding
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogWithMessage
import org.koin.android.ext.android.inject

class TeamMembershipFragment : Fragment() {

    lateinit var binding: FragmentMembershipBinding

    private val membershipViewModel: TeamMembershipViewModel by inject()

    companion object {
        fun newInstance(teamId: String): TeamMembershipFragment {
            val args = Bundle()
            args.putString(Constants.Bundle.TEAM_ID, teamId)

            val fragment = TeamMembershipFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val teamId = arguments?.getString(Constants.Bundle.TEAM_ID) ?: ""
        membershipViewModel.teamId = teamId
        return FragmentMembershipBinding.inflate(inflater, container, false)
                .also { binding = it }
                .apply {
                    val teamMembershipActionBottomSheet = TeamMembershipActionBottomSheetFragment({ teamMembershipId -> membershipViewModel.getTeamMembership(teamMembershipId) },
                            { teamMembershipId ->
                                showDialogWithMessage(requireContext(), getString(R.string.delete_membership), getString(R.string.confirm_delete_membership_action),
                                        onPositiveButtonClick = { dialog, _ ->
                                            dialog.dismiss()
                                            membershipViewModel.deleteMembership(teamMembershipId, resources.getInteger(R.integer.membership_list_size))
                                        },
                                        onNegativeButtonClick = { dialog, _ ->
                                            dialog.dismiss()
                                        })
                            },

                            { teamMembershipId ->
                                membershipViewModel.updateMembership(teamMembershipId, true)
                            },
                            { teamMembershipId ->
                                membershipViewModel.updateMembership(teamMembershipId, false)
                            }
                    )

                    val membershipClientAdapter = TeamMembershipClientAdapter(teamMembershipActionBottomSheet, requireActivity().supportFragmentManager)
                    membershipsRecyclerView.adapter = membershipClientAdapter
                    membershipsRecyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

                    membershipViewModel.memberships.observe(viewLifecycleOwner, Observer { list ->
                        list?.let {
                            binding.progressLayout.visibility = View.GONE
                            membershipClientAdapter.memberships.clear()
                            membershipClientAdapter.memberships.addAll(it)
                            membershipClientAdapter.notifyDataSetChanged()
                        }
                    })

                    membershipViewModel.membershipDetails.observe(viewLifecycleOwner, Observer { model ->
                        model?.let {
                            displayMembershipDetails(it)
                        }
                    })

                    membershipViewModel.membershipError.observe(viewLifecycleOwner, Observer { error ->
                        error?.let {
                            showErrorDialog(it)
                        }
                    })

                }.root
    }

    private fun displayMembershipDetails(teamMembershipDetails: TeamMembershipModel?) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.members_details)

        DialogTeamMembershipDetailsBinding.inflate(layoutInflater)
                .apply {
                    membership = teamMembershipDetails

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
        builder.setMessage(errorMessage)

        builder.setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getTeamMembers()
    }

    private fun getTeamMembers() {
        binding.progressLayout.visibility = View.VISIBLE
        val maxMemberships = resources.getInteger(R.integer.membership_list_size)
        membershipViewModel.getTeamMembersIn(maxMemberships)
    }
}

class TeamMembershipClientAdapter(private val teamMembershipActionBottomSheet: TeamMembershipActionBottomSheetFragment,
                                  private val supportFragmentManager: FragmentManager) : RecyclerView.Adapter<TeamMembershipClientViewHolder>() {
    var memberships: MutableList<TeamMembershipModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamMembershipClientViewHolder {
        return TeamMembershipClientViewHolder(teamMembershipActionBottomSheet, ListItemTeamMembershipClientBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                supportFragmentManager)
    }

    override fun getItemCount(): Int = memberships.size

    override fun onBindViewHolder(holder: TeamMembershipClientViewHolder, position: Int) {
        holder.bind(memberships[position])
    }

}

class TeamMembershipClientViewHolder(private val teamMembershipActionBottomSheet: TeamMembershipActionBottomSheetFragment,
                                     private val binding: ListItemTeamMembershipClientBinding,
                                     supportFragmentManager: FragmentManager) : RecyclerView.ViewHolder(binding.root) {
    var membership: TeamMembershipModel? = null

    init {
        binding.root.setOnLongClickListener { _ ->
            membership?.let {
                teamMembershipActionBottomSheet.teamMembershipId = it.teamMembershipId
                teamMembershipActionBottomSheet.show(supportFragmentManager, "Team Membership Options")
            }
            true
        }
    }

    fun bind(membership: TeamMembershipModel) {
        this.membership = membership
        binding.membership = membership
        binding.executePendingBindings()
    }
}