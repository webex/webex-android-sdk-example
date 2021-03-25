package com.ciscowebex.androidsdk.kitchensink

import android.app.AlertDialog
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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import com.ciscowebex.androidsdk.phone.CallAssociationType
import com.ciscowebex.androidsdk.auth.PhoneServiceRegistrationFailureReason
import com.ciscowebex.androidsdk.auth.UCLoginServerConnectionStatus
import com.ciscowebex.androidsdk.phone.AdvancedSetting


class WebexViewModel(val webex: Webex, val repository: WebexRepository) : BaseViewModel() {
    private val tag = "WebexViewModel"

    var _callMembershipsLiveData = MutableLiveData<List<CallMembership>>()
    val _muteAllLiveData = MutableLiveData<Boolean>()
    val _cucmLiveData = MutableLiveData<Pair<WebexRepository.CucmEvent, String>>()
    val _callingLiveData = MutableLiveData<WebexRepository.CallLiveData>()
    val _membershipLiveData = MutableLiveData<WebexRepository.CallLiveData>()
    val _infoLiveData = MutableLiveData<WebexRepository.CallLiveData>()
    val _mediaInfoLiveData = MutableLiveData<WebexRepository.CallLiveData>()
    val _disconnectedLiveData = MutableLiveData<WebexRepository.CallLiveData>()
    val _startAssociationLiveData = MutableLiveData<WebexRepository.CallLiveData>()
    val _startShareLiveData = MutableLiveData<Boolean>()
    val _stopShareLiveData = MutableLiveData<Boolean>()

    var callMembershipsLiveData: LiveData<List<CallMembership>> = _callMembershipsLiveData
    val muteAllLiveData: LiveData<Boolean> = _muteAllLiveData
    val cucmLiveData: LiveData<Pair<WebexRepository.CucmEvent, String>> = _cucmLiveData
    val callingLiveData: LiveData<WebexRepository.CallLiveData> = _callingLiveData
    val membershipLiveData: LiveData<WebexRepository.CallLiveData> = _membershipLiveData
    val infoLiveData: LiveData<WebexRepository.CallLiveData> = _infoLiveData
    val mediaInfoLiveData: LiveData<WebexRepository.CallLiveData> = _mediaInfoLiveData
    val disconnectedLiveData: LiveData<WebexRepository.CallLiveData> = _disconnectedLiveData
    val startAssociationLiveData: LiveData<WebexRepository.CallLiveData> = _startAssociationLiveData
    val startShareLiveData: LiveData<Boolean> = _startShareLiveData
    val stopShareLiveData: LiveData<Boolean> = _stopShareLiveData

    private val _incomingListenerLiveData = MutableLiveData<Call?>()
    val incomingListenerLiveData: LiveData<Call?> = _incomingListenerLiveData

    private val _signOutListenerLiveData = MutableLiveData<Boolean>()
    val signOutListenerLiveData: LiveData<Boolean> = _signOutListenerLiveData

    private val _tokenLiveData = MutableLiveData<Pair<String?, PersonModel>>()
    val tokenLiveData: LiveData<Pair<String?, PersonModel>> = _tokenLiveData

    var selfPersonId: String? = null

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

    var enableBgtoggle: Boolean
        get() = repository.enableBgtoggle
        set(value) {
            repository.enableBgtoggle = value
        }

    init {
        repository._callMembershipsLiveData = _callMembershipsLiveData
        repository._cucmLiveData = _cucmLiveData
        repository._muteAllLiveData = _muteAllLiveData
        repository._callingLiveData = _callingLiveData
        repository._membershipLiveData = _membershipLiveData
        repository._infoLiveData = _infoLiveData
        repository._mediaInfoLiveData = _mediaInfoLiveData
        repository._startAssociationLiveData = _startAssociationLiveData
        repository._disconnectedLiveData = _disconnectedLiveData
        repository._startShareLiveData = _startShareLiveData
        repository._stopShareLiveData = _stopShareLiveData
    }

