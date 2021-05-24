package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetSpaceMemberOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SpaceMembershipActionBottomSheetFragment(val membershipDetailsClickListener: (String) -> Unit, val membershipSetModeratorClickListener: (String) -> Unit,
                                               val membershipRemoveModeratorClickListener: (String) -> Unit, val showPersonDetails: (String) -> Unit, val deleteMembership: (String, Int) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetSpaceMemberOptionsBinding
    var membershipId : String = ""
    var personId: String = ""
    var position: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BottomSheetSpaceMemberOptionsBinding.inflate(inflater, container, false).also { binding = it }.apply {

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

            deleteMembership.setOnClickListener {
                dismiss()
                deleteMembership(membershipId, position)
            }

            cancel.setOnClickListener { dismiss() }
        }.root
    }

}