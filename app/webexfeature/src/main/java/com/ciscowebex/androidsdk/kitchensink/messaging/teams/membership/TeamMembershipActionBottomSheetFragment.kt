package com.ciscowebex.androidsdk.kitchensink.messaging.teams.membership

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ciscowebex.androidsdk.kitchensink.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TeamMembershipActionBottomSheetFragment(val membershipDetailsClickListener: (String) -> Unit,
                                              val deleteMembershipClickListener: (String) -> Unit,
                                              val membershipSetModeratorClickListener: (String) -> Unit,
                                              val membershipRemoveModeratorClickListener: (String) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var getMembershipDetails: TextView
    private lateinit var setMembershipModerator: TextView
    private lateinit var removeMembershipModerator: TextView
    private lateinit var deleteMembership: TextView
    private lateinit var cancel: TextView

    var teamMembershipId: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_team_member_options, container, false)

        getMembershipDetails = view.findViewById(R.id.getMembershipDetails)
        setMembershipModerator = view.findViewById(R.id.setMembershipModerator)
        removeMembershipModerator = view.findViewById(R.id.removeMembershipModerator)
        deleteMembership = view.findViewById(R.id.deleteMembership)
        cancel = view.findViewById(R.id.cancel)

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

        return view
    }

}