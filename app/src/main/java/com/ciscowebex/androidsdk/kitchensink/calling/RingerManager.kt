package com.ciscowebex.androidsdk.kitchensink.calling

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.util.SparseIntArray
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.phone.Call
import org.koin.core.component.KoinComponent
import java.io.IOException


open class RingerManager(private val androidContext: Context): KoinComponent {

    private val tag = "RingerManager"

    private var vibrator: Vibrator = androidContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private var audioManager: AudioManager = androidContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val inCallSoundPool: SoundPool = SoundPool.Builder()
                                                .setMaxStreams(1)
                                                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).build())
                                                .build()
    private val dtmfSoundPool: SoundPool = SoundPool.Builder().setMaxStreams(1).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING).build()).build()
    private var incomingCallPlayer: MediaPlayer? = null
    private val INCALL_RINGTONE_COUNT = 5
    private val DTMF_RINGTONE_COUNT = 12
    private val inCallSoundArray: SparseIntArray = SparseIntArray(INCALL_RINGTONE_COUNT)
    private val dtmfSoundArray: SparseIntArray = SparseIntArray(DTMF_RINGTONE_COUNT)
    private var currentPlayingToneId = 0
    private var dtmfSoundPlaying = false
    private var inCallSoundPlaying = false
    private var audioFocusGainForRingtone = false
    private var needAudioFocusForCall = false
    private val ringerLock = Any()

    init {
        loadInCallTones()
        loadDtmfTones()
    }

    private fun loadInCallTones() {
        inCallSoundArray.put(0, inCallSoundPool.load(androidContext, R.raw.call_1_1_ringback, 1))
        inCallSoundArray.put(1, inCallSoundPool.load(androidContext, R.raw.reconnect, 1))
        inCallSoundArray.put(2, inCallSoundPool.load(androidContext, R.raw.busytone, 1))
        inCallSoundArray.put(3, inCallSoundPool.load(androidContext, R.raw.callwaiting, 1))
        inCallSoundArray.put(4,inCallSoundPool.load(androidContext, R.raw.fastbusy, 1))
    }

    private fun loadDtmfTones() {
        dtmfSoundArray.put(0, dtmfSoundPool.load(androidContext, R.raw.keypad_digit_0, 1))
        dtmfSoundArray.put(1, dtmfSoundPool.load(androidContext, R.raw.keypad_digit_1, 1))
        dtmfSoundArray.put(2, dtmfSoundPool.load(androidContext, R.raw.keypad_digit_2, 1))
        dtmfSoundArray.put(3, dtmfSoundPool.load(androidContext, R.raw.keypad_digit_3, 1))
        dtmfSoundArray.put(4, dtmfSoundPool.load(androidContext, R.raw.keypad_digit_4, 1))
        dtmfSoundArray.put(5, dtmfSoundPool.load(androidContext, R.raw.keypad_digit_5, 1))
        dtmfSoundArray.put(6, dtmfSoundPool.load(androidContext, R.raw.keypad_digit_6, 1))
        dtmfSoundArray.put(7, dtmfSoundPool.load(androidContext, R.raw.keypad_digit_7, 1))
        dtmfSoundArray.put(8, dtmfSoundPool.load(androidContext, R.raw.keypad_digit_8, 1))
        dtmfSoundArray.put(9, dtmfSoundPool.load(androidContext, R.raw.keypad_digit_9, 1))
        dtmfSoundArray.put(10, dtmfSoundPool.load(androidContext, R.raw.keypad_digit_asterisk, 1))
        dtmfSoundArray.put(11, dtmfSoundPool.load(androidContext, R.raw.keypad_digit_hash, 1))
    }

    private fun playCallTone(ringerType: Call.RingerType) {
        if (currentPlayingToneId != 0) return
        inCallSoundPlaying = true
        val streamVolume = getStreamVolume(AudioManager.STREAM_VOICE_CALL)
        when(ringerType) {
            Call.RingerType.Outgoing -> currentPlayingToneId = inCallSoundPool.play(inCallSoundArray[0], streamVolume, streamVolume, 1, -1, 1.toFloat())
            Call.RingerType.Reconnect ->  currentPlayingToneId = inCallSoundPool.play(inCallSoundArray[1], streamVolume, streamVolume, 1, 5, 1.toFloat())
            Call.RingerType.BusyTone ->  currentPlayingToneId = inCallSoundPool.play(inCallSoundArray[2], streamVolume, streamVolume, 1, 5, 1.toFloat())
            Call.RingerType.CallWaiting ->  currentPlayingToneId = inCallSoundPool.play(inCallSoundArray[3], streamVolume, streamVolume, 1, -1, 1.toFloat())
            Call.RingerType.NotFound ->  currentPlayingToneId = inCallSoundPool.play(inCallSoundArray[4], streamVolume, streamVolume, 1, 5, 1.toFloat())
            else -> Log.d(tag,"Incall tone not available")
        }

        Log.d(tag, "currentPlayingToneId=$currentPlayingToneId")
    }

    private fun playDtmfTone(ringerType: Call.RingerType) {
        val streamVolume = getStreamVolume(AudioManager.STREAM_DTMF)
        dtmfSoundPlaying = true
        when(ringerType)
        {
            Call.RingerType.DTMF_0 -> currentPlayingToneId = dtmfSoundPool.play(dtmfSoundArray[0], streamVolume, streamVolume, 1, 1, 1.toFloat())
            Call.RingerType.DTMF_1 -> currentPlayingToneId = dtmfSoundPool.play(dtmfSoundArray[1], streamVolume, streamVolume, 1, 1, 1.toFloat())
            Call.RingerType.DTMF_2 -> currentPlayingToneId = dtmfSoundPool.play(dtmfSoundArray[2], streamVolume, streamVolume, 1, 1, 1.toFloat())
            Call.RingerType.DTMF_3 -> currentPlayingToneId = dtmfSoundPool.play(dtmfSoundArray[3], streamVolume, streamVolume, 1, 1, 1.toFloat())
            Call.RingerType.DTMF_4 -> currentPlayingToneId = dtmfSoundPool.play(dtmfSoundArray[4], streamVolume, streamVolume, 1, 1, 1.toFloat())
            Call.RingerType.DTMF_5 -> currentPlayingToneId = dtmfSoundPool.play(dtmfSoundArray[5], streamVolume, streamVolume, 1, 1, 1.toFloat())
            Call.RingerType.DTMF_6 -> currentPlayingToneId = dtmfSoundPool.play(dtmfSoundArray[6], streamVolume, streamVolume, 1, 1, 1.toFloat())
            Call.RingerType.DTMF_7 -> currentPlayingToneId = dtmfSoundPool.play(dtmfSoundArray[7], streamVolume, streamVolume, 1, 1, 1.toFloat())
            Call.RingerType.DTMF_8 -> currentPlayingToneId = dtmfSoundPool.play(dtmfSoundArray[8], streamVolume, streamVolume, 1, 1, 1.toFloat())
            Call.RingerType.DTMF_9 -> currentPlayingToneId = dtmfSoundPool.play(dtmfSoundArray[9], streamVolume, streamVolume, 1, 1, 1.toFloat())
            Call.RingerType.DTMF_STAR -> currentPlayingToneId = dtmfSoundPool.play(dtmfSoundArray[10], streamVolume, streamVolume, 1, 1, 1.toFloat())
            Call.RingerType.DTMF_POUND -> currentPlayingToneId = dtmfSoundPool.play(dtmfSoundArray[11], streamVolume, streamVolume, 1, 1, 1.toFloat())
            else -> Log.d(tag, "type not found")
        }
    }

    fun startRinger(type: Call.RingerType) {
        Log.d(tag, "startRinger type: $type")
        synchronized(ringerLock) {
            handleStartRinger(type)
        }
    }

    fun stopRinger(type: Call.RingerType) {
        Log.d(tag, "stopRinger type: $type")
        synchronized(ringerLock) {
            handleStopRinger(type)
        }
    }

    private fun handleStartRinger(type: Call.RingerType) {
        Log.d(tag, "handleStartRinger type: $type")
        when (type) {
            Call.RingerType.Incoming -> playIncomingTone()
            Call.RingerType.Outgoing,Call.RingerType.CallWaiting, Call.RingerType.NotFound, Call.RingerType.BusyTone, Call.RingerType.Reconnect -> playCallTone(type)
            Call.RingerType.DTMF_0, Call.RingerType.DTMF_1, Call.RingerType.DTMF_2, Call.RingerType.DTMF_3, Call.RingerType.DTMF_4, Call.RingerType.DTMF_5, Call.RingerType.DTMF_6, Call.RingerType.DTMF_7,
            Call.RingerType.DTMF_8, Call.RingerType.DTMF_9, Call.RingerType.DTMF_STAR, Call.RingerType.DTMF_POUND -> playDtmfTone(type)
        }
    }

    private fun handleStopRinger(type: Call.RingerType) {
        Log.d(tag, "handleStopRinger type: $type")
        when (type) {
            Call.RingerType.Incoming -> stopIncomingTone()
            else ->
                stopCallTone()
        }
    }

    private fun playIncomingTone() {
        Log.d(tag,"playIncomingTone")
        playIncomingCallTone()
    }

    private fun stopIncomingTone() {
        Log.d(tag, "stopIncomingTone")
        stopIncomingCallTone()
    }

    private fun playIncomingCallTone() {
        Log.d(tag,  "playing ringtone for  incoming call")
        if (incomingCallPlayer?.isPlaying == true) {
            Log.d(tag, "incoming call is already playing, ignore this request")
            return
        }
        startVibrate()
        Log.d(tag, "start playing incoming tone")
        requestAudioFocusForRingtone()
        if (incomingCallPlayer == null) {
            incomingCallPlayer = MediaPlayer()
            setupMediaPlayer(incomingCallPlayer)
        }
        incomingCallPlayer?.start()
    }

    private fun setupMediaPlayer(mediaPlayer: MediaPlayer?) {
        Log.d(tag, "setupMediaPlayer")
        val incomingCallToneUri: Uri = Uri.parse("android.resource://" + androidContext.packageName + "/" + R.raw.call_1_1_ringtone)
        mediaPlayer?.run {
            try {
                setDataSource(androidContext, incomingCallToneUri)
                setRingtoneStreamType(this)
                isLooping = true
                prepare()
            } catch (e: IOException) {
                Log.e(tag, "io exception when setting tone: $incomingCallToneUri")
            } catch (illegalException: IllegalStateException) {
                Log.e(tag, "Illegal state when setting tone:$incomingCallToneUri")
            }
        }
    }

    private fun stopIncomingCallTone() {
        abandonAudioFocusForRingtone()
        incomingCallPlayer?.run {
            stop()
            release()
        }
        incomingCallPlayer = null
        stopVibrate()
    }

    private val focusRequest: AudioFocusRequestCompat by lazy {
        AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN_TRANSIENT)
                .setOnAudioFocusChangeListener(initFocusChangeListener())
                .build()
    }

    private fun initFocusChangeListener(): AudioManager.OnAudioFocusChangeListener {
        return AudioManager.OnAudioFocusChangeListener {
            when (it) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    Log.d(tag, "OnAudioFocusChanged:AUDIOFOCUS_GAIN")
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    Log.d(tag, "OnAudioFocusChanged:AUDIOFOCUS_LOSS")
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    Log.d(tag, "OnAudioFocusChanged:AUDIOFOCUS_LOSS_TRANSIENT")
                }
                else -> {
                    Log.d(tag, "OnAudioFocusChanged, state: $it")
                }
            }
        }
    }

    private fun requestAudioFocusForRingtone() {
        Log.d(tag, "audioFocusGainForRingtone: $audioFocusGainForRingtone")
        if (!audioFocusGainForRingtone) {
            audioManager.mode = AudioManager.MODE_RINGTONE
            AudioManagerCompat.requestAudioFocus(audioManager, focusRequest)
            audioFocusGainForRingtone = true
        }
    }

    private fun requestAudioFocusForCall() {
        if (!audioFocusGainForRingtone) {
            Log.d(tag, "requesting audio focus for calls")
            val result = AudioManagerCompat.requestAudioFocus(audioManager, focusRequest)
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocusGainForRingtone = true
            }
            needAudioFocusForCall = !audioFocusGainForRingtone
        } else {
            needAudioFocusForCall = true
        }
    }

    private fun abandonAudioFocusForRingtone() {
        Log.d(tag,"audioFocusGainForRingtone: $audioFocusGainForRingtone")
        if (audioFocusGainForRingtone) {
            AudioManagerCompat.abandonAudioFocusRequest(audioManager, focusRequest)
            if (needAudioFocusForCall) {
                // in case stop ringer callback is called later than requestAudioFocusForCall(),
                // causing audio focus is not gained by call, need to request audio focus again for call
                Log.d(tag, "request audio focus again for call")
                requestAudioFocusForCall()
            } else {
                // do not need reset audio mode when in a call
                Log.d(tag, "reset audio mode when ringer stopped")
                audioManager.mode = AudioManager.MODE_NORMAL
            }
            audioFocusGainForRingtone = false
        }
    }

    private fun startVibrate() {
        if (shouldVibrate()) {
            Log.d(tag, "start vibrating...")
            val vibratePattern = longArrayOf(0, 1000, 1750)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(vibratePattern, 0)
                val attributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .build()
                vibrator.vibrate(effect, attributes)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(vibratePattern, 0)
            }
        }
    }

    private fun stopVibrate() {
        if (vibrator.hasVibrator()) {
            Log.d(tag,"stop vibrating...")
            vibrator.cancel()
        }
    }

    private fun shouldVibrate(): Boolean {
        val silentMode = audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT
        return vibrator.hasVibrator() && !silentMode
    }

    // Only for incoming call
    private fun setRingtoneStreamType(mediaPlayer: MediaPlayer?) {
        val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()
        mediaPlayer?.setAudioAttributes(attributes)
    }


    private fun stopCallTone() {
        Log.d(tag, "stopCallTone currentPlayingToneId=$currentPlayingToneId")
        if (currentPlayingToneId != 0) {
            if(inCallSoundPlaying)
                inCallSoundPool.stop(currentPlayingToneId)
            if(dtmfSoundPlaying)
                dtmfSoundPool.stop(currentPlayingToneId)
            currentPlayingToneId = 0
        }
    }

    private fun getStreamVolume(stream: Int): Float {
        return audioManager.getStreamVolume(stream).toFloat() / audioManager.getStreamMaxVolume(stream).toFloat()
    }
}