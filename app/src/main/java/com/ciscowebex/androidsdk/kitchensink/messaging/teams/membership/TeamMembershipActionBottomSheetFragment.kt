package com.ciscowebex.androidsdk.kitchensink.messaging.teams.membership

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetTeamMemberOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TeamMembershipActionBottomSheetFragment(val membershipDetailsClickListener: (String) -> Unit,
                                              val deleteMembershipClickListener: (String) -> Unit,
                                              val membershipSetModeratorClickListener: (String) -> Unit,
                                              val membershipRemoveModeratorClickListener: (String) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetTeamMemberOptionsBinding
    var teamMembershipId: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BottomSheetTeamMemberOptionsBinding.inflate(inflater, container, false).also { binding = it }.apply {

            getMembershipDetails.setOnClickListener {
                dismiss()
                membershipDetailsClickListener(teamMembershipId)
            }

            setMembershipModerator.setOnClickListener {
                dismiss()
                membershipSetModeratorClickListener(teamMembershipId)
            }

            removeMembershipModerator.setOnClickListener {
                dismiss()
                membershipRemoveModeratorClickListener(teamMembershipId)
            }

            deleteMembership.setOnClickListener {
                dismiss()
                deleteMembershipClickListener(teamMembershipId)
            }

            cancel.setOnClickListener { dismiss() }
        }.root
    }

}