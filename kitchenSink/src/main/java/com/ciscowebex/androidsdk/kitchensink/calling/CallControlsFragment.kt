package com.ciscowebex.androidsdk.kitchensink.calling

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.WebexViewModel
import com.ciscowebex.androidsdk.kitchensink.calling.ScreenShareForegroundService.Companion.startScreenShareForegroundService
import com.ciscowebex.androidsdk.kitchensink.calling.ScreenShareForegroundService.Companion.stopScreenShareForegroundService
import com.ciscowebex.androidsdk.kitchensink.calling.ScreenShareForegroundService.Companion.updateScreenShareForegroundService
import com.ciscowebex.androidsdk.kitchensink.calling.participants.ParticipantsFragment
import com.ciscowebex.androidsdk.kitchensink.databinding.FragmentCallControlsBinding
import com.ciscowebex.androidsdk.kitchensink.person.PersonViewModel
import com.ciscowebex.androidsdk.kitchensink.utils.AudioManagerUtils
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.phone.Call
import com.ciscowebex.androidsdk.phone.MediaOption
import com.ciscowebex.androidsdk.phone.CallObserver
import com.ciscowebex.androidsdk.phone.CallMembership
import com.ciscowebex.androidsdk.phone.CallAssociationType
import com.ciscowebex.androidsdk.phone.CallSchedule
import com.ciscowebex.androidsdk.phone.Phone
import org.koin.android.ext.android.inject
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.internal.media.device.AudioDeviceConnectionManager
import com.ciscowebex.androidsdk.internal.media.device.AudioManagerDelegate
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemCallMeetingBinding
import java.util.Date


class CallControlsFragment : Fragment(), OnClickListener {
    private val TAG = "CallControlsFragment"
    private lateinit var webexViewModel: WebexViewModel
    private lateinit var binding: FragmentCallControlsBinding
    private var callFailed = false
    private var isIncomingActivity = false
    private var callingActivity = 0
    private var audioManagerUtils: AudioManagerUtils? = null
    var onLockSelfVideoMutedState = true
    var onLockRemoteSharingStateON = false
    private val ringerManager: RingerManager by inject()
    private val personViewModel : PersonViewModel by inject()
    private lateinit var callOptionsBottomSheetFragment: CallBottomSheetFragment
    private lateinit var incomingInfoAdapter: IncomingInfoAdapter

    enum class ShareButtonState {
        OFF,
        ON,
        DISABLED
    }

    companion object {
        const val REQUEST_CODE = 1212
        const val TAG = "CallControlsFragment"
        private const val CALLER_ID = "callerId"
        const val MEDIA_PROJECTION_REQUEST = 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil.inflate<FragmentCallControlsBinding>(LayoutInflater.from(context),
                R.layout.fragment_call_controls, container, false).also { binding = it }.apply {
            webexViewModel = (activity as? CallActivity)?.webexViewModel!!
            Log.d(TAG, "CallControlsFragment onCreateView webexViewModel: $webexViewModel")
            setUpViews()
            observerCallLiveData()
            initAudioManager()
        }.root
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
    }

    private fun checkIsOnHold() {
        val isOnHold = webexViewModel.currentCallId?.let { webexViewModel.isOnHold(it) }
        binding.ibHoldCall.isSelected = isOnHold ?: false
    }

    private fun getMediaOption(): MediaOption {
        return if (webexViewModel.callCapability == WebexRepository.CallCap.Audio_Only) {
            MediaOption.audioOnly()
        } else {
            MediaOption.audioVideoSharing(Pair(binding.localView, binding.remoteView), binding.screenShareView)
           // MediaOption.audioVideo(Pair(binding.localView, binding.remoteView))
        }
    }

    fun dialOutgoingCall(callerId: String) {
        Log.d(TAG, "dialOutgoingCall")
        webexViewModel.dial(callerId, getMediaOption())
    }

