package com.ciscowebex.androidsdk.kitchensink

import android.app.AlertDialog
import android.app.Notification
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.kitchensink.firebase.RegisterTokenService
import com.ciscowebex.androidsdk.kitchensink.person.PersonModel
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.WebexError
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import com.ciscowebex.androidsdk.auth.PhoneServiceRegistrationFailureReason
import com.ciscowebex.androidsdk.auth.TokenAuthenticator
import com.ciscowebex.androidsdk.auth.UCLoginServerConnectionStatus
import com.ciscowebex.androidsdk.internal.ResultImpl
import com.ciscowebex.androidsdk.kitchensink.annotation.AnnotationRenderer
import com.ciscowebex.androidsdk.kitchensink.calling.CallObserverInterface
import com.ciscowebex.androidsdk.kitchensink.utils.CallObjectStorage
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.message.LocalFile
import com.ciscowebex.androidsdk.people.ProductCapability
import com.ciscowebex.androidsdk.phone.ShareConfig
import com.ciscowebex.androidsdk.phone.BreakoutSession.BreakoutSessionError
import com.ciscowebex.androidsdk.phone.Call
import com.ciscowebex.androidsdk.phone.CallObserver
import com.ciscowebex.androidsdk.phone.MediaOption
import com.ciscowebex.androidsdk.phone.CallMembership
import com.ciscowebex.androidsdk.phone.Phone
import com.ciscowebex.androidsdk.phone.CallAssociationType
import com.ciscowebex.androidsdk.phone.AdvancedSetting
import com.ciscowebex.androidsdk.phone.MakeHostError
import com.ciscowebex.androidsdk.phone.AuxStream
import com.ciscowebex.androidsdk.phone.VirtualBackground
import com.ciscowebex.androidsdk.phone.CameraExposureISO
import com.ciscowebex.androidsdk.phone.CameraExposureDuration
import com.ciscowebex.androidsdk.phone.CameraExposureTargetBias
import com.ciscowebex.androidsdk.phone.MediaStream
import com.ciscowebex.androidsdk.phone.MediaStreamQuality
import com.ciscowebex.androidsdk.phone.BreakoutSession
import com.ciscowebex.androidsdk.phone.Breakout
import com.ciscowebex.androidsdk.phone.CompanionMode
import com.ciscowebex.androidsdk.phone.DirectTransferResult
import com.ciscowebex.androidsdk.phone.InviteParticipantError
import com.ciscowebex.androidsdk.phone.SwitchToAudioVideoCallResult
import com.ciscowebex.androidsdk.phone.PhoneConnectionResult
import com.ciscowebex.androidsdk.phone.ReceivingNoiseInfo
import com.ciscowebex.androidsdk.phone.ReceivingNoiseRemovalEnableResult
import com.ciscowebex.androidsdk.phone.ReclaimHostError
import com.ciscowebex.androidsdk.phone.annotation.LiveAnnotationListener
import com.ciscowebex.androidsdk.phone.annotation.LiveAnnotationsPolicy
import com.ciscowebex.androidsdk.phone.closedCaptions.CaptionItem
import com.ciscowebex.androidsdk.phone.closedCaptions.ClosedCaptionsInfo
import com.google.firebase.installations.FirebaseInstallations
import java.io.PrintWriter

class WebexViewModel(val webex: Webex, val repository: WebexRepository) : BaseViewModel() {
    private val tag = "WebexViewModel"

    var _callMembershipsLiveData = MutableLiveData<List<CallMembership>>()
    val _muteAllLiveData = MutableLiveData<Boolean>()
    val _ucLiveData = MutableLiveData<Pair<WebexRepository.UCCallEvent, String>>()
    private val _authLiveData = MutableLiveData<String>()
    val _callingLiveData = MutableLiveData<WebexRepository.CallLiveData>()
    val _startAssociationLiveData = MutableLiveData<WebexRepository.CallLiveData>()
    val _startShareLiveData = MutableLiveData<Boolean>()
    val _stopShareLiveData = MutableLiveData<Boolean>()
    val _setCompositeLayoutLiveData = MutableLiveData<Pair<Boolean, String>>()
    val _setRemoteVideoRenderModeLiveData = MutableLiveData<Pair<Boolean, String>>()
    val _forceSendingVideoLandscapeLiveData = MutableLiveData<Boolean>()
    val _startAudioDumpLiveData = MutableLiveData<Boolean>()
    val _stopAudioDumpLiveData = MutableLiveData<Boolean>()
    val _canStartAudioDumpLiveData = MutableLiveData<Boolean>()

    var callMembershipsLiveData: LiveData<List<CallMembership>> = _callMembershipsLiveData
    val muteAllLiveData: LiveData<Boolean> = _muteAllLiveData
    val ucLiveData: LiveData<Pair<WebexRepository.UCCallEvent, String>> = _ucLiveData
    val authLiveData: LiveData<String> = _authLiveData
    val callingLiveData: LiveData<WebexRepository.CallLiveData> = _callingLiveData
    val startAssociationLiveData: LiveData<WebexRepository.CallLiveData> = _startAssociationLiveData
    val startShareLiveData: LiveData<Boolean> = _startShareLiveData
    val stopShareLiveData: LiveData<Boolean> = _stopShareLiveData
    val setCompositeLayoutLiveData: LiveData<Pair<Boolean, String>> = _setCompositeLayoutLiveData
    val setRemoteVideoRenderModeLiveData: LiveData<Pair<Boolean, String>> = _setRemoteVideoRenderModeLiveData
    val forceSendingVideoLandscapeLiveData: LiveData<Boolean> = _forceSendingVideoLandscapeLiveData
    val startAudioDumpLiveData: LiveData<Boolean> = _startAudioDumpLiveData
    val stopAudioDumpLiveData: LiveData<Boolean> = _stopAudioDumpLiveData
    val canStartAudioDumpLiveData: LiveData<Boolean> = _canStartAudioDumpLiveData

    private val _incomingListenerLiveData = MutableLiveData<Call?>()
    val incomingListenerLiveData: LiveData<Call?> = _incomingListenerLiveData
    private val _hasConflictCalls = MutableLiveData<Boolean>()
    val hasConflictCalls: LiveData<Boolean> = _hasConflictCalls

    private val _signOutListenerLiveData = MutableLiveData<Boolean>()
    val signOutListenerLiveData: LiveData<Boolean> = _signOutListenerLiveData

    private val _tokenLiveData = MutableLiveData<Pair<String?, PersonModel>>()
    val tokenLiveData: LiveData<Pair<String?, PersonModel>> = _tokenLiveData

    private val _virtualBackground = MutableLiveData<List<VirtualBackground>>()
    val virtualBackground: LiveData<List<VirtualBackground>> = _virtualBackground

    private val _virtualBgError = MutableLiveData<String>()
    val virtualBgError: LiveData<String> = _virtualBgError

    private val _initialSpacesSyncCompletedLiveData = MutableLiveData<Boolean>()
    val initialSpacesSyncCompletedLiveData: LiveData<Boolean> = _initialSpacesSyncCompletedLiveData