    fun setLogLevel() {
        webex.setLogLevel(Webex.LogLevel.ALL)
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

    fun setIncomingListener() {
        webex.phone.setIncomingCallListener(object : Phone.IncomingCallListener {
            override fun onIncomingCall(call: Call?) {
                call?.let {
                    _incomingListenerLiveData.postValue(it)
                    setCallObserver(it)
                } ?: run {
                    Log.d(tag, "setIncomingCallListener Call object null")
                }
            }
        })
    }

    fun signOut() {
        webex.signOut(CompletionHandler { result ->
            result?.let {
                _signOutListenerLiveData.postValue(result.isSuccessful)
            }
        })
    }

    fun dial(input: String, option: MediaOption) {
        webex.phone.dial(input, option, CompletionHandler { result ->
            Log.d(tag, "Omnius: onCallEvent CallStateChanged")
            if (result.isSuccessful) {
                result.data?.let { _call ->
                    currentCallId = _call.getCallId()
                    setCallObserver(_call)
                    _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.DialCompleted, _call))
                }
            } else {
                _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.DialFailed, null, null, result.error?.errorMessage))
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
                _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.OnConnected, call))
            }

            override fun onRinging(call: Call?) {
                _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.OnRinging, call))
            }

            override fun onWaiting(call: Call?, reason: Call.WaitReason?) {
                Log.d(tag, "CallObserver onWaiting reason: $reason")
                _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.OnWaiting, call))
            }

            override fun onDisconnected(event: CallObserver.CallDisconnectedEvent?) {
                Log.d(tag, "CallObserver onDisconnected event: $event")
                _disconnectedLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.OnDisconnected, event?.getCall(), null, null, null, null, event))
            }

            override fun onInfoChanged(call: Call?) {
                _infoLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.OnInfoChanged, call))
            }

            override fun onMediaChanged(event: CallObserver.MediaChangedEvent?) {
                Log.d(tag, "CallObserver OnMediaChanged event: $event")
                _mediaInfoLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.OnMediaChanged, event?.getCall(), null, null, null, event))
            }

            override fun onCallMembershipChanged(event: CallObserver.CallMembershipChangedEvent?) {
                _membershipLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.OnCallMembershipEvent, event?.getCall(), null, null, event))
            }

            override fun onScheduleChanged(call: Call?) {
                _callingLiveData.postValue(WebexRepository.CallLiveData(WebexRepository.CallEvent.OnScheduleChanged, call))
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
        getCall(callId).setSendingVideo(!doMute)
    }

    fun getCall(callId: String): Call {
        return repository.getCall(callId)
    }

    fun muteAllParticipantAudio(callId: String) {
        if (!isSendingAudio) {
            muteSelfAudio(callId)
        }
        Log.d(tag, "postParticipantData muteAllParticipantAudio: $doMuteAll")
        webex.phone.getCall(callId).muteAllParticipantAudio(doMuteAll)
    }

    fun muteParticipant(callId: String, participantId: String) {
        repository.participantMuteMap[participantId]?.let { doMute ->
            if (participantId == selfPersonId) {
                muteSelfAudio(callId)
            } else {
                webex.phone.getCall(callId).muteParticipantAudio(participantId, doMute)
            }
        }
    }

    fun muteSelfAudio(callId: String) {
        Log.d(tag, "muteSelfAudio isSendingAudio: $isSendingAudio")
        getCall(callId).setSendingAudio(!isSendingAudio)
    }

    fun startShare(callId: String) {
        getCall(callId).startSharing(CompletionHandler { result ->
            _startShareLiveData.postValue(result.isSuccessful)
        })
    }

    fun setSendingSharing(callId: String, value: Boolean) {
        getCall(callId).setSendingSharing(value)
    }

    fun stopShare(callId: String) {
        getCall(callId).stopSharing(CompletionHandler { result ->
            _stopShareLiveData.postValue(result.isSuccessful)
        })
    }

    fun sendFeedback(callId: String, rating: Int, comment: String) {
        getCall(callId).sendFeedback(rating, comment)
    }

    fun sendDTMF(callId: String, keys: String) {
        getCall(callId).sendDTMF(keys, CompletionHandler { result ->
            if (result.isSuccessful) {
                Log.d(tag, "sendDTMF successful")
            } else {
                Log.d(tag, "sendDTMF error: ${result.error?.errorMessage}")
            }
        })
    }

    fun hangup(callId: String) {
        getCall(callId).hangup(CompletionHandler { result ->
            if (result.isSuccessful) {
                Log.d(tag, "hangup successful")
            } else {
                Log.d(tag, "hangup error: ${result.error?.errorMessage}")
            }
        })
    }

    fun rejectCall(callId: String) {
        getCall(callId).reject(CompletionHandler { result ->
            if (result.isSuccessful) {
                Log.d(tag, "rejectCall successful")
            } else {
                Log.d(tag, "rejectCall error: ${result.error?.errorMessage}")
            }
        })
    }

    fun holdCall(callId: String) {
        val callInfo = getCall(callId)
        val isOnHold = callInfo.isOnHold()
        Log.d(tag, "holdCall isOnHold = $isOnHold")
        webex.phone.getCall(callId).holdCall(!isOnHold)
    }

    fun isOnHold(callId: String) = webex.phone.getCall(callId).isOnHold()

    fun getParticipants(_callId: String) {
        val callParticipants = webex.phone.getCall(_callId).getMemberships()
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
        webex.phone.getCall(callId).startAssociatedCall(dialNumber, associationType, audioCall, CompletionHandler { result ->
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
        return webex.phone.getCall(fromCallId).transferCall(toCallId)
    }

    fun mergeCalls(currentCallId: String, targetCallId: String) {
        return webex.phone.getCall(currentCallId).mergeCalls(targetCallId)
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
        getCall(currentCallId.orEmpty()).switchAudioOutput(mode)
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

    fun getVideoRenderViews(callId: String): Pair<View?, View?> {
        return webex.phone.getCall(callId).getVideoRenderViews()
    }

    fun setVideoRenderViews(callId: String, localVideoView: View, remoteVideoView: View) {
        webex.phone.getCall(callId).setVideoRenderViews(Pair(localVideoView, remoteVideoView))
    }

    fun getSharingRenderView(callId: String): View? {
        return webex.phone.getCall(callId).getSharingRenderView()
    }

    fun setSharingRenderView(callId: String, view: View?) {
        webex.phone.getCall(callId).setSharingRenderView(view)
    }

    fun setRemoteVideoRenderMode(callId: String, mode: Call.VideoRenderMode) {
        webex.phone.getCall(callId).setRemoteVideoRenderMode(mode)
    }
}