    private fun checkLicenseAPIs() {
        val license = webexViewModel.getVideoCodecLicense()
        Log.d(TAG, "checkLicenseAPIs license $license")
        val URL = webexViewModel.getVideoCodecLicenseURL()
        Log.d(TAG, "checkLicenseAPIs license URL $URL")
        webexViewModel.requestVideoCodecActivation(AlertDialog.Builder(activity))
    }

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
                } else {
                    updateScreenShareButtonState(ShareButtonState.OFF)
                    Log.d(TAG, "User cancelled screen request")
                    stopScreenShareForegroundService()
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
                        onCallJoined(call)
                        handleCUCMControls(call)
                    }
                    WebexRepository.CallEvent.DialFailed -> {
                        val callActivity = activity as CallActivity?
                        callActivity?.alertDialog(true, "")
                    }
                    WebexRepository.CallEvent.OnRinging -> {
                        Log.d(TAG, "CallObserver OnRinging : " + call?.getCallId())
                        ringerManager.startRinger(RingerManager.RingerType.Outgoing)
                    }
                    WebexRepository.CallEvent.OnConnected -> {
                        Log.d(TAG, "CallObserver onConnected : " + call?.getCallId())
                        onCallConnected(call?.getCallId().orEmpty())

                        ringerManager.stopRinger(if (isIncomingActivity) RingerManager.RingerType.Incoming else RingerManager.RingerType.Outgoing)

                        webexViewModel.sendDTMF(call?.getCallId().orEmpty(), "2")
                        webexViewModel.sendFeedback(call?.getCallId().orEmpty(), 5, "Testing Comments SDK-v3")
                    }
                    WebexRepository.CallEvent.AnswerCompleted -> {
                        Log.d(TAG, "answer Lambda callInfo Id: ${call?.getCallId()}")
                        onCallJoined(call)
                        handleCUCMControls(null)
                    }
                    WebexRepository.CallEvent.AnswerFailed -> {
                        Log.d(TAG, "answer Lambda failed $errorMessage")
                        callEndedUIUpdate(call?.getCallId().orEmpty())
                    }
                    WebexRepository.CallEvent.OnScheduleChanged -> {
                        Log.d(TAG, "CallObserver OnScheduleChanged : " + call?.getCallId())
                        schedulesChanged(call)
                    }
                    else -> {}
                }
            }
        })

        webexViewModel.disconnectedLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                val event = it.event
                val errorMessage = it.errorMessage
                val disconnectEvent = it.disconnectEvent
                val call = it.call

                when (event) {
                    WebexRepository.CallEvent.OnDisconnected -> {
                        Log.d(TAG, "CallObserver onDisconnected : " + call?.getCallId())

                        var callFailed = false
                        var callEnded = false

                        disconnectEvent?.let { _event ->
                            val _call = _event.getCall()
                            when (_event) {
                                is CallObserver.LocalLeft -> {
                                    Log.d(TAG, "CallObserver LocalLeft")
                                }
                                is CallObserver.LocalDecline -> {
                                    Log.d(TAG, "CallObserver LocalDecline")
                                }
                                is CallObserver.LocalCancel -> {
                                    Log.d(TAG, "CallObserver LocalCancel")
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
                                }
                                is CallObserver.OtherDeclined -> {
                                    Log.d(TAG, "CallObserver OtherDeclined")
                                }
                                is CallObserver.CallErrorEvent -> {
                                    Log.d(TAG, "CallObserver CallErrorEvent")
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
                                onCallFailed(call?.getCallId().orEmpty())
                            }
                            callEnded -> {
                                onCallTerminated(call?.getCallId().orEmpty())
                            }
                            else -> {
                                onCallDisconnected(call)
                            }
                        }

                        ringerManager.stopRinger(if (isIncomingActivity) RingerManager.RingerType.Incoming else RingerManager.RingerType.Outgoing)
                    }
                    else -> {}
                }
            }
        })

        webexViewModel.infoLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                val event = it.event
                val errorMessage = it.errorMessage
                val mediaEvent = it.mediaChangeEvent
                val call = it.call

                when (event) {
                    WebexRepository.CallEvent.OnInfoChanged -> {
                        Log.d(TAG, "CallObserver onInfoChanged : " + call?.getCallId())

                        Handler(Looper.getMainLooper()).post {
                            call?.let { _call ->
                                binding.ibHoldCall.isSelected = _call.isOnHold()
                            }
                        }
                    }
                    else -> {}
                }
            }
        })

        webexViewModel.mediaInfoLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                val event = it.event
                val errorMessage = it.errorMessage
                val mediaEvent = it.mediaChangeEvent

                when (event) {
                    WebexRepository.CallEvent.OnMediaChanged -> {
                        Log.d(TAG, "CallObserver OnMediaChanged")

                        mediaEvent?.let { _event ->
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
                                    onScreenShareVideoStreamInUseChanged(call?.getCallId().orEmpty())
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
                                else -> {}
                            }
                        }
                    }
                    else -> {}
                }
            }
        })

        webexViewModel.membershipLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                val event = it.event
                val errorMessage = it.errorMessage
                val callMembershipEvent = it.callMembershipEvent

                when (event) {
                    WebexRepository.CallEvent.OnCallMembershipEvent -> {
                        Log.d(TAG, "CallObserver OnCallMembershipEvent")

                        callMembershipEvent?.let { membershipEvent ->
                            Log.d(TAG, "CallObserver OnCallMembershipEvent : ${membershipEvent.getCallMembership()?.getEmail()}")
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
                    else -> {}
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

                        onCallJoined(call)
                        handleCUCMControls(call)
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
    }

    private fun audioEventChanged(callMembership: CallMembership?, call: Call?, isSendingAudio: Boolean? = null, isRemoteSendingAudio: Boolean? = null) {
        Handler(Looper.getMainLooper()).post {
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

    private fun handleCUCMControls(call: Call?) {
        Handler(Looper.getMainLooper()).post {
            Log.d(TAG, "handleCUCMControls isAddedCall = ${webexViewModel.isAddedCall}")
            webexViewModel.currentCallId?.let { callId ->

                var _call = call

                if (_call == null) {
                    _call = webexViewModel.getCall(callId)
                }

                when {
                    _call.isCUCMCall() && webexViewModel.isAddedCall -> {
                        binding.ibTransferCall.visibility = View.VISIBLE
                        binding.ibMerge.visibility = View.VISIBLE
                        binding.ibAdd.visibility = View.INVISIBLE
                        binding.ibVideo.visibility = View.INVISIBLE
                    }
                    !_call.isCUCMCall() -> {
                        binding.ibAdd.visibility = View.GONE
                        binding.ibTransferCall.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun showMutedIcon(showMuted: Boolean) {
        binding.ibMute.isSelected = showMuted
    }

    private fun setUpViews() {
        Log.d(TAG, "setUpViews fragment")
        personViewModel.getMe()
        videoViewState(true)

        webexViewModel.enableBackgroundStream(webexViewModel.enableBgtoggle)
        webexViewModel.enableAudioBNR(true)
        webexViewModel.setAudioBNRMode(Phone.AudioBRNMode.HP)
        webexViewModel.setDefaultFacingMode(Phone.FacingMode.USER)

        webexViewModel.setVideoMaxTxFPSSetting(5)
        webexViewModel.setShareMaxCaptureFPSSetting(5)
        webexViewModel.setVideoEnableCamera2Setting(true)
        webexViewModel.setVideoEnableDecoderMosaicSetting(true)

        webexViewModel.setHardwareAccelerationEnabled(true)
        webexViewModel.setVideoMaxRxBandwidth(Phone.DefaultBandwidth.MAX_BANDWIDTH_720P.getValue())
        webexViewModel.setVideoMaxTxBandwidth(Phone.DefaultBandwidth.MAX_BANDWIDTH_720P.getValue())
        webexViewModel.setSharingMaxRxBandwidth(Phone.DefaultBandwidth.MAX_BANDWIDTH_SESSION.getValue())
        webexViewModel.setAudioMaxRxBandwidth(Phone.DefaultBandwidth.MAX_BANDWIDTH_AUDIO.getValue())

        val incomingCallEvent: (Call) -> Unit = { call ->
            Log.d(tag, "incomingCallEvent")
            webexViewModel.currentCallId = call.getCallId().orEmpty()
        }

        val incomingCallPickEvent: (Call) -> Unit = { call ->
            Log.d(tag, "incomingCallPickEvent")
            webexViewModel.answer(call, getMediaOption())
        }

        val incomingCallCancelEvent: (Call) -> Unit = { call ->
            Log.d(tag, "incomingCallEndEvent")
            endIncomingCall(call.getCallId().orEmpty())
        }

        incomingInfoAdapter = IncomingInfoAdapter(incomingCallEvent, incomingCallPickEvent, incomingCallCancelEvent)
        binding.incomingRecyclerView.adapter = incomingInfoAdapter

        callOptionsBottomSheetFragment = CallBottomSheetFragment({ call -> receivingVideoListener(call) },
                { call -> receivingAudioListener(call) },
                { call -> receivingSharingListener(call) },
                { call -> scalingModeClickListener(call) })

        callingActivity = activity?.intent?.getIntExtra(Constants.Intent.CALLING_ACTIVITY_ID, 0)!!
        if (callingActivity == 1) {
            isIncomingActivity = true
            binding.mainContentLayout.visibility = View.GONE
            binding.incomingCallHeader.visibility = View.VISIBLE
            incomingLayoutState(false)

            webexViewModel.setIncomingListener()
            webexViewModel.incomingListenerLiveData.observe(viewLifecycleOwner, Observer {
                it?.let {
                    ringerManager.startRinger(RingerManager.RingerType.Incoming)
                    onIncomingCall(it)
                }
            })
        } else {
            isIncomingActivity = false
            binding.incomingCallHeader.visibility = View.GONE
            incomingLayoutState(true)

            binding.callingHeader.text = getString(R.string.calling)
            val callerId = activity?.intent?.getStringExtra(Constants.Intent.OUTGOING_CALL_CALLER_ID)
            binding.tvName.text = callerId
        }

        binding.ibMute.setOnClickListener(this)
        binding.ibParticipants.setOnClickListener(this)
        binding.ibSpeaker.setOnClickListener(this)
        binding.ibAdd.setOnClickListener(this)
        binding.ibTransferCall.setOnClickListener(this)
        binding.ibHoldCall.setOnClickListener(this)
        binding.ivCancelCall.setOnClickListener(this)
        binding.ibVideo.setOnClickListener(this)
        binding.ibSwapCamera.setOnClickListener(this)
        binding.ibMerge.setOnClickListener(this)
        binding.ibScreenShare.setOnClickListener(this)
        binding.mainContentLayout.setOnClickListener(this)
        binding.ibMoreOption.setOnClickListener(this)

        initAddedCallControls()

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
                binding.ibSpeaker -> {
                    toggleSpeaker(v)
                }
                binding.ibAdd -> {
                    //while associating a call, existing call needs to be put on hold
                    webexViewModel.holdCall(callId)
                    startActivityForResult(DialerActivity.getIntent(requireContext()), REQUEST_CODE)
                }
                binding.ibTransferCall -> {
                    transferCall()
                    initAddedCallControls()
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

                    val mode = call.getFacingMode()

                    if (mode == Phone.FacingMode.ENVIROMENT) {
                        call.setFacingMode(Phone.FacingMode.USER)
                    } else {
                        call.setFacingMode(Phone.FacingMode.ENVIROMENT)
                    }
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
                else -> {
                }
            }
        }
    }

    private fun mainContentLayoutClickListener() {
        Log.d(TAG, "mainContentLayoutClickListener")
        if (binding.incomingRecyclerView.visibility == View.VISIBLE) {
            return
        }

        if (binding.controlGroup.visibility == View.VISIBLE) {
            binding.controlGroup.visibility = View.GONE
        } else {
            binding.controlGroup.visibility = View.VISIBLE
        }
    }

    private fun screenShareButtonVisibilityState() {
        webexViewModel.currentCallId?.let {
            val canShare = webexViewModel.getCall(it).canShare()
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
        return call.isSendingSharing()
    }

    private fun isReceivingSharing(callId: String): Boolean {
        val call = webexViewModel.getCall(callId)
        return call.isReceivingSharing()
    }

    private fun shareScreen() {
        Log.d(TAG, "shareScreen")

        webexViewModel.currentCallId?.let {
            val isSharing = isLocalSharing(it)
            Log.d(TAG, "shareScreen isSharing: $isSharing")
            if (!isSharing) {
                startScreenShareForegroundService()
                updateScreenShareButtonState(ShareButtonState.DISABLED)
                webexViewModel.startShare(webexViewModel.currentCallId.orEmpty())
            } else {
                updateScreenShareButtonState(ShareButtonState.DISABLED)
                webexViewModel.currentCallId?.let { id -> webexViewModel.stopShare(id) }
                stopScreenShareForegroundService()
            }
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
            endIncomingCall()
        } else {
            webexViewModel.currentCallId?.let {
                webexViewModel.hangup(it)
            } ?: run {
                activity?.finish()
            }
        }
    }

    private fun incomingLayoutState(hide: Boolean) {
        if (hide) {
            binding.incomingRecyclerView.visibility = View.GONE
            binding.mainContentLayout.visibility = View.VISIBLE
        } else {
            binding.incomingRecyclerView.visibility = View.VISIBLE
            binding.mainContentLayout.visibility = View.GONE

            if (incomingInfoAdapter.info.size > 0) {
                for (model in incomingInfoAdapter.info) {
                    model.isEnabled = true
                }
                incomingInfoAdapter.notifyDataSetChanged()
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
            binding.callingHeader.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.tvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
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
            binding.remoteViewLayout.visibility = View.VISIBLE
        }

        videoViewTextColorState(toHide)
        videoButtonState(toHide)
    }

    private fun videoButtonState(videoViewHidden: Boolean) {
        if (videoViewHidden) {
            binding.ibVideo.background = ContextCompat.getDrawable(requireActivity(), R.drawable.turn_off_video_active)
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

    private fun onCallConnected(callId: String) {
        Log.d(TAG, "CallControlsFragment onCallConnected callerId: $callId, currentCallId: ${webexViewModel.currentCallId}")

        Handler(Looper.getMainLooper()).post {

            if (callId == webexViewModel.currentCallId) {
                val callInfo = webexViewModel.getCall(callId)

                val isSelfVideoMuted = !callInfo.isSendingVideo()
                webexViewModel.isRemoteVideoMuted = !callInfo.isReceivingVideo()
                Log.d(TAG, "CallControlsFragment onCallConnected isAudioOnly: ${callInfo.isAudioOnly()} isSelfVideoMuted: ${isSelfVideoMuted}, webexViewModel.isRemoteVideoMuted: ${webexViewModel.isRemoteVideoMuted}")

                if (isIncomingActivity) {
                    if (callId == webexViewModel.currentCallId) {
                        binding.videoCallLayout.visibility = View.VISIBLE
                        incomingLayoutState(true)
                    }
                }

                webexViewModel.isLocalVideoMuted = isSelfVideoMuted

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
                    binding.remoteViewLayout.visibility = View.VISIBLE
                }

                binding.controlGroup.visibility = View.VISIBLE

                screenShareButtonVisibilityState()
                videoViewTextColorState(webexViewModel.isRemoteVideoMuted)

                Log.d(TAG, "CallControlsFragment onCallConnected from: ${callInfo.getFrom()?.getName()} to: ${callInfo.getTo()?.getName()}")
            }

        }
    }

    private fun onScreenShareStateChanged(callId: String, label: String) {
        Log.d(TAG, "CallControlsFragment onScreenShareStateChanged callerId: $callId, label: $label")

        if (webexViewModel.currentCallId != callId) {
            return
        }

        Handler(Looper.getMainLooper()).post {

            val callInfo = webexViewModel.getCall(callId)

            val remoteSharing = isReceivingSharing(callId)
            val localSharing = isLocalSharing(callId)
            Log.d(TAG, "CallControlsFragment onScreenShareStateChanged isRemoteSharing: ${remoteSharing}, isLocalSharing: ${localSharing}")

            if (localSharing) {
                updateScreenShareForegroundService(callId)
                updateScreenShareButtonState(ShareButtonState.ON)
            } else {
                updateScreenShareButtonState(ShareButtonState.OFF)
            }
        }
    }

    private fun onScreenShareVideoStreamInUseChanged(callId: String) {
        Log.d(TAG, "CallControlsFragment onScreenShareVideoStreamInUseChanged callerId: $callId")

        if (webexViewModel.currentCallId != callId) {
            return
        }

        Handler(Looper.getMainLooper()).post {

            val remoteSharing = isReceivingSharing(callId)
            val localSharing = isLocalSharing(callId)
            Log.d(TAG, "CallControlsFragment onScreenShareVideoStreamInUseChanged isRemoteSharing: ${remoteSharing}, isLocalSharing: ${localSharing}")
            if (remoteSharing) {
                binding.controlGroup.visibility = View.GONE
                screenShareViewRemoteState(false)
                val view = webexViewModel.getSharingRenderView(callId)
                if (view == null) {
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

    private fun onVideoStreamingChanged(callId: String) {
        Log.d(TAG, "CallControlsFragment onVideoStreamingChanged callerId: $callId")

        if (webexViewModel.currentCallId == null) {
            return
        }

        Handler(Looper.getMainLooper()).post {

            if (webexViewModel.isLocalVideoMuted) {
                localVideoViewState(true)
            } else {
                localVideoViewState(false)
                val pair = webexViewModel.getVideoRenderViews(callId)
                if (pair.first == null) {
                    webexViewModel.setVideoRenderViews(callId, binding.localView, binding.remoteView)
                }
            }

            if (webexViewModel.isRemoteVideoMuted) {
                binding.remoteViewLayout.visibility = View.GONE
            } else {
                if (webexViewModel.isRemoteScreenShareON) {
                    resizeRemoteVideoView()
                }
                binding.remoteViewLayout.visibility = View.VISIBLE
                val pair = webexViewModel.getVideoRenderViews(callId)
                if (pair.second == null) {
                    webexViewModel.setVideoRenderViews(callId, binding.localView, binding.remoteView)
                }
                toggleSpeaker(binding.ibSpeaker)
            }

            videoViewTextColorState(webexViewModel.isRemoteVideoMuted)

            Log.d(TAG, "CallControlsFragment onVideoStreamingChanged isLocalVideoMuted: ${webexViewModel.isLocalVideoMuted}, isRemoteVideoMuted: ${webexViewModel.isRemoteVideoMuted}")

            if (webexViewModel.isLocalVideoMuted) {
                videoButtonState(true)
            } else {
                videoButtonState(false)
            }
        }
    }

    private fun toggleSpeaker(v: View) {
        v.isSelected = !v.isSelected
        when {
            v.isSelected -> {
                webexViewModel.switchAudioMode(Call.AudioOutputMode.SPEAKER)
            }
            audioManagerUtils?.isBluetoothHeadsetConnected == true -> {
                webexViewModel.switchAudioMode(Call.AudioOutputMode.BLUETOOTH_HEADSET)
            }
            audioManagerUtils?.isWiredHeadsetOn == true -> {
                webexViewModel.switchAudioMode(Call.AudioOutputMode.HEADSET)
            }
            else -> {
                webexViewModel.switchAudioMode(Call.AudioOutputMode.PHONE)
            }
        }
    }

    internal fun handleFCMIncomingCall(callId: String) {
        Handler(Looper.getMainLooper()).post {
            onIncomingCall(webexViewModel.getCall(callId))
        }
    }

    private fun onIncomingCall(call: Call) {
        Handler(Looper.getMainLooper()).post {

            Log.d(TAG, "CallControlsFragment onIncomingCall callerId: ${call.getCallId()}, callInfo title: ${call.getTitle()}")

            binding.incomingCallHeader.visibility = View.GONE

            val schedules= call.getSchedules()
            incomingLayoutState(false)

            schedules?.let {
                val item = schedules.first()
                if (!checkIncomingAdapterList(item)) {
                    val model = MeetingInfoModel.convertToMeetingInfoModel(call, item)
                    incomingInfoAdapter.info.add(model)
                    Log.d(TAG, "CallControlsFragment onIncomingCall schedules size: ${schedules.size}")
                }
            } ?: run {
                val model = OneToOneIncomingCallModel(call)
                incomingInfoAdapter.info.add(model)
            }

            incomingInfoAdapter.notifyDataSetChanged()
        }
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
        Handler(Looper.getMainLooper()).post {
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
        Handler(Looper.getMainLooper()).post {
            try {
                val callInfo = webexViewModel.getCall(callId)
                Log.d(TAG, "CallControlsFragment showCallHeader callerId: $callId, callInfo title: ${callInfo.getTitle()}")

                binding.tvName.text = callInfo.getTitle()
                binding.callingHeader.text = getString(R.string.onCall)
            } catch (e: Exception) {
                Log.d(TAG, "error: ${e.message}")
            }
        }
    }

    private fun onCallFailed(callId: String) {
        Log.d(TAG, "CallControlsFragment onCallFailed callerId: $callId")

        Handler(Looper.getMainLooper()).post {
            if (webexViewModel.isAddedCall) {
                resumePrevCallIfAdded(callId)
                updateCallHeader()
            }

            callFailed = !webexViewModel.isAddedCall

            val callActivity = activity as CallActivity?
            callActivity?.alertDialog(!webexViewModel.isAddedCall, "")
        }
    }

    private fun onCallDisconnected(call: Call?) {
        call?.let { _call ->
            Log.d(TAG, "CallControlsFragment onCallDisconnected callerId: ${_call.getCallId().orEmpty()}")
            Handler(Looper.getMainLooper()).post {
                val schedules = call.getSchedules()
                schedules?.let {
                    incomingLayoutState(false)
                }
            }
        }
    }

    private fun onCallTerminated(callId: String) {
        Log.d(TAG, "CallControlsFragment onCallTerminated callerId: $callId")

        Handler(Looper.getMainLooper()).post {
            if (webexViewModel.isAddedCall) {
                resumePrevCallIfAdded(callId)
                updateCallHeader()
                initAddedCallControls()
            }

            if (!callFailed && !webexViewModel.isAddedCall) {
                stopScreenShareForegroundService()
                callEndedUIUpdate(callId, true)
            }
            webexViewModel.isAddedCall = false
        }
    }

    private fun callEndedUIUpdate(callId: String, terminated: Boolean = false) {
        if (isIncomingActivity) {
            for (model in incomingInfoAdapter.info) {
                if ( (model is OneToOneIncomingCallModel) && (model.call.getCallId() == callId)) {
                    incomingInfoAdapter.info.remove(model)
                    break
                } else if (model is MeetingInfoModel) {
                    if (Date().after(model.endTime)) {
                        incomingInfoAdapter.info.remove(model)
                        break
                    }

                    if (terminated && (model.call.getCallId().orEmpty() == callId)) {
                        incomingInfoAdapter.info.remove(model)
                        break
                    }
                }
            }
            incomingInfoAdapter.notifyDataSetChanged()

            if (incomingInfoAdapter.info.isNotEmpty()) {
                webexViewModel.currentCallId = null
                incomingLayoutState(false)
            } else {
                activity?.finish()
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

    private fun mergeCalls() {
        Log.d(tag, "mergeCalls currentCallId = ${webexViewModel.currentCallId}, targetCallId = ${webexViewModel.oldCallId}")
        if (webexViewModel.currentCallId != null && webexViewModel.oldCallId != null) {
            webexViewModel.mergeCalls(webexViewModel.currentCallId!!, webexViewModel.oldCallId!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val callNumber = data?.getStringExtra(CALLER_ID) ?: ""
            //start call association to add new person on call
            startAssociatedCall(callNumber, CallAssociationType.Transfer, true)
        }
    }

    private fun muteSelfVideo(value: Boolean) {
        webexViewModel.currentCallId?.let {
            webexViewModel.muteSelfVideo(it, value)
        }
    }

    private fun receivingVideoListener(call: Call) {
        Log.d(TAG, "receivingVideoListener")
        if (call.isReceivingVideo()) {
            webexViewModel.setReceivingVideo(call, false)
        } else {
            webexViewModel.setReceivingVideo(call, true)
        }
    }

    private fun receivingAudioListener(call: Call) {
        Log.d(TAG, "receivingAudioListener")
        if (call.isReceivingAudio()) {
            webexViewModel.setReceivingAudio(call, false)
        } else {
            webexViewModel.setReceivingAudio(call, true)
        }
    }

    private fun receivingSharingListener(call: Call) {
        Log.d(TAG, "receivingSharingListener")
        if (call.isReceivingSharing()) {
            webexViewModel.setReceivingSharing(call, false)
        } else {
            webexViewModel.setReceivingSharing(call, true)
        }
    }

    private fun scalingModeClickListener(call: Call) {
        Log.d(TAG, "scalingModeClickListener")

        if (webexViewModel.scalingMode == Call.VideoRenderMode.Fit) {
            webexViewModel.scalingMode = Call.VideoRenderMode.CropFill
        } else if (webexViewModel.scalingMode == Call.VideoRenderMode.CropFill) {
            webexViewModel.scalingMode = Call.VideoRenderMode.StretchFill
        } else if (webexViewModel.scalingMode == Call.VideoRenderMode.StretchFill) {
            webexViewModel.scalingMode = Call.VideoRenderMode.Fit
        }

        webexViewModel.setRemoteVideoRenderMode(call.getCallId().orEmpty(), webexViewModel.scalingMode)
    }

    private fun showBottomSheet(call: Call) {
        callOptionsBottomSheetFragment.call = call
        callOptionsBottomSheetFragment.scalingModeValue = webexViewModel.scalingMode
        activity?.supportFragmentManager?.let { callOptionsBottomSheetFragment.show(it, CallBottomSheetFragment.TAG) }
    }

    class IncomingInfoAdapter(private val incomingCallEvent: (Call) -> Unit, private val IncomingCallPickEvent: (Call) -> Unit, private val incomingCallCancelEvent: (Call) -> Unit) : RecyclerView.Adapter<IncomingInfoViewHolder>() {
        var info: MutableList<IncomingCallInfoModel> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomingInfoViewHolder {
            return IncomingInfoViewHolder(ListItemCallMeetingBinding.inflate(LayoutInflater.from(parent.context), parent, false), incomingCallEvent, IncomingCallPickEvent, incomingCallCancelEvent)
        }

        override fun getItemCount(): Int = info.size

        override fun onBindViewHolder(holder: IncomingInfoViewHolder, position: Int) {
            holder.bind(info[position])
        }
    }

    class IncomingInfoViewHolder(private val binding: ListItemCallMeetingBinding, private val incomingCallEvent: (Call) -> Unit,
                                 private val IncomingCallPickEvent: (Call) -> Unit, private val incomingCallCancelEvent: (Call) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        var item: IncomingCallInfoModel? = null
        val tag = "IncomingInfoViewHolder"
        init {
            binding.meetingJoinButton.setOnClickListener {
                item?.let { model ->
                    if (model is MeetingInfoModel) {
                        incomingCallEvent(model.call)
                        Log.d(tag, "JoinButton clicked meetingInfo: ${model.subject}")
                        IncomingCallPickEvent(model.call)
                        model.isEnabled = false
                        binding.meetingJoinButton.alpha = 0.5f
                        binding.meetingJoinButton.isEnabled = false
                    }
                }
            }

            binding.ivPickCall.setOnClickListener {
                item?.let { model ->
                    if (model is OneToOneIncomingCallModel) {
                        incomingCallEvent(model.call)
                        Log.d(tag, "ivPickCall clicked")
                        IncomingCallPickEvent(model.call)
                        model.isEnabled = false
                        binding.ivPickCall.alpha = 0.5f
                        binding.ivPickCall.isEnabled = false
                    }
                }
            }

            binding.ivCancelCall.setOnClickListener {
                item?.let { model ->
                    if (model is OneToOneIncomingCallModel) {
                        incomingCallCancelEvent(model.call)
                    }
                }
            }
        }

        fun bind(model: IncomingCallInfoModel) {
            item = model

            if (model is MeetingInfoModel) {
                if (model.isEnabled) {
                    binding.meetingJoinButton.alpha = 1.0f
                    binding.meetingJoinButton.isEnabled = true
                } else {
                    binding.meetingJoinButton.alpha = 0.5f
                    binding.meetingJoinButton.isEnabled = false
                }

                binding.titleTextView.text = model.subject
                binding.meetingTimeTextView.text = model.timeString
                binding.meetingTimeTextView.visibility = View.VISIBLE
                binding.callingOneToOneButtonLayout.visibility = View.GONE
                binding.meetingJoinButton.visibility = View.VISIBLE
            } else if (model is OneToOneIncomingCallModel) {
                if (model.isEnabled) {
                    binding.ivPickCall.alpha = 1.0f
                    binding.ivPickCall.isEnabled = true
                } else {
                    binding.ivPickCall.alpha = 0.5f
                    binding.ivPickCall.isEnabled = false
                }

                binding.meetingJoinButton.visibility = View.GONE
                binding.meetingTimeTextView.visibility = View.GONE
                binding.callingOneToOneButtonLayout.visibility = View.VISIBLE
                binding.titleTextView.text = model.call.getTitle()
            }
            binding.executePendingBindings()
        }
    }
}