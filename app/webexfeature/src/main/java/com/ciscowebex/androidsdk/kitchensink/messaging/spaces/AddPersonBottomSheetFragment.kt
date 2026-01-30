package com.ciscowebex.androidsdk.kitchensink.messaging.spaces

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ciscowebex.androidsdk.kitchensink.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddPersonBottomSheetFragment(private val onOptionSelected: (Options) -> Unit) : BottomSheetDialogFragment() {
    companion object {
        val TAG = AddPersonBottomSheetFragment::class.java.simpleName
        enum class Options {
            ADD_BY_PERSON_ID,
            ADD_BY_EMAIL_ID
        }
    }

    private lateinit var addPersonByIdLabel: TextView
    private lateinit var addPersonByEmailLabel: TextView
    private lateinit var cancel: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_add_member_options, container, false)

        addPersonByIdLabel = view.findViewById(R.id.addPersonByIdLabel)
        addPersonByEmailLabel = view.findViewById(R.id.addPersonByEmailLabel)
        cancel = view.findViewById(R.id.cancel)

        addPersonByIdLabel.setOnClickListener {
            onOptionSelected(Options.ADD_BY_PERSON_ID)
            dismiss()
        }
        addPersonByEmailLabel.setOnClickListener {
            onOptionSelected(Options.ADD_BY_EMAIL_ID)
            dismiss()
        }
        cancel.setOnClickListener {
            dismiss()
        }

        return view
    }
}