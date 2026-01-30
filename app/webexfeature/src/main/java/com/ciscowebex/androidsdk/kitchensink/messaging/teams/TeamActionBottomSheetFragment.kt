package com.ciscowebex.androidsdk.kitchensink.messaging.teams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ciscowebex.androidsdk.kitchensink.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TeamActionBottomSheetFragment(
        val editClickListener: (String, String) -> Unit,
        val addSpaceClickListener : (String) -> Unit,
        val deleteTeamClickListener : (String, String) -> Unit,
        val getMembersClickListener : (String) -> Unit,
        val addMemberClickListener: (TeamModel) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var getMembers: TextView
    private lateinit var editTeamName: TextView
    private lateinit var addSpaceFromTeam: TextView
    private lateinit var addMembers: TextView
    private lateinit var deleteTeam: TextView
    private lateinit var cancel: TextView

    var teamId : String = ""
    var teamTitle: String = ""
    lateinit var team: TeamModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_team_options, container, false)

        getMembers = view.findViewById(R.id.getMembers)
        editTeamName = view.findViewById(R.id.editTeamName)
        addSpaceFromTeam = view.findViewById(R.id.addSpaceFromTeam)
        addMembers = view.findViewById(R.id.addMembers)
        deleteTeam = view.findViewById(R.id.deleteTeam)
        cancel = view.findViewById(R.id.cancel)

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

        return view
    }

}