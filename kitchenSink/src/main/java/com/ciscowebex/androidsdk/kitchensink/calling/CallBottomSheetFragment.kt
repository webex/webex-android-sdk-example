package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetCallOptionsBinding
import com.ciscowebex.androidsdk.phone.Call
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ciscowebex.androidsdk.kitchensink.R

class CallBottomSheetFragment(val receivingVideoClickListener: (Call) -> Unit,
                              val receivingAudioClickListener: (Call) -> Unit,
                              val receivingSharingClickListener: (Call) -> Unit): BottomSheetDialogFragment() {
    companion object {
        val TAG = "MessageActionBottomSheetFragment"
    }

    private lateinit var binding: BottomSheetCallOptionsBinding
    lateinit var call: Call

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BottomSheetCallOptionsBinding.inflate(inflater, container, false).also { binding = it }.apply {

            var receivingVideoText = getString(R.string.receiving_video)
            receivingVideoText += if (call.isReceivingVideo()) {
                " - " + getString(R.string.receiving_on)
            } else {
                " - " + getString(R.string.receiving_off)
            }
            receivingVideo.text = receivingVideoText

            receivingVideo.setOnClickListener {
                dismiss()
                receivingVideoClickListener(call)
            }

            var receivingAudioText = getString(R.string.receiving_audio)
            receivingAudioText += if (call.isReceivingAudio()) {
                " - " + getString(R.string.receiving_on)
            } else {
                " - " + getString(R.string.receiving_off)
            }
            receivingAudio.text = receivingAudioText

            receivingAudio.setOnClickListener {
                dismiss()
                receivingAudioClickListener(call)
            }

            var receivingSharingText = getString(R.string.receiving_sharing)
            receivingSharingText += if (call.isReceivingSharing()) {
                " - " + getString(R.string.receiving_on)
            } else {
                " - " + getString(R.string.receiving_off)
            }
            receivingSharing.text = receivingSharingText

            receivingSharing.setOnClickListener {
                dismiss()
                receivingSharingClickListener(call)
            }

            cancel.setOnClickListener { dismiss() }
        }.root
    }
}