package com.ciscowebex.androidsdk.kitchensink.messaging.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetAddMemberOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddPersonBottomSheetFragment(private val onOptionSelected: (Options) -> Unit) : BottomSheetDialogFragment() {
    companion object {
        val TAG = AddPersonBottomSheetFragment::class.java.simpleName
        enum class Options {
            ADD_BY_PERSON_ID,
            ADD_BY_EMAIL_ID
        }
    }

    private lateinit var binding: BottomSheetAddMemberOptionsBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BottomSheetAddMemberOptionsBinding.inflate(inflater, container, false).also { binding = it }.apply {
            binding.addPersonByIdLabel.setOnClickListener {
                onOptionSelected(Options.ADD_BY_PERSON_ID)
                dismiss()
            }
            binding.addPersonByEmailLabel.setOnClickListener {
                onOptionSelected(Options.ADD_BY_EMAIL_ID)
                dismiss()
            }
            binding.cancel.setOnClickListener {
                dismiss()
            }
        }.root
    }
}