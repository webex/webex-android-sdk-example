package com.ciscowebex.androidsdk.kitchensink.calling

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Pair
import android.util.Rational
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.WebexError
import com.ciscowebex.androidsdk.kitchensink.BuildConfig
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.WebexViewModel
import com.ciscowebex.androidsdk.kitchensink.annotation.AnnotationRenderer
import com.ciscowebex.androidsdk.kitchensink.auth.LoginActivity
import com.ciscowebex.androidsdk.kitchensink.calling.captions.ClosedCaptionsController
import com.ciscowebex.androidsdk.kitchensink.calling.captions.ClosedCaptionsViewModel
import com.ciscowebex.androidsdk.kitchensink.calling.captions.LanguageData
import com.ciscowebex.androidsdk.kitchensink.calling.captions.REQUEST_CODE_SPOKEN_LANGUAGE
import com.ciscowebex.androidsdk.kitchensink.calling.captions.REQUEST_CODE_TRANSLATION_LANGUAGE
import com.ciscowebex.androidsdk.kitchensink.calling.participants.ParticipantsFragment
import com.ciscowebex.androidsdk.kitchensink.calling.transcription.TranscriptionsDialogFragment
import com.ciscowebex.androidsdk.kitchensink.databinding.DialogEnterMeetingPinBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.FragmentCallControlsBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.ScreenshareconfigBinding
import com.ciscowebex.androidsdk.kitchensink.person.PersonViewModel
import com.ciscowebex.androidsdk.kitchensink.setup.BackgroundOptionsBottomSheetFragment
import com.ciscowebex.androidsdk.kitchensink.utils.AudioManagerUtils
import com.ciscowebex.androidsdk.kitchensink.utils.CallObjectStorage
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.Constants.Intent.CLOSED_CAPTION_LANGUAGE_ITEM
import com.ciscowebex.androidsdk.kitchensink.utils.extensions.toast
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogForDTMF
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogWithMessage
import com.ciscowebex.androidsdk.kitchensink.utils.UIUtils
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogForHostKey
import com.ciscowebex.androidsdk.message.LocalFile
import com.ciscowebex.androidsdk.phone.AuxStream
import com.ciscowebex.androidsdk.phone.Call
import com.ciscowebex.androidsdk.phone.CallAssociationType
import com.ciscowebex.androidsdk.phone.CallMembership
import com.ciscowebex.androidsdk.phone.CallObserver
import com.ciscowebex.androidsdk.phone.CallSchedule
import com.ciscowebex.androidsdk.phone.MediaOption
import com.ciscowebex.androidsdk.phone.MediaRenderView
import com.ciscowebex.androidsdk.phone.MultiStreamObserver
import com.ciscowebex.androidsdk.phone.MediaStreamQuality
import com.ciscowebex.androidsdk.phone.MediaStreamChangeEventType
import com.ciscowebex.androidsdk.phone.MediaStreamChangeEventInfo
import com.ciscowebex.androidsdk.phone.MediaStreamType
import com.ciscowebex.androidsdk.phone.Phone
import com.ciscowebex.androidsdk.phone.VirtualBackground
import com.ciscowebex.androidsdk.phone.Breakout
import com.ciscowebex.androidsdk.phone.BreakoutSession
import com.ciscowebex.androidsdk.phone.CompanionMode
import com.ciscowebex.androidsdk.phone.ReceivingNoiseInfo
import com.ciscowebex.androidsdk.phone.RemoteShareCallback
import com.ciscowebex.androidsdk.phone.ShareConfig
import com.ciscowebex.androidsdk.phone.annotation.LiveAnnotationsPolicy
import com.ciscowebex.androidsdk.phone.closedCaptions.CaptionItem
import com.ciscowebex.androidsdk.phone.closedCaptions.ClosedCaptionsInfo
import com.ciscowebex.androidsdk.kitchensink.utils.GlobalExceptionHandler
import com.ciscowebex.androidsdk.kitchensink.CallManagementService
import com.ciscowebex.androidsdk.phone.AudioDumpResult
import org.koin.android.ext.android.inject
import com.ciscowebex.androidsdk.utils.internal.MimeUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CallControlsFragment : Fragment(), OnClickListener, CallObserverInterface, RemoteShareCallback {
    private val TAG = "CallControlsFragment"
    val webexViewModel: WebexViewModel by viewModel()
    val captionsViewModel: ClosedCaptionsViewModel by viewModel()
    private lateinit var binding: FragmentCallControlsBinding
    private var callFailed = false
    private var isIncomingActivity = false
    private var callingActivity = 0
    private var audioManagerUtils: AudioManagerUtils? = null
    val SHARE_SCREEN_FOREGROUND_SERVICE_NOTIFICATION_ID = 0xabc61
    private val ringerManager: RingerManager by inject()
    private val personViewModel : PersonViewModel by inject()
    private lateinit var callOptionsBottomSheetFragment: CallBottomSheetFragment
    private lateinit var incomingCallBottomSheetFragment: IncomingCallBottomSheetFragment
    private lateinit var cameraOptionsBottomSheetFragment: CameraOptionsBottomSheetFragment
    private lateinit var cameraOptionsDataBottomSheetFragment: CameraOptionsDataBottomSheetFragment
    private lateinit var multiStreamOptionsBottomSheetFragment: MultiStreamOptionsBottomSheetFragment
    private lateinit var multiStreamDataOptionsBottomSheetFragment: MultiStreamDataOptionsBottomSheetFragment
    private lateinit var mediaStreamBottomSheetFragment: MediaStreamBottomSheetFragment
    private lateinit var photoViewerBottomSheetFragment: PhotoViewerBottomSheetFragment
    private lateinit var breakoutSessionBottomSheetFragment: BreakoutSessionsBottomSheetFragment
    private lateinit var switchAudioBottomSheetFragment: SwitchAudioBottomSheetFragment
    private lateinit var incomingInfoAdapter: IncomingCallBottomSheetFragment.IncomingInfoAdapter
    private lateinit var breakoutSessionsAdapter: BreakoutSessionsBottomSheetFragment.BreakoutSessionsAdapter
    private lateinit var captionsController: ClosedCaptionsController
    private val mAuxStreamViewMap: HashMap<View, AuxStreamViewHolder> = HashMap()
    private var callerId: String = ""
    var bottomSheetFragment: BackgroundOptionsBottomSheetFragment? = null
    var onCallActionListener: OnCallActionListener? = null
    private var breakoutSessions : List<BreakoutSession> = emptyList()
    private var breakout: Breakout? = null
    private var dialType = DialType.NONE
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private lateinit var passwordDialogBinding: DialogEnterMeetingPinBinding
    private lateinit var passwordDialog : Dialog
    private var isInPipMode = false
    private var screenShareOptionsDialog: AlertDialog? = null
    private lateinit var annotationPermissionDialog: AlertDialog
    private lateinit var moveMeeting: CompanionMode

    // Is true when trying to join a Breakout Session, and becomes false when successfully joined or error occurs
    // Call onDisconnected is fired when user is the last one to leave main session and tries to join a breakout session.
    private var attemptingToJoinABreakoutSession = false
    private val mHandler = Handler(Looper.getMainLooper())
    private var callManagementServiceIntent: Intent? = null

    interface OnCallActionListener {
        fun onEndAndAnswer(currentCallId: String, newCallId: String, handler: CompletionHandler<Boolean>)
    }

    enum class NetworkStatus {
        PoorUplink,
        PoorDownlink,
        Good,
        NoNetwork
    }

    var currentNetworkStatus = NetworkStatus.Good

    enum class ShareButtonState {
        OFF,
        ON,
        DISABLED
    }

    enum class DialType {
        NONE,
        HOST,
        OTHERS
    }

    class AuxStreamViewHolder(var item: View) {
        var mediaRenderView: MediaRenderView = item.findViewById(R.id.view_video)
        var textView: TextView = item.findViewById(R.id.name)
        var audioState: ImageView = item.findViewById(R.id.iv_audio_state)
        var viewAvatar: ImageView = item.findViewById(R.id.view_avatar)
        var remoteBorder: RelativeLayout = item.findViewById(R.id.remote_border)
        var moreOption: ImageButton = item.findViewById(R.id.ib_more_option)
        var streamType: MediaStreamType = MediaStreamType.Unknown
        var parentLayout: RelativeLayout = item.findViewById(R.id.parentLayout)
        var pinStreamImageView: ImageView = item.findViewById(R.id.iv_pinstream)
        var personID: String? = null
    }

    companion object {
        const val REQUEST_CODE = 1212
        const val REQUEST_CODE_BLINDTRANSFER = 1213
        const val TAG = "CallControlsFragment"
        private const val CALLER_ID = "callerId"
        const val MEDIA_PROJECTION_REQUEST = 1
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onCallActionListener = activity as OnCallActionListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil.inflate<FragmentCallControlsBinding>(LayoutInflater.from(context),
                R.layout.fragment_call_controls, container, false).also { binding = it }.apply {
            Log.d(TAG, "CallControlsFragment onCreateView webexViewModel: $webexViewModel")

            setUpViews(getArguments())
            observerCallLiveData()
            initAudioManager()
//        Enable below line to check is USM is enabled
//        Toast.makeText(requireActivity().applicationContext, "isUSMEnabled ${webexViewModel.webex.phone.isUnifiedSpaceMeetingEnabled()}", Toast.LENGTH_LONG).show()

        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.screenShareView.setRemoteShareCallback(this)
    }

    private fun initAudioManager() {
        audioManagerUtils = AudioManagerUtils(requireContext())
    }

    override fun onResume() {
        Log.d(TAG, "CallControlsFragment onResume")
        super.onResume()
        checkIsOnHold()
        webexViewModel.currentCallId?.let {
            onVideoStreamingChanged(it)
        }
        webexViewModel.enableStreams()
    }

    fun getCurrentActiveCallId() : String?{
        return webexViewModel.currentCallId
    }

    private fun checkIsOnHold() {
        val isOnHold = webexViewModel.currentCallId?.let { webexViewModel.isOnHold(it) }
        binding.ibHoldCall.isSelected = isOnHold ?: false
    }

    private fun getMediaOption(isModerator: Boolean = false, pin: String = "", captcha: String = "", captchaId: String = "", companionMode: CompanionMode = CompanionMode.None): MediaOption {
        val mediaOption: MediaOption
        if (webexViewModel.callCapability == WebexRepository.CallCap.Audio_Only) {
            mediaOption = MediaOption.audioOnly()
        } else {
            mediaOption = MediaOption.audioVideoSharing(Pair(binding.localView, binding.remoteView), binding.screenShareView)
           // MediaOption.audioVideo(Pair(binding.localView, binding.remoteView))
        }
        mediaOption.setModerator(isModerator)
        mediaOption.setPin(pin)
        mediaOption.setCaptchaCode(captcha)
        mediaOption.setCaptchaId(captchaId)
        mediaOption.setCompanionMode(companionMode)
        return mediaOption
    }

    fun dialOutgoingCall(callerId: String, isModerator: Boolean = false, pin: String = "", captcha: String = "", captchaId: String = "", isCucmOrWxcCall: Boolean, moveMeeting: CompanionMode = CompanionMode.None) {
        Log.d(TAG, "dialOutgoingCall")
        this.callerId = callerId
        if(isCucmOrWxcCall) {
            webexViewModel.dialPhoneNumber(callerId, getMediaOption(isModerator, pin, captcha, captchaId, moveMeeting))
        }
        else {
            webexViewModel.dial(callerId, getMediaOption(isModerator, pin, captcha, captchaId, moveMeeting))
        }
    }

    private fun checkLicenseAPIs() {
        val license = webexViewModel.getVideoCodecLicense()
        Log.d(TAG, "checkLicenseAPIs license $license")
        val URL = webexViewModel.getVideoCodecLicenseURL()
        Log.d(TAG, "checkLicenseAPIs license URL $URL")
        webexViewModel.requestVideoCodecActivation(AlertDialog.Builder(activity))
    }

    override fun onConnected(call: Call?) {
        Log.d(TAG, "CallObserver onConnected callId: ${call?.getCallId()}, hasAnyoneJoined: ${webexViewModel.hasAnyoneJoined()}, " +
                "correlationId: ${call?.getCorrelationId()}, "+
                "externalTrackingId: ${call?.getExternalTrackingId()}, "+
                "isMeeting: ${webexViewModel.isMeeting()}, " +
                "isPmr: ${webexViewModel.isPmr()}, " +
                "isSelfCreator: ${webexViewModel.isSelfCreator()}, " +
                "isSpaceMeeting: ${webexViewModel.isSpaceMeeting()}, "+
                "isScheduledMeeting: ${webexViewModel.isScheduledMeeting()}")
        // Setting exception handler before making any call.
        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler())
        onCallConnected(call?.getCallId().orEmpty(), call?.isCUCMCall() ?: false, call?.isWebexCallingOrWebexForBroadworks() ?: false)
        webexViewModel.sendFeedback(call?.getCallId().orEmpty(), 5, "Testing Comments SDK-v3")
        webexViewModel.setShareMaxCaptureFPSSetting(30)

        val exposureDuration = webexViewModel.getCameraExposureDuration()
        val exposureISO = webexViewModel.getCameraExposureISO()
        val exposureBias = webexViewModel.getCameraExposureTargetBias()

        Log.d(TAG, "CallObserver camera settings: " +
                "getCameraFlashMode: ${webexViewModel.getCameraFlashMode()} " +
                "getCameraTorchMode: ${webexViewModel.getCameraTorchMode()} " +
                "exposureDuration min: ${exposureDuration?.min}, max: ${exposureDuration?.max}, current: ${exposureDuration?.current} " +
                "exposureISO min: ${exposureISO?.min}, max: ${exposureISO?.max}, current: ${exposureISO?.current} " +
                "exposureBias min: ${exposureBias?.min}, max: ${exposureBias?.max}, current: ${exposureBias?.current} "+
                "zoom Factor: ${webexViewModel.getVideoZoomFactor()} ")

        if (incomingCallBottomSheetFragment.isVisible) {
            incomingCallBottomSheetFragment.dismiss()
        }
    }

    override fun onStartRinging(call: Call?, ringerType: Call.RingerType) {
        Log.d(TAG, "startRinger: $ringerType")
        // Start call monitoring service when dialing a call
        startCallMonitoringForegroundService()
        ringerManager.startRinger(ringerType)
    }

    override fun onStopRinging(call: Call?, ringerType: Call.RingerType) {
        Log.d(tag, "stopRinger: $ringerType")
        ringerManager.stopRinger(ringerType)
    }

    override fun onWaiting(call: Call?) {
        Log.d(TAG, "CallObserver OnWaiting : " + call?.getCallId())
    }

    override fun onDisconnected(call: Call?, event: CallObserver.CallDisconnectedEvent?) {
        Log.d(TAG, "CallObserver onDisconnected : " + call?.getCallId())
        Thread.setDefaultUncaughtExceptionHandler(null)
        callManagementServiceIntent?.let {
            activity?.stopService(it)
            callManagementServiceIntent = null
        }
        var callFailed = false
        var callEnded = false
        var localClose = false

        var failedError: WebexError<Any>? = null
        event?.let { _event ->
            val _call = _event.getCall()
            when (_event) {
                is CallObserver.LocalLeft -> {
                    Log.d(TAG, "CallObserver LocalLeft")
                    localClose = true
                }
                is CallObserver.LocalDecline -> {
                    Log.d(TAG, "CallObserver LocalDecline")
                }
                is CallObserver.LocalCancel -> {
                    Log.d(TAG, "CallObserver LocalCancel")
                    localClose = true
                }
                is CallObserver.RemoteLeft -> {
                    Log.d(TAG, "CallObserver RemoteLeft")
                }
                is CallObserver.RemoteDecline -> {
                    Log.d(TAG, "CallObserver RemoteDecline")
                }
                is CallObserver.RemoteCancel -> {
                    Log.d(TAG, "CallObserver RemoteCancel")
                }
                is CallObserver.OtherConnected -> {
                    Log.d(TAG, "CallObserver OtherConnected")
                    callEnded = true
                }
                is CallObserver.OtherDeclined -> {
                    Log.d(TAG, "CallObserver OtherDeclined")
                }
                is CallObserver.CallErrorEvent -> {
                    Log.d(TAG, "CallObserver CallErrorEvent")
                    failedError = _event.getError()
                    callFailed = true
                }
                is CallObserver.CallEnded -> {
                    Log.d(TAG, "CallObserver CallEnded")
                    callEnded = true
                }
                else -> {}
            }
        }

        when {
            callFailed -> {
                onCallFailed(call?.getCallId().orEmpty(), failedError)
            }
            callEnded -> {
                onCallTerminated(call?.getCallId().orEmpty())
            }
            else -> {
                val schedules = call?.getSchedules()
                if (localClose && !attemptingToJoinABreakoutSession) {
                    if (schedules == null && !isIncomingActivity) {
                        /**
                         * Taken care of space call when local left
                         */
                        onCallTerminated(call?.getCallId().orEmpty())
                    } else {
                        /*
                        * Below line takes care of ending the call if it is scheduled call and local leaves or ends the meeting
                        * This fixes white screen issue when call is started from calendar meeting fragment
                        */
                        if (!isIncomingActivity) {
                            onCallTerminated(call?.getCallId().orEmpty())
                        } else
                            onCallDisconnected(call)
                    }
                }
            }
        }

        (activity as CallActivity).onDisconnected(call, event)
    }

    override fun onInfoChanged(call: Call?) {
        Log.d(TAG, "CallObserver onInfoChanged : " + call?.getCallId())

        mHandler.post {
            call?.let { _call ->
                binding.ibHoldCall.isSelected = _call.isOnHold()
                Log.d(TAG, "CallObserver onInfoChanged isSendingDTMFEnabled : " + _call.isSendingDTMFEnabled())

                if (_call.isSendingDTMFEnabled() && !callOptionsBottomSheetFragment.isDTMFOptionEnabled()) {
                    Log.d(TAG, "CallObserver onInfoChanged DTMF Enabled")
                    Toast.makeText(activity, "DTMF Option Enabled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onMediaChanged(call: Call?, event: CallObserver.MediaChangedEvent?) {
        Log.d(TAG, "CallObserver OnMediaChanged")

        event?.let { _event ->
            val call = _event.getCall()
            when (_event) {
                is CallObserver.RemoteSendingVideoEvent -> {
                    Log.d(TAG, "CallObserver OnMediaChanged RemoteSendingVideoEvent: ${_event.isSending()}")
                    webexViewModel.isRemoteVideoMuted = !_event.isSending()
                    onVideoStreamingChanged(call?.getCallId().orEmpty())
                }
                is CallObserver.SendingVideo -> {
                    Log.d(TAG, "CallObserver OnMediaChanged SendingVideo: ${_event.isSending()}")
                    webexViewModel.isLocalVideoMuted = !_event.isSending()
                    onVideoStreamingChanged(call?.getCallId().orEmpty())
                }
                is CallObserver.ReceivingVideo -> {
                    Log.d(TAG, "CallObserver OnMediaChanged ReceivingVideo: ${_event.isReceiving()}")
                    webexViewModel.isRemoteVideoMuted = !_event.isReceiving()
                    onVideoStreamingChanged(call?.getCallId().orEmpty())
                }
                is CallObserver.RemoteSendingAudioEvent -> {
                    Log.d(TAG, "CallObserver OnMediaChanged RemoteSendingAudioEvent: ${_event.isSending()}")
                    audioEventChanged(null, call, null, _event.isSending())
                }
                is CallObserver.SendingAudio -> {
                    Log.d(TAG, "CallObserver OnMediaChanged SendingAudio: ${_event.isSending()}")
                    audioEventChanged(null, call, _event.isSending())
                }
                is CallObserver.ReceivingAudio -> {
                    Log.d(TAG, "CallObserver OnMediaChanged ReceivingAudio: ${_event.isReceiving()}")
                    audioEventChanged(null, call, null, _event.isReceiving())
                }
                is CallObserver.RemoteSendingSharingEvent -> {
                    Log.d(TAG, "CallObserver OnMediaChanged RemoteSendingSharingEvent: ${_event.isSending()}")
                    onScreenShareStateChanged(call?.getCallId().orEmpty(), call?.getScreenShareLabel().orEmpty())
                    onScreenShareVideoStreamInUseChanged(call?.getCallId().orEmpty(), _event.isSending())
                }
                is CallObserver.SendingSharingEvent -> {
                    Log.d(TAG, "CallObserver OnMediaChanged SendingSharingEvent: ${_event.isSending()}")
                    onScreenShareStateChanged(call?.getCallId().orEmpty(), call?.getScreenShareLabel().orEmpty())
                    onScreenShareVideoStreamInUseChanged(call?.getCallId().orEmpty())
                }
                is CallObserver.ReceivingSharing -> {
                    Log.d(TAG, "CallObserver OnMediaChanged ReceivingSharing: ${_event.isReceiving()}")
                    onScreenShareStateChanged(call?.getCallId().orEmpty(), call?.getScreenShareLabel().orEmpty())
                    onScreenShareVideoStreamInUseChanged(call?.getCallId().orEmpty())
                }
                is CallObserver.CameraSwitched -> {
                    Log.d(TAG, "CallObserver CameraSwitched")
                }
                is CallObserver.LocalVideoViewSizeChanged -> {
                    Log.d(TAG, "CallObserver LocalVideoViewSizeChanged")
                }
                is CallObserver.RemoteVideoViewSizeChanged -> {
                    Log.d(TAG, "CallObserver RemoteVideoViewSizeChanged")
                }
                is CallObserver.LocalSharingViewSizeChanged -> {
                    Log.d(TAG, "CallObserver LocalSharingViewSizeChanged")
                }
                is CallObserver.RemoteSharingViewSizeChanged -> {
                    Log.d(TAG, "CallObserver RemoteSharingViewSizeChanged")
                }
                is CallObserver.ActiveSpeakerChangedEvent -> {
                    Log.d(TAG, "CallObserver ActiveSpeakerChangedEvent from: ${_event.from()}, To: ${_event.to()}")
                }
                is CallObserver.MediaStreamAvailabilityEvent -> {
                    onMediaStreamAvailabilityEvent(call?.getCallId().orEmpty(), _event)
                }
                else -> {}
            }
        }
    }

    private fun onMediaStreamAvailabilityEvent(callId: String, event: CallObserver.MediaStreamAvailabilityEvent) {
        Log.d(TAG, "CallControlsFragment onMediaStreamAvailabilityEvent callerId: $callId")

        if ((webexViewModel.currentCallId != callId) || (!webexViewModel.multistreamNewApproach)) {
            return
        }

        mHandler.post {
            Log.d(TAG, "CallObserver OnMediaChanged MediaStreamAvailabilityEvent: ${event.isAvailable()}")
            val list = webexViewModel.getMediaStreams()
            list?.let { mediaList ->
                Log.d(TAG, "CallObserver OnMediaChanged MediaStreamAvailabilityEvent list size : ${mediaList.size}")
            }

            if (event.isAvailable()) {
                if (event.getStream()?.getStreamType() == MediaStreamType.Stream1) {
                    //remote media stream
                    onVideoStreamingChanged(webexViewModel.currentCallId.toString())
                    setRemoteVideoInformation(event.getStream()?.getPerson()?.getDisplayName().orEmpty(), !(event.getStream()?.getPerson()?.isSendingAudio() ?: true))
                } else {
                    Log.d(TAG, "CallObserver OnMediaChanged MediaStreamAvailabilityEvent personID: ${event.getStream()?.getPerson()?.getPersonId()}," +
                            "personName: ${event.getStream()?.getPerson()?.getDisplayName()}")
                    val view = getMediaStreamView(true, event.getStream()?.getStreamType() ?: MediaStreamType.Unknown,
                        event.getStream()?.getPerson()?.getPersonId())
                    event.getStream()?.setRenderView(view)
                    val auxStreamViewHolder = mAuxStreamViewMap[view]
                    auxStreamViewHolder?.let {
                        binding.viewAuxVideos.addView(it.item)
                        val membership = event.getStream()?.getPerson()
                        Log.d(tag, "CallObserver OnMediaChanged MediaStreamAvailabilityEvent successful membership: " + membership?.getDisplayName())
                        it.textView.text = membership?.getDisplayName()
                        val muted = !(membership?.isSendingAudio() ?: true)

                        if (muted) {
                            it.audioState.setImageResource(R.drawable.ic_microphone_muted_bold)
                        } else {
                            it.audioState.setImageResource(R.drawable.ic_microphone_36)
                        }

                        if (membership?.isSendingVideo() == true) {
                            auxStreamViewHolder.viewAvatar.visibility = View.GONE
                        } else {
                            auxStreamViewHolder.viewAvatar.visibility = View.VISIBLE
                        }
                    }
                }

                event.getStream()?.setOnMediaStreamInfoChanged { type, info ->
                    mediaStreamInfoChangedListener(type, info)
                }
            } else {
                mAuxStreamViewMap.containsKey(event.getStream()?.getRenderView()).let {
                    val auxStreamViewHolder = mAuxStreamViewMap[event.getStream()?.getRenderView()]
                    mAuxStreamViewMap.remove(event.getStream()?.getRenderView())
                    binding.viewAuxVideos.removeView(auxStreamViewHolder?.item)
                }
            }
        }
    }

    private fun mediaStreamInfoChangedListener(type: MediaStreamChangeEventType, info: MediaStreamChangeEventInfo) {
        mHandler.post {
            Log.d(tag, "CallObserver OnMediaChanged setOnMediaStreamInfoChanged type: $type  name: ${info.getStream().getPerson()?.getDisplayName()}")
            when (type) {
                MediaStreamChangeEventType.Video -> {
                    val auxStreamViewHolder = mAuxStreamViewMap[info.getStream().getRenderView()]

                    if (auxStreamViewHolder != null) {
                        Log.d(tag, "CallObserver OnMediaChanged setOnMediaStreamInfoChanged isSendingVideo: ${info.getStream().getPerson()?.isSendingVideo()}")
                        if (info.getStream().getPerson()?.isSendingVideo() == true) {
                            auxStreamViewHolder.viewAvatar.visibility = View.GONE
                            auxStreamViewHolder.mediaRenderView.visibility = View.VISIBLE
                            info.getStream().setRenderView(auxStreamViewHolder.mediaRenderView)
                        } else {
                            val membership = info.getStream().getPerson()
                            membership?.let { member ->
                                if (member.getPersonId().isNotEmpty()) {
                                    Log.d(tag, "CallObserver OnMediaChanged setOnMediaStreamInfoChanged viewAvatar visible")
                                    auxStreamViewHolder.viewAvatar.visibility = View.VISIBLE
                                    auxStreamViewHolder.mediaRenderView.visibility = View.GONE
                                }
                            }
                        }
                    }
                }

                MediaStreamChangeEventType.Audio -> {
                    val auxStreamViewHolder = mAuxStreamViewMap[info.getStream().getRenderView()]

                    if (auxStreamViewHolder != null) {
                        Log.d(tag, "CallObserver OnMediaChanged setOnMediaStreamInfoChanged isSendingAudio: " + info.getStream().getPerson()?.isSendingAudio())
                        val membership = info.getStream().getPerson()
                        membership?.let { member ->
                            if (member.getPersonId().isNotEmpty()) {
                                if (member.isSendingAudio()) {
                                    auxStreamViewHolder.audioState.setImageResource(R.drawable.ic_microphone_36)
                                } else {
                                    auxStreamViewHolder.audioState.setImageResource(R.drawable.ic_microphone_muted_bold)
                                }
                            }
                        }
                    } else {
                        if (info.getStream().getStreamType() == MediaStreamType.Stream1) {
                            setRemoteVideoInformation(info.getStream().getPerson()?.getDisplayName().orEmpty(), !(info.getStream().getPerson()?.isSendingAudio() ?: true))
                        }
                    }
                }

                MediaStreamChangeEventType.Size -> {
                    Log.d(tag, "CallObserver OnMediaChanged setOnMediaStreamInfoChanged width: " + info.getStream().getSize().width +
                            " height: " + info.getStream().getSize().height)
                }

                MediaStreamChangeEventType.PinState -> {
                    val auxStreamViewHolder = mAuxStreamViewMap[info.getStream().getRenderView()]

                    if (auxStreamViewHolder != null) {
                        Log.d(tag, "CallObserver OnMediaChanged setOnMediaStreamInfoChanged PinState " +
                                "isPinned: ${info.getStream().isPinned()} personID: ${info.getStream().getPerson()?.getPersonId()}")
                        val membership = info.getStream().getPerson()
                        membership?.let { member ->
                            if (member.getPersonId().isNotEmpty()) {
                                Log.d(tag, "CallObserver OnMediaChanged setOnMediaStreamInfoChanged PinState getPersonId not empty")
                                    if (isMediaStreamAlreadyPinned(member.getPersonId(), info.getStream().getStreamType())) {
                                    Log.d(tag, "CallObserver OnMediaChanged setOnMediaStreamInfoChanged PinState isPinned")
                                    auxStreamViewHolder.pinStreamImageView.visibility = View.VISIBLE
                                    auxStreamViewHolder.parentLayout.background = ContextCompat.getDrawable(requireActivity(), R.drawable.border_category_c)
                                } else {
                                    auxStreamViewHolder.pinStreamImageView.visibility = View.GONE
                                    auxStreamViewHolder.parentLayout.background = ContextCompat.getDrawable(requireActivity(), R.drawable.border_category_b)
                                }
                            }
                        }
                    }
                }

                MediaStreamChangeEventType.Membership -> {
                    Log.d(tag, "CallObserver OnMediaChanged setOnMediaStreamInfoChanged Membership from: ${info.fromMembership().getPersonId()} to: ${info.toMembership().getPersonId()}")
                    val auxStreamViewHolder = mAuxStreamViewMap[info.getStream().getRenderView()]
                    val membership = info.getStream().getPerson()
                    membership?.let { member ->
                        Log.d(tag, "CallObserver OnMediaChanged setOnMediaStreamInfoChanged name: " + member.getDisplayName())
                        auxStreamViewHolder?.viewAvatar?.visibility = if (member.isSendingVideo()) View.GONE else View.VISIBLE
                        auxStreamViewHolder?.textView?.text = member.getDisplayName()
                        auxStreamViewHolder?.personID = member.getPersonId()
                        auxStreamViewHolder?.streamType = info.getStream().getStreamType()
                        if (isMediaStreamAlreadyPinned(member.getPersonId(), auxStreamViewHolder?.streamType)) {
                            auxStreamViewHolder?.pinStreamImageView?.visibility = View.VISIBLE
                            auxStreamViewHolder?.parentLayout?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.border_category_c)
                        } else {
                            auxStreamViewHolder?.pinStreamImageView?.visibility = View.GONE
                            auxStreamViewHolder?.parentLayout?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.border_category_b)
                        }

                        if (info.getStream().getStreamType() == MediaStreamType.Stream1) {
                            setRemoteVideoInformation(member.getDisplayName().orEmpty(), !(member.isSendingAudio()))
                        }
                    }
                }

                else -> {}
            }
        }
    }

    override fun onCallMembershipChanged(call: Call?, event: CallObserver.CallMembershipChangedEvent?) {
        Log.d(TAG, "CallObserver OnCallMembershipEvent")

        event?.let { membershipEvent ->
            val call = membershipEvent.getCall()
            val callMembership = membershipEvent.getCallMembership()
            when (membershipEvent) {
                is CallObserver.MembershipJoinedEvent -> {
                    Log.d(TAG, "CallObserver OnCallMembershipEvent MembershipJoinedEvent")
                    audioEventChanged(callMembership, call)
                }
                is CallObserver.MembershipLeftEvent -> {
                    Log.d(TAG, "CallObserver OnCallMembershipEvent MembershipLeftEvent")
                }
                is CallObserver.MembershipDeclinedEvent -> {
                    Log.d(TAG, "CallObserver OnCallMembershipEvent MembershipDeclinedEvent")
                }
                is CallObserver.MembershipSendingVideoEvent -> {
                    Log.d(TAG, "CallObserver OnCallMembershipEvent MembershipSendingVideoEvent")
                }
                is CallObserver.MembershipSendingAudioEvent -> {
                    Log.d(TAG, "CallObserver OnCallMembershipEvent MembershipSendingAudioEvent")
                }
                is CallObserver.MembershipSendingSharingEvent -> {
                    Log.d(TAG, "CallObserver OnCallMembershipEvent MembershipSendingSharingEvent")
                }
                is CallObserver.MembershipWaitingEvent -> {
                    Log.d(TAG, "CallObserver OnCallMembershipEvent MembershipWaitingEvent")
                }
                is CallObserver.MembershipAudioMutedControlledEvent -> {
                    Log.d(TAG, "CallObserver OnCallMembershipEvent MembershipAudioMutedControlledEvent")
                    audioEventChanged(callMembership, call)
                }
                else -> {}
            }
        }
    }

    override fun onScheduleChanged(call: Call?) {
        Log.d(TAG, "CallObserver OnScheduleChanged : " + call?.getCallId())
        schedulesChanged(call)
    }

    override fun onCpuHitThreshold() {
        Log.d(TAG, "CallObserver onCpuHitThreshold")
    }

    override fun onPhotoCaptured(imageData: ByteArray?) {
        Log.d(TAG, "CallObserver onPhotoCaptured")
        imageData?.let {
            Log.d(TAG, "CallObserver onPhotoCaptured imageData Size: ${imageData.size}")
            photoViewerBottomSheetFragment.imageData = imageData
            activity?.supportFragmentManager?.let { photoViewerBottomSheetFragment.show(it, PhotoViewerBottomSheetFragment.TAG) }
        }
    }

    override fun onMediaQualityInfoChanged(mediaQualityInfo: Call.MediaQualityInfo) {
        Log.d(TAG, "CallObserver mediaQualityInfo changed : ${mediaQualityInfo.name}")
        updateNetworkStatusChange(mediaQualityInfo)
    }

    override fun onBroadcastMessageReceivedFromHost(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            showDialogWithMessage(requireContext(), R.string.message_from_host, message)
        }
    }

    override fun onHostAskingReturnToMainSession() {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), getString(R.string.host_asking_return_to_main), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onJoinableSessionUpdated(breakoutSessions: List<BreakoutSession>) {
        this.breakoutSessions = breakoutSessions
        breakoutSessionsAdapter.sessions = breakoutSessions.toMutableList()
        if (breakoutSessionBottomSheetFragment.isAdded && breakoutSessionBottomSheetFragment.isVisible) {
            breakoutSessionsAdapter.notifyDataSetChanged()
        }
        Log.d(tag, "BreakoutSession Joinable sessions updated : size -> ${breakoutSessions.size}")
    }

    override fun onJoinedSessionUpdated(breakoutSession: BreakoutSession) {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.tvName.text = breakoutSession.getName()
        }
    }

    override fun onReturnedToMainSession() {
        breakout = null
        val callInfo = webexViewModel.currentCallId?.let { webexViewModel.getCall(it) }
        lifecycleScope.launch(Dispatchers.Main) {
            binding.callingHeader.text = getString(R.string.onCall)
            binding.tvName.text = callInfo?.getTitle()
            binding.btnReturnToMainSession.visibility = View.INVISIBLE
        }
    }

    override fun onSessionClosing() {
        val closingText = "Breakout Session: Closing in ${breakout?.getDelay()} seconds"
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), closingText, Toast.LENGTH_LONG).show()
        }
    }

    override fun onSessionEnabled() {
        Log.d(tag, "BreakoutSession onSessionEnabled()")
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), getString(R.string.breakout_session_enabled), Toast.LENGTH_LONG).show()
        }
    }

    override fun onSessionJoined(breakoutSession: BreakoutSession) {
        attemptingToJoinABreakoutSession = false
        lifecycleScope.launch(Dispatchers.Main) {
            binding.callingHeader.text = getString(R.string.breakout_session)
            binding.tvName.text = breakoutSession.getName()
            binding.btnReturnToMainSession.visibility = View.VISIBLE
        }
    }

    override fun onSessionStarted(breakout: Breakout) {
        this.breakout = breakout
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), getString(R.string.breakout_session_started), Toast.LENGTH_LONG).show()
        }
    }

    override fun onBreakoutUpdated(breakout: Breakout) {
        this.breakout = breakout
    }

    override fun onBreakoutError(error: BreakoutSession.BreakoutSessionError) {
        attemptingToJoinABreakoutSession = false
        val errorText = "${getString(R.string.breakout_error_occured)} : ${error.name}"
        lifecycleScope.launch(Dispatchers.Main) {
            showDialogWithMessage(requireContext(), R.string.error_occurred, errorText)
        }
    }

    override fun onReceivingNoiseInfoChanged(info: ReceivingNoiseInfo) {
        Log.d(tag, "ReceivingNoiseRemoval: Info change noiseDetected = ${info.isNoiseDetected()}, NREnabled = ${info.isNoiseRemovalEnabled()}")

        binding.ivReceivingNoiseRemoval.visibility = View.VISIBLE
        if (info.isNoiseDetected() && !info.isNoiseRemovalEnabled()) {
            binding.ivReceivingNoiseRemoval.setImageResource(R.drawable.ic_noise_detected_filled)
        } else if (!info.isNoiseRemovalEnabled()) {
            binding.ivReceivingNoiseRemoval.setImageResource(R.drawable.ic_noise_none_filled)
        } else if (info.isNoiseRemovalEnabled()) {
            binding.ivReceivingNoiseRemoval.setImageResource(R.drawable.ic_noise_detected_canceled_filled)
        }
    }

    override fun onClosedCaptionsArrived(captions: CaptionItem) {
        CoroutineScope(Dispatchers.Main).launch {
            captionsController.showCaptionView(binding.root, captions)
            if(captions.isFinal) {
                captionsViewModel.updateData(captions)
                Log.d(TAG, " Captions are arrived from ${captions.getDisplayName()}")
            }
        }
    }

    override fun onClosedCaptionsInfoChanged(closedCaptionsInfo: ClosedCaptionsInfo) {
        Log.d(
            TAG,
            " Captions Info changed: current spkn: ${
                closedCaptionsInfo.getCurrentSpokenLanguage().getLanguageTitle()
            } and trns ${closedCaptionsInfo.getCurrentTranslationLanguage().getLanguageTitle()}"
        )
        captionsController.setLanguages(requireContext(), closedCaptionsInfo) { intent, code ->
            startActivityForResult(intent, code)
        }
    }

    override fun onMoveMeetingFailed(call: Call?) {
       showDialogWithMessage(requireContext(), R.string.move_meeting_failed, getString(R.string.move_meeting_failed_message))
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observerCallLiveData() {

        personViewModel.person.observe(viewLifecycleOwner, Observer { person ->
            person?.let {
                webexViewModel.selfPersonId = it.personId
            }
        })

        webexViewModel.startShareLiveData.observe(viewLifecycleOwner, Observer { status ->
            status?.let {
                if (it) {
                    Log.d(TAG, "startShareLiveData success")
                    // For this share screen session initialize live annotations
                    webexViewModel.initalizeAnnotations(AnnotationRenderer(requireContext()))
                    if(BuildConfig.FLAVOR != "wxc") {
                        binding.annotationPolicy.visibility = VISIBLE
                        binding.annotationPolicy.text = webexViewModel.getCurrentLiveAnnotationPolicy().toString()
                    }

                } else {
                    updateScreenShareButtonState(ShareButtonState.OFF)
                    Log.d(TAG, "User cancelled screen request")
                }
            }
        })

        webexViewModel.stopShareLiveData.observe(viewLifecycleOwner, Observer { status ->
            status?.let {
                if (it) {
                    Log.d(TAG, "stopShareLiveData success")
                } else {
                    Log.d(TAG, "stopShareLiveData Failed")
                }
            }
            binding.annotationPolicy.visibility = INVISIBLE
        })

        webexViewModel.setCompositeLayoutLiveData.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                if (it.first) {
                    Log.d(TAG, "setCompositeLayoutLiveData success")
                    webexViewModel.compositedVideoLayout = webexViewModel.compositedLayoutState
                } else {
                    Log.d(TAG, "setCompositeLayoutLiveData Failed")
                }
            }
        })

        webexViewModel.setRemoteVideoRenderModeLiveData.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                if (it.first) {
                    Log.d(TAG, "setRemoteVideoRenderModeLiveData success")
                } else {
                    Log.d(TAG, "setRemoteVideoRenderModeLiveData Failed: ${it.second}")
                    showDialogWithMessage(requireContext(), R.string.scaling_mode, it.second)
                }
            }
        })

        webexViewModel.callingLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                val event = it.event
                val call = it.call
                val sharingLabel = it.sharingLabel
                val errorMessage = it.errorMessage

                when (event) {
                    WebexRepository.CallEvent.DialCompleted -> {
                        Log.d(tag, "callingLiveData DIAL_COMPLETED callerId: ${call?.getCallId()}")
                        dismissErrorDialog()
                        onCallJoined(call)
                        handleCallControls(call)
                    }
                    WebexRepository.CallEvent.DialFailed, WebexRepository.CallEvent.WrongApiCalled, WebexRepository.CallEvent.CannotStartInstantMeeting -> {
                        dismissErrorDialog()
                        val callActivity = activity as CallActivity?
                        callActivity?.alertDialog(true, errorMessage ?: event.name)
                    }
                    WebexRepository.CallEvent.AnswerCompleted -> {
                        webexViewModel.currentCallId = call?.getCallId()
                        Log.d(TAG, "answer Lambda callInfo Id: ${call?.getCallId()}")
                        dismissErrorDialog()
                        onCallJoined(call)
                        handleCallControls(null)
                    }
                    WebexRepository.CallEvent.AnswerFailed -> {
                        Log.d(TAG, "answer Lambda failed $errorMessage")
                        dismissErrorDialog()
                        callEndedUIUpdate(call?.getCallId().orEmpty())
                    }
                    WebexRepository.CallEvent.MeetingPinOrPasswordRequired,
                    WebexRepository.CallEvent.InCorrectPassword,
                    WebexRepository.CallEvent.InCorrectPasswordOrHostKey,
                    WebexRepository.CallEvent.CaptchaRequired,
                    WebexRepository.CallEvent.InCorrectPasswordWithCaptcha,
                    WebexRepository.CallEvent.InCorrectPasswordOrHostKeyWithCaptcha -> {
                        Log.d(TAG, "Call Observer Error case : " + it.errorMessage)
                        onMeetingHostPinError(it.captcha, event)
                    }
                    else -> {
                        dismissErrorDialog()
                    }
                }

                call?.let {
                    captionsController = ClosedCaptionsController(call)
                }
            }
        })

        webexViewModel.startAssociationLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                val event = it.event
                val call = it.call
                val errorMessage = it.errorMessage

                when (event) {
                    WebexRepository.CallEvent.AssociationCallCompleted -> {
                        webexViewModel.isAddedCall = true
                        webexViewModel.oldCallId = webexViewModel.currentCallId
                        webexViewModel.currentCallId = call?.getCallId()?:""

                        call?.let { _call ->
                            CallObjectStorage.addCallObject(_call)
                        }
                        onCallJoined(call)
                        handleCallControls(call)
                        Log.d(tag, "startAssociatedCall currentCallId = ${webexViewModel.currentCallId}, oldCallId = ${webexViewModel.oldCallId}")
                    }
                    WebexRepository.CallEvent.AssociationCallFailed -> {
                        Log.d(TAG, "startAssociatedCall Lambda failed $errorMessage")
                        val callActivity = activity as CallActivity?
                        callActivity?.alertDialog(false, resources.getString(R.string.start_associated_call_failed))
                    }
                    else -> {}
                }
            }
        })

        webexViewModel.forceSendingVideoLandscapeLiveData.observe(viewLifecycleOwner, Observer { result ->
            if (result) {
                webexViewModel.isSendingVideoForceLandscape = !webexViewModel.isSendingVideoForceLandscape
            }
        })

        webexViewModel.virtualBgError.observe(viewLifecycleOwner, Observer { error ->
            Log.d(tag, error)
            requireContext().toast(error)
        })

        webexViewModel.virtualBackground.observe(viewLifecycleOwner, Observer {
            val emptyBackground = VirtualBackground()

            if (bottomSheetFragment == null) {
                bottomSheetFragment =
                    BackgroundOptionsBottomSheetFragment(onBackgroundChanged = { virtualBackground ->
                        if (!webexViewModel.isVirtualBackgroundSupported()) {
                            Log.d(tag, getString(R.string.virtual_bg_not_supported))
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.virtual_bg_not_supported),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            return@BackgroundOptionsBottomSheetFragment
                        }

                        webexViewModel.applyVirtualBackground(
                            virtualBackground,
                            Phone.VirtualBackgroundMode.CALL
                        )
                    },
                        onBackgroundRemoved = { virtualBackground ->
                            webexViewModel.removeVirtualBackground(virtualBackground)
                        },
                        onNewBackgroundAdded = { file ->
                            if (!webexViewModel.isVirtualBackgroundSupported()) {
                                Log.d(tag, getString(R.string.virtual_bg_not_supported))
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.virtual_bg_not_supported),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                return@BackgroundOptionsBottomSheetFragment
                            }

                            val localFile = processAttachmentFile(file)
                            webexViewModel.addVirtualBackground(localFile)
                        },
                        onBottomSheetDimissed = {
                            bottomSheetFragment = null
                        })

                bottomSheetFragment?.show(
                    childFragmentManager,
                    BackgroundOptionsBottomSheetFragment::class.java.name
                )
            }

            bottomSheetFragment?.backgrounds?.clear()
            bottomSheetFragment?.backgrounds?.addAll(it)
            bottomSheetFragment?.backgrounds?.add(emptyBackground)
            bottomSheetFragment?.adapter?.notifyDataSetChanged()
        })

        webexViewModel.annotationEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is WebexViewModel.AnnotationEvent.PERMISSION_ASK -> toggleAnnotationPermissionDialog(true, event.personId)
                is WebexViewModel.AnnotationEvent.PERMISSION_EXPIRED -> toggleAnnotationPermissionDialog(false, event.personId)
            }
        }
        webexViewModel.authLiveData.observe(viewLifecycleOwner, Observer {
            if (it != null && it == Constants.Callbacks.RE_LOGIN_REQUIRED) {
                Log.d(tag, "onReAuthRequired Re login is required by user.")
                onSignedOut()
            }
        })

        webexViewModel.startAudioDumpLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    Log.d(TAG, "startAudioDumpLiveData success")
                    showRecordingAlertDialog(requireActivity())
                } else {
                    Log.d(TAG, "startAudioDumpLiveData Failed")
                    showSnackbar("Failed to start audio dump")
                }
            }
        })
        webexViewModel.stopAudioDumpLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                if(alertDialogBuilder?.isShowing == true) {
                    alertDialogBuilder?.dismiss()
                }
                timerHandler.removeCallbacksAndMessages(null)
                if (it) {
                    Log.d(TAG, "stopAudioDumpLiveData success")
                } else {
                    Log.d(TAG, "stopAudioDumpLiveData Failed")
                    showSnackbar("Failed to stop audio dump")
                }
            }
        })
        webexViewModel.canStartAudioDumpLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    Log.d(TAG, "canStartAudioDumpLiveData success")
                    if(!webexViewModel.isRecordingAudioDump()) {
                        webexViewModel.startAudioDump()
                    }

                } else {
                    Log.d(TAG, "canStartAudioDumpLiveData Failed")
                    showSnackbar("Audio dump is not supported")
                }
            }
        })
    }

    private fun onSignedOut() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun handleOnBackgroundChanged(virtualBackground: VirtualBackground) {
        if(!webexViewModel.isVirtualBackgroundSupported()) {
            Log.d(tag, "virtual background is not supported")
            requireContext().toast(getString(R.string.virtual_bg_not_supported))
            return
        }

        webexViewModel.applyVirtualBackground(virtualBackground, Phone.VirtualBackgroundMode.CALL)
    }

    private fun handleOnNewBackgroundAdded(file: File) {
        if(!webexViewModel.isVirtualBackgroundSupported()) {
            Log.d(tag, "virtual background is not supported")
            requireContext().toast(getString(R.string.virtual_bg_not_supported))
            return
        }

        val localFile = processAttachmentFile(file)
        webexViewModel.addVirtualBackground(localFile)
    }


    private fun processAttachmentFile(file: File): LocalFile {
        var thumbnail: LocalFile.Thumbnail? = null
        if (MimeUtils.getContentTypeByFilename(file.name) == MimeUtils.ContentType.IMAGE) {
            thumbnail = LocalFile.Thumbnail(file, null, resources.getInteger(R.integer.virtual_bg_thumbnail_width), resources.getInteger(R.integer.virtual_bg_thumbnail_height))
        }

        return LocalFile(file, null, thumbnail, null)
    }

    private fun onMeetingHostPinError(captcha: Phone.Captcha?, error: WebexRepository.CallEvent) {

        if(dialType == DialType.NONE) { // this is the case when the dialed called first time, so decide the dial type here
            showDialogWithMessage(requireContext(), getString(R.string.meeting_error), getString(R.string.are_you_host), cancelable = false,
                onPositiveButtonClick = { dialog, _ ->
                    dialog.dismiss()
                    dialType = DialType.HOST
                    createErrorDialog(true, captcha, error)

                },
                onNegativeButtonClick = { dialog, _ ->
                    dialog.dismiss()
                    dialType = DialType.OTHERS
                    createErrorDialog(false, captcha, error)
                })
        } else { // second time onwards just set host or others to bypass the host confirming dialog
            if(passwordDialog.isShowing) {
                updateErrorDialog(captcha, error)
            } else {
                createErrorDialog(dialType == DialType.HOST, captcha, error)
            }
        }
    }

    private fun updateErrorDialog(
        captchaData: Phone.Captcha? = null,
        error: WebexRepository.CallEvent = WebexRepository.CallEvent.MeetingPinOrPasswordRequired
    ) {
        val isCaptchaAvailable = (captchaData!=null)
        passwordDialogBinding.root.apply {
            if(isCaptchaAvailable) {
                passwordDialogBinding.captchaRootLayout.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load(captchaData?.getImageUrl()) // image url
                    .placeholder(R.color.black) // any placeholder to load at start
                    .centerCrop()
                    .into(passwordDialogBinding.captchImage)
                passwordDialogBinding.captchaAudio.tag = captchaData?.getAudioUrl()
                passwordDialogBinding.submit.tag = captchaData
            } else {
                passwordDialogBinding.captchaRootLayout.visibility = View.GONE
            }

            passwordDialogBinding.progressBar.visibility = View.GONE
            passwordDialogBinding.submit.visibility = View.VISIBLE

            passwordDialogBinding.pinTitleEditText.text.clear()
            passwordDialogBinding.captchaInputText.text.clear()

            when (error) {
                WebexRepository.CallEvent.MeetingPinOrPasswordRequired,
                WebexRepository.CallEvent.CaptchaRequired-> {
                    passwordDialogBinding.errorText.text = ""
                }
                WebexRepository.CallEvent.InCorrectPassword ,
                WebexRepository.CallEvent.InCorrectPasswordWithCaptcha -> {
                    passwordDialogBinding.errorText.text = getString(R.string.incorrectPin)
                }
                WebexRepository.CallEvent.InCorrectPasswordOrHostKey ,
                WebexRepository.CallEvent.InCorrectPasswordOrHostKeyWithCaptcha -> {
                    passwordDialogBinding.errorText.text = getString(R.string.incorrectPinOrHostKey)
                }
                else -> { }
            }
        }
    }

    private fun createErrorDialog(
        isHost: Boolean,
        captchaData: Phone.Captcha? = null,
        error: WebexRepository.CallEvent = WebexRepository.CallEvent.MeetingPinOrPasswordRequired
    ) {

        passwordDialogBinding = DialogEnterMeetingPinBinding.inflate(layoutInflater)
            .apply {

                // Captcha data validation if any
                if (captchaData != null) {
                    captchaRootLayout.visibility = View.VISIBLE
                    Glide.with(requireContext())
                        .load(captchaData.getImageUrl()) // image url
                        .placeholder(R.color.black) // any placeholder to load at start
                        .centerCrop()
                        .into(captchImage)
                    captchaAudio.tag = captchaData.getAudioUrl()
                } else {
                    captchaRootLayout.visibility = View.GONE
                }

                // Prepare error message
                errorText.text = ""
                if (error == WebexRepository.CallEvent.InCorrectPassword ||
                    error == WebexRepository.CallEvent.InCorrectPasswordWithCaptcha
                ) {
                    errorText.text = getString(R.string.incorrectPin)
                } else if (error == WebexRepository.CallEvent.InCorrectPasswordOrHostKey ||
                    error == WebexRepository.CallEvent.InCorrectPasswordOrHostKeyWithCaptcha
                ) {
                    errorText.text = getString(R.string.incorrectPinOrHostKey)
                }

                // Handle submit action for pin and captcha
                submit.tag = captchaData
                submit.setOnClickListener {
                    it.let {
                        // reset the previous error
                        errorText.text = ""

                        if (pinTitleEditText.text.isEmpty()) {
                            val error = if (isHost) getString(R.string.host_key_required) else getString(R.string.meeting_pin_required)
                            errorText.text = error
                        } else if(captchaData != null && captchaInputText.text.isEmpty()) {
                            val error = getString(R.string.captcha_empty_error)
                            errorText.text = error
                        } else{
                            submit.visibility = View.INVISIBLE
                            progressBar.visibility = View.VISIBLE
                            val data = it.tag as Phone.Captcha?

                            dialOutgoingCall(callerId, isHost,
                                pinTitleEditText.text.toString(),
                                captchaInputText.text.toString(),
                                data?.getId()?:"", false, moveMeeting)
                        }
                    }
                }

                //initialize audio action
                captchaAudio.setOnClickListener {
                    it?.let{
                        playAudio(it.tag as String)
                    }
                }

                //initialize refresh action
                refresh.setOnClickListener {
                    mediaPlayer.reset()
                    webexViewModel.refreshCaptcha()
                }
            }

        val title = if (isHost) getString(R.string.enter_host_key) else getString(R.string.enter_meeting_pin)
        passwordDialog.setTitle(title)
        passwordDialogBinding.pinTitleLabel.text = title
        passwordDialog.setContentView(passwordDialogBinding.root)
        passwordDialog.show()
    }

    private fun dismissErrorDialog(){
        if(passwordDialog.isShowing) passwordDialog.dismiss()
    }

    private fun playAudio(url: String) {
        try {
            val uri: Uri = Uri.parse(url)
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mediaPlayer.setDataSource(requireContext(), uri)
            mediaPlayer.prepareAsync()

            mediaPlayer.setOnPreparedListener {
                it.start()
            }
        } catch (e: java.lang.Exception) {
            println(e.toString())
        }
    }

    private fun audioEventChanged(callMembership: CallMembership?, call: Call?, isSendingAudio: Boolean? = null, isRemoteSendingAudio: Boolean? = null) {
        mHandler.post {
            callMembership?.let { member ->
                if (member.getPersonId() == webexViewModel.selfPersonId) {
                    val audioMuted = !member.isSendingAudio()

                    webexViewModel.isSendingAudio = member.isSendingAudio()
                    if (audioMuted) {
                        showMutedIcon(true)
                    } else {
                        showMutedIcon(false)
                    }
                }
            } ?: run {
                isSendingAudio?.let { audio ->
                    val audioMuted = !audio

                    webexViewModel.isSendingAudio = audio
                    if (audioMuted) {
                        showMutedIcon(true)
                    } else {
                        showMutedIcon(false)
                    }
                }
            }

            webexViewModel.postParticipantData(call?.getMemberships())
            showCallHeader(call?.getCallId().orEmpty())
        }
    }

    private fun schedulesChanged(call: Call?) {
        val schedules= call?.getSchedules()
        schedules?.let {
            for (item in schedules) {
                incomingInfoAdapter.info.forEach { model ->
                    if ((model is MeetingInfoModel) && (model.meetingId == item.getId())) {
                        val infoModel = MeetingInfoModel.convertToMeetingInfoModel(call, item)
                        incomingInfoAdapter.info.remove(model)
                        incomingInfoAdapter.info.add(infoModel)
                        incomingInfoAdapter.notifyDataSetChanged()
                        return
                    }
                }
            }
        } ?: run {
            //Canceled meeting
            incomingInfoAdapter.info.forEach { model ->
                if (model is MeetingInfoModel) {
                    incomingInfoAdapter.info.remove(model)
                }
            }
            incomingInfoAdapter.notifyDataSetChanged()
        }
    }

    private fun handleCallControls(call: Call?) {
        mHandler.post {
            Log.d(TAG, "handleCallControls isAddedCall = ${webexViewModel.isAddedCall}")
            webexViewModel.currentCallId?.let { callId ->

                var _call = call

                if (_call == null) {
                    _call = webexViewModel.getCall(callId)
                }

                _call?.let {
                    when {
                        (it.isCUCMCall() || it.isWebexCallingOrWebexForBroadworks()) && webexViewModel.isAddedCall -> {
                            binding.ibTransferCall.visibility = View.VISIBLE
                            binding.ibMerge.visibility = View.VISIBLE
                            binding.ibAdd.visibility = View.INVISIBLE
                            binding.ibVideo.visibility = View.INVISIBLE
                        }
                        it.isWebexCallingOrWebexForBroadworks() && webexViewModel.isVideoEnabled() -> {
                            binding.ibSwitchToAudioVideoCall.visibility = View.VISIBLE
                        }
                        !webexViewModel.isVideoEnabled() && !(webexViewModel.isMeeting()) -> {
                            binding.ibVideo.visibility = View.INVISIBLE
                        }
                        !it.isCUCMCall() && !it.isWebexCallingOrWebexForBroadworks() -> {
                            binding.ibAdd.visibility = View.GONE
                            binding.ibTransferCall.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }
    }


    private fun showMutedIcon(showMuted: Boolean) {
        binding.ibMute.isSelected = showMuted
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webexViewModel.currentCallId?.let {
            webexViewModel.setVideoRenderViews(it)
        }
        webexViewModel.cleanup()
        webexViewModel.callObserverInterface = null
        mHandler.removeCallbacksAndMessages(null)
    }

    private fun setUpViews(bundle: Bundle?) {
        Log.d(TAG, "setUpViews fragment")
        personViewModel.getMe()
        videoViewState(true)
        webexViewModel.callObserverInterface = this

        webexViewModel.enableBackgroundStream(webexViewModel.enableBgStreamtoggle)
        webexViewModel.enableAudioBNR(true)
        webexViewModel.setAudioBNRMode(Phone.AudioBRNMode.HP)
        webexViewModel.setDefaultFacingMode(Phone.FacingMode.USER)

        webexViewModel.setVideoMaxTxFPSSetting(30)
        webexViewModel.setVideoEnableCamera2Setting(true)
        webexViewModel.setVideoEnableDecoderMosaicSetting(true)

        webexViewModel.setHardwareAccelerationEnabled(webexViewModel.enableHWAcceltoggle)
        webexViewModel.setVideoMaxTxBandwidth(webexViewModel.getUserPreferredMaxBandwidth())
        webexViewModel.setVideoMaxRxBandwidth(webexViewModel.getUserPreferredMaxBandwidth())
        webexViewModel.setSharingMaxRxBandwidth(Phone.DefaultBandwidth.MAX_BANDWIDTH_SESSION.getValue())
        webexViewModel.setAudioMaxRxBandwidth(Phone.DefaultBandwidth.MAX_BANDWIDTH_AUDIO.getValue())

        webexViewModel.setVideoStreamMode(webexViewModel.streamMode)

        val incomingCallPickEvent: (Call?) -> Unit = { call ->
            Log.d(tag, "incomingCallPickEvent")
            ringerManager.stopRinger(Call.RingerType.Incoming)
            call?.let {
                if (webexViewModel.hasAnyoneJoined()) {
                    onCallActionListener?.onEndAndAnswer(
                        webexViewModel.currentCallId.orEmpty(),
                        call.getCallId().orEmpty(), CompletionHandler { result ->
                            if (result.isSuccessful && result.data == false) {
                                answerCall(it)
                            }
                        }
                    )
                } else answerCall(it)
            }
        }

        val incomingCallCancelEvent: (Call?) -> Unit = { call ->
            Log.d(tag, "incomingCallEndEvent callId: ${call?.getCallId()}")
            ringerManager.stopRinger(Call.RingerType.Incoming)
            endIncomingCall(call?.getCallId().orEmpty())
        }

        incomingInfoAdapter = IncomingCallBottomSheetFragment.IncomingInfoAdapter(
            incomingCallPickEvent,
            incomingCallCancelEvent
        )

        breakoutSessionsAdapter = BreakoutSessionsBottomSheetFragment.BreakoutSessionsAdapter {
            webexViewModel.joinBreakoutSession(it)
            breakoutSessionBottomSheetFragment.dismiss()
            attemptingToJoinABreakoutSession = true
        }

        callOptionsBottomSheetFragment = CallBottomSheetFragment(
            { call -> showIncomingCallBottomSheet()},
            { call -> showTranscriptions(call) },
            { call -> toggleWXAClickListener(call) },
            { call -> receivingVideoListener(call) },
            { call -> receivingAudioListener(call) },
            { call -> receivingSharingListener(call) },
            { call -> scalingModeClickListener(call) },
            { call -> virtualBackgroundOptionsClickListener(call) },
            { call -> compositeStreamLayoutClickListener(call) },
            { call -> swapVideoClickListener(call) },
            { call -> forceLandscapeClickListener(call) },
            { call -> cameraOptionsClickListener(call) },
            { call -> multiStreamOptionsClickListener(call) },
            { call -> sendDTMFClickListener(call) },
            {  claimHostClickListener() },
            { showBreakoutSessions() },
            { call -> showCaptionDialog(call) },
            { startAudioDump() })

        multiStreamOptionsBottomSheetFragment = MultiStreamOptionsBottomSheetFragment({ call -> setCategoryAOptionClickListener(call) },
            { call -> setCategoryBOptionClickListener(call) },
            { call -> removeCategoryAClickListener(call) },
            { call -> removeCategoryBClickListener(call) })

        multiStreamDataOptionsBottomSheetFragment = MultiStreamDataOptionsBottomSheetFragment(
            { call, quality, duplicate -> categoryAOptionsOkListener(call, quality, duplicate) },
            { call, numStreams, quality -> categoryBOptionsOkListener(call, numStreams, quality) } )

        mediaStreamBottomSheetFragment= MediaStreamBottomSheetFragment(
            { renderView, personID, quality -> pinStreamClickListener(renderView, personID, quality) },
            { renderView, personID -> unpinStreamClickListener(renderView, personID) } ,
            { renderView, personID -> closeStreamStreamClickListener(renderView, personID) } )

        initIncomingCallBottomSheet()

        breakoutSessionBottomSheetFragment = BreakoutSessionsBottomSheetFragment()

        cameraOptionsBottomSheetFragment = CameraOptionsBottomSheetFragment({ call -> zoomFactorClickListener(call) },
                { call -> torchModeClickListener(call) },
                { call -> flashModeClickListener(call) },
                { call -> cameraFocusClickListener(call) },
                { call -> cameraCustomExposureClickListener(call) },
                { call -> cameraAutoExposureClickListener(call) },
                { call -> takePhotoClickListener(call) })

        cameraOptionsDataBottomSheetFragment = CameraOptionsDataBottomSheetFragment ({ x -> zoomfactorValueSetListener(x) },
            { x, y -> cameraFocusValueSetClickListener(x, y) },
            { x, y -> cameraCustomExposureValueSetClickListener(x, y) },
            { x -> cameraAutoExposureValueSetClickListener(x) })

        photoViewerBottomSheetFragment = PhotoViewerBottomSheetFragment()

        switchAudioBottomSheetFragment = SwitchAudioBottomSheetFragment({toggleAudioMode(AudioMode.EARPIECE)},
            {toggleAudioMode(AudioMode.SPEAKER)}, {toggleAudioMode(AudioMode.BLUETOOTH)}, {toggleAudioMode(AudioMode.WIRED_HEADSET)})

        callingActivity = bundle?.getInt(Constants.Intent.CALLING_ACTIVITY_ID, 0)!!
        moveMeeting = if(bundle.getBoolean(Constants.Intent.MOVE_MEETING, false)) {
            CompanionMode.MoveMeeting
        } else    {
            CompanionMode.None
        }
        val incomingCallId = bundle.getString(Constants.Intent.CALL_ID) ?: ""
        if (callingActivity == 1) {
            isIncomingActivity = true
            binding.mainContentLayout.visibility = View.GONE
            binding.incomingCallHeader.visibility = View.VISIBLE
            val acceptedCall = bundle.getBoolean(Constants.Action.WEBEX_CALL_ACCEPT_ACTION)
            if(!acceptedCall) {
                incomingLayoutState(false)
                ringerManager.startRinger(Call.RingerType.Incoming)
            }
            val _call = CallObjectStorage.getCallObject(incomingCallId)
            _call?.let { call ->
                webexViewModel.setCallObserver(call)
            }

            if(acceptedCall){
                _call?.let {
                    if(it.getStatus() != Call.CallStatus.CONNECTED) { // For resumed fragments
                        answerCall(it)
                    }else{
                        binding.incomingCallHeader.visibility = View.GONE
                        incomingLayoutState(true)
                        onCallJoined(it)
                        handleCallControls(it)
                        onConnected(it)
                        webexViewModel.currentCallId = it.getCallId()
                        it.holdCall(false)
                    }
                }
            }

            if(_call?.getStatus() != Call.CallStatus.CONNECTED) {
                onIncomingCall(_call, !acceptedCall)
            }

        } else {
            isIncomingActivity = false
            binding.incomingCallHeader.visibility = View.GONE
            incomingLayoutState(true)

            binding.callingHeader.text = getString(R.string.calling)
            val callerId = bundle.getString(Constants.Intent.OUTGOING_CALL_CALLER_ID)
            binding.tvName.text = callerId
            val _call = CallObjectStorage.getCallObject(incomingCallId)
            _call?.let{
                webexViewModel.setCallObserver(it)
                if(it.getStatus() == Call.CallStatus.CONNECTED){
                    binding.incomingCallHeader.visibility = View.GONE
                    incomingLayoutState(true)
                    onCallJoined(it)
                    handleCallControls(it)
                    onConnected(it)
                    webexViewModel.currentCallId = it.getCallId()
                    it.holdCall(false)
                }
            }
        }

        binding.ibMute.setOnClickListener(this)
        binding.ibParticipants.setOnClickListener(this)
        binding.ibAudioMode.setOnClickListener(this)
        binding.ibAdd.setOnClickListener(this)
        binding.ibTransferCall.setOnClickListener(this)
        binding.ibDirecttransferCall.setOnClickListener(this)
        binding.ibDirecttransferCall.visibility = View.GONE

        binding.ibHoldCall.setOnClickListener(this)
        binding.ivCancelCall.setOnClickListener(this)
        binding.ibVideo.setOnClickListener(this)
        binding.ibSwapCamera.setOnClickListener(this)
        binding.ibMerge.setOnClickListener(this)
        binding.ibScreenShare.setOnClickListener(this)
        binding.mainContentLayout.setOnClickListener(this)
        binding.ibMoreOption.setOnClickListener(this)
        binding.btnReturnToMainSession.setOnClickListener(this)
        binding.ibSwitchToAudioVideoCall.setOnClickListener(this)
        binding.ivReceivingNoiseRemoval.setOnClickListener(this)

        initAddedCallControls()
        binding.ivNetworkSignal.setOnClickListener(this)
        binding.ivNetworkSignal.visibility = View.GONE
        binding.btnReturnToMainSession.visibility = View.INVISIBLE

        passwordDialog = Dialog(requireContext())

        binding.annotationPolicy.setOnClickListener(this)
    }

    private fun startAudioDump() {
        webexViewModel.canStartRecordingAudioDump()
    }

    var alertDialogBuilder : AlertDialog? = null
    var timerHandler = Handler(Looper.getMainLooper())
    private fun showRecordingAlertDialog(activity: Activity) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Recording")
        val timerTextView = TextView(activity).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_HORIZONTAL
            textSize = 20f
        }

        val startTime = System.currentTimeMillis()
        val timerRunnable = object : Runnable {
            override fun run() {
                val millis = System.currentTimeMillis() - startTime
                val seconds = (millis / 1000).toInt()
                val minutes = seconds / 60
                val hours = minutes / 60
                timerTextView.text = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
                timerHandler.postDelayed(this, 500)
            }
        }
        timerHandler.postDelayed(timerRunnable, 0)

        builder.setView(timerTextView)
        builder.setMessage("Audio recording is in progress:")
        builder.setCancelable(false)
        builder.setPositiveButton("STOP") { dialog, _ ->
            dialog.dismiss()
            timerHandler.removeCallbacks(timerRunnable)
            webexViewModel.stopAudioDump()
        }

        alertDialogBuilder = builder.create()

        alertDialogBuilder?.show()
    }

    private fun showCaptionDialog(call: Call?) {
        captionsController.showCaptionDialog(requireContext(), call) {intent, code ->
            startActivityForResult(intent, code)
        }
    }

    private fun showBreakoutSessions() {
        breakoutSessionsAdapter.sessions = breakoutSessions.toMutableList()
        breakoutSessionBottomSheetFragment.adapter = breakoutSessionsAdapter
        activity?.supportFragmentManager?.let { breakoutSessionBottomSheetFragment.show(it, BreakoutSessionsBottomSheetFragment.TAG) }
        breakoutSessionsAdapter.notifyDataSetChanged()
    }

    fun answerCall(call: Call) {
        webexViewModel.answer(call, getMediaOption())
        captionsController = ClosedCaptionsController(call)
    }

    private fun initIncomingCallBottomSheet() {
        incomingCallBottomSheetFragment = IncomingCallBottomSheetFragment { bottomSheet ->
            ringerManager.stopRinger(Call.RingerType.Incoming)
            if (webexViewModel.hasAnyoneJoined()) {
                bottomSheet.dismiss()
            } else {
                requireActivity().finish()
            }
        }
    }

    override fun onClick(v: View?) {
        webexViewModel.currentCallId?.let { callId ->
            when (v) {
                binding.ibMute -> {
                    webexViewModel.muteSelfAudio(callId)
                }
                binding.ibParticipants -> {
                    val dialog = ParticipantsFragment.newInstance(callId)
                    dialog.show(childFragmentManager, ParticipantsFragment::javaClass.name)
                }
                binding.ibAudioMode -> {
                    switchAudioBottomSheetFragment.show(childFragmentManager, SwitchAudioBottomSheetFragment::javaClass.name)
                }
                binding.ibAdd -> {
                    //while associating a call, existing call needs to be put on hold
                    isIncomingActivity = false
                    webexViewModel.holdCall(callId)
                    startActivityForResult(DialerActivity.getIntent(requireContext()), REQUEST_CODE)
                }
                binding.ibTransferCall -> {
                    transferCall()
                    initAddedCallControls()
                }
                binding.ibDirecttransferCall -> {
                    startActivityForResult(DialerActivity.getIntent(requireContext()), REQUEST_CODE_BLINDTRANSFER)
                }
                binding.ibMerge -> {
                    mergeCalls()
                    initAddedCallControls()
                }
                binding.ibHoldCall -> {
                    webexViewModel.holdCall(callId)
                }
                binding.ivCancelCall -> {
                    endCall()
                }
                binding.ibVideo -> {
                    muteSelfVideo(!webexViewModel.isLocalVideoMuted)
                }
                binding.ibSwapCamera -> {
                    val call = webexViewModel.getCall(webexViewModel.currentCallId.orEmpty())

                    call?.let {
                        val mode = it.getFacingMode()

                        if (mode == Phone.FacingMode.ENVIROMENT) {
                            it.setFacingMode(Phone.FacingMode.USER)
                        } else {
                            it.setFacingMode(Phone.FacingMode.ENVIROMENT)
                        }
                    }
                }
                binding.ibSwitchToAudioVideoCall ->{
                    switchToAudioOrVideoCall()
                }

                binding.ibScreenShare -> {
                    shareScreen()
                }
                binding.mainContentLayout -> {
                    mainContentLayoutClickListener()
                }
                binding.ibMoreOption -> {
                    webexViewModel.currentCallId?.let {
                        showBottomSheet(webexViewModel.getCall(it))
                    }
                }
                binding.ivNetworkSignal -> {
                    val text = "Network Status : ${currentNetworkStatus.name}"
                    Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
                }
                binding.btnReturnToMainSession -> {
                    webexViewModel.returnToMainSession()
                }
                binding.ivReceivingNoiseRemoval -> {
                    if (webexViewModel.getReceivingNoiseInfo()?.isNoiseRemovalEnabled() == true)
                        webexViewModel.enableReceivingNoiseRemoval(false) {
                            if (it.isSuccessful)
                                Log.d(TAG, "ReceivingNoiseRemoval enableAPIResult = successfully disabled NR")
                            else
                                Log.d(TAG, "ReceivingNoiseRemoval enableAPIResult = error : ${it.error?.errorMessage.orEmpty()}")
                        }
                    else
                        webexViewModel.enableReceivingNoiseRemoval(true) {
                            if (it.isSuccessful)
                                Log.d(TAG, "ReceivingNoiseRemoval enableAPIResult = successfully enabled NR")
                            else
                                Log.d(TAG, "ReceivingNoiseRemoval enableAPIResult = error : ${it.error?.errorMessage.orEmpty()}")
                        }
                }
                binding.annotationPolicy -> {
                    showPolicySelectionListDialog()
                }
                else -> {
                }
            }
        }
    }

    private fun showPolicySelectionListDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.annotation_policy))
        val policyList = { resources.getStringArray(R.array.annotation_policy) }
        builder.setSingleChoiceItems(policyList(), 0) { dialog, which ->
            when (which) {
                0 -> {
                    webexViewModel.setLiveAnnotationPolicy(LiveAnnotationsPolicy.NobodyCanAnnotate)
                }
                1 -> {
                    webexViewModel.setLiveAnnotationPolicy(LiveAnnotationsPolicy.AnyoneCanAnnotate)
                }
                2 -> {
                    webexViewModel.setLiveAnnotationPolicy(LiveAnnotationsPolicy.NeedAskForAnnotate)
                }
            }
            binding.annotationPolicy.text = policyList()[which]
            dialog.dismiss()
        }
        builder.show()
    }

    private fun mainContentLayoutClickListener() {
        Log.d(TAG, "mainContentLayoutClickListener")

        if (binding.controlGroup.visibility == View.VISIBLE) {
            binding.controlGroup.visibility = View.GONE
        } else {
            binding.controlGroup.visibility = View.VISIBLE
        }
    }

    private fun screenShareButtonVisibilityState() {
        webexViewModel.currentCallId?.let {
            val canShare = webexViewModel.getCall(it)?.canShare() ?: false
            Log.d(TAG, "CallControlsFragment screenShareButtonVisibilityState canShare: $canShare")

            if (canShare) {
                binding.ibScreenShare.visibility = View.VISIBLE
            } else {
                binding.ibScreenShare.visibility = View.INVISIBLE
            }

        } ?: run {
            binding.ibScreenShare.visibility = View.INVISIBLE
        }
    }

    private fun directTransferButtonStateUpdate() {
        webexViewModel.currentCallId?.let {callId ->
            val call = webexViewModel.getCall(callId)
            call?.let {
                if (it.isWebexCallingOrWebexForBroadworks() && !it.isGroupCall()) {
                    binding.ibDirecttransferCall.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun updateScreenShareButtonState(state: ShareButtonState) {
        when (state) {
            ShareButtonState.OFF -> {
                binding.ibScreenShare.isEnabled = true
                binding.ibScreenShare.alpha = 1.0f
                binding.ibScreenShare.background = ContextCompat.getDrawable(requireActivity(), R.drawable.screen_sharing_default)
            }
            ShareButtonState.ON -> {
                binding.ibScreenShare.isEnabled = true
                binding.ibScreenShare.alpha = 1.0f
                binding.ibScreenShare.background = ContextCompat.getDrawable(requireActivity(), R.drawable.screen_sharing_active)
            }
            ShareButtonState.DISABLED -> {
                binding.ibScreenShare.isEnabled = false
                binding.ibScreenShare.alpha = 0.5f
            }
        }
    }

    private fun isLocalSharing(callId: String): Boolean {
        val call = webexViewModel.getCall(callId)
        return call?.isSendingSharing() ?: false
    }

    private fun isReceivingSharing(callId: String): Boolean {
        val call = webexViewModel.getCall(callId)
        return call?.isReceivingSharing() ?: false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        service?.createNotificationChannel(chan)
        return channelId
    }

    private fun buildScreenShareForegroundServiceNotification(): Notification {
        val contentId = R.string.notification_start_share_foreground_text

        val channelId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel("screen_share_service_v3_sdk", "Background Screen Share Service v3 SDK")
                } else { "" }


        val notificationBuilder =
                NotificationCompat.Builder(requireContext(), channelId)
                        .setSmallIcon(R.drawable.app_notification_icon)
                        .setContentTitle(getString(R.string.notification_share_foreground_title))
                        .setContentText(getString(contentId))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setTicker(getString(contentId))
                        .setDefaults(Notification.DEFAULT_SOUND)

        return notificationBuilder.build()
    }

    private fun shareScreen() {
        Log.d(TAG, "shareScreen")

        webexViewModel.currentCallId?.let {
            val isSharing = isLocalSharing(it)
            if(!isSharing) {
                screenShareConfigDialog()
            }
            else {
                updateScreenShareButtonState(ShareButtonState.DISABLED)
                webexViewModel.currentCallId?.let { id -> webexViewModel.stopShare(id) }
            }
            Log.d(TAG, "shareScreen isSharing: $isSharing")
        }
    }

    private fun screenShareConfigDialog() {
        val builder = AlertDialog.Builder(requireContext())
        var shareConfig:ShareConfig? = ShareConfig()
        builder.setTitle(getString(R.string.screenShare_config))
        ScreenshareconfigBinding.inflate(layoutInflater).apply {
            builder.setView(this.root)
            optimizeTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.defaultShare -> {
                        shareConfig?.setShareType(Call.ShareOptimizeType.Default)
                    }
                    R.id.autoDetection -> {
                        shareConfig?.setShareType(Call.ShareOptimizeType.AutoDetection)
                    }
                    R.id.optimizeForText -> {
                        shareConfig?.setShareType(Call.ShareOptimizeType.OptimizeText)
                    }
                    R.id.optimizeForVideo -> {
                        shareConfig?.setShareType(Call.ShareOptimizeType.OptimizeVideo)
                    }
                    R.id.noOptimizeType -> {
                        shareConfig = null
                    }
                }
            }
            enableAudioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.enableAudioTrue -> {
                        shareConfig?.setEnableAudio(true)
                    }
                    R.id.enableAudioFalse -> {
                        shareConfig?.setEnableAudio(false)
                    }
                }
            }
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                updateScreenShareButtonState(ShareButtonState.DISABLED)
                if (requireContext().applicationInfo.targetSdkVersion >= 29) {
                    webexViewModel.startShare(webexViewModel.currentCallId.orEmpty(), buildScreenShareForegroundServiceNotification(), SHARE_SCREEN_FOREGROUND_SERVICE_NOTIFICATION_ID, shareConfig)
                } else {
                    webexViewModel.startShare(webexViewModel.currentCallId.orEmpty(), shareConfig)
                }
            }
            builder.setNeutralButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            builder.setOnDismissListener {
                screenShareOptionsDialog = null
            }
        }
        screenShareOptionsDialog = builder.create()
        screenShareOptionsDialog?.setCanceledOnTouchOutside(false)
        screenShareOptionsDialog?.show()
    }

    private fun toggleAnnotationPermissionDialog(show: Boolean, personID: String?) {
        if (show) {
            // Show the permission dialog
            annotationPermissionDialog = AlertDialog.Builder(context)
                .setTitle("Live Annotation Permission")
                .setMessage("Annotation request received.")
                .setPositiveButton(getString(R.string.accept)) { _, _ ->
                    webexViewModel.handleAnnotationPermission(true, personID!!)
                }
                .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                    webexViewModel.handleAnnotationPermission(false, personID!!)
                }
                .create()

            annotationPermissionDialog.show()
        } else {
            if(annotationPermissionDialog.isShowing) annotationPermissionDialog.dismiss()
        }
    }

    fun needBackPressed(): Boolean {
        if (isIncomingActivity &&
                webexViewModel.currentCallId == null) {
            return false
        }

        return true
    }

    fun onBackPressed() {
        endCall()
    }


    private fun endCall() {
        if (isIncomingActivity) {
            if (!incomingCallBottomSheetFragment.isVisible) {
                webexViewModel.currentCallId?.let {
                    webexViewModel.hangup(it)
                    activity?.finish()
                } ?: run {
                    activity?.finish()
                }
            } else {
                incomingCallBottomSheetFragment.dismiss()
                endIncomingCall()
            }
        } else {
            webexViewModel.currentCallId?.let {
                webexViewModel.hangup(it)
                activity?.finish()
            } ?: run {
                activity?.finish()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun incomingLayoutState(hide: Boolean) {
        if (hide) {
            binding.mainContentLayout.visibility = View.VISIBLE
        } else {
            binding.mainContentLayout.visibility = View.GONE
            if (incomingInfoAdapter.info.size > 0) {
                for (model in incomingInfoAdapter.info) {
                    model.isEnabled = true
                }
                showIncomingCallBottomSheet()
            }
        }
    }

    private fun videoViewTextColorState(hidden: Boolean) {
        var hide = hidden
        if (hide && webexViewModel.isRemoteScreenShareON) {
            hide = false
        }

        if (hide) {
            binding.callingHeader.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        } else {
            val status = isMainStageRemoteVideoUnMuted()
            if (status) {
                binding.callingHeader.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                binding.tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }
    }

    private fun localVideoViewState(toHide: Boolean) {
        if (toHide) {
            binding.localViewLayout.visibility = View.GONE
            binding.ibSwapCamera.visibility = View.GONE
        } else {
            binding.localViewLayout.visibility = View.VISIBLE
            binding.ibSwapCamera.visibility = View.VISIBLE
            binding.localView.setZOrderOnTop(true)
        }
    }

    private fun screenShareViewRemoteState(toHide: Boolean, needResize: Boolean = true) {
        Log.d(TAG, "screenShareViewRemoteState toHide: $toHide")
        if (toHide) {
            binding.screenShareView.visibility = View.GONE
            webexViewModel.isRemoteScreenShareON = false
        } else {
            binding.screenShareView.visibility = View.VISIBLE
            webexViewModel.isRemoteScreenShareON = true
        }
        if (needResize) {
            resizeRemoteVideoView()
        }
    }

    private fun resizeRemoteVideoView() {
        Log.d(TAG, "resizeRemoteVideoView isRemoteScreenShareON ${webexViewModel.isRemoteScreenShareON}")
        if (webexViewModel.isRemoteScreenShareON) {
            val width = resources.getDimension(R.dimen.remote_video_view_width).toInt()
            val height = resources.getDimension(R.dimen.remote_video_view_height).toInt()

            val params = ConstraintLayout.LayoutParams(width, height)
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
            params.marginStart = resources.getDimension(R.dimen.remote_video_view_margin_start).toInt()
            params.bottomMargin = resources.getDimension(R.dimen.remote_video_view_margin_Bottom).toInt()
            binding.remoteViewLayout.layoutParams = params
            binding.remoteViewLayout.background = ContextCompat.getDrawable(requireActivity(), R.drawable.surfaceview_border)
            binding.remoteView.setZOrderOnTop(true)
        } else {
            val params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
            params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
            params.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            binding.remoteViewLayout.layoutParams = params
            binding.remoteViewLayout.background = ContextCompat.getDrawable(requireActivity(), R.drawable.surfaceview_transparent_border)
            binding.remoteView.setZOrderOnTop(false)
        }
    }

    private fun videoViewState(toHide: Boolean) {
        localVideoViewState(toHide)
        if (toHide) {
            binding.remoteViewLayout.visibility = View.GONE
        } else {
            val status = isMainStageRemoteVideoUnMuted()
            if (status) {
                binding.remoteViewLayout.visibility = View.VISIBLE
            }
        }

        videoViewTextColorState(toHide)
        videoButtonState(toHide)
    }

    private fun videoButtonState(videoViewHidden: Boolean) {
        if (videoViewHidden) {
            binding.ibVideo.background = ContextCompat.getDrawable(requireActivity(),
                R.drawable.turn_off_video_active
            )
        } else {
            binding.ibVideo.background = ContextCompat.getDrawable(requireActivity(), R.drawable.turn_on_video_default)
        }
    }

    private fun endIncomingCall() {
        webexViewModel.currentCallId?.let {
            endIncomingCall(it)
        } ?: run {
            activity?.finish()
        }
    }

    private fun endIncomingCall(callId: String) {
        if (webexViewModel.incomingCallJoinedCallId != null && webexViewModel.incomingCallJoinedCallId == callId)
            webexViewModel.hangup(callId)
        else
            webexViewModel.rejectCall(callId)
    }

    private fun onCallConnected(callId: String, isCucmCall: Boolean, isWebexCallingOrWebexForBroadworks: Boolean) {
        Log.d(TAG, "CallControlsFragment onCallConnected callerId: $callId, currentCallId: ${webexViewModel.currentCallId}")
        if (webexViewModel.currentCallId.isNullOrEmpty() && (isCucmCall || isWebexCallingOrWebexForBroadworks)) {
            webexViewModel.currentCallId = callId
        }

        mHandler.post {
            val layout = webexViewModel.getCompositedLayout()
            Log.d(TAG, "onCallConnected getCompositedLayout: $layout")
            binding.ivNetworkSignal.visibility = View.VISIBLE
            webexViewModel.setCompositedLayout(layout)
            webexViewModel.setRemoteVideoRenderMode(callId, webexViewModel.scalingMode)

            if (!webexViewModel.multistreamNewApproach) {
                binding.tvRemoteUserName.visibility = View.GONE
                binding.ivRemoteAudioState.visibility = View.GONE

                webexViewModel.getCall(callId)?.setMultiStreamObserver(object : MultiStreamObserver {
                    override fun onAuxStreamChanged(event: MultiStreamObserver.AuxStreamChangedEvent?) {
                        Log.d(tag, "MultiStreamObserver onAuxStreamChanged : $event")
                        mHandler.post {
                            val auxStream: AuxStream? = event?.getAuxStream()

                            when (event) {
                                is MultiStreamObserver.AuxStreamOpenedEvent -> {
                                    if (event.isSuccessful()) {
                                        val auxStreamViewHolder = mAuxStreamViewMap[event.getRenderView()]
                                        Log.d(tag, "MultiStreamObserver AuxStreamOpenedEvent successful")
                                        auxStreamViewHolder?.let {
                                            binding.viewAuxVideos.addView(it.item)
                                            val membership = auxStream?.getPerson()
                                            Log.d(tag, "MultiStreamObserver AuxStreamOpenedEvent successful membership: " + membership?.getDisplayName())
                                            it.textView.text = membership?.getDisplayName()
                                        }
                                    } else {
                                        Log.d(tag, "MultiStreamObserver AuxStreamOpenedEvent failed: " + event.getError()?.errorMessage)
                                        mAuxStreamViewMap.remove(event.getRenderView())
                                    }
                                }
                                is MultiStreamObserver.AuxStreamClosedEvent -> {
                                    if (event.isSuccessful()) {
                                        Log.d(tag, "MultiStreamObserver AuxStreamClosedEvent successful")
                                        val auxStreamViewHolder = mAuxStreamViewMap[event.getRenderView()]
                                        mAuxStreamViewMap.remove(event.getRenderView())
                                        binding.viewAuxVideos.removeView(auxStreamViewHolder?.item)
                                    } else {
                                        Log.d(tag, "MultiStreamObserver AuxStreamClosedEvent failed: " + event.getError()?.errorMessage)
                                    }
                                }
                                is MultiStreamObserver.AuxStreamSendingVideoEvent -> {
                                    Log.d(tag, "AuxStreamSendingVideoEvent: " + auxStream?.isSendingVideo())
                                    auxStream?.let {
                                        val auxStreamViewHolder = mAuxStreamViewMap[it.getRenderView()]

                                        if (auxStreamViewHolder != null) {
                                            if (it.isSendingVideo()) {
                                                auxStreamViewHolder.viewAvatar.visibility = View.GONE
                                            } else {
                                                val membership = it.getPerson()
                                                membership?.let { member ->
                                                    if (member.getPersonId().isNotEmpty()) {
                                                        auxStreamViewHolder.viewAvatar.visibility = View.VISIBLE
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                is MultiStreamObserver.AuxStreamPersonChangedEvent -> {
                                    Log.d(tag, "MultiStreamObserver AuxStreamPersonChangedEvent getPerson: " + auxStream?.getPerson() + " from: " + event.from() + " to: " + event.to())
                                    auxStream?.let {
                                        val auxStreamViewHolder = mAuxStreamViewMap[it.getRenderView()]
                                        val membership = it.getPerson()
                                        membership?.let { member ->
                                            Log.d(tag, "MultiStreamObserver AuxStreamPersonChangedEvent name: " + member.getDisplayName())
                                            auxStreamViewHolder?.viewAvatar?.visibility = if (it.isSendingVideo()) View.GONE else View.VISIBLE
                                            auxStreamViewHolder?.textView?.text = member.getDisplayName()
                                        }
                                    }
                                }
                                is MultiStreamObserver.AuxStreamSizeChangedEvent -> {
                                    Log.d(tag, "MultiStreamObserver AuxStreamSizeChangedEvent width: " + event.getAuxStream()?.getSize()?.width +
                                            " height: " + event.getAuxStream()?.getSize()?.height)
                                }
                            }
                        }
                    }

                    override fun onAuxStreamAvailable(): View? {
                        Log.d(tag, "MultiStreamObserver onAuxStreamAvailable")
                        return getMediaStreamView(false, MediaStreamType.Unknown, null)
                    }

                    override fun onAuxStreamUnavailable(): View? {
                        Log.d(tag, "MultiStreamObserver onAuxStreamUnavailable")
                        return null
                    }

                })
            }

            if (callId == webexViewModel.currentCallId) {
                val callInfo = webexViewModel.getCall(callId)

                var isSelfVideoMuted = true
                callInfo?.let { _callInfo ->
                    isSelfVideoMuted = !_callInfo.isSendingVideo()
                    webexViewModel.isRemoteVideoMuted = !_callInfo.isReceivingVideo()
                    Log.d(TAG, "CallControlsFragment onCallConnected isAudioOnly: ${_callInfo.isAudioOnly()} isSelfVideoMuted: ${isSelfVideoMuted}, webexViewModel.isRemoteVideoMuted: ${webexViewModel.isRemoteVideoMuted}")
                    Log.d(TAG, "CallControlsFragment onCallConnected from: ${_callInfo.getFrom()?.getDisplayName()} to: ${_callInfo.getTo()?.getDisplayName()}")
                }

                if (isIncomingActivity) {
                    if (callId == webexViewModel.currentCallId) {
                        binding.videoCallLayout.visibility = View.VISIBLE
                        incomingLayoutState(true)
                    }
                }

                webexViewModel.isLocalVideoMuted = isSelfVideoMuted

                onVideoStreamingChanged(callId)

                if (webexViewModel.isLocalVideoMuted) {
                    localVideoViewState(true)
                    videoButtonState(true)
                } else {
                    localVideoViewState(false)
                    videoButtonState(false)
                }

                if (webexViewModel.isRemoteVideoMuted) {
                    binding.remoteViewLayout.visibility = View.GONE
                } else {
                    val status = isMainStageRemoteVideoUnMuted()
                    if (status) {
                        binding.remoteViewLayout.visibility = View.VISIBLE
                    }
                }

                binding.controlGroup.visibility = View.VISIBLE

                screenShareButtonVisibilityState()
                directTransferButtonStateUpdate()
                videoViewTextColorState(webexViewModel.isRemoteVideoMuted)
                updateAudioModeButton()
            }
        }
    }

    private fun updateAudioModeButton() {
        webexViewModel.getCurrentAudioOutputMode()?.let { outputMode ->
            when (outputMode) {
                Call.AudioOutputMode.PHONE -> binding.ibAudioMode.setImageResource(R.drawable.ic_earpiece)
                Call.AudioOutputMode.SPEAKER -> binding.ibAudioMode.setImageResource(R.drawable.ic_speaker)
                Call.AudioOutputMode.BLUETOOTH_HEADSET -> binding.ibAudioMode.setImageResource(R.drawable.ic_bluetooth)
                Call.AudioOutputMode.HEADSET -> binding.ibAudioMode.setImageResource(R.drawable.ic_headset)
            }
        }
    }

    private fun isMediaStreamAlreadyPinned(personID: String?, streamType: MediaStreamType?) : Boolean {
        personID?.let { id ->
            webexViewModel.getMediaStreams()?.let { streamList ->

                for (stream in streamList) {
                    Log.d(TAG, "CallControlsFragment isMediaStreamAlreadyPinned personID: $personID, isPinned: ${stream.isPinned()}, streamType: ${stream.getStreamType()}")
                }

                val stream = streamList.find { stream -> stream.getStreamType() == streamType }
                stream?.let {
                    return it.isPinned()
                }
            }
        }

        return false
    }

    private fun getMediaStreamView(newApproach: Boolean, type: MediaStreamType, personID: String?): MediaRenderView {
        val auxStreamView: View = LayoutInflater.from(activity).inflate(R.layout.multistream_view, null)
        val auxStreamViewHolder = AuxStreamViewHolder(auxStreamView)
        mAuxStreamViewMap[auxStreamViewHolder.mediaRenderView] = auxStreamViewHolder
        if (newApproach) {
            auxStreamViewHolder.audioState.visibility = View.VISIBLE
            auxStreamViewHolder.moreOption.visibility = View.VISIBLE
            auxStreamViewHolder.streamType = type
            auxStreamViewHolder.personID = personID
            auxStreamViewHolder.moreOption.tag = auxStreamViewHolder.mediaRenderView
            val alreadyPinned = isMediaStreamAlreadyPinned(personID, type)

            Log.d(TAG, "getMediaStreamView personID $personID, alreadyPinned: $alreadyPinned, type: $type")
            if (alreadyPinned) {
                auxStreamViewHolder.pinStreamImageView.visibility = View.VISIBLE
                auxStreamViewHolder.parentLayout.background = ContextCompat.getDrawable(requireActivity(), R.drawable.border_category_c)
            } else {
                auxStreamViewHolder.pinStreamImageView.visibility = View.GONE
                auxStreamViewHolder.parentLayout.background = ContextCompat.getDrawable(requireActivity(), R.drawable.border_category_b)
            }

            auxStreamViewHolder.moreOption.setOnClickListener {
                val view = auxStreamViewHolder.moreOption.tag as MediaRenderView
                val holder = mAuxStreamViewMap[view]
                Log.d(TAG, "CallControlsFragment getMediaStreamView moreOption tag: ${holder?.streamType}, personID: ${holder?.personID}")
                webexViewModel.currentCallId?.let {
                    showMediaStreamBottomSheet(webexViewModel.getCall(it), view, holder?.personID, isMediaStreamAlreadyPinned(holder?.personID, holder?.streamType))
                }
            }
        } else {
            auxStreamViewHolder.moreOption.visibility = View.GONE
            auxStreamViewHolder.audioState.visibility = View.GONE
        }
        return auxStreamViewHolder.mediaRenderView
    }

    private fun onScreenShareStateChanged(callId: String, label: String) {
        Log.d(TAG, "CallControlsFragment onScreenShareStateChanged callerId: $callId, label: $label")

        if (webexViewModel.currentCallId != callId) {
            return
        }

        mHandler.post {

            val callInfo = webexViewModel.getCall(callId)

            val remoteSharing = isReceivingSharing(callId)
            val localSharing = isLocalSharing(callId)
            Log.d(TAG, "CallControlsFragment onScreenShareStateChanged isRemoteSharing: $remoteSharing, isLocalSharing: $localSharing")

            if (localSharing) {
                updateScreenShareButtonState(ShareButtonState.ON)
            } else {
                updateScreenShareButtonState(ShareButtonState.OFF)
            }
        }
    }

    // remoteStartedSharing : true means remote has started sharing else false means remote has ended sharing
    private fun onScreenShareVideoStreamInUseChanged(callId: String, remoteStartedSharing: Boolean? = null) {
        Log.d(TAG, "CallControlsFragment onScreenShareVideoStreamInUseChanged callerId: $callId")
        if (webexViewModel.currentCallId != callId) {
            return
        }

        mHandler.post {
            val remoteSharing = isReceivingSharing(callId)
            val localSharing = isLocalSharing(callId)
            Log.d(TAG, "CallControlsFragment onScreenShareVideoStreamInUseChanged isRemoteSharing: ${remoteSharing}, isLocalSharing: ${localSharing}")
            if (remoteSharing) {
                binding.controlGroup.visibility = View.GONE
                screenShareViewRemoteState(false)
                val view = webexViewModel.getSharingRenderView(callId)
                if (view == null || (remoteStartedSharing != null)) {
                    webexViewModel.setSharingRenderView(callId, binding.screenShareView)
                }
            }
            else {
                onVideoStreamingChanged(callId)
                screenShareViewRemoteState(true)
                binding.controlGroup.visibility = View.VISIBLE
            }

            videoViewTextColorState(!remoteSharing)
        }
    }

    private fun setRemoteVideoInformation(name: String, audioMuted: Boolean) {
        binding.tvRemoteUserName.text = name

        if (audioMuted) {
            binding.ivRemoteAudioState.setImageResource(R.drawable.ic_microphone_muted_bold)
        } else {
            binding.ivRemoteAudioState.setImageResource(R.drawable.ic_microphone_36)
        }
    }

    private fun onVideoStreamingChanged(callId: String) {
        Log.d(TAG, "CallControlsFragment onVideoStreamingChanged callerId: $callId")

        if (webexViewModel.currentCallId == null) {
            return
        }

        mHandler.post {
            if(isAdded) {
                if (webexViewModel.isLocalVideoMuted) {
                    localVideoViewState(true)
                } else {
                    localVideoViewState(false)
                    val pair = webexViewModel.getVideoRenderViews(callId)
                    if (pair.first == null) {
                        webexViewModel.setVideoRenderViews(
                            callId,
                            binding.localView,
                            binding.remoteView
                        )
                    }
                }

                if (webexViewModel.isRemoteVideoMuted) {
                    binding.remoteViewLayout.visibility = View.GONE

                    binding.ivRemoteAudioState.visibility = View.GONE
                    binding.tvRemoteUserName.visibility = View.GONE
                } else {
                    if (webexViewModel.isRemoteScreenShareON) {
                        resizeRemoteVideoView()
                    }
                    val status = isMainStageRemoteVideoUnMuted()

                    if (status) {
                        binding.remoteViewLayout.visibility = View.VISIBLE
                        val pair = webexViewModel.getVideoRenderViews(callId)
                        if (pair.second == null && webexViewModel.callCapability != WebexRepository.CallCap.Audio_Only) {
                            webexViewModel.setVideoRenderViews(
                                callId,
                                binding.localView,
                                binding.remoteView
                            )
                        }

                        if (webexViewModel.streamMode != Phone.VideoStreamMode.COMPOSITED) {
                            if (!webexViewModel.multistreamNewApproach) {
                                binding.ivRemoteAudioState.visibility = View.GONE
                                binding.tvRemoteUserName.visibility = View.GONE
                            } else {
                                binding.ivRemoteAudioState.visibility = View.VISIBLE
                                binding.tvRemoteUserName.visibility = View.VISIBLE
                            }
                        } else {
                            binding.ivRemoteAudioState.visibility = View.GONE
                            binding.tvRemoteUserName.visibility = View.GONE
                        }
                    }
                }

                videoViewTextColorState(webexViewModel.isRemoteVideoMuted)

                Log.d(
                    TAG,
                    "CallControlsFragment onVideoStreamingChanged isLocalVideoMuted: ${webexViewModel.isLocalVideoMuted}, isRemoteVideoMuted: ${webexViewModel.isRemoteVideoMuted}"
                )

                if (webexViewModel.isLocalVideoMuted) {
                    videoButtonState(true)
                } else {
                    videoButtonState(false)
                }
                if (isInPipMode && !webexViewModel.isRemoteVideoMuted) {
                    localVideoViewState(true)
                    binding.ivRemoteAudioState.visibility = View.GONE
                    binding.tvRemoteUserName.visibility = View.GONE
                }
            }
        }
    }

    private fun isMainStageRemoteVideoUnMuted() : Boolean {
        var status = false
         if (!webexViewModel.isRemoteVideoMuted) {
             Log.d(TAG, "CallControlsFragment isMainStageRemoteVideoUnMuted isRemoteVideoMuted false")
             val streams = webexViewModel.getMediaStreams()
             Log.d(TAG, "CallControlsFragment isMainStageRemoteVideoUnMuted streams: ${streams?.size}")
             streams?.let { streamList ->
                 val stream = streamList.find { stream -> stream.getStreamType() == MediaStreamType.Stream1 }
                 stream?.let { st ->
                     Log.d(TAG, "CallControlsFragment isMainStageRemoteVideoUnMuted found stream")
                     status = st.getPerson()?.isSendingVideo() ?: false
                 }
             } ?: run {
                 status = false
             }
         }
        Log.d(TAG, "CallControlsFragment isMainStageRemoteVideoUnMuted return status: $status")
        return status
    }

    private fun toggleAudioMode(mode: AudioMode) {
        when (mode) {
            AudioMode.SPEAKER -> {
                webexViewModel.switchAudioMode(Call.AudioOutputMode.SPEAKER) {
                    if (it.data == true)
                        binding.ibAudioMode.setImageResource(R.drawable.ic_speaker)
                }
            }
            AudioMode.BLUETOOTH -> {
                webexViewModel.switchAudioMode(Call.AudioOutputMode.BLUETOOTH_HEADSET) {
                    if (it.data == true)
                        binding.ibAudioMode.setImageResource(R.drawable.ic_bluetooth)
                }
            }
            AudioMode.EARPIECE -> {
                webexViewModel.switchAudioMode(Call.AudioOutputMode.PHONE) {
                    if (it.data == true)
                        binding.ibAudioMode.setImageResource(R.drawable.ic_earpiece)
                }
            }
            AudioMode.WIRED_HEADSET -> {
                webexViewModel.switchAudioMode(Call.AudioOutputMode.HEADSET) {
                    if (it.data == true)
                        binding.ibAudioMode.setImageResource(R.drawable.ic_headset)
                }
            }
        }
    }

    enum class AudioMode {
        SPEAKER,
        EARPIECE,
        BLUETOOTH,
        WIRED_HEADSET
    }

    internal fun handleFCMIncomingCall(callId: String) {
        mHandler.post {
            webexViewModel.setFCMIncomingListenerObserver(callId)
        }
    }

    private fun onIncomingCall(call: Call?, showBottomSheet : Boolean = true) {
        mHandler.post {
            Log.d(TAG, "CallControlsFragment onIncomingCall callerId: ${call?.getCallId()}, callInfo title: ${call?.getTitle()}")
            // Start call monitoring service when incoming call is received.
            startCallMonitoringForegroundService()
            binding.incomingCallHeader.visibility = View.GONE
            val schedules= call?.getSchedules()
            incomingLayoutState(false)
            val twentyFourHrsFromNow = Date().time + 86400000
            // Only get meetings till next 24 hours.
            val filteredMeetings = schedules?.filter { it.getStart()?.time ?: (twentyFourHrsFromNow + 10) <= twentyFourHrsFromNow}
            filteredMeetings?.let { meetings ->
                for (meeting in meetings) {
                    Log.d(TAG,"subject = ${meeting.getSubject()} & meetingId = ${meeting.getId()}")
                    if (!checkIncomingAdapterList(meeting)) {
                        val model = MeetingInfoModel.convertToMeetingInfoModel(call, meeting)
                        incomingInfoAdapter.info.add(model)
                        Log.d(TAG, "CallControlsFragment onIncomingCall meetings size: ${meetings.size}")
                    }
                }
            } ?: run {
                val group = call?.isGroupCall() ?: false
                if (group) {
                    val model = SpaceIncomingCallModel(call)
                    incomingInfoAdapter.info.add(model)
                } else {
                    val model = OneToOneIncomingCallModel(call)
                    incomingInfoAdapter.info.add(model)
                }
            }

            val incomingCalls = incomingInfoAdapter.info.filter { it.call?.getCallId() != webexViewModel.currentCallId }
            incomingInfoAdapter.info.clear()
            incomingInfoAdapter.info.addAll(incomingCalls)
            if(showBottomSheet) {
                if(!incomingInfoAdapter.info.isEmpty()) {
                    showIncomingCallBottomSheet()
                }
            }
        }
    }

    private fun showIncomingCallBottomSheet() {
        Log.d(TAG, "showIncomingCallBottomSheet "+incomingCallBottomSheetFragment)
        if (!incomingCallBottomSheetFragment.isAdded && !incomingCallBottomSheetFragment.isVisible) {
            incomingCallBottomSheetFragment.adapter = incomingInfoAdapter
            incomingCallBottomSheetFragment.isCancelable = false
            activity?.supportFragmentManager?.let { incomingCallBottomSheetFragment.show(it, IncomingCallBottomSheetFragment.TAG) }
            incomingCallBottomSheetFragment.view?.requestLayout()
        }
        incomingInfoAdapter.notifyDataSetChanged()
    }

    private fun checkIncomingAdapterList(item: CallSchedule): Boolean {
        incomingInfoAdapter.info.forEach { _model ->
            if ((_model is MeetingInfoModel) && (_model.meetingId == item.getId())) {
                return true
            }
        }

        return false
    }

    private fun onCallJoined(call: Call?) {
        Log.d(TAG, "CallControlsFragment onCallJoined callerId: ${call?.getCallId().orEmpty()}, currentCallId: ${webexViewModel.currentCallId}")
        mHandler.post {
            if (call?.getCallId().orEmpty() == webexViewModel.currentCallId) {
                showCallHeader(call?.getCallId().orEmpty())
                call?.let {
                    val schedules = it.getSchedules()
                    schedules?.let {
                        binding.callingHeader.text = getString(R.string.meeting)
                    }
                }
            }
            if (callingActivity == 1) {
                webexViewModel.incomingCallJoinedCallId = call?.getCallId().orEmpty()
            }
            Log.d(TAG,"CallControlsFragment callingHeader text: ${binding.callingHeader.text}")
        }
    }

    private fun showCallHeader(callId: String) {
        mHandler.post {
            try {
                if (breakout == null) {
                    val callInfo = webexViewModel.getCall(callId)
                    Log.d(TAG, "CallControlsFragment showCallHeader callerId: $callId")
                    binding.tvName.text = callInfo?.getTitle()
                    binding.callingHeader.text = getString(R.string.onCall)
                }
            } catch (e: Exception) {
                Log.d(TAG, "error: ${e.message}")
            }
        }
    }

    private fun onCallFailed(callId: String, failedError: WebexError<Any>?) {
        Log.d(TAG, "CallControlsFragment onCallFailed callerId: $callId")

        mHandler.post {
            if (webexViewModel.isAddedCall) {
                resumePrevCallIfAdded(callId)
                updateCallHeader()
            }

            callFailed = !webexViewModel.isAddedCall

            val callActivity = activity as CallActivity?
            callActivity?.alertDialog(!webexViewModel.isAddedCall, failedError?.errorMessage.orEmpty())
        }
    }

    private fun onCallDisconnected(call: Call?) {
        call?.let { _call ->
            Log.d(TAG, "CallControlsFragment onCallDisconnected callerId: ${_call.getCallId().orEmpty()}")
            mHandler.post {
                val schedules = call.getSchedules()
                schedules?.let {
                    incomingLayoutState(false)
                } ?: run {
                    if (call.isGroupCall()) {
                        incomingLayoutState(false)
                    }
                }
            }
        }
    }

    private fun onCallTerminated(callId: String) {
        Log.d(TAG, "CallControlsFragment onCallTerminated callerId: $callId")
        webexViewModel.clearCallObservers(callId)
        CallObjectStorage.removeCallObject(callId)

        mHandler.post {
            if (webexViewModel.isAddedCall) {
                resumePrevCallIfAdded(callId)
                updateCallHeader()
                initAddedCallControls()
            }

            if (!callFailed && !webexViewModel.isAddedCall) {
                callEndedUIUpdate(callId, true)
            }
            webexViewModel.isAddedCall = false
        }
    }

    private fun callEndedUIUpdate(callId: String, terminated: Boolean = false) {
        if (isIncomingActivity) {
            for (model in incomingInfoAdapter.info) {
                if ( (model is OneToOneIncomingCallModel) && (model.call?.getCallId() == callId)) {
                    incomingInfoAdapter.info.remove(model)
                    break
                } else if (model is MeetingInfoModel) {
                    if (Date().after(model.endTime)) {
                        incomingInfoAdapter.info.remove(model)
                        break
                    }

                    if (terminated && (model.call?.getCallId().orEmpty() == callId)) {
                        incomingInfoAdapter.info.remove(model)
                        break
                    }
                } else if ( (model is SpaceIncomingCallModel) && (model.call?.getCallId() == callId)) {
                    incomingInfoAdapter.info.remove(model)
                    break
                }
            }
            incomingInfoAdapter.notifyDataSetChanged()

            if (incomingInfoAdapter.info.isNotEmpty()) {
                webexViewModel.currentCallId = null
                incomingLayoutState(false)
            } else {
                if(webexViewModel.hasAnyoneJoined() && incomingCallBottomSheetFragment.isVisible) {
                    incomingCallBottomSheetFragment.dismiss()
                } else activity?.finish()
            }
        } else {
            activity?.finish()
        }
    }

    private fun initAddedCallControls() {
        binding.ibTransferCall.visibility = View.INVISIBLE
        binding.ibVideo.visibility = View.VISIBLE
        binding.ibAdd.visibility = View.VISIBLE
        binding.ibMerge.visibility = View.INVISIBLE
    }

    private fun onNewCallHeader(callerId: String?) {
        binding.callingHeader.text = getString(R.string.calling)
        binding.tvName.text = callerId
    }

    private fun resumePrevCallIfAdded(callId: String) {
        //resume old call
        if (callId == webexViewModel.currentCallId) {
            webexViewModel.currentCallId = webexViewModel.oldCallId
            Log.d(TAG, "resumePrevCallIfAdded currentCallId = ${webexViewModel.currentCallId}")
            webexViewModel.currentCallId?.let { _currentCallId ->
                webexViewModel.holdCall(_currentCallId)
            }
            webexViewModel.oldCallId = null //old is  disconnected need to make it null
        }
    }

    private fun updateCallHeader() {
        webexViewModel.currentCallId?.let {
            showCallHeader(it)
        }
    }

    private fun startAssociatedCall(dialNumber: String, associationType: CallAssociationType, audioCall: Boolean) {
        Log.d(tag, "startAssociatedCall dialNumber = $dialNumber : associationType = $associationType : audioCall = $audioCall")
        webexViewModel.currentCallId?.let { callId ->
            onNewCallHeader(callId)
            webexViewModel.startAssociatedCall(callId, dialNumber, associationType, audioCall)
        }
    }

    private fun transferCall() {
        Log.d(tag, "transferCall currentCallId = ${webexViewModel.currentCallId}, oldCallId = ${webexViewModel.oldCallId}")
        if (webexViewModel.currentCallId != null && webexViewModel.oldCallId != null) {
            webexViewModel.transferCall(webexViewModel.oldCallId!!, webexViewModel.currentCallId!!)
        }
    }

    private fun directTransferCall(toPhoneNumber: String) {
        Log.d(tag, "directTransferCall currentCallId = ${webexViewModel.currentCallId}")
        webexViewModel.currentCallId?.let { callId ->
            webexViewModel.directTransferCall(callId, toPhoneNumber) { result ->
                if (!result.isSuccessful) {
                    Log.d(tag, "DirectTransferCall result : ${result.error?.errorMessage}")
                }
            }
        }
    }

    private fun mergeCalls() {
        Log.d(tag, "mergeCalls currentCallId = ${webexViewModel.currentCallId}, targetCallId = ${webexViewModel.oldCallId}")
        if (webexViewModel.currentCallId != null && webexViewModel.oldCallId != null) {
            webexViewModel.mergeCalls(webexViewModel.currentCallId!!, webexViewModel.oldCallId!!)
        }
    }

    private fun switchToAudioOrVideoCall() {
        Log.d(tag, "callId = ${webexViewModel.currentCallId}")
        if(webexViewModel.currentCallId != null){
                val callInfo = webexViewModel.getCall(webexViewModel.currentCallId.toString())
                if (callInfo != null) {
                    webexViewModel.switchToAudioOrVideoCall(webexViewModel.currentCallId.toString(), callInfo.isAudioOnly()) { result ->
                        if (!result.isSuccessful) {
                            Log.d(tag, "SwitchToAudioOrVideoCall call result : ${result.error?.errorMessage}")
                        }
                    }
                }
         }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        handleActivityResult(requestCode, resultCode, data)
    }

    private fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val callNumber = data?.getStringExtra(CALLER_ID) ?: ""
            //start call association to add new person on call
            startAssociatedCall(callNumber, CallAssociationType.Transfer, true)
        } else if (requestCode == REQUEST_CODE_BLINDTRANSFER && resultCode == Activity.RESULT_OK) {
            val callNumber = data?.getStringExtra(CALLER_ID) ?: ""
            directTransferCall(callNumber)
        } else if (
            (requestCode == REQUEST_CODE_SPOKEN_LANGUAGE || requestCode == REQUEST_CODE_TRANSLATION_LANGUAGE) &&
            resultCode == Activity.RESULT_OK
        ) {
            captionsController.handleLanguageSelection(
                requireContext(),
                requestCode,
                data?.getParcelableExtra<LanguageData>(CLOSED_CAPTION_LANGUAGE_ITEM)
            )
        }
    }

    private fun muteSelfVideo(value: Boolean) {
        webexViewModel.currentCallId?.let {
            webexViewModel.muteSelfVideo(it, value)
        }
    }

    private fun showTranscriptions(call: Call?) {
        Log.d(TAG, "showTranscriptions")
        call?.let {
            val bottomSheet =  TranscriptionsDialogFragment()
            bottomSheet.show(childFragmentManager, TranscriptionsDialogFragment::class.java.simpleName)
        }
    }

    private fun toggleWXAClickListener(call: Call?) {
        if (call?.getWXA()?.canControlWXA() == true) {
            val isEnabled = call.getWXA().isEnabled()
            call.getWXA().enableWXA(!isEnabled, CompletionHandler { result ->
                Log.d(TAG, "enableWXA callback: result ${{result.isSuccessful}} isEnabled ${result.data}")
            })
        }
    }

    private fun receivingVideoListener(call: Call?) {
        Log.d(TAG, "receivingVideoListener")
        call?.let {
            if (it.isReceivingVideo()) {
                webexViewModel.setReceivingVideo(it, false)
            } else {
                webexViewModel.setReceivingVideo(it, true)
            }
        }
    }

    private fun receivingAudioListener(call: Call?) {
        Log.d(TAG, "receivingAudioListener")
        call?.let {
            if (it.isReceivingAudio()) {
                webexViewModel.setReceivingAudio(it, false)
            } else {
                webexViewModel.setReceivingAudio(it, true)
            }
        }
    }

    private fun receivingSharingListener(call: Call?) {
        Log.d(TAG, "receivingSharingListener")
        call?.let {
            if (it.isReceivingSharing()) {
                webexViewModel.setReceivingSharing(it, false)
            } else {
                webexViewModel.setReceivingSharing(it, true)
            }
        }
    }

    private fun swapVideoClickListener(call: Call?) {
        Log.d(TAG, "swapVideoClickListener")
        if (webexViewModel.isVideoViewsSwapped) {
            webexViewModel.setVideoRenderViews(webexViewModel.currentCallId.orEmpty(), binding.remoteView, binding.localView)
            webexViewModel.isVideoViewsSwapped = false
        } else {
            webexViewModel.setVideoRenderViews(webexViewModel.currentCallId.orEmpty(), binding.localView, binding.remoteView)
            webexViewModel.isVideoViewsSwapped = true
        }
    }

    private fun forceLandscapeClickListener(call: Call?) {
        Log.d(TAG, "forceLandscapeClickListener isSendingVideoForceLandscape: ${webexViewModel.isSendingVideoForceLandscape}")
        val value = !webexViewModel.isSendingVideoForceLandscape
        webexViewModel.forceSendingVideoLandscape(webexViewModel.currentCallId.orEmpty(), value)
    }

    private fun cameraOptionsClickListener(call: Call?) {
        Log.d(TAG, "cameraOptionsClickListener")
        showCameraOptionsBottomSheet(call)
    }

    private fun multiStreamOptionsClickListener(call: Call?) {
        Log.d(TAG, "multiStreamOptionsClickListener")
        showMultiStreamOptionsBottomSheetFragment(call)
    }

    private fun sendDTMFClickListener(call: Call?) {
        Log.d(TAG, "sendDTMFClickListener")
        showDialogForDTMF(requireContext(), getString(R.string.enter_dtmf_number), onPositiveButtonClick = { dialog: DialogInterface, number: String ->
            webexViewModel.sendDTMF(webexViewModel.currentCallId.orEmpty(), number)
            dialog.dismiss()
        }, onNegativeButtonClick = { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        })
    }

    private fun claimHostClickListener() {
        Log.d(TAG, "claimHostClickListener")
        showDialogForHostKey(requireContext(), getString(R.string.enter_host_key), onPositiveButtonClick = { dialog: DialogInterface, number: String ->
            webexViewModel.reclaimHost(number){
                if (it.isSuccessful) {
                    showSnackbar("Reclaim Host Successful")
                    Log.d(TAG, "Reclaim Host Successful")
                } else {
                    showSnackbar("Reclaim Host failed ${it.error?.errorMessage}")
                    Log.d(TAG, "Reclaim Host failed ${it.error?.errorMessage}")
                }
            }
            dialog.dismiss()
        }, onNegativeButtonClick = { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        })
    }

    private fun setCategoryAOptionClickListener(call: Call?) {
        Log.d(TAG, "setCategoryAOptionClickListener")
        showMultiStreamDataOptionsBottomSheetFragment(call, MultiStreamDataOptionsBottomSheetFragment.OptionType.CategoryA)
    }

    private fun setCategoryBOptionClickListener(call: Call?) {
        Log.d(TAG, "setCategoryBOptionClickListener")
        showMultiStreamDataOptionsBottomSheetFragment(call, MultiStreamDataOptionsBottomSheetFragment.OptionType.CategoryB)
    }

    private fun removeCategoryAClickListener(call: Call?) {
        Log.d(TAG, "removeCategoryAClickListener")
        webexViewModel.removeMediaStreamCategoryA()
    }

    private fun removeCategoryBClickListener(call: Call?) {
        Log.d(TAG, "removeCategoryBClickListener")
        webexViewModel.removeMediaStreamsCategoryB()
    }

    private fun categoryAOptionsOkListener(call: Call?, quality: MediaStreamQuality, duplicate: Boolean) {
        Log.d(TAG, "categoryAOptionsOkListener quality: $quality, duplicate: $duplicate")
        webexViewModel.setMediaStreamCategoryA(duplicate, quality)
    }

    private fun categoryBOptionsOkListener(call: Call?, numStreams: String?, quality: MediaStreamQuality) {
        Log.d(TAG, "categoryBOptionsOkListener numStreams: $numStreams, quality: $quality")
        var streams = 0
        if (!numStreams.isNullOrEmpty()) {
            streams = numStreams.toInt()
        }
        webexViewModel.setMediaStreamsCategoryB(streams, quality)
    }

    private fun showMultiStreamOptionsBottomSheetFragment(call: Call?) {
        multiStreamOptionsBottomSheetFragment.call = call
        activity?.supportFragmentManager?.let { multiStreamOptionsBottomSheetFragment.show(it, MultiStreamOptionsBottomSheetFragment.TAG) }
    }

    private fun showMultiStreamDataOptionsBottomSheetFragment(call: Call?, optionType: MultiStreamDataOptionsBottomSheetFragment.OptionType) {
        multiStreamDataOptionsBottomSheetFragment.call = call
        multiStreamDataOptionsBottomSheetFragment.type = optionType
        activity?.supportFragmentManager?.let { multiStreamDataOptionsBottomSheetFragment.show(it, MultiStreamDataOptionsBottomSheetFragment.TAG) }
    }

    private fun showMediaStreamBottomSheet(call: Call?, renderView: MediaRenderView, personID: String?, alreadyPinned: Boolean) {
        mediaStreamBottomSheetFragment.call = call
        mediaStreamBottomSheetFragment.renderView = renderView
        mediaStreamBottomSheetFragment.alreadyPinned = alreadyPinned
        mediaStreamBottomSheetFragment.personID = personID
        mediaStreamBottomSheetFragment.isMediaStreamsPinningSupported = webexViewModel.isMediaStreamsPinningSupported()
        Log.d(TAG, "mediaStreamBottomSheetFragment.isMediaStreamsPinningSupported: ${mediaStreamBottomSheetFragment.isMediaStreamsPinningSupported}")
        activity?.supportFragmentManager?.let { mediaStreamBottomSheetFragment.show(it, MediaStreamBottomSheetFragment.TAG) }
    }

    private fun pinStreamClickListener(renderView: MediaRenderView?, personID: String?, quality: MediaStreamQuality) {
        renderView?.let { view ->
            val streams = webexViewModel.getMediaStreams()
            streams?.let { streamList ->
                val stream = streamList.find { stream -> stream.getPerson()?.getPersonId() == personID }
                stream?.let { st ->
                    st.getPerson()?.let { person ->
                        Log.d(TAG, "pinStreamClickListener personID $personID, getDisplayName: ${person.getDisplayName()}")
                        webexViewModel.setMediaStreamCategoryC(person.getPersonId(), quality)
                    }
                }
            }
        }
    }

    private fun unpinStreamClickListener(renderView: MediaRenderView?, personID: String?) {
        Log.d(TAG, "unpinStreamClickListener")
        renderView?.let {
            webexViewModel.removeMediaStreamCategoryC(personID ?: "")
        }
    }

    private fun closeStreamStreamClickListener(renderView: MediaRenderView?, personID: String?) {
        Log.d(TAG, "closeStreamStreamClickListener")
    }

    private fun showCameraDataOptionsBottomSheetFragment(call: Call?, type: CameraOptionsDataBottomSheetFragment.OptionType, propertyText1: String?, propertyText2: String?, property2Visibility: Boolean) {
        cameraOptionsDataBottomSheetFragment.call = call
        cameraOptionsDataBottomSheetFragment.type = type
        cameraOptionsDataBottomSheetFragment.propertyText = propertyText1
        cameraOptionsDataBottomSheetFragment.propertyText2 = propertyText2
        cameraOptionsDataBottomSheetFragment.doMakeProperty2RelLayoutVisible = property2Visibility
        activity?.supportFragmentManager?.let { cameraOptionsDataBottomSheetFragment.show(it, CameraOptionsDataBottomSheetFragment.TAG) }
    }

    private fun zoomFactorClickListener(call: Call?) {
        Log.d(TAG, "zoomFactorClickListener")
        val propertyText1 = resources.getString(R.string.zoom_factor) + " " + String.format("%.1f", webexViewModel.getVideoZoomFactor())
        showCameraDataOptionsBottomSheetFragment(call, CameraOptionsDataBottomSheetFragment.OptionType.ZOOM_FACTOR, propertyText1, null, false)
    }

    private fun torchModeClickListener(call: Call?) {
        Log.d(TAG, "torchModeClickListener")
        if  (webexViewModel.torchMode == Call.TorchMode.OFF) {
            webexViewModel.torchMode = Call.TorchMode.ON
        } else if  (webexViewModel.torchMode == Call.TorchMode.ON) {
            webexViewModel.torchMode = Call.TorchMode.AUTO
        } else {
            webexViewModel.torchMode = Call.TorchMode.OFF
        }
        val status = webexViewModel.setCameraTorchMode(webexViewModel.torchMode)
        Log.d(TAG, "torchModeClickListener status: $status")
    }

    private fun flashModeClickListener(call: Call?) {
        Log.d(TAG, "flashModeClickListener")
        if  (webexViewModel.flashMode == Call.FlashMode.OFF) {
            webexViewModel.flashMode = Call.FlashMode.ON
        } else if  (webexViewModel.flashMode == Call.FlashMode.ON) {
            webexViewModel.flashMode = Call.FlashMode.AUTO
        } else {
            webexViewModel.flashMode = Call.FlashMode.OFF
        }
        val status = webexViewModel.setCameraFlashMode(webexViewModel.flashMode)
        Log.d(TAG, "flashModeClickListener status: $status")
    }

    private fun cameraFocusClickListener(call: Call?) {
        Log.d(TAG, "cameraFocusClickListener")
        val propertyText1 = resources.getString(R.string.camera_focus) + "\nPointX: "
        val propertyText2 = "PointY: "
        showCameraDataOptionsBottomSheetFragment(call, CameraOptionsDataBottomSheetFragment.OptionType.CAMERA_FOCUS_POINT, propertyText1, propertyText2, true)
    }

    private fun cameraCustomExposureClickListener(call: Call?) {
        Log.d(TAG, "cameraCustomExposureClickListener")
        val propertyText1 = resources.getString(R.string.camera_custom_exposure) + "\nDuration current: " + String.format("%f", webexViewModel.getCameraExposureDuration()?.current) +
                " \nmin: " + String.format("%f", webexViewModel.getCameraExposureDuration()?.min) + " max: " + String.format("%f", webexViewModel.getCameraExposureDuration()?.max)
        val propertyText2 = "ISO: " + String.format("%.1f", webexViewModel.getCameraExposureISO()?.current) +
                " \nmin: " + String.format("%.1f", webexViewModel.getCameraExposureISO()?.min) + " max: " + String.format("%.1f", webexViewModel.getCameraExposureISO()?.max)
        showCameraDataOptionsBottomSheetFragment(call, CameraOptionsDataBottomSheetFragment.OptionType.CUSTOM_EXPOSURE, propertyText1, propertyText2, true)
    }

    private fun cameraAutoExposureClickListener(call: Call?) {
        Log.d(TAG, "cameraAutoExposureClickListener")
        val propertyText1 = resources.getString(R.string.camera_auto_exposure) + " " + String.format("%.1f", webexViewModel.getCameraExposureTargetBias()?.current)+
                " \n min: " + String.format("%.1f", webexViewModel.getCameraExposureTargetBias()?.min) + " max: " + String.format("%.1f", webexViewModel.getCameraExposureTargetBias()?.max)
        showCameraDataOptionsBottomSheetFragment(call, CameraOptionsDataBottomSheetFragment.OptionType.AUTO_EXPOSURE, propertyText1, null, false)
    }

    private fun takePhotoClickListener(call: Call?) {
        Log.d(TAG, "takePhotoClickListener")
        val success = webexViewModel.takePhoto()
        if(!success) {
            Toast.makeText(activity, "Photo capture not supported", Toast.LENGTH_LONG).show()
        }
    }

    private fun zoomfactorValueSetListener(factor: Float) {
        Log.d(TAG, "zoomfactorValueSetListener factor: $factor")
        val status = webexViewModel.setVideoZoomFactor(factor)
        Log.d(TAG, "zoomfactorValueSetListener factor: $factor status: $status")
    }

    private fun cameraFocusValueSetClickListener(pointX: Float, pointY: Float) {
        Log.d(TAG, "cameraFocusValueSetClickListener pointX: $pointX, pointY: $pointY")
        val status = webexViewModel.setCameraFocusAtPoint(pointX, pointY)
        Log.d(TAG, "cameraFocusValueSetClickListener status: $status")
    }

    private fun cameraCustomExposureValueSetClickListener(duration: Double, iso: Float) {
        Log.d(TAG, "cameraCustomExposureValueSetClickListener duration: $duration, iso: $iso")
        val status = webexViewModel.setCameraCustomExposure(duration, iso)
        Log.d(TAG, "cameraCustomExposureValueSetClickListener status: $status")
    }

    private fun cameraAutoExposureValueSetClickListener(targetBias: Float) {
        Log.d(TAG, "cameraAutoExposureValueSetClickListener targetBias: $targetBias")
        val status = webexViewModel.setCameraAutoExposure(targetBias)
        Log.d(TAG, "cameraAutoExposureValueSetClickListener status: $status")
    }

    private fun compositeStreamLayoutClickListener(call: Call?) {
        Log.d(TAG, "compositeStreamLayoutClickListener getCompositedLayout: ${webexViewModel.getCompositedLayout()}")

        if (webexViewModel.compositedVideoLayout == MediaOption.CompositedVideoLayout.NOT_SUPPORTED) {
            showDialogWithMessage(requireContext(), R.string.composite_stream, resources.getString(R.string.composite_stream_not_supported))
            return
        }

        var layout = webexViewModel.compositedVideoLayout

        when (layout) {
            MediaOption.CompositedVideoLayout.FILMSTRIP -> {
                layout = MediaOption.CompositedVideoLayout.GRID
            }
            MediaOption.CompositedVideoLayout.GRID -> {
                layout = MediaOption.CompositedVideoLayout.SINGLE
            }
            MediaOption.CompositedVideoLayout.SINGLE -> {
                layout = MediaOption.CompositedVideoLayout.FILMSTRIP
            }
            else -> {}
        }

        webexViewModel.setCompositedLayout(layout)
    }

    private fun virtualBackgroundOptionsClickListener(call: Call?) {
        webexViewModel.fetchVirtualBackgrounds()
    }

    private fun scalingModeClickListener(call: Call?) {
        Log.d(TAG, "scalingModeClickListener")

        when (webexViewModel.scalingMode) {
            Call.VideoRenderMode.Fit -> {
                webexViewModel.scalingMode = Call.VideoRenderMode.CropFill
            }
            Call.VideoRenderMode.CropFill -> {
                webexViewModel.scalingMode = Call.VideoRenderMode.StretchFill
            }
            Call.VideoRenderMode.StretchFill -> {
                webexViewModel.scalingMode = Call.VideoRenderMode.Fit
            }
        }

        webexViewModel.setRemoteVideoRenderMode(call?.getCallId().orEmpty(), webexViewModel.scalingMode)
    }

    private fun showBottomSheet(call: Call?) {
        callOptionsBottomSheetFragment.call = call
        callOptionsBottomSheetFragment.scalingModeValue = webexViewModel.scalingMode
        callOptionsBottomSheetFragment.compositeLayoutValue = webexViewModel.compositedVideoLayout
        callOptionsBottomSheetFragment.streamMode = webexViewModel.streamMode
        callOptionsBottomSheetFragment.multiStreamNewApproach = webexViewModel.multistreamNewApproach
        callOptionsBottomSheetFragment.isSendingVideoForceLandscape = webexViewModel.isSendingVideoForceLandscape
        activity?.supportFragmentManager?.let { callOptionsBottomSheetFragment.show(it, CallBottomSheetFragment.TAG) }
    }

    private fun showCameraOptionsBottomSheet(call: Call?) {
        cameraOptionsBottomSheetFragment.call = call
        cameraOptionsBottomSheetFragment.torchModeValue = webexViewModel.torchMode
        cameraOptionsBottomSheetFragment.flashModeValue = webexViewModel.flashMode
        activity?.supportFragmentManager?.let { cameraOptionsBottomSheetFragment.show(it, CameraOptionsBottomSheetFragment.TAG) }
    }

    private fun updateNetworkStatusChange(mediaQualityInfo: Call.MediaQualityInfo) {
        when (mediaQualityInfo) {
            Call.MediaQualityInfo.NetworkLost -> {
                binding.ivNetworkSignal.setImageResource(R.drawable.ic_no_network)
                currentNetworkStatus = NetworkStatus.NoNetwork
            }
            Call.MediaQualityInfo.Good -> {
                binding.ivNetworkSignal.setImageResource(R.drawable.ic_good_network)
                currentNetworkStatus = NetworkStatus.Good
            }
            Call.MediaQualityInfo.PoorUplink -> {
                binding.ivNetworkSignal.setImageResource(R.drawable.ic_poor_network)
                currentNetworkStatus = NetworkStatus.PoorUplink
            }
            Call.MediaQualityInfo.PoorDownlink -> {
                binding.ivNetworkSignal.setImageResource(R.drawable.ic_poor_network)
                currentNetworkStatus = NetworkStatus.PoorDownlink
            }
            Call.MediaQualityInfo.HighCpuUsage -> showDialogWithMessage(requireContext(), R.string.warning, getString(R.string.high_cpu_usage))
            Call.MediaQualityInfo.DeviceLimitation -> showDialogWithMessage(requireContext(), R.string.warning, getString(R.string.device_limitation))
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d("CallControlFragment", "newConfig ${newConfig.orientation}" )
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.localViewLayout.layoutParams.height =
                requireActivity().resources.getDimension(R.dimen.local_video_view_width).toInt()
            binding.localViewLayout.layoutParams.width =
                requireActivity().resources.getDimension(R.dimen.local_video_view_height).toInt()
        }
        else {
            binding.localViewLayout.layoutParams.height =
                requireActivity().resources.getDimension(R.dimen.local_video_view_height).toInt()
            binding.localViewLayout.layoutParams.width =
                requireActivity().resources.getDimension(R.dimen.local_video_view_width).toInt()
        }
        binding.localViewLayout.requestLayout()
    }

    fun pipVisibility(currentView: Int, inPipMode: Boolean) {
            isInPipMode = inPipMode
        if(currentView == View.GONE){
            binding.videoCallLayout.layoutParams.height=400
            if(binding.screenShareView.isVisible){
                binding.remoteViewLayout.visibility=currentView
            }
        }else{
            binding.videoCallLayout.layoutParams.height = resources.getDimension(R.dimen.video_view_height).toInt()
            binding.remoteViewLayout.visibility=currentView
        }
        binding.localViewLayout.visibility = currentView
        binding.viewAuxVideosContainer.visibility = currentView
        binding.ivNetworkSignal.visibility = currentView
        binding.tvRemoteUserName.visibility = currentView
        binding.ivRemoteAudioState.visibility = currentView
        binding.callingHeader.visibility = currentView
        binding.tvName.visibility = currentView
        binding.optionButtonsContainer.visibility = currentView
        binding.ibMute.visibility = currentView
        binding.ibHoldCall.visibility = currentView
        binding.ibAudioMode.visibility = currentView
        binding.controlsRow2.visibility = currentView
        binding.ibVideo.visibility = currentView
        binding.ibParticipants.visibility = currentView
        binding.controlsRow3.visibility = currentView
        binding.ibScreenShare.visibility = currentView
        binding.ibSwapCamera.visibility = currentView
        binding.ibMoreOption.visibility = currentView
        binding.ivCancelCall.visibility = currentView
        binding.controlsRow4.visibility = currentView
        binding.ibSwitchToAudioVideoCall.visibility = currentView
        var returnToMainSessionVisibility = View.INVISIBLE
        if (currentView == View.VISIBLE && breakout != null) {
            returnToMainSessionVisibility = View.VISIBLE
        }
        binding.btnReturnToMainSession.visibility = returnToMainSessionVisibility
    }

    fun aspectRatio(): Rational {
        var width = binding.videoCallLayout.width.toInt()
        var height = binding.videoCallLayout.height.toInt()

        return if (width> 0 && height> 0) {
            getCoercedRational(width, height)
        } else if (UIUtils.isPortraitMode(requireContext())) {
            Rational(9, 16)
        } else {
            Rational(16, 9)
        }
    }

    /**
     * Get rational coerce in (0.476, 2.1)
     * */
    fun getCoercedRational(width: Int, height: Int): Rational {
        return when {
            width.toFloat() / height.toFloat() > 2.1f -> Rational(21, 10)
            width.toFloat() / height.toFloat() < 1 / 2.1f -> Rational(10, 21)
            else -> Rational(width, height)
        }
    }

    override fun onPause() {
        Log.d(tag, "BreakoutSession onPause() called")
        super.onPause()
    }

    override fun onStop() {
        Log.d(tag, "BreakoutSession onStop() called")
        dismissErrorDialog()
        mediaPlayer.reset()
        super.onStop()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    // Remote share callback
    override fun onShareStarted() {
        Log.d(TAG, "onShareStarted")
    }

    override fun onShareStopped() {
        Log.d(TAG, "onShareStopped")
    }

    override fun onFrameSizeChanged(width: Int, height: Int) {
        Log.d(TAG, "onFrameSizeChanged width: $width, height: $height")
    }

    private fun startCallMonitoringForegroundService() {
        // Start CallManagement service to cut call when app is killed from recent tasks
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            callManagementServiceIntent = Intent(activity, CallManagementService::class.java)
            activity?.startForegroundService(callManagementServiceIntent)
        }
    }
}