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
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogWithMessage
import org.koin.android.ext.android.inject

class TeamMembershipFragment : Fragment() {

    // Removed DataBinding - using findViewById instead
    // lateinit var binding: FragmentMembershipBinding

    private lateinit var membershipsRecyclerView: RecyclerView
    private lateinit var progressLayout: View

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
        
        val view = inflater.inflate(R.layout.fragment_membership, container, false)
        
        // Initialize views
        membershipsRecyclerView = view.findViewById(R.id.membershipsRecyclerView)
        progressLayout = view.findViewById(R.id.progressLayout)
        
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
                progressLayout.visibility = View.GONE
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

        return view
    }

    private fun displayMembershipDetails(teamMembershipDetails: TeamMembershipModel?) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.members_details)

        val dialogView = layoutInflater.inflate(R.layout.dialog_team_membership_details, null)
        
        teamMembershipDetails?.let { membership ->
            dialogView.findViewById<TextView>(R.id.membershipIdTextView).text = membership.teamMembershipId ?: ""
            dialogView.findViewById<TextView>(R.id.membershipPersonIdTextView).text = membership.personId ?: ""
            dialogView.findViewById<TextView>(R.id.membershipPersonEmailTextView).text = membership.personEmail ?: ""
            dialogView.findViewById<TextView>(R.id.membershipPersonDisplayNameTextView).text = membership.personDisplayName ?: ""
            dialogView.findViewById<TextView>(R.id.membershipIsModeratorTextView).text = membership.isModerator.toString()
            dialogView.findViewById<TextView>(R.id.membershipPersonOrgIdTextView).text = membership.personOrgId ?: ""
            dialogView.findViewById<TextView>(R.id.membershipDateCreatedTextView).text = membership.created?.toString() ?: ""
        }

        builder.setView(dialogView)
        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
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
        progressLayout.visibility = View.VISIBLE
        val maxMemberships = resources.getInteger(R.integer.membership_list_size)
        membershipViewModel.getTeamMembersIn(maxMemberships)
    }
}

class TeamMembershipClientAdapter(private val teamMembershipActionBottomSheet: TeamMembershipActionBottomSheetFragment,
                                  private val supportFragmentManager: FragmentManager) : RecyclerView.Adapter<TeamMembershipClientViewHolder>() {
    var memberships: MutableList<TeamMembershipModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamMembershipClientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_team_membership_client, parent, false)
        return TeamMembershipClientViewHolder(teamMembershipActionBottomSheet, view, supportFragmentManager)
    }

    override fun getItemCount(): Int = memberships.size

    override fun onBindViewHolder(holder: TeamMembershipClientViewHolder, position: Int) {
        holder.bind(memberships[position])
    }

}

class TeamMembershipClientViewHolder(
    private val teamMembershipActionBottomSheet: TeamMembershipActionBottomSheetFragment,
    itemView: View,
    private val supportFragmentManager: FragmentManager
) : RecyclerView.ViewHolder(itemView) {
    var membership: TeamMembershipModel? = null
    
    private val membershipContainer: ConstraintLayout = itemView.findViewById(R.id.membershipContainer)
    private val membershipPersonDisplayNameTextView: TextView = itemView.findViewById(R.id.membershipPersonDisplayNameTextView)
    private val membershipPersonEmailTextView: TextView = itemView.findViewById(R.id.membershipPersonEmailTextView)
    private val membershipCreatedTextView: TextView = itemView.findViewById(R.id.membershipCreatedTextView)

    init {
        itemView.setOnLongClickListener { _ ->
            membership?.let {
                teamMembershipActionBottomSheet.teamMembershipId = it.teamMembershipId
                teamMembershipActionBottomSheet.show(supportFragmentManager, "Team Membership Options")
            }
            true
        }
    }

    fun bind(membership: TeamMembershipModel) {
        this.membership = membership
        membershipPersonDisplayNameTextView.text = membership.personDisplayName ?: ""
        membershipPersonEmailTextView.text = membership.personEmail ?: ""
        membershipCreatedTextView.text = membership.created?.toString() ?: ""
    }
}