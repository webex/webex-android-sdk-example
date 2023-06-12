package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetDataMultiStreamOptionsBinding
import com.ciscowebex.androidsdk.phone.Call
import com.ciscowebex.androidsdk.phone.MediaStreamQuality
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MultiStreamDataOptionsBottomSheetFragment(val categoryAOptionsOkListener: (Call?, MediaStreamQuality, Boolean) -> Unit,
                                                val categoryBOptionsOkListener: (Call?, String?, MediaStreamQuality) -> Unit): BottomSheetDialogFragment() {
    companion object {
        val TAG = "MultiStreamDataOptionsBottomSheetFragment"
    }

    enum class OptionType {
        None,
        CategoryA,
        CategoryB
    }

    private lateinit var binding: BottomSheetDataMultiStreamOptionsBinding
    var call: Call? = null
    var type: OptionType = OptionType.None
    private var duplicateValue = false
    private var streamQuality = MediaStreamQuality.LD

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return BottomSheetDataMultiStreamOptionsBinding.inflate(inflater, container, false).also { binding = it }.apply {

            when (type) {
                OptionType.CategoryA -> {
                    numStreamsLayout.visibility = View.GONE
                    duplicateValueLayout.visibility = View.VISIBLE
                }
                OptionType.CategoryB -> {
                    numStreamsLayout.visibility = View.VISIBLE
                    duplicateValueLayout.visibility = View.GONE
                }
                else -> {}
            }

            qualitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    val quality = resources.getStringArray(R.array.qualityArray)[position]
                    Log.d(tag, "selected quality $quality")
                    when (quality) {
                        "LD" -> streamQuality = MediaStreamQuality.LD
                        "SD" -> streamQuality = MediaStreamQuality.SD
                        "HD" -> streamQuality = MediaStreamQuality.HD
                        "FHD" -> streamQuality = MediaStreamQuality.FHD
                    }
                }
            }

            qualitySpinner.setSelection(resources.getStringArray(R.array.qualityArray).indexOf(streamQuality.name))

            duplicateValueToggle.isChecked = false

            duplicateValueToggle.setOnCheckedChangeListener { _, checked ->
                duplicateValue = checked
            }

            ok.setOnClickListener {
                when (type) {
                    OptionType.CategoryA -> {
                        categoryAOptionsOkListener(call, streamQuality, duplicateValue)
                    }
                    OptionType.CategoryB -> {
                        var numStreams: String? = null

                        if (!numStreamsEditText.text.isNullOrEmpty()) {
                            numStreams = numStreamsEditText.text.toString()
                        }
                        categoryBOptionsOkListener(call, numStreams, streamQuality)
                    }
                    else -> {}
                }
                clearData()
                dismiss()
            }
            cancel.setOnClickListener {
                clearData()
                dismiss()
            }
        }.root
    }

    fun clearData() {
        binding.duplicateValueToggle.isChecked = false
        binding.qualitySpinner.setSelection(resources.getStringArray(R.array.qualityArray).indexOf(MediaStreamQuality.LD.name))
        binding.numStreamsEditText.text.clear()
    }
}