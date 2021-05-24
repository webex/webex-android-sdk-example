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
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import com.ciscowebex.androidsdk.kitchensink.R
import org.koin.core.KoinComponent
import java.io.IOException


open class RingerManager(private val androidContext: Context): KoinComponent {
    enum class RingerType {
        Incoming,
        Outgoing
    }

    private val tag = "RingerManager"

    private var vibrator: Vibrator = androidContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private var audioManager: AudioManager = androidContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val inCallSoundPool: SoundPool = SoundPool.Builder()
                                                .setMaxStreams(1)
                                                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).build())
                                                .build()
    private var incomingCallPlayer: MediaPlayer? = null

    private var currentPlayingToneId = 0
    private var audioFocusGainForRingtone = false
    private var needAudioFocusForCall = false
    private val ringerLock = Any()
    private var outGoingCallId = 0

    init {
        loadInCallTones()
    }

    private fun loadInCallTones() {
        outGoingCallId = inCallSoundPool.load(androidContext, R.raw.ring_back, 1)
    }

    private fun playOutgoingCallTone() {
        if (currentPlayingToneId != 0) return
        val streamVolume = getStreamVolume(AudioManager.STREAM_VOICE_CALL)
        currentPlayingToneId = inCallSoundPool.play(outGoingCallId, streamVolume, streamVolume, 1, -1, 1.toFloat())
        Log.d(tag, "currentPlayingToneId=$currentPlayingToneId")
    }

    fun startRinger(type: RingerType) {
        Log.d(tag, "startRinger type: $type")
        synchronized(ringerLock) {
            handleStartRinger(type)
        }
    }

    fun stopRinger(type: RingerType) {
        Log.d(tag, "stopRinger type: $type")
        synchronized(ringerLock) {
            handleStopRinger(type)
        }
    }

    private fun handleStartRinger(type: RingerType) {
        Log.d(tag, "handleStartRinger type: $type")
        when (type) {
            RingerType.Incoming -> playIncomingTone()
            RingerType.Outgoing -> playOutgoingCallTone()
        }
    }

    private fun handleStopRinger(type: RingerType) {
        Log.d(tag, "handleStopRinger type: $type")
        when (type) {
            RingerType.Incoming -> stopIncomingTone()
            RingerType.Outgoing -> stopCallTone()
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
        val incomingCallToneUri: Uri = Uri.parse("android.resource://" + androidContext.packageName + "/" + R.raw.notification_oneone_call)
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
            inCallSoundPool.stop(currentPlayingToneId)
            currentPlayingToneId = 0
        }
    }

    private fun getStreamVolume(stream: Int): Float {
        return audioManager.getStreamVolume(stream).toFloat() / audioManager.getStreamMaxVolume(stream).toFloat()
    }
}