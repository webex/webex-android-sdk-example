package com.ciscowebex.androidsdk.kitchensink.utils

import android.bluetooth.BluetoothHeadset
import android.content.Context
import android.media.AudioManager


open class AudioManagerUtils(val context: Context) {
    private var audioManager: AudioManager? = null

    fun initAudioManager() {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        audioManager?.mode = AudioManager.MODE_IN_CALL
        audioManager?.isSpeakerphoneOn = false
    }

    fun toggleSpeaker(){
        audioManager?.isSpeakerphoneOn = audioManager?.isSpeakerphoneOn == false
    }

    fun putOnSpeaker(){
        audioManager?.isSpeakerphoneOn = true
    }

    @Suppress("DEPRECATION")
    fun isHeadSetDeviceAvailable(): Boolean = audioManager?.isWiredHeadsetOn ?: false
}