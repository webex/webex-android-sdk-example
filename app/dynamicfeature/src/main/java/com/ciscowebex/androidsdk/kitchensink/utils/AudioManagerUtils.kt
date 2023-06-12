package com.ciscowebex.androidsdk.kitchensink.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.media.AudioManager
import android.util.Log


open class AudioManagerUtils(val context: Context) {
    private var audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val tag = "AudioManagerUtils"

    val isWiredHeadsetOn: Boolean
        get() {
            val isWiredHeadsetOn = audioManager.isWiredHeadsetOn
            Log.i(tag, "AudioManager.isWiredHeadsetOn = $isWiredHeadsetOn")
            return isWiredHeadsetOn
        }

    val isBluetoothHeadsetConnected: Boolean
        get() {
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val isBtConnected = (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled
                    && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED)
            Log.i(tag, "AudioManager.isBluetoothHeadsetConnected = $isBtConnected")
            return isBtConnected
        }
}