package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ciscowebex.androidsdk.kitchensink.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SpaceMembershipActionBottomSheetFragment(val membershipDetailsClickListener: (String) -> Unit, val membershipSetModeratorClickListener: (String) -> Unit,
                                               val membershipRemoveModeratorClickListener: (String) -> Unit, val showPersonDetails: (String) -> Unit, val deleteMembership: (String, Int) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var getMembershipDetails: TextView
    private lateinit var setMembershipModerator: TextView
    private lateinit var removeMembershipModerator: TextView
    private lateinit var getPersonDetails: TextView
    private lateinit var deleteMembershipView: TextView
    private lateinit var cancel: TextView

    var membershipId : String = ""
    var personId: String = ""
    var position: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_space_member_options, container, false)

        getMembershipDetails = view.findViewById(R.id.getMembershipDetails)
        setMembershipModerator = view.findViewById(R.id.setMembershipModerator)
        removeMembershipModerator = view.findViewById(R.id.removeMembershipModerator)
        getPersonDetails = view.findViewById(R.id.getPersonDetails)
        deleteMembershipView = view.findViewById(R.id.deleteMembership)
        cancel = view.findViewById(R.id.cancel)

        getMembershipDetails.setOnClickListener {
            dismiss()
            membershipDetailsClickListener(membershipId)
        }

        setMembershipModerator.setOnClickListener {
            dismiss()
            membershipSetModeratorClickListener(membershipId)
        }

        removeMembershipModerator.setOnClickListener {
            dismiss()
            membershipRemoveModeratorClickListener(membershipId)
        }

        getPersonDetails.setOnClickListener {
            dismiss()
            showPersonDetails(personId)
        }

        deleteMembershipView.setOnClickListener {
            dismiss()
            deleteMembership(membershipId, position)
        }

        cancel.setOnClickListener { dismiss() }

        return view
    }

}