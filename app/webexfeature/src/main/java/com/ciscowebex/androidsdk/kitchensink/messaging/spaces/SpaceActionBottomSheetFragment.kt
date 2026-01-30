package com.ciscowebex.androidsdk.kitchensink.messaging.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ciscowebex.androidsdk.kitchensink.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SpaceActionBottomSheetFragment(
    val editClickListener: (String, String) -> Unit,
    val getMeetingInfoClickListener: (String) -> Unit,
    val listMembersInSpaceClickListener: (String) -> Unit,
    val deleteSpaceClickListener: (String, String) -> Unit,
    val markSpaceReadClickListener: (String) -> Unit,
    val showSpaceMembersWithReadStatusClickListener: (String) -> Unit,
    val onAddMemberClicked: (SpaceModel) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var editSpaceName: TextView
    private lateinit var addMembers: TextView
    private lateinit var getMeetingInfo: TextView
    private lateinit var listMembersInSpace: TextView
    private lateinit var showSpaceMembersWithReadStatus: TextView
    private lateinit var markSpaceRead: TextView
    private lateinit var deleteSpace: TextView
    private lateinit var cancel: TextView

    var spaceId: String = ""
    var spaceTitle: String = ""
    lateinit var space: SpaceModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_space_options, container, false)

        editSpaceName = view.findViewById(R.id.editSpaceName)
        addMembers = view.findViewById(R.id.addMembers)
        getMeetingInfo = view.findViewById(R.id.getMeetingInfo)
        listMembersInSpace = view.findViewById(R.id.listMembersInSpace)
        showSpaceMembersWithReadStatus = view.findViewById(R.id.showSpaceMembersWithReadStatus)
        markSpaceRead = view.findViewById(R.id.markSpaceRead)
        deleteSpace = view.findViewById(R.id.deleteSpace)
        cancel = view.findViewById(R.id.cancel)

        editSpaceName.setOnClickListener {
            dismiss()
            editClickListener(spaceId, spaceTitle)
        }

        addMembers.setOnClickListener {
            dismiss()
            onAddMemberClicked(space)
        }

        getMeetingInfo.setOnClickListener {
            dismiss()
            getMeetingInfoClickListener(spaceId)
        }

        listMembersInSpace.setOnClickListener {
            dismiss()
            listMembersInSpaceClickListener(spaceId)
        }

        showSpaceMembersWithReadStatus.setOnClickListener {
            dismiss()
            showSpaceMembersWithReadStatusClickListener(spaceId)
        }

        markSpaceRead.setOnClickListener {
            dismiss()
            markSpaceReadClickListener(spaceId)
        }

        deleteSpace.setOnClickListener {
            dismiss()
            deleteSpaceClickListener(spaceId, spaceTitle)
        }

        cancel.setOnClickListener { dismiss() }

        return view
    }

}