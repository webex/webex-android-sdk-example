package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetMultiStreamOptionsBinding
import com.ciscowebex.androidsdk.phone.Call
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MultiStreamOptionsBottomSheetFragment(val setCategoryAOptionClickListener: (Call?) -> Unit,
                                            val setCategoryBOptionClickListener: (Call?) -> Unit,
                                            val removeCategoryAClickListener: (Call?) -> Unit,
                                            val removeCategoryBClickListener: (Call?) -> Unit): BottomSheetDialogFragment() {
    companion object {
        val TAG = "MultiStreamOptionsBottomSheet"
    }

    private lateinit var binding: BottomSheetMultiStreamOptionsBinding
    var call: Call? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return BottomSheetMultiStreamOptionsBinding.inflate(inflater, container, false).also { binding = it }.apply {

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
        }.root
    }
}