    private val _annotationEvent = MutableLiveData<AnnotationEvent>()
    val annotationEvent: LiveData<AnnotationEvent> get() = _annotationEvent
    sealed class AnnotationEvent {
        data class PERMISSION_ASK(val personId: String) : AnnotationEvent()
        data class PERMISSION_EXPIRED(val personId: String) : AnnotationEvent()
    }


    var selfPersonId: String? = null
    var compositedLayoutState = MediaOption.CompositedVideoLayout.NOT_SUPPORTED

    var callObserverInterface: CallObserverInterface? = null

    var isVideoViewsSwapped: Boolean = true

    var isSendingVideoForceLandscape: Boolean = false
    var torchMode = Call.TorchMode.OFF
    var flashMode = Call.FlashMode.OFF

    var callCapability: WebexRepository.CallCap
        get() = repository.callCapability
        set(value) {
            repository.callCapability = value
        }

    var scalingMode: Call.VideoRenderMode
        get() = repository.scalingMode
        set(value) {
            repository.scalingMode = value
        }

    var compositedVideoLayout: MediaOption.CompositedVideoLayout
        get() = repository.compositedVideoLayout
        set(value) {
            repository.compositedVideoLayout = value
        }

    var streamMode: Phone.VideoStreamMode
        get() = repository.streamMode
        set(value) {
            repository.streamMode = value
        }

    var isAddedCall: Boolean
        get() = repository.isAddedCall
        set(value) {
            repository.isAddedCall = value
        }

    var currentCallId: String?
        get() = repository.currentCallId
        set(value) {
            repository.currentCallId = value
        }

    var oldCallId: String?
        get() = repository.oldCallId
        set(value) {
            repository.oldCallId = value
        }

    var isSendingAudio: Boolean
        get() = repository.isSendingAudio
        set(value) {
            repository.isSendingAudio = value
        }

    var doMuteAll: Boolean
        get() = repository.doMuteAll
        set(value) {
            repository.doMuteAll = value
        }

    var incomingCallJoinedCallId: String?
        get() = repository.incomingCallJoinedCallId
        set(value) {
            repository.incomingCallJoinedCallId = value
        }

    var isLocalVideoMuted: Boolean
        get() = repository.isLocalVideoMuted
        set(value) {
            repository.isLocalVideoMuted = value
        }

    var isRemoteVideoMuted: Boolean
        get() = repository.isRemoteVideoMuted
        set(value) {
            repository.isRemoteVideoMuted = value
        }

    var isUCServerLoggedIn: Boolean
        get() = repository.isUCServerLoggedIn
        set(value) {
            repository.isUCServerLoggedIn = value
        }

    var ucServerConnectionStatus: UCLoginServerConnectionStatus
        get() = repository.ucServerConnectionStatus
        set(value) {
            repository.ucServerConnectionStatus = value
        }

    var ucServerConnectionFailureReason: PhoneServiceRegistrationFailureReason
        get() = repository.ucServerConnectionFailureReason
        set(value) {
            repository.ucServerConnectionFailureReason = value
        }

    var isRemoteScreenShareON: Boolean
        get() = repository.isRemoteScreenShareON
        set(value) {
            repository.isRemoteScreenShareON = value
        }

    var enableBgStreamtoggle: Boolean
        get() = repository.enableBgStreamtoggle
        set(value) {
            repository.enableBgStreamtoggle = value
        }

    var enableBgConnectiontoggle: Boolean
        get() = repository.enableBgConnectiontoggle
        set(value) {
            repository.enableBgConnectiontoggle = value
        }

    var enablePhoneStatePermission: Boolean
        get() = repository.enablePhoneStatePermission
        set(value) {
            repository.enablePhoneStatePermission = value
        }

    var enableHWAcceltoggle: Boolean
        get() = repository.enableHWAcceltoggle
        set(value) {
            repository.enableHWAcceltoggle = value
        }

    var logFilter: String
        get() = repository.logFilter
        set(value) {
            repository.logFilter = value
        }

    var maxVideoBandwidth: String
        get() = repository.maxVideoBandwidth
        set(value) {
            repository.maxVideoBandwidth = value
        }

    var isConsoleLoggerEnabled: Boolean
        get() = repository.isConsoleLoggerEnabled
        set(value) {
            repository.isConsoleLoggerEnabled = value
        }

    var multistreamNewApproach: Boolean
        get() = repository.multiStreamNewApproach
        set(value) {
            repository.multiStreamNewApproach = value
        }

    init {
        repository._callMembershipsLiveData = _callMembershipsLiveData
        repository._ucLiveData = _ucLiveData
        repository._authLiveDataList.add(_authLiveData)
        repository._muteAllLiveData = _muteAllLiveData
        repository._callingLiveData = _callingLiveData
        repository._startAssociationLiveData = _startAssociationLiveData
        repository._startShareLiveData = _startShareLiveData
        repository._stopShareLiveData = _stopShareLiveData
        repository._startAudioDumpLiveData = _startAudioDumpLiveData
        repository._stopAudioDumpLiveData = _stopAudioDumpLiveData
        repository._canStartAudioDumpLiveData = _canStartAudioDumpLiveData
    }

    fun setLogLevel(logLevel: String) {
        var level: Webex.LogLevel = Webex.LogLevel.ALL
        when (logLevel) {
            WebexRepository.LogLevel.ALL.name -> level = Webex.LogLevel.ALL
            WebexRepository.LogLevel.VERBOSE.name -> level = Webex.LogLevel.VERBOSE
            WebexRepository.LogLevel.INFO.name -> level = Webex.LogLevel.INFO
            WebexRepository.LogLevel.WARNING.name -> level = Webex.LogLevel.WARNING
            WebexRepository.LogLevel.DEBUG.name -> level = Webex.LogLevel.DEBUG
            WebexRepository.LogLevel.ERROR.name -> level = Webex.LogLevel.ERROR
            WebexRepository.LogLevel.NO.name -> level = Webex.LogLevel.NO
        }
        webex.setLogLevel(level)
    }

    fun enableConsoleLogger(enable: Boolean) {
        webex.enableConsoleLogger(enable)
    }

    override fun onCleared() {
        repository.clearCallData()
        repository._authLiveDataList.remove(_authLiveData)
    }

    fun setSpaceObserver() {
        repository.setSpaceObserver()
    }

    fun setMembershipObserver() {
        repository.setMembershipObserver()
    }

    fun setMessageObserver() {
        repository.setMessageObserver()
    }

    fun setCalendarMeetingObserver() {
        repository.setCalendarMeetingObserver()
    }

    fun setIncomingListener() {
        if(!repository.isIncomingCallListenerSet("viewmodel"+this)) {
            repository.setIncomingCallListener("viewmodel"+this, object : Phone.IncomingCallListener {
                override fun onIncomingCall(call: Call?, hasActiveConflictCalls : Boolean) {
                    call?.let {
                        Log.d(tag, "setIncomingCallListener Call object : ${it.getCallId()}, correlationId : ${it.getCorrelationId()}")
                        _incomingListenerLiveData.postValue(it)
                        _hasConflictCalls.postValue(hasActiveConflictCalls)
                    } ?: run {
                        Log.d(tag, "setIncomingCallListener Call object null")
                    }
                }
            })
        }
    }

    fun setFCMIncomingListenerObserver(callId: String) {
        val call = CallObjectStorage.getCallObject(callId)
        call?.let {
            setCallObserver(it)
        }
    }

