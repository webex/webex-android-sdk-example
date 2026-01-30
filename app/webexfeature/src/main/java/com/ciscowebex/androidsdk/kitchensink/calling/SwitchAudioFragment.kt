package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ciscowebex.androidsdk.kitchensink.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SwitchAudioBottomSheetFragment(val onEarpieceSelected: () -> Unit,
                          val onSpeakerSelected: () -> Unit,
                          val onBluetoothSelected: () -> Unit,
                          val onHeadsetSelected: () -> Unit): BottomSheetDialogFragment() {
    companion object {
        val TAG = "SwitchAudioBottomSheetFragment"
    }

    private lateinit var selectSpeakerOption: TextView
    private lateinit var selectBluetooth: TextView
    private lateinit var selectPhoneEarpieceOption: TextView
    private lateinit var selectHeadset: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.bottom_sheet_switch_audio_options, container, false)

        selectSpeakerOption = view.findViewById(R.id.selectSpeakerOption)
        selectBluetooth = view.findViewById(R.id.selectBluetooth)
        selectPhoneEarpieceOption = view.findViewById(R.id.selectPhoneEarpieceOption)
        selectHeadset = view.findViewById(R.id.selectHeadset)

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

        return view
    }
}