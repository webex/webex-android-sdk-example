package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.phone.Call
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MultiStreamOptionsBottomSheetFragment(val setCategoryAOptionClickListener: (Call?) -> Unit,
                                            val setCategoryBOptionClickListener: (Call?) -> Unit,
                                            val removeCategoryAClickListener: (Call?) -> Unit,
                                            val removeCategoryBClickListener: (Call?) -> Unit): BottomSheetDialogFragment() {
    companion object {
        val TAG = "MultiStreamOptionsBottomSheet"
    }

    private lateinit var setCategoryAOption: TextView
    private lateinit var setCategoryBOption: TextView
    private lateinit var removeCategoryA: TextView
    private lateinit var removeCategoryB: TextView
    private lateinit var cancel: TextView
    var call: Call? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.bottom_sheet_multi_stream_options, container, false)

        setCategoryAOption = view.findViewById(R.id.setCategoryAOption)
        setCategoryBOption = view.findViewById(R.id.setCategoryBOption)
        removeCategoryA = view.findViewById(R.id.removeCategoryA)
        removeCategoryB = view.findViewById(R.id.removeCategoryB)
        cancel = view.findViewById(R.id.cancel)

        setCategoryAOption.setOnClickListener {
            dismiss()
            setCategoryAOptionClickListener(call)
        }

        setCategoryBOption.setOnClickListener {
            dismiss()
            setCategoryBOptionClickListener(call)
        }

        removeCategoryA.setOnClickListener {
            dismiss()
            removeCategoryAClickListener(call)
        }

        removeCategoryB.setOnClickListener {
            dismiss()
            removeCategoryBClickListener(call)
        }

        cancel.setOnClickListener { dismiss() }

        return view
    }
}