package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetSwitchAudioOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SwitchAudioBottomSheetFragment(val onEarpieceSelected: () -> Unit,
                          val onSpeakerSelected: () -> Unit,
                          val onBluetoothSelected: () -> Unit,
                          val onHeadsetSelected: () -> Unit): BottomSheetDialogFragment() {
    companion object {
        val TAG = "SwitchAudioBottomSheetFragment"
    }

    private lateinit var binding: BottomSheetSwitchAudioOptionsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return BottomSheetSwitchAudioOptionsBinding.inflate(inflater, container, false).also { binding = it }.apply {

            selectSpeakerOption.setOnClickListener {
                dismiss()
                onSpeakerSelected()
            }

            selectBluetooth.setOnClickListener {
                dismiss()
                onBluetoothSelected()
            }

            selectPhoneEarpieceOption.setOnClickListener {
                dismiss()
                onEarpieceSelected()
            }

            selectHeadset.setOnClickListener {
                dismiss()
                onHeadsetSelected()
            }
        }.root
    }
}