    fun signOut() {
        webex.authenticator?.deauthorize(CompletionHandler { result ->
            result?.let {
                _signOutListenerLiveData.postValue(it.isSuccessful)
                if (!it.isSuccessful) {
                    Log.d(tag, "Logut error : ${it.error?.errorMessage}")
                }
            }
        })
    }

    fun connectPhoneServices(callback: CompletionHandler<PhoneConnectionResult>){
        webex.phone.connectPhoneServices(callback)
    }

    fun disconnectPhoneServices(callback: CompletionHandler<PhoneConnectionResult>){
        webex.phone.disconnectPhoneServices(callback)
    }

    fun dialPhoneNumber(input:String, option: MediaOption) {
        webex.phone.dialPhoneNumber(input, option, CompletionHandler { result ->
            Log.i(tag, "dialPhoneNumber isSuccessful: ${result.isSuccessful}")
            if (result.isSuccessful) {
                result.data?.let { _call ->
                    CallObjectStorage.addCallObject(_call)
                    currentCallId = _call.getCallId()
                    setCallObserver(_call)
                    _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.DialCompleted, _call))
                }
            } else {
                result.error?.let { error ->
                    when(error.errorCode){
                        WebexError.ErrorCode.INVALID_API_ERROR.code -> {
                            _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.WrongApiCalled, null, null, result.error?.errorMessage))
                        }
                        else -> {
                            _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.DialFailed, null, null, result.error?.errorMessage))
                        }
                    }
                } ?: run {
                    _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.DialFailed, null, null, result.error?.errorMessage))
                }
            }
        })
    }

    fun dial(input: String, option: MediaOption) {
        webex.phone.dial(input, option, CompletionHandler { result ->
            Log.d(tag, "dial isSuccessful: ${result.isSuccessful}")
            if (result.isSuccessful) {
                result.data?.let { _call ->
                    CallObjectStorage.addCallObject(_call)
                    currentCallId = _call.getCallId()
                    setCallObserver(_call)
                    _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.DialCompleted, _call))
                }
            } else {
                result.error?.let { error ->

                    when(error.errorCode){
                        WebexError.ErrorCode.HOST_PIN_OR_MEETING_PASSWORD_REQUIRED.code -> {
                            _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.MeetingPinOrPasswordRequired, null))
                        }
                        WebexError.ErrorCode.INVALID_PASSWORD.code -> {
                            _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.InCorrectPassword, null, null))
                        }

                        WebexError.ErrorCode.INVALID_PASSWORD_OR_HOST_KEY.code -> {
                            _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.InCorrectPasswordOrHostKey, null, null))
                        }
                        WebexError.ErrorCode.CAPTCHA_REQUIRED.code -> {
                            _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.CaptchaRequired, null, error.data as Phone.Captcha))
                        }
                        WebexError.ErrorCode.INVALID_PASSWORD_WITH_CAPTCHA.code -> {
                            _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.InCorrectPasswordWithCaptcha, null, error.data as Phone.Captcha))
                        }
                        WebexError.ErrorCode.INVALID_PASSWORD_OR_HOST_KEY_WITH_CAPTCHA.code -> {
                            _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.InCorrectPasswordOrHostKeyWithCaptcha, null, error.data as Phone.Captcha))
                        }
                        WebexError.ErrorCode.CANNOT_START_INSTANT_MEETING.code -> {
                            _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.CannotStartInstantMeeting, null, null, result.error?.errorMessage))
                        }
                        else -> {
                            _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.DialFailed, null, null, result.error?.errorMessage))
                        }

                    }
                } ?: run {
                    _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.DialFailed, null, null, result.error?.errorMessage))
                }
            }
        })
    }

    fun refreshCaptcha() {
        webex.phone.refreshMeetingCaptcha() {
            _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.CaptchaRequired, null, it.data))
        }
    }

    fun answer(call: Call, mediaOption: MediaOption) {
        call.answer(mediaOption, CompletionHandler { result ->
            if (result.isSuccessful) {
                result.data.let {
                    _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.AnswerCompleted, call))
                }
            } else {
                _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.AnswerFailed, null, null, result.error?.errorMessage))
            }
        })
    }


    inner class VMCallObserver(val call:Call) : CallObserver {
        override fun onConnected(call: Call?) {
            Log.d(tag, "CallObserver onConnected")
            callObserverInterface?.onConnected(call)
        }

        override fun onRinging(call: Call?) {
            Log.d(tag, "CallObserver onRinging")
            callObserverInterface?.onRinging(call)
        }

        override fun onStartRinging(call: Call?, ringerType: Call.RingerType) {
            Log.d(tag, "CallObserver onStartRinging")
            callObserverInterface?.onStartRinging(call, ringerType)
        }

        override fun onStopRinging(call: Call?, ringerType: Call.RingerType) {
            Log.d(tag, "CallObserver onStopRinging")
            callObserverInterface?.onStopRinging(call, ringerType)
        }

        override fun onWaiting(call: Call?, reason: Call.WaitReason?) {
            Log.d(tag, "CallObserver onWaiting reason: $reason")
            callObserverInterface?.onWaiting(call)
        }

        override fun onDisconnected(event: CallObserver.CallDisconnectedEvent?) {
            Log.d(tag, "CallObserver onDisconnected event: ${this@WebexViewModel} $callObserverInterface $event")
            callObserverInterface?.onDisconnected(call, event)
            annotationRenderer?.stopRendering()
        }

        override fun onInfoChanged(call: Call?) {
            callObserverInterface?.onInfoChanged(call)
        }

        override fun onMediaChanged(event: CallObserver.MediaChangedEvent?) {
            Log.d(tag, "CallObserver OnMediaChanged event: $event")
            callObserverInterface?.onMediaChanged(call, event)
            event?.getCall()
                ?.let { CallObjectStorage.updateCallObject(call.getCallId().toString(), it) }

        }

        override fun onCallMembershipChanged(event: CallObserver.CallMembershipChangedEvent?) {
            Log.d(tag, "CallObserver onCallMembershipChanged event: $event")
            callObserverInterface?.onCallMembershipChanged(call, event)
            getParticipants(event?.getCall()?.getCallId().orEmpty())
        }

        override fun onScheduleChanged(call: Call?) {
            callObserverInterface?.onScheduleChanged(call)
        }

        override fun onCpuHitThreshold() {
            callObserverInterface?.onCpuHitThreshold()
        }

        override fun onPhotoCaptured(imageData: ByteArray?) {
            callObserverInterface?.onPhotoCaptured(imageData)
        }

        override fun onMediaQualityInfoChanged(mediaQualityInfo: Call.MediaQualityInfo) {
            callObserverInterface?.onMediaQualityInfoChanged(mediaQualityInfo)
        }

        override fun onBroadcastMessageReceivedFromHost(message: String) {
            callObserverInterface?.onBroadcastMessageReceivedFromHost(message)
        }

        override fun onHostAskingReturnToMainSession() {
            callObserverInterface?.onHostAskingReturnToMainSession()
        }

        override fun onJoinableSessionUpdated(breakoutSessions: List<BreakoutSession>) {
            callObserverInterface?.onJoinableSessionUpdated(breakoutSessions)
        }

        override fun onJoinedSessionUpdated(breakoutSession: BreakoutSession) {
            callObserverInterface?.onJoinedSessionUpdated(breakoutSession)
        }

        override fun onReturnedToMainSession() {
            callObserverInterface?.onReturnedToMainSession()
        }

        override fun onSessionClosing() {
            callObserverInterface?.onSessionClosing()
        }

        override fun onSessionEnabled() {
            callObserverInterface?.onSessionEnabled()
        }

        override fun onSessionJoined(breakoutSession: BreakoutSession) {
            callObserverInterface?.onSessionJoined(breakoutSession)
        }

        override fun onSessionStarted(breakout: Breakout) {
            callObserverInterface?.onSessionStarted(breakout)
        }

        override fun onBreakoutUpdated(breakout: Breakout) {
            callObserverInterface?.onBreakoutUpdated(breakout)
        }

        override fun onBreakoutError(error: BreakoutSessionError) {
            callObserverInterface?.onBreakoutError(error)
        }

        override fun onReceivingNoiseInfoChanged(info: ReceivingNoiseInfo) {
            callObserverInterface?.onReceivingNoiseInfoChanged(info)
        }

        override fun onClosedCaptionsArrived(closedCaptions: CaptionItem) {
            callObserverInterface?.onClosedCaptionsArrived(closedCaptions)
        }

        override fun onClosedCaptionsInfoChanged(closedCaptionsInfo: ClosedCaptionsInfo) {
            callObserverInterface?.onClosedCaptionsInfoChanged(closedCaptionsInfo)
        }
    }

    val callObserverMap : HashMap<String, VMCallObserver> = HashMap()

    fun setCallObserver(call: Call) {
        // check if existing observer is present. Reuse it
        var observer = callObserverMap[call.getCallId()]
        if(observer == null){
            observer = VMCallObserver(call)
            callObserverMap[call.getCallId()!!] = observer
        }
        repository.setCallObserver(call,observer)
    }

    fun setReceivingVideo(call: Call, receiving: Boolean) {
        call.setReceivingVideo(receiving)
    }

    fun setReceivingAudio(call: Call, receiving: Boolean) {
        call.setReceivingAudio(receiving)
    }

    fun setReceivingSharing(call: Call, receiving: Boolean) {
        call.setReceivingSharing(receiving)
    }

    fun muteSelfVideo(callId: String, doMute: Boolean) {
        getCall(callId)?.setSendingVideo(!doMute)
    }

    fun getCall(callId: String): Call? {
        return repository.getCall(callId)
    }

    fun muteAllParticipantAudio(callId: String) {
        Log.d(tag, "postParticipantData muteAllParticipantAudio: $doMuteAll")
        getCall(callId)?.muteAllParticipantAudio(doMuteAll)
    }

    fun muteParticipant(callId: String, participantId: String) {
        repository.participantMuteMap[participantId]?.let { doMute ->
            if (participantId == selfPersonId) {
                muteSelfAudio(callId)
            } else {
                getCall(callId)?.muteParticipantAudio(participantId, doMute)
            }
        }
    }

    fun muteSelfAudio(callId: String) {
        Log.d(tag, "muteSelfAudio isSendingAudio: $isSendingAudio")
        getCall(callId)?.setSendingAudio(!isSendingAudio)
    }

    fun switchToAudioOrVideoCall(callId: String, switchToVideoCall: Boolean, callback: CompletionHandler<SwitchToAudioVideoCallResult>) {
        Log.d(tag, "switchToAudioOrVideoCall call: $switchToVideoCall")
        if(switchToVideoCall)
        {
            getCall(callId)?.switchToVideoCall(callId, callback)
        }
        else
        {
            getCall(callId)?.switchToAudioCall(callId, callback)
        }
    }

    fun startShare(callId: String, shareConfig: ShareConfig?) {
        val call = getCall(callId)
        call?.let {
            it.startSharing(CompletionHandler { result ->
                _startShareLiveData.postValue(result.isSuccessful)

            }, shareConfig)
        }
    }

    fun startShare(callId: String, notification: Notification?, notificationId: Int, shareConfig: ShareConfig?) {
        val call = getCall(callId)
        call?.let {
            it.startSharing(notification, notificationId, CompletionHandler { result ->
                _startShareLiveData.postValue(result.isSuccessful)
            }, shareConfig)
        }
    }

    fun setSendingSharing(callId: String, value: Boolean) {
        getCall(callId)?.setSendingSharing(value)
    }

    fun stopShare(callId: String) {
        getCall(callId)?.stopSharing(CompletionHandler { result ->
            _stopShareLiveData.postValue(result.isSuccessful)
        })
    }

    private var annotationRenderer: AnnotationRenderer? = null
    fun initalizeAnnotations(renderer: AnnotationRenderer) {
        getCall(currentCallId.orEmpty())?.getLiveAnnotationHandle()?.let {annotations->

            annotations.setLiveAnnotationsPolicy(LiveAnnotationsPolicy.NeedAskForAnnotate){
                if (it.isSuccessful) {
                    Log.d(tag, "setLiveAnnotationsPolicy successful")
                } else {
                    Log.d(tag, "setLiveAnnotationsPolicy error: ${it.error?.errorMessage}")
                }
            }

            annotations.setLiveAnnotationListener(object : LiveAnnotationListener {
                override fun onLiveAnnotationRequestReceived(personId: String) {
                    _annotationEvent.postValue(AnnotationEvent.PERMISSION_ASK(personId))
                }

                override fun onLiveAnnotationRequestExpired(personId: String) {
                    _annotationEvent.postValue(AnnotationEvent.PERMISSION_EXPIRED(personId))
                }

                override fun onLiveAnnotationsStarted() {
                    annotationRenderer = renderer.apply {
                        setAnnotationRendererCallback(object : AnnotationRenderer.AnnotationRendererCallback {
                            override fun onAnnotationRenderingReady() {
                                Log.d(tag, "onAnnotationRenderingReady")
                            }

                            override fun onAnnotationRenderingStopped() {
                                Log.d(tag, "onAnnotationRenderingStopped")
                                getCall(currentCallId.orEmpty())?.getLiveAnnotationHandle()?.stopLiveAnnotations()
                                annotationRenderer = null
                            }
                        })
                        startRendering()
                    }
                }

                override fun onLiveAnnotationDataArrived(data: String) {
                    annotationRenderer?.renderData(data)
                }

                override fun onLiveAnnotationsStopped() {
                    annotationRenderer?.stopRendering()
                }

            })
        }
    }

    fun handleAnnotationPermission(grant: Boolean, personId: String) {
        getCall(currentCallId.orEmpty())?.getLiveAnnotationHandle()?.respondToLiveAnnotationRequest(personId, grant) {
            if (it.isSuccessful) {
                Log.d(tag, "permission handled")
            } else {
                Log.d(tag, "permission error: ${it.error?.errorMessage}")
            }
        }
    }

    fun getCurrentLiveAnnotationPolicy(): LiveAnnotationsPolicy? {
        return getCall(currentCallId.orEmpty())?.getLiveAnnotationHandle()?.getLiveAnnotationsPolicy()
    }

    fun setLiveAnnotationPolicy(policy: LiveAnnotationsPolicy) {
        getCall(currentCallId.orEmpty())?.getLiveAnnotationHandle()?.setLiveAnnotationsPolicy(policy) {
            if (it.isSuccessful) {
                Log.d(tag, "setLiveAnnotationsPolicy successful")
            } else {
                Log.d(tag, "setLiveAnnotationsPolicy error: ${it.error?.errorMessage}")
            }
        }
    }

    fun sendFeedback(callId: String, rating: Int, comment: String) {
        getCall(callId)?.sendFeedback(rating, comment)
    }

    fun sendDTMF(callId: String, keys: String) {
        getCall(callId)?.sendDTMF(keys, CompletionHandler { result ->
            if (result.isSuccessful) {
                Log.d(tag, "sendDTMF successful")
            } else {
                Log.d(tag, "sendDTMF error: ${result.error?.errorMessage}")
            }
        })
    }

    fun cancel() {
        webex.phone.cancel()
    }

    fun hangup(callId: String) {
        getCall(callId)?.hangup(CompletionHandler { result ->
            if (result.isSuccessful) {
                Log.d(tag, "hangup successful")
            } else {
                Log.d(tag, "hangup error: ${result.error?.errorMessage}")
            }
        })
    }

    fun rejectCall(callId: String) {
        getCall(callId)?.reject(CompletionHandler { result ->
            if (result.isSuccessful) {
                Log.d(tag, "rejectCall successful")
            } else {
                Log.d(tag, "rejectCall error: ${result.error?.errorMessage}")
            }
        })
    }

    fun holdCall(callId: String) {
        val callInfo = getCall(callId)
        val isOnHold = callInfo?.isOnHold() ?: false
        Log.d(tag, "holdCall isOnHold = $isOnHold")
        callInfo?.holdCall(!isOnHold)
    }

    fun isOnHold(callId: String) = getCall(callId)?.isOnHold()

    fun getParticipants(_callId: String) {
        val callParticipants = getCall(_callId)?.getMemberships() ?: ArrayList()
        _callMembershipsLiveData.postValue(callParticipants)

        callParticipants.forEach {
            repository.participantMuteMap[it.getPersonId()] = it.isSendingAudio()
        }
    }

    fun setUCDomainServerUrl(ucDomain: String, serverUrl: String) {
        webex.setUCDomainServerUrl(ucDomain, serverUrl)
    }

    fun setCallServiceCredential(username: String, password: String) {
        webex.setCallServiceCredential(username, password)
    }

    fun isUCLoggedIn(): Boolean {
        return webex.isUCLoggedIn()
    }

    fun getUCServerConnectionStatus(): UCLoginServerConnectionStatus {
        return webex.getUCServerConnectionStatus()
    }

    fun getUCServerFailureReason(): PhoneServiceRegistrationFailureReason {
        return repository.ucServerConnectionFailureReason
    }

    fun retryUCSSOLogin() {
        webex.retryUCSSOLogin()
    }

    fun ucCancelSSOLogin() {
        webex.ucCancelSSOLogin()
    }

    fun forceRegisterPhoneServices() {
        webex.forceRegisterPhoneServices()
    }

    fun startUCServices() {
        webex.startUCServices()
    }

    fun startAssociatedCall(callId: String, dialNumber: String, associationType: CallAssociationType, audioCall: Boolean) {
        getCall(callId)?.startAssociatedCall(dialNumber, associationType, audioCall, CompletionHandler { result ->
            Log.d(tag, "startAssociatedCall Lambda")
            if (result.isSuccessful) {
                Log.d(tag, "startAssociatedCall Lambda isSuccessful")
                result.data?.let {
                    setCallObserver(it)
                    _startAssociationLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.AssociationCallCompleted, it))
                }
            } else {
                Log.d(tag, "startAssociatedCall Lambda isSuccessful 5")
                _startAssociationLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.AssociationCallFailed, null, null, result.error?.errorMessage))
                Log.d(tag, "startAssociatedCall Lambda isSuccessful 6")
            }
        })
    }

    fun transferCall(fromCallId: String, toCallId: String) {
        getCall(fromCallId)?.transferCall(toCallId)
    }

    fun directTransferCall(callId: String, toPhoneNumber: String, callback: CompletionHandler<DirectTransferResult>) {
        getCall(callId)?.directTransferCall(toPhoneNumber, callback)
    }

    fun mergeCalls(currentCallId: String, targetCallId: String) {
        getCall(currentCallId)?.mergeCalls(targetCallId)
    }

    fun getlogFileUri(includelastRunLog: Boolean = false): Uri {
        return webex.getlogFileUri(includelastRunLog)
    }

    fun setPushTokens(id: String, token: String){
        if(BuildConfig.WEBHOOK_URL.isEmpty()) {
            webex.phone.setPushTokens(KitchenSinkApp.applicationContext().packageName, id, token, Constants.Keys.appId)
        }
    }

    fun getFCMToken(personModel: PersonModel) {
        FirebaseMessaging.getInstance().token
                .addOnCompleteListener(object : OnCompleteListener<String?> {
                    override fun onComplete(task: Task<String?>) {
                        if (!task.isSuccessful) {
                            Log.w(tag, "Fetching FCM registration token failed", task.exception)
                            return
                        }
                        // Get new FCM registration token
                        val token: String? = task.result
                        sendTokenToServer(Pair(token, personModel))
                        FirebaseInstallations.getInstance().id.addOnCompleteListener(object : OnCompleteListener<String?> {
                            override fun onComplete(task: Task<String?>) {
                                if (!task.isSuccessful) {
                                    Log.w(tag, "Fetching FCM registration id failed", task.exception)
                                    return
                                }
                                val mId = task.result
                                if(!mId.isNullOrEmpty() && !token.isNullOrEmpty())
                                    setPushTokens(mId, token)
                            }
                        })
                    }
                })
    }

    private fun sendTokenToServer(it: Pair<String?, PersonModel>) {
        val json = JSONObject()
        json.put("pushProvider", "FCM")
        json.put("deviceToken", it.first)
        json.put("userId", it.second.encodedId)
        //json.put("voipToken", "NA")
        RegisterTokenService().execute(json.toString())
    }

    fun postParticipantData(data: List<CallMembership>?) {
        synchronized(this) {
            _callMembershipsLiveData.postValue(data)

            var isRemoteSendingAudio = false

            data?.let {
                val iterator = it.iterator()
                while(iterator.hasNext()) {
                    val item = iterator.next()
                    if (item.getPersonId() != selfPersonId) {
                        if (item.isSendingAudio()) {
                            isRemoteSendingAudio = true
                        }
                    }
                    repository.participantMuteMap[item.getPersonId()] = item.isSendingAudio()
                }
            }

            Log.d(tag, "postParticipantData hasMutedAll: $isRemoteSendingAudio")
            doMuteAll = isRemoteSendingAudio
            repository._muteAllLiveData?.postValue(doMuteAll)
        }
    }

    fun getHeader(state: CallMembership.State): String {
        return when(state) {
            CallMembership.State.UNKNOWN -> "Not in meeting"
            CallMembership.State.JOINED -> "In meeting"
            CallMembership.State.WAITING -> "In lobby"
            CallMembership.State.IDLE -> "Idle"
            CallMembership.State.DECLINED -> "Call declined"
            CallMembership.State.LEFT -> "Left meeting"
            CallMembership.State.NOTIFIED -> "Notified"
        }
    }

    fun setVideoMaxTxFPSSetting(fps: Int) {
        webex.phone.setAdvancedSetting(AdvancedSetting.VideoMaxTxFPS(fps) as AdvancedSetting<*>)
    }

    fun setVideoEnableDecoderMosaicSetting(value: Boolean) {
        webex.phone.setAdvancedSetting(AdvancedSetting.VideoEnableDecoderMosaic(value) as AdvancedSetting<*>)
    }

    fun setShareMaxCaptureFPSSetting(fps: Int) {
        webex.phone.setAdvancedSetting(AdvancedSetting.ShareMaxCaptureFPS(fps) as AdvancedSetting<*>)
    }

    fun setVideoEnableCamera2Setting(value: Boolean) {
        webex.phone.setAdvancedSetting(AdvancedSetting.VideoEnableCamera2(value) as AdvancedSetting<*>)
    }

    fun enablePhotoCaptureSetting(value :Boolean){
        webex.phone.setAdvancedSetting((AdvancedSetting.EnablePhotoCapture(value) as AdvancedSetting<*>))
    }

    fun getEnablePhotoCaptureSetting(): Boolean? {
        return webex.phone.getAdvancedSetting(AdvancedSetting.EnablePhotoCapture::class)
            ?.getValue() as Boolean?
    }

    fun switchAudioMode(mode: Call.AudioOutputMode, handler: CompletionHandler<Boolean>) {
        getCall(currentCallId.orEmpty())?.switchAudioOutput(mode) { result ->
            if (result.data == false) {
                Log.d(tag, "ATSDK Error: Switch mode to ${mode.name} failed")
                handler.onComplete(ResultImpl.success(false))
            } else {
                Log.d(tag, "ATSDK Switch mode to ${mode.name} success")
                handler.onComplete(ResultImpl.success(true))
            }
        }
    }

    fun getCurrentAudioOutputMode(): Call.AudioOutputMode? {
        return getCall(currentCallId.orEmpty())?.getCurrentAudioOutput()
    }

    fun enableAudioBNR(value: Boolean) {
        webex.phone.enableAudioBNR(value)
    }

    fun isAudioBNREnable(): Boolean {
        return webex.phone.isAudioBNREnable()
    }

    fun setAudioBNRMode(mode: Phone.AudioBRNMode) {
        webex.phone.setAudioBNRMode(mode)
    }

    fun getAudioBNRMode(): Phone.AudioBRNMode {
        return webex.phone.getAudioBNRMode()
    }

    fun setDefaultFacingMode(mode: Phone.FacingMode) {
        webex.phone.setDefaultFacingMode(mode)
    }

    fun getDefaultFacingMode() : Phone.FacingMode {
        return webex.phone.getDefaultFacingMode()
    }

    fun disableVideoCodecActivation() {
        webex.phone.disableVideoCodecActivation()
    }

    fun getVideoCodecLicense(): String {
        return webex.phone.getVideoCodecLicense()
    }

    fun getVideoCodecLicenseURL(): String {
        return webex.phone.getVideoCodecLicenseURL()
    }

    fun requestVideoCodecActivation(builder: AlertDialog.Builder) {
        webex.phone.requestVideoCodecActivation(builder, CompletionHandler { result ->
            Log.d(tag, "requestVideoCodecActivation result action: ${result.data}")
        })
    }

    fun setHardwareAccelerationEnabled(enable: Boolean) {
        webex.phone.setHardwareAccelerationEnabled(enable)
    }


    fun getUserPreferredMaxBandwidth():Int{
        var videoBandwidth:Int
        when (maxVideoBandwidth) {
            WebexRepository.BandWidthOptions.BANDWIDTH_90P.name -> videoBandwidth = Phone.DefaultBandwidth.MAX_BANDWIDTH_90P.getValue()
            WebexRepository.BandWidthOptions.BANDWIDTH_180P.name -> videoBandwidth = Phone.DefaultBandwidth.MAX_BANDWIDTH_180P.getValue()
            WebexRepository.BandWidthOptions.BANDWIDTH_360P.name -> videoBandwidth = Phone.DefaultBandwidth.MAX_BANDWIDTH_360P.getValue()
            WebexRepository.BandWidthOptions.BANDWIDTH_1080P.name -> videoBandwidth = Phone.DefaultBandwidth.MAX_BANDWIDTH_1080P.getValue()
            else ->{
                videoBandwidth = Phone.DefaultBandwidth.MAX_BANDWIDTH_720P.getValue()
            }
        }
        return videoBandwidth
    }

    fun setVideoMaxTxBandwidth(bandwidth: Int){
        webex.phone.setVideoMaxTxBandwidth(bandwidth)
    }

    fun setVideoMaxRxBandwidth(bandwidth: Int){
        webex.phone.setVideoMaxRxBandwidth(bandwidth)
    }

    fun setSharingMaxRxBandwidth(bandwidth: Int) {
        webex.phone.setSharingMaxRxBandwidth(bandwidth)
    }

    fun setAudioMaxRxBandwidth(bandwidth: Int) {
        webex.phone.setAudioMaxRxBandwidth(bandwidth)
    }

    fun startPreview(preView: View) {
        webex.phone.startPreview(preView)
    }

    fun stopPreview() {
        webex.phone.stopPreview()
    }

    fun enableBackgroundConnection(enable: Boolean) {
        webex.phone.enableBackgroundConnection(enable)
    }

    fun enableBackgroundStream(enable: Boolean) {
        webex.phone.enableBackgroundStream(enable)
    }

    fun enableAskingReadPhoneStatePermission(enable: Boolean) {
        webex.phone.enableAskingReadPhoneStatePermission(enable)
    }

    fun getVideoRenderViews(callId: String): Pair<View?, View?> {
        return getCall(callId)?.getVideoRenderViews() ?: Pair(null, null)
    }

    fun setVideoRenderViews(callId: String, localVideoView: View?, remoteVideoView: View?) {
        getCall(callId)?.setVideoRenderViews(Pair(localVideoView, remoteVideoView))
    }

    fun setVideoRenderViews(callId: String) {
        getCall(callId)?.setVideoRenderViews(null)
    }

    fun forceSendingVideoLandscape(callId: String, forceLandscape: Boolean) {
        getCall(callId)?.forceSendingVideoLandscape(forceLandscape, CompletionHandler { result ->
            if (result.isSuccessful) {
                Log.d(tag, "forceSendingVideoLandscape Lambda isSuccessful")
                _forceSendingVideoLandscapeLiveData.postValue(true)
            } else {
                Log.d(tag, "forceSendingVideoLandscape Lambda error: ${result.error?.errorMessage}")
                _forceSendingVideoLandscapeLiveData.postValue(false)
            }
        })
    }

    fun getSharingRenderView(callId: String): View? {
        return getCall(callId)?.getSharingRenderView()
    }

    fun setSharingRenderView(callId: String, view: View?) {
        getCall(callId)?.setSharingRenderView(view)
    }

    fun setRemoteVideoRenderMode(callId: String, mode: Call.VideoRenderMode) {
        getCall(callId)?.setRemoteVideoRenderMode(mode, CompletionHandler {
            it.let {
                if (it.isSuccessful) {
                    Log.d(tag, "setRemoteVideoRenderMode successful")
                    _setRemoteVideoRenderModeLiveData.postValue(Pair(true, ""))
                } else {
                    Log.d(tag, "setRemoteVideoRenderMode failed: ${it.error?.errorMessage}")
                    _setRemoteVideoRenderModeLiveData.postValue(Pair(false, it.error?.errorMessage ?: ""))
                }
            }
        })
    }

    fun letIn(callId: String, callMembership: CallMembership) {
        getCall(callId)?.letIn(callMembership)
    }

    fun setVideoStreamMode(mode: Phone.VideoStreamMode) {
        webex.phone.setVideoStreamMode(mode)
    }

    fun getVideoStreamMode(): Phone.VideoStreamMode {
        return webex.phone.getVideoStreamMode()
    }

    fun getCompositedLayout(): MediaOption.CompositedVideoLayout {
        return getCall(currentCallId.orEmpty())?.getCompositedVideoLayout() ?: MediaOption.CompositedVideoLayout.NOT_SUPPORTED
    }

    fun setCompositedLayout(compositedLayout: MediaOption.CompositedVideoLayout) {
        compositedLayoutState = compositedLayout
        getCall(currentCallId.orEmpty())?.setCompositedVideoLayout(compositedLayout, CompletionHandler { result ->
            if (result.isSuccessful) {
                Log.d(tag, "setCompositedLayout Lambda isSuccessful")
                _setCompositeLayoutLiveData.postValue(Pair(true, ""))
            } else {
                Log.d(tag, "setCompositedLayout Lambda error: ${result.error?.errorMessage}")
                _setCompositeLayoutLiveData.postValue(Pair(false, result.error?.errorMessage ?: ""))
            }
        })
    }

    fun closeAuxStream(view: View) {
        getCall(currentCallId.orEmpty())?.closeAuxStream(view)
    }

    fun getAuxStream(view: View): AuxStream? {
        return getCall(currentCallId.orEmpty())?.getAuxStream(view)
    }

    fun getAvailableAuxStreamCount(): Int {
        return getCall(currentCallId.orEmpty())?.getAvailableAuxStreamCount() ?: 0
    }

    fun getOpenedAuxStreamCount(): Int {
        return getCall(currentCallId.orEmpty())?.getOpenedAuxStreamCount() ?: 0
    }

    fun openAuxStream(view: View) {
        getCall(currentCallId.orEmpty())?.openAuxStream(view)
    }

    fun hasAnyoneJoined(): Boolean {
        return getCall(currentCallId.orEmpty())?.hasAnyoneJoined() ?: false
    }

    fun isMeeting(): Boolean {
        return getCall(currentCallId.orEmpty())?.isMeeting() ?: false
    }

    fun isPmr(): Boolean {
        return getCall(currentCallId.orEmpty())?.isPmr() ?: false
    }

    fun isSelfCreator(): Boolean {
        return getCall(currentCallId.orEmpty())?.isSelfCreator() ?: false
    }

    fun isSpaceMeeting(): Boolean {
        return getCall(currentCallId.orEmpty())?.isSpaceMeeting() ?: false
    }

    fun isScheduledMeeting(): Boolean {
        return getCall(currentCallId.orEmpty())?.isScheduledMeeting() ?: false
    }

    fun getServiceUrl(type: Phone.ServiceUrlType): String? {
        return webex.phone.getServiceUrl(type)
    }

    fun setOnTokenExpiredListener() {
        webex.authenticator?.let {
            if (it is TokenAuthenticator) {
                it.setOnTokenExpiredListener(CompletionHandler {
                    // Handle when auth token has expired.
                    // When a token expires, new instances of `Webex` and `Authenticator` need to be created and used with a new token
                    Log.d(tag, "KS setOnTokenExpiredListener")
                    _signOutListenerLiveData.postValue(it.isSuccessful)
                })
            }
        }
    }

    fun isVirtualBackgroundSupported() = webex.phone.isVirtualBackgroundSupported()

    fun fetchVirtualBackgrounds() {
        repository.getVirtualBackgrounds(CompletionHandler {
            if (it.isSuccessful)
                _virtualBackground.postValue(it.data)
            else
                _virtualBgError.postValue(it.error?.errorMessage)
        })
    }

    fun addVirtualBackground(imgFile: LocalFile) {
        repository.addVirtualBackground(imgFile, CompletionHandler {
            if (it.isSuccessful)
                fetchVirtualBackgrounds()
            else
                _virtualBgError.postValue(it.error?.errorMessage)
        })
    }

    fun addVirtualBackground(imgFile: LocalFile, handler: CompletionHandler<VirtualBackground>) {
        repository.addVirtualBackground(imgFile, handler)
    }

    fun applyVirtualBackground(background: VirtualBackground, mode: Phone.VirtualBackgroundMode) {
        repository.applyVirtualBackground(background, mode, CompletionHandler {
            if (it.isSuccessful && it.data == true)
                Log.d(tag, "virtual background applied")
            else
                _virtualBgError.postValue(it.error?.errorMessage)
        })
    }

    fun removeVirtualBackground(background: VirtualBackground) {
        repository.removeVirtualBackground(background, CompletionHandler {
            if (it.isSuccessful && it.data == true) {
                Log.d(tag, "virtual background removed")
                fetchVirtualBackgrounds()
            }
            else {
                _virtualBgError.postValue(it.error?.errorMessage)
            }
        })
    }

    fun setMaxVirtualBackgrounds(limit: Int) {
        repository.setMaxVirtualBackgrounds(limit)
    }

    fun getMaxVirtualBackgrounds(): Int {
        return repository.getMaxVirtualBackgrounds()
    }

    fun setCameraFocusAtPoint(pointX: Float, pointY: Float): Boolean {
        return getCall(currentCallId.orEmpty())?.setCameraFocusAtPoint(pointX, pointY) ?: false
    }

    fun setCameraFlashMode(mode: Call.FlashMode): Boolean {
        return getCall(currentCallId.orEmpty())?.setCameraFlashMode(mode) ?: false
    }

    fun getCameraFlashMode(): Call.FlashMode {
        return getCall(currentCallId.orEmpty())?.getCameraFlashMode() ?: Call.FlashMode.OFF
    }

    fun setCameraTorchMode(mode: Call.TorchMode): Boolean {
        return getCall(currentCallId.orEmpty())?.setCameraTorchMode(mode) ?: false
    }

    fun getCameraTorchMode(): Call.TorchMode {
        return getCall(currentCallId.orEmpty())?.getCameraTorchMode() ?: Call.TorchMode.OFF
    }

    fun getCameraExposureDuration(): CameraExposureDuration? {
        return getCall(currentCallId.orEmpty())?.getCameraExposureDuration()
    }

    fun getCameraExposureISO(): CameraExposureISO? {
        return getCall(currentCallId.orEmpty())?.getCameraExposureISO()
    }

    fun getCameraExposureTargetBias(): CameraExposureTargetBias? {
        return getCall(currentCallId.orEmpty())?.getCameraExposureTargetBias()
    }

    fun setCameraCustomExposure(duration: Double, iso: Float): Boolean {
        return getCall(currentCallId.orEmpty())?.setCameraCustomExposure(duration, iso) ?: false
    }

    fun setCameraAutoExposure(targetBias: Float): Boolean {
        return getCall(currentCallId.orEmpty())?.setCameraAutoExposure(targetBias) ?: false
    }

    fun setVideoZoomFactor(factor: Float): Boolean {
        return getCall(currentCallId.orEmpty())?.setVideoZoomFactor(factor) ?: false
    }

    fun getVideoZoomFactor(): Float {
        return getCall(currentCallId.orEmpty())?.getVideoZoomFactor() ?: 1.0f
    }

    fun takePhoto(): Boolean {
        return getCall(currentCallId.orEmpty())?.takePhoto() ?: false
    }

    fun setMediaStreamCategoryA(duplicate: Boolean, quality: MediaStreamQuality) {
        getCall(currentCallId.orEmpty())?.setMediaStreamCategoryA(duplicate, quality)
    }

    fun setMediaStreamsCategoryB(numStreams: Int, quality: MediaStreamQuality) {
        getCall(currentCallId.orEmpty())?.setMediaStreamsCategoryB(numStreams, quality)
    }

    fun setMediaStreamCategoryC(participantId: String, quality: MediaStreamQuality) {
        getCall(currentCallId.orEmpty())?.setMediaStreamCategoryC(participantId, quality)
    }

    fun removeMediaStreamCategoryA() {
        getCall(currentCallId.orEmpty())?.removeMediaStreamCategoryA()
    }

    fun removeMediaStreamsCategoryB() {
        getCall(currentCallId.orEmpty())?.removeMediaStreamsCategoryB()
    }

    fun removeMediaStreamCategoryC(participantId: String) {
        getCall(currentCallId.orEmpty())?.removeMediaStreamCategoryC(participantId)
    }

    fun getMediaStreams(): List<MediaStream>? {
        return getCall(currentCallId.orEmpty())?.getMediaStreams()
    }

    fun isMediaStreamsPinningSupported(): Boolean {
        return getCall(currentCallId.orEmpty())?.isMediaStreamsPinningSupported() ?: false
    }

    fun joinBreakoutSession(breakoutSession: BreakoutSession) {
        getCall(currentCallId.orEmpty())?.joinBreakoutSession(breakoutSession)
    }

    fun returnToMainSession() {
        getCall(currentCallId.orEmpty())?.returnToMainSession()
    }

    fun getCallingType(): Phone.CallingType {
        return webex.phone.getCallingType()
    }

    fun makeHost(participantId: String, handler: CompletionHandler<MakeHostError>) {
        getCall(currentCallId.orEmpty())?.makeHost(participantId) { result ->
            if (result.isSuccessful) {
                Log.d(tag, "Make host successful")
                handler.onComplete(ResultImpl.success())
            } else {
                Log.d(tag, "Make host failed")
                handler.onComplete(ResultImpl.error(result.error?.errorMessage))
            }
        }
    }

    fun reclaimHost(hostKey:String, handler: CompletionHandler<ReclaimHostError>) {
        getCall(currentCallId.orEmpty())?.reclaimHost(hostKey) { result ->
            if (result.isSuccessful) {
                Log.d(tag, "reclaimHost successful")
                handler.onComplete(ResultImpl.success())
            } else {
                Log.d(tag, "reclaimHost failed")
                handler.onComplete(ResultImpl.error(result.error?.errorMessage))
            }
        }
    }

    fun setOnInitialSpacesSyncCompletedListener() {
        repository.setOnInitialSpacesSyncCompletedListener() {
            _initialSpacesSyncCompletedLiveData.postValue(true)
        }
    }

    fun isSpacesSyncCompleted(): Boolean {
        return webex.spaces.isSpacesSyncCompleted()
    }

    fun getProductCapability(): ProductCapability {
        return webex.people.getProductCapability()
    }

    fun getReceivingNoiseInfo(): ReceivingNoiseInfo? {
        return getCall(currentCallId.orEmpty())?.getReceivingNoiseInfo()
    }

    fun enableReceivingNoiseRemoval(enable: Boolean, callback: CompletionHandler<ReceivingNoiseRemovalEnableResult>) {
        getCall(currentCallId.orEmpty())?.enableReceivingNoiseRemoval(enable, callback)
    }

    fun isVideoEnabled(): Boolean {
        return getCall(currentCallId.orEmpty())?.isVideoEnabled() ?: false
    }

    fun inviteParticipant(invitee: String, callback: CompletionHandler<InviteParticipantError>) {
        getCall(currentCallId.orEmpty())?.inviteParticipant(invitee) { result ->
            if (result.isSuccessful) {
                Log.d(tag, "InviteParticipant successful")
                callback.onComplete(ResultImpl.success())
            } else {
                Log.d(tag, "InviteParticipant failed")
                callback.onComplete(ResultImpl.error(result.error?.errorMessage))
            }
        }
    }

    fun cleanup() {
        repository.removeIncomingCallListener("viewmodel"+this)
        for (entry in callObserverMap.entries.iterator()) {
            repository.removeCallObserver(entry.key, entry.value)
        }
        callObserverMap.clear()
    }

    @Synchronized
    fun clearCallObservers(callId: String) {
        repository.clearCallObservers(callId)
    }

    fun enableStreams() {
        webex.phone.enableStreams(true)
    }

    fun printObservers(writer : PrintWriter) {
        writer.println("******** Call Observers **********")
        callObserverMap.forEach { (key, value) -> writer.println("$key = $value") }
        writer.println("******************")
        repository.printObservers(writer)
    }

    fun startAudioDump() {
        getCall(currentCallId.orEmpty())?.startAudioDump(KitchenSinkApp.applicationContext()) {
            if (it.isSuccessful) {
                Log.d(tag, "[AudioDump] startAudioDump successful")
            } else {
                Log.d(tag, "[AudioDump] startAudioDump error: ${it.error?.errorMessage}")
            }
            _startAudioDumpLiveData.postValue(it.isSuccessful)
        }
    }

    fun stopAudioDump() {
        getCall(currentCallId.orEmpty())?.stopAudioDump() {
            if (it.isSuccessful) {
                Log.d(tag, "[AudioDump] stopAudioDump successful")
            } else {
                Log.d(tag, "[AudioDump] stopAudioDump error: ${it.error?.errorMessage}")
            }
            _stopAudioDumpLiveData.postValue(it.isSuccessful)
        }

    }

    fun canStartRecordingAudioDump() {
        getCall(currentCallId.orEmpty())?.canStartRecordingAudioDump {
            if (it.isSuccessful) {
                Log.d(tag, "[AudioDump] canStartRecordingAudioDump successful")
            } else {
                Log.d(tag, "[AudioDump] canStartRecordingAudioDump error: ${it.error?.errorMessage}")
            }
            _canStartAudioDumpLiveData.postValue(it.isSuccessful)
        }
    }


    fun isRecordingAudioDump(): Boolean {
        return getCall(currentCallId.orEmpty())?.isRecordingAudioDump() ?: false
    }
}