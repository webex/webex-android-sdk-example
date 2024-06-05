package com.ciscowebex.androidsdk.kitchensink.messaging.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetSpaceOptionsBinding
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

    private lateinit var binding: BottomSheetSpaceOptionsBinding
    var spaceId: String = ""
    var spaceTitle: String = ""
    lateinit var space: SpaceModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BottomSheetSpaceOptionsBinding.inflate(inflater, container, false).also { binding = it }.apply {

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
        }.root
    }

}