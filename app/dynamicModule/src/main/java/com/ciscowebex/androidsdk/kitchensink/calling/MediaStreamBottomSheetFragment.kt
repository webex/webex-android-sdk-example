package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetMediaStreamBinding
import com.ciscowebex.androidsdk.phone.Call
import com.ciscowebex.androidsdk.phone.MediaRenderView
import com.ciscowebex.androidsdk.phone.MediaStreamQuality
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MediaStreamBottomSheetFragment(val pinStreamClickListener: (MediaRenderView?, String?, MediaStreamQuality) -> Unit,
                                     val unpinStreamClickListener: (MediaRenderView?, String?) -> Unit,
                                     val closeStreamStreamClickListener: (MediaRenderView?, String?) -> Unit): BottomSheetDialogFragment() {
    companion object {
        val TAG = "MediaStreamBottomSheetFragment"
    }

    private lateinit var binding: BottomSheetMediaStreamBinding
    var call: Call? = null
    var renderView: MediaRenderView? = null
    var personID: String? = null
    var alreadyPinned: Boolean = false
    var isMediaStreamsPinningSupported: Boolean = false
    private var streamQuality = MediaStreamQuality.LD

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BottomSheetMediaStreamBinding.inflate(inflater, container, false).also { binding = it }.apply {

            hidePinOptions()

            Log.d("MediaStreamBottomSheetFragment", "isMediaStreamsPinningSupported: $isMediaStreamsPinningSupported, personID: $personID" +
                    "alreadyPinned: $alreadyPinned")

            if (isMediaStreamsPinningSupported) {
                if (alreadyPinned) {
                    pinStream.visibility = View.GONE
                    unpinStream.visibility = View.VISIBLE
                } else {
                    unpinStream.visibility = View.GONE
                    pinStream.visibility = View.VISIBLE
                }
            } else {
                pinStream.visibility = View.GONE
                unpinStream.visibility = View.GONE
            }

            pinStream.setOnClickListener {
                showPinOptions()
            }

            unpinStream.setOnClickListener {
                dismiss()
                unpinStreamClickListener(renderView, personID)
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

            ok.setOnClickListener {
                dismiss()
                pinStreamClickListener(renderView, personID, streamQuality)
            }

            cancel.setOnClickListener { dismiss() }
        }.root
    }

    private fun showPinOptions() {
        binding.OptionsRelLayout.visibility = View.GONE
        binding.ok.visibility = View.VISIBLE
        binding.propertyOptionsRelLayout.visibility = View.VISIBLE
    }

    private fun hidePinOptions() {
        binding.ok.visibility = View.GONE
        binding.propertyOptionsRelLayout.visibility = View.GONE
        binding.OptionsRelLayout.visibility = View.VISIBLE
    }
}