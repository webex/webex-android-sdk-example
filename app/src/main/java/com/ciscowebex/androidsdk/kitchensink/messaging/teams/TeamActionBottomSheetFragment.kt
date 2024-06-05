package com.ciscowebex.androidsdk.kitchensink.messaging.teams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetTeamOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TeamActionBottomSheetFragment(
        val editClickListener: (String, String) -> Unit,
        val addSpaceClickListener : (String) -> Unit,
        val deleteTeamClickListener : (String, String) -> Unit,
        val getMembersClickListener : (String) -> Unit,
        val addMemberClickListener: (TeamModel) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetTeamOptionsBinding
    var teamId : String = ""
    var teamTitle: String = ""
    lateinit var team: TeamModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BottomSheetTeamOptionsBinding.inflate(inflater, container, false).also { binding = it }.apply {

            getMembers.setOnClickListener {
                dismiss()
                getMembersClickListener(teamId)
            }
            editTeamName.setOnClickListener {
                dismiss()
                editClickListener(teamId, teamTitle)
            }

            addSpaceFromTeam.setOnClickListener {
                dismiss()
                addSpaceClickListener(teamId)
            }

            addMembers.setOnClickListener {
                dismiss()
                addMemberClickListener(team)
             }

            deleteTeam.setOnClickListener {
                dismiss()
                deleteTeamClickListener(teamId, teamTitle)
            }

            cancel.setOnClickListener { dismiss() }
        }.root
    }

}