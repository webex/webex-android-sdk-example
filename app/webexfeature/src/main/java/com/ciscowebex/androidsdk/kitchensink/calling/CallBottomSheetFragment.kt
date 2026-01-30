package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ciscowebex.androidsdk.phone.Call
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexViewModel
import com.ciscowebex.androidsdk.phone.MediaOption
import com.ciscowebex.androidsdk.phone.Phone

class CallBottomSheetFragment(val showIncomingCallsClickListener: (Call?) -> Unit,
                              val transcriptionClickListener: (Call?) -> Unit,
                              val toggleWXAClickListener: (Call?) -> Unit,
                              val receivingVideoClickListener: (Call?) -> Unit,
                              val receivingAudioClickListener: (Call?) -> Unit,
                              val receivingSharingClickListener: (Call?) -> Unit,
                              val scalingModeClickListener: (Call?) -> Unit,
                              val virtualBackgroundOptionsClickListener: (Call?) -> Unit,
                              val compositeStreamLayoutClickListener: (Call?) -> Unit,
                              val swapVideoClickListener: (Call?) -> Unit,
                              val forceLandscapeClickListener: (Call?) -> Unit,
                              val cameraOptionsClickListener: (Call?) -> Unit,
                              val multiStreamOptionsClickListener: (Call?) -> Unit,
                              val sendDTMFClickListener: (Call?) -> Unit,
                              val claimHostClickListener: () -> Unit,
                              val showBreakoutSessions: () -> Unit,
                              val closedCaptionOptions: (Call?) -> Unit,
                              val startAudioDumpListener: () -> Unit,
                              val toggleReceiverSpeechEnhancement: () -> Unit, val webexViewModel: WebexViewModel): BottomSheetDialogFragment() {
    companion object {
        val TAG = "CallBottomSheetFragment"
    }

    private lateinit var receivingVideo: TextView
    private lateinit var receivingAudio: TextView
    private lateinit var receivingSharing: TextView
    private lateinit var scalingMode: TextView
    private lateinit var compositeStream: TextView
    private lateinit var multiStreamOptions: TextView
    private lateinit var swapVideo: TextView
    private lateinit var sendingVideoforceLandscape: TextView
    private lateinit var bgOptionsBtn: TextView
    private lateinit var cameraOptions: TextView
    private lateinit var showTranscripts: TextView
    private lateinit var claimHost: TextView
    private lateinit var showIncomingCall: TextView
    private lateinit var enableWXA: TextView
    private lateinit var sendDTMF: TextView
    private lateinit var breakoutSessions: TextView
    private lateinit var closedCaptionOptionsView: TextView
    private lateinit var startAudioDump: TextView
    private lateinit var enableReceiverSpeechEnhancement: TextView
    private lateinit var cancel: TextView
    
    var call: Call? = null
    lateinit var scalingModeValue: Call.VideoRenderMode
    lateinit var compositeLayoutValue: MediaOption.CompositedVideoLayout
    lateinit var streamMode: Phone.VideoStreamMode
    var isSendingVideoForceLandscape: Boolean = false
    var multiStreamNewApproach: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_call_options, container, false)
        
        receivingVideo = view.findViewById(R.id.receivingVideo)
        receivingAudio = view.findViewById(R.id.receivingAudio)
        receivingSharing = view.findViewById(R.id.receivingSharing)
        scalingMode = view.findViewById(R.id.scalingMode)
        compositeStream = view.findViewById(R.id.compositeStream)
        multiStreamOptions = view.findViewById(R.id.multiStreamOptions)
        swapVideo = view.findViewById(R.id.swapVideo)
        sendingVideoforceLandscape = view.findViewById(R.id.sendingVideoforceLandscape)
        bgOptionsBtn = view.findViewById(R.id.bgOptionsBtn)
        cameraOptions = view.findViewById(R.id.cameraOptions)
        showTranscripts = view.findViewById(R.id.showTranscripts)
        claimHost = view.findViewById(R.id.claimHost)
        showIncomingCall = view.findViewById(R.id.showIncomingCall)
        enableWXA = view.findViewById(R.id.enableWXA)
        sendDTMF = view.findViewById(R.id.sendDTMF)
        breakoutSessions = view.findViewById(R.id.breakoutSessions)
        closedCaptionOptionsView = view.findViewById(R.id.closedCaptionOptions)
        startAudioDump = view.findViewById(R.id.startAudioDump)
        enableReceiverSpeechEnhancement = view.findViewById(R.id.enableReceiverSpeechEnhancement)
        cancel = view.findViewById(R.id.cancel)

        return view.apply {

        var receivingVideoText = getString(R.string.receiving_video)
        val receivingVideoStatus = call?.isReceivingVideo() ?: false
        receivingVideoText += if (receivingVideoStatus) {
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
        val receiving = call?.isReceivingAudio() ?: false
        receivingAudioText += if (receiving) {
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
        val sharing = call?.isReceivingSharing() ?: false
        receivingSharingText += if (sharing) {
            " - " + getString(R.string.receiving_on)
        } else {
            " - " + getString(R.string.receiving_off)
        }
        receivingSharing.text = receivingSharingText

        receivingSharing.setOnClickListener {
            dismiss()
            receivingSharingClickListener(call)
        }

        var scalingTypeText = getString(R.string.scaling_mode)

        scalingTypeText += when (scalingModeValue) {
            Call.VideoRenderMode.Fit -> {
                " - " + getString(R.string.scaling_mode_fit)
            }
            Call.VideoRenderMode.CropFill -> {
                " - " + getString(R.string.scaling_mode_cropFill)
            }
            Call.VideoRenderMode.StretchFill -> {
                " - " + getString(R.string.scaling_mode_stretchFill)
            }
            Call.VideoRenderMode.NotSupported -> {
                " - " + getString(R.string.scaling_mode_not_supported)
            }
            else -> {
                " - " + getString(R.string.scaling_mode_unknown)
            }
        }
        scalingMode.text = scalingTypeText
        scalingMode.setOnClickListener {
            dismiss()
            scalingModeClickListener(call)
        }

        var compositeLayoutText = getString(R.string.composite_stream)

        compositeLayoutText += when (compositeLayoutValue) {
            MediaOption.CompositedVideoLayout.FILMSTRIP -> {
                " - " + getString(R.string.composite_stream_filmstrip)
            }
            MediaOption.CompositedVideoLayout.GRID -> {
                " - " + getString(R.string.composite_stream_grid)
            }
            MediaOption.CompositedVideoLayout.SINGLE -> {
                " - " + getString(R.string.composite_stream_single)
            }
            MediaOption.CompositedVideoLayout.NOT_SUPPORTED -> {
                " - " + getString(R.string.composite_stream_not_supported)
            }
            else -> {
                " - " + getString(R.string.composite_stream_unknown)
            }
        }

        if (streamMode == Phone.VideoStreamMode.COMPOSITED) {
            compositeStream.isEnabled = true
            compositeStream.alpha = 1.0f
            multiStreamOptions.visibility = View.GONE
        } else {
            compositeStream.isEnabled = false
            compositeStream.alpha = 0.5f
            compositeLayoutText = getString(R.string.video_stream_mode_multi)
            if (multiStreamNewApproach) {
                multiStreamOptions.visibility = View.VISIBLE
            } else {
                multiStreamOptions.visibility = View.GONE
            }
        }

        compositeStream.text = compositeLayoutText
        compositeStream.setOnClickListener {
            dismiss()
            compositeStreamLayoutClickListener(call)
        }

        swapVideo.setOnClickListener {
            dismiss()
            swapVideoClickListener(call)
        }

        var sendingVideoforceLandscapeText = getString(R.string.sending_video_force_landscape)
        sendingVideoforceLandscapeText += if (isSendingVideoForceLandscape) {
            " - " + getString(R.string.receiving_on)
        } else {
            " - " + getString(R.string.receiving_off)
        }
        sendingVideoforceLandscape.text = sendingVideoforceLandscapeText
        sendingVideoforceLandscape.setOnClickListener {
            dismiss()
            forceLandscapeClickListener(call)
        }

        bgOptionsBtn.setOnClickListener {
            dismiss()
            virtualBackgroundOptionsClickListener(call)
        }

        cameraOptions.setOnClickListener {
            dismiss()
            cameraOptionsClickListener(call)
        }

        showTranscripts.setOnClickListener {
            dismiss()
            transcriptionClickListener(call)
        }

        claimHost.setOnClickListener {
            dismiss()
            claimHostClickListener()
        }

        showIncomingCall.setOnClickListener {
            dismiss()
            showIncomingCallsClickListener(call)
        }

        enableWXA.text = if (call?.getWXA()?.isEnabled() == true) "Disable Webex Assistant" else "Enable Webex Assistant"
        enableWXA.setOnClickListener {
            dismiss()
            toggleWXAClickListener(call)
        }

        val showDTMFOption = call?.isSendingDTMFEnabled() ?: false

        if (showDTMFOption) {
            sendDTMF.visibility = View.VISIBLE
        } else {
            sendDTMF.visibility = View.GONE
        }
        sendDTMF.setOnClickListener {
            dismiss()
            sendDTMFClickListener(call)
        }

        multiStreamOptions.setOnClickListener {
            dismiss()
            multiStreamOptionsClickListener(call)
        }

        breakoutSessions.setOnClickListener {
            dismiss()
            showBreakoutSessions()
        }

        closedCaptionOptionsView.setOnClickListener {
            dismiss()
            closedCaptionOptions(call)
        }

        startAudioDump.setOnClickListener {
            dismiss()
            startAudioDumpListener()
        }

        enableReceiverSpeechEnhancement.text = if (webexViewModel.isReceiverSpeechEnhancementEnabled()) "Receiver Speech Enhancement: ON" else "Receiver Speech Enhancement: OFF"

        enableReceiverSpeechEnhancement.setOnClickListener {
            dismiss()
            toggleReceiverSpeechEnhancement()
        }

        cancel.setOnClickListener { dismiss() }
        }
    }

    fun isDTMFOptionEnabled() : Boolean {
        if (::sendDTMF.isInitialized) {
            if (sendDTMF.visibility == View.VISIBLE) {
                return true
            }
        }

        return false
    }
}