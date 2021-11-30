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
import com.ciscowebex.androidsdk.phone.Call
import com.ciscowebex.androidsdk.phone.CallObserver
import com.ciscowebex.androidsdk.phone.MediaOption
import com.ciscowebex.androidsdk.phone.CallMembership
import com.ciscowebex.androidsdk.phone.Phone
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.WebexError
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import com.ciscowebex.androidsdk.phone.CallAssociationType
import com.ciscowebex.androidsdk.auth.PhoneServiceRegistrationFailureReason
import com.ciscowebex.androidsdk.auth.TokenAuthenticator
import com.ciscowebex.androidsdk.auth.UCLoginServerConnectionStatus
import com.ciscowebex.androidsdk.kitchensink.calling.CallObserverInterface
import com.ciscowebex.androidsdk.kitchensink.utils.CallObjectStorage
import com.ciscowebex.androidsdk.message.LocalFile
import com.ciscowebex.androidsdk.phone.AdvancedSetting
import com.ciscowebex.androidsdk.phone.AuxStream
import com.ciscowebex.androidsdk.phone.VirtualBackground


class WebexViewModel(val webex: Webex, val repository: WebexRepository) : BaseViewModel() {
    private val tag = "WebexViewModel"

    var _callMembershipsLiveData = MutableLiveData<List<CallMembership>>()
    val _muteAllLiveData = MutableLiveData<Boolean>()
    val _cucmLiveData = MutableLiveData<Pair<WebexRepository.CucmEvent, String>>()
    val _callingLiveData = MutableLiveData<WebexRepository.CallLiveData>()
    val _startAssociationLiveData = MutableLiveData<WebexRepository.CallLiveData>()
    val _startShareLiveData = MutableLiveData<Boolean>()
    val _stopShareLiveData = MutableLiveData<Boolean>()
    val _setCompositeLayoutLiveData = MutableLiveData<Pair<Boolean, String>>()
    val _setRemoteVideoRenderModeLiveData = MutableLiveData<Pair<Boolean, String>>()
    val _forceSendingVideoLandscapeLiveData = MutableLiveData<Boolean>()

    var callMembershipsLiveData: LiveData<List<CallMembership>> = _callMembershipsLiveData
    val muteAllLiveData: LiveData<Boolean> = _muteAllLiveData
    val cucmLiveData: LiveData<Pair<WebexRepository.CucmEvent, String>> = _cucmLiveData
    val callingLiveData: LiveData<WebexRepository.CallLiveData> = _callingLiveData
    val startAssociationLiveData: LiveData<WebexRepository.CallLiveData> = _startAssociationLiveData
    val startShareLiveData: LiveData<Boolean> = _startShareLiveData
    val stopShareLiveData: LiveData<Boolean> = _stopShareLiveData
    val setCompositeLayoutLiveData: LiveData<Pair<Boolean, String>> = _setCompositeLayoutLiveData
    val setRemoteVideoRenderModeLiveData: LiveData<Pair<Boolean, String>> = _setRemoteVideoRenderModeLiveData
    val forceSendingVideoLandscapeLiveData: LiveData<Boolean> = _forceSendingVideoLandscapeLiveData

    private val _incomingListenerLiveData = MutableLiveData<Call?>()
    val incomingListenerLiveData: LiveData<Call?> = _incomingListenerLiveData

    private val _signOutListenerLiveData = MutableLiveData<Boolean>()
    val signOutListenerLiveData: LiveData<Boolean> = _signOutListenerLiveData

    private val _tokenLiveData = MutableLiveData<Pair<String?, PersonModel>>()
    val tokenLiveData: LiveData<Pair<String?, PersonModel>> = _tokenLiveData

    private val _virtualBackground = MutableLiveData<List<VirtualBackground>>()
    val virtualBackground: LiveData<List<VirtualBackground>> = _virtualBackground

    private val _virtualBgError = MutableLiveData<String>()
    val virtualBgError: LiveData<String> = _virtualBgError

    var selfPersonId: String? = null
    var compositedLayoutState = MediaOption.CompositedVideoLayout.NOT_SUPPORTED

    var callObserverInterface: CallObserverInterface? = null

    var isVideoViewsSwapped: Boolean = true

    var isSendingVideoForceLandscape: Boolean = false

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

    var isCUCMServerLoggedIn: Boolean
        get() = repository.isCUCMServerLoggedIn
        set(value) {
            repository.isCUCMServerLoggedIn = value
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

    var isConsoleLoggerEnabled: Boolean
        get() = repository.isConsoleLoggerEnabled
        set(value) {
            repository.isConsoleLoggerEnabled = value
        }

    init {
        repository._callMembershipsLiveData = _callMembershipsLiveData
        repository._cucmLiveData = _cucmLiveData
        repository._muteAllLiveData = _muteAllLiveData
        repository._callingLiveData = _callingLiveData
        repository._startAssociationLiveData = _startAssociationLiveData
        repository._startShareLiveData = _startShareLiveData
        repository._stopShareLiveData = _stopShareLiveData
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
        webex.phone.setIncomingCallListener(object : Phone.IncomingCallListener {
            override fun onIncomingCall(call: Call?) {
                call?.let {
                    Log.d(tag, "setIncomingCallListener Call object : ${it.getCallId()}")
                    CallObjectStorage.addCallObject(it)
                    _incomingListenerLiveData.postValue(it)
                    setCallObserver(it)
                } ?: run {
                    Log.d(tag, "setIncomingCallListener Call object null")
                }
            }
        })
    }

    fun setFCMIncomingListenerObserver(callId: String) {
        val call = CallObjectStorage.getCallObject(callId)
        call?.let {
            setCallObserver(it)
        }
    }

    fun setGlobalIncomingListener() {
        repository.setIncomingListener()
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
                result.error?.let { errorCode ->
                    if (errorCode.errorCode == WebexError.ErrorCode.HOST_PIN_OR_MEETING_PASSWORD_REQUIRED.code) {
                        _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.MeetingPinOrPasswordRequired, null))
                    } else {
                        _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.DialFailed, null, null, result.error?.errorMessage))
                    }
                } ?: run {
                    _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.DialFailed, null, null, result.error?.errorMessage))
                }
            }
        })
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

    private fun setCallObserver(call: Call) {
        call.setObserver(object : CallObserver {
            override fun onConnected(call: Call?) {
                callObserverInterface?.onConnected(call)
            }

            override fun onRinging(call: Call?) {
                callObserverInterface?.onRinging(call)
            }

            override fun onWaiting(call: Call?, reason: Call.WaitReason?) {
                Log.d(tag, "CallObserver onWaiting reason: $reason")
                callObserverInterface?.onWaiting(call)
            }

            override fun onDisconnected(event: CallObserver.CallDisconnectedEvent?) {
                Log.d(tag, "CallObserver onDisconnected event: $event")
                callObserverInterface?.onDisconnected(call, event)
            }

            override fun onInfoChanged(call: Call?) {
                callObserverInterface?.onInfoChanged(call)
            }

            override fun onMediaChanged(event: CallObserver.MediaChangedEvent?) {
                Log.d(tag, "CallObserver OnMediaChanged event: $event")
                callObserverInterface?.onMediaChanged(call, event)
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
        })
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
        if (!isSendingAudio) {
            muteSelfAudio(callId)
        }
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

    fun startShare(callId: String) {
        getCall(callId)?.startSharing(CompletionHandler { result ->
            _startShareLiveData.postValue(result.isSuccessful)
        })
    }

    fun startShare(callId: String, notification: Notification?, notificationId: Int) {
        getCall(callId)?.startSharing(notification, notificationId, CompletionHandler { result ->
            _startShareLiveData.postValue(result.isSuccessful)
        })
    }

    fun setSendingSharing(callId: String, value: Boolean) {
        getCall(callId)?.setSendingSharing(value)
    }

    fun stopShare(callId: String) {
        getCall(callId)?.stopSharing(CompletionHandler { result ->
            _stopShareLiveData.postValue(result.isSuccessful)
        })
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
        repository._callMembershipsLiveData?.postValue(callParticipants)

        callParticipants.forEach {
            repository.participantMuteMap[it.getPersonId()] = it.isSendingAudio()
        }
    }

    fun setUCDomainServerUrl(ucDomain: String, serverUrl: String) {
        webex.setUCDomainServerUrl(ucDomain, serverUrl)
    }

    fun setCUCMCredential(username: String, password: String) {
        webex.setCUCMCredential(username, password)
    }

    fun isUCLoggedIn(): Boolean {
        return webex.isUCLoggedIn()
    }

    fun getUCServerConnectionStatus(): UCLoginServerConnectionStatus {
        return webex.getUCServerConnectionStatus()
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

    fun mergeCalls(currentCallId: String, targetCallId: String) {
        getCall(currentCallId)?.mergeCalls(targetCallId)
    }

    fun getlogFileUri(includelastRunLog: Boolean = false): Uri {
        return webex.getlogFileUri(includelastRunLog)
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
                        Log.d(tag, "$token")
                        sendTokenToServer(Pair(token, personModel))
                    }
                })
    }

    private fun sendTokenToServer(it: Pair<String?, PersonModel>) {
        val json = JSONObject()
        json.put("token", it.first)
        json.put("personId", it.second.personId)
        json.put("email", it.second.emailList)
        RegisterTokenService().execute(json.toString())
    }

    fun postParticipantData(data: List<CallMembership>?) {
        synchronized(this) {
            _callMembershipsLiveData.postValue(data)

            var isRemoteSendingAudio = false
            data?.forEach {
                if (it.getPersonId() != selfPersonId) {
                    if (it.isSendingAudio()) {
                        isRemoteSendingAudio = true
                    }
                }
                repository.participantMuteMap[it.getPersonId()] = it.isSendingAudio()
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

    fun switchAudioMode(mode: Call.AudioOutputMode) {
        getCall(currentCallId.orEmpty())?.switchAudioOutput(mode)
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

    fun setVideoMaxRxBandwidth(bandwidth: Int) {
        webex.phone.setVideoMaxRxBandwidth(bandwidth)
    }

    fun setVideoMaxTxBandwidth(bandwidth: Int) {
        webex.phone.setVideoMaxTxBandwidth(bandwidth)
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

    fun setVideoRenderViews(callId: String, localVideoView: View, remoteVideoView: View) {
        getCall(callId)?.setVideoRenderViews(Pair(localVideoView, remoteVideoView))
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
}