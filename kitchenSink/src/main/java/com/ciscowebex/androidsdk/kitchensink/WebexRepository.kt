package com.ciscowebex.androidsdk.kitchensink

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.WebexDelegate
import com.ciscowebex.androidsdk.phone.Call
import com.ciscowebex.androidsdk.message.Message
import com.ciscowebex.androidsdk.people.Person
import com.ciscowebex.androidsdk.space.Space
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.auth.PhoneServiceRegistrationFailureReason
import com.ciscowebex.androidsdk.auth.UCLoginServerConnectionStatus
import com.ciscowebex.androidsdk.membership.Membership
import com.ciscowebex.androidsdk.membership.MembershipObserver
import com.ciscowebex.androidsdk.message.MessageObserver
import com.ciscowebex.androidsdk.space.SpaceObserver
import com.ciscowebex.androidsdk.phone.CallMembership
import com.ciscowebex.androidsdk.phone.CallObserver
import com.ciscowebex.androidsdk.phone.NotificationCallType

class WebexRepository(val webex: Webex) : WebexDelegate {
    private val tag = "WebexRepository"

    enum class CucmEvent {
        ShowSSOLogin,
        ShowNonSSOLogin,
        OnUCLoginFailed,
        OnUCLoggedIn,
        OnUCServerConnectionStateChanged
    }

    enum class SpaceEvent {
        Created,
        Updated,
        CallStarted,
        CallEnded
    }

    enum class MembershipEvent {
        Created,
        Updated,
        Deleted,
        MessageSeen
    }

    enum class MessageEvent {
        Received,
        Edited,
        Deleted,
        MessageThumbnailUpdated
    }

    enum class CallEvent {
        DialCompleted,
        DialFailed,
        OnConnected,
        OnDisconnected,
        OnRinging,
        OnWaiting,
        OnInfoChanged,
        OnCallMembershipEvent,
        OnMediaChanged,
        AnswerCompleted,
        AnswerFailed,
        AssociationCallCompleted,
        AssociationCallFailed,
        OnScheduleChanged
    }

    data class CallLiveData(val event: CallEvent,
                            val call: Call? = null,
                            val sharingLabel: String? = null,
                            val errorMessage: String? = null,
                            val callMembershipEvent: CallObserver.CallMembershipChangedEvent? = null,
                            val mediaChangeEvent: CallObserver.MediaChangedEvent? = null,
                            val disconnectEvent: CallObserver.CallDisconnectedEvent? = null) {}

    var isAddedCall = false
    var currentCallId: String? = null
    var oldCallId: String? = null
    var isSendingAudio = true
    var doMuteAll = true
    var incomingCallJoinedCallId: String? = null
    var isLocalVideoMuted = true
    var isRemoteVideoMuted = true
    var isRemoteScreenShareON = false

    val participantMuteMap = hashMapOf<String, Boolean>()
    var isCUCMServerLoggedIn = false
    var ucServerConnectionStatus: UCLoginServerConnectionStatus = UCLoginServerConnectionStatus.Idle
    var ucServerConnectionFailureReason: PhoneServiceRegistrationFailureReason = PhoneServiceRegistrationFailureReason.Unknown

    var _callMembershipsLiveData: MutableLiveData<List<CallMembership>>? = null
    var _muteAllLiveData: MutableLiveData<Boolean>? = null
    var _cucmLiveData: MutableLiveData<Pair<CucmEvent, String>>? = null
    var _callingLiveData: MutableLiveData<CallLiveData>? = null
    var _membershipLiveData: MutableLiveData<CallLiveData>? = null
    var _infoLiveData: MutableLiveData<CallLiveData>? = null
    var _mediaInfoLiveData: MutableLiveData<CallLiveData>? = null
    var _startAssociationLiveData: MutableLiveData<CallLiveData>? = null
    var _disconnectedLiveData: MutableLiveData<CallLiveData>? = null
    var _startShareLiveData: MutableLiveData<Boolean>? = null
    var _stopShareLiveData: MutableLiveData<Boolean>? = null

    var _spaceEventLiveData: MutableLiveData<Pair<SpaceEvent, Any?>>? = null
    var _membershipEventLiveData: MutableLiveData<Pair<MembershipEvent, Membership?>>? = null
    var _messageEventLiveData: MutableLiveData<Pair<MessageEvent, Any?>>? = null

    init {
        webex.delegate = this
    }

    fun clearCallData() {
        isAddedCall = false
        currentCallId = null
        oldCallId = null
        incomingCallJoinedCallId = null
        isSendingAudio = true
        doMuteAll = true
        isLocalVideoMuted = true
        isRemoteScreenShareON = false
        isRemoteVideoMuted = true

        _callMembershipsLiveData = null
        _muteAllLiveData = null
        _callingLiveData = null
        _membershipLiveData = null
        _infoLiveData = null
        _mediaInfoLiveData = null
        _startAssociationLiveData = null
        _disconnectedLiveData = null
        _startShareLiveData = null
        _stopShareLiveData = null
    }

    fun clearSpaceData(){
        _spaceEventLiveData = null
    }

    fun setSpaceObserver() {
        webex.spaces.setSpaceObserver(object : SpaceObserver {
            override fun onEvent(event: SpaceObserver.SpaceEvent) {
                Log.d(tag, "onEvent: $event")
                when (event) {
                    is SpaceObserver.SpaceCallStarted -> {
                        _spaceEventLiveData?.postValue(Pair(SpaceEvent.CallStarted, event.getSpaceId()))
                    }
                    is SpaceObserver.SpaceCallEnded -> {
                        _spaceEventLiveData?.postValue(Pair(SpaceEvent.CallEnded, event.getSpaceId()))
                    }
                    is SpaceObserver.SpaceCreated -> {
                        _spaceEventLiveData?.postValue(Pair(SpaceEvent.Created, event.getSpace()))
                    }
                    is SpaceObserver.SpaceUpdated -> {
                        _spaceEventLiveData?.postValue(Pair(SpaceEvent.Updated, event.getSpace()))
                    }
                }
            }
        })
    }

    fun setMembershipObserver() {
        webex.memberships.setMembershipObserver(object : MembershipObserver {
            override fun onEvent(event: MembershipObserver.MembershipEvent?) {
                Log.d(tag, "onMembershipEvent: $event")
                when (event) {
                    is MembershipObserver.MembershipCreated -> {
                        _membershipEventLiveData?.postValue(Pair(MembershipEvent.Created, event.getMembership()))
                    }
                    is MembershipObserver.MembershipUpdated -> {
                        _membershipEventLiveData?.postValue(Pair(MembershipEvent.Updated, event.getMembership()))
                    }
                    is MembershipObserver.MembershipDeleted -> {
                        _membershipEventLiveData?.postValue(Pair(MembershipEvent.Deleted, event.getMembership()))
                    }
                    is MembershipObserver.MembershipMessageSeen -> {
                        _membershipEventLiveData?.postValue(Pair(MembershipEvent.MessageSeen, event.getMembership()))
                    }
                }
            }
        })
    }

    fun setMessageObserver() {
        webex.messages.setMessageObserver(object : MessageObserver {
            override fun onEvent(event: MessageObserver.MessageEvent) {
                Log.d(tag, "onMessageEvent: $event")
                when (event) {
                    is MessageObserver.MessageReceived -> {
                        _messageEventLiveData?.postValue(Pair(MessageEvent.Received, event.getMessage()))
                    }
                    is MessageObserver.MessageDeleted -> {
                        _messageEventLiveData?.postValue(Pair(MessageEvent.Deleted, event.getMessageId()))
                    }
                    is MessageObserver.MessageFileThumbnailsUpdated -> {
                        Log.d(tag, "onMessageFileThumbnailsUpdated triggered!")
                        _messageEventLiveData?.postValue(Pair(MessageEvent.MessageThumbnailUpdated, event.getFiles()))
                    }
                    is MessageObserver.MessageEdited -> {
                        _messageEventLiveData?.postValue(Pair(MessageEvent.Edited, event.getMessage()))
                    }
                }
            }
        })
    }

    fun getCall(callId: String): Call {
        return webex.phone.getCall(callId)
    }

    fun getCallIdByNotificationId(notificationId: String, callType: NotificationCallType): String {
        return webex.getCallIdByNotificationId(notificationId, callType)
    }

    fun stopShare(callId: String) {
        webex.phone.getCall(callId).stopSharing(CompletionHandler { result ->
            _stopShareLiveData?.postValue(result.isSuccessful)
        })
    }

    fun getSpace(spaceId: String, handler: CompletionHandler<Space>){
        webex.spaces.get(spaceId, handler)
    }

    fun getPerson(personId: String, handler: CompletionHandler<Person>){
        webex.people.get(personId, handler)
    }

    fun listMessages(spaceId: String, handler: CompletionHandler<List<Message>>){
        webex.messages.list(spaceId, null, 10000, null, handler)
    }

    // Callbacks
    override fun showUCSSOLoginView(ssoUrl: String) {
        _cucmLiveData?.postValue(Pair(CucmEvent.ShowSSOLogin, ssoUrl))
        Log.d(tag, "showUCSSOLoginView")
    }

    override fun showUCNonSSOLoginView() {
        _cucmLiveData?.postValue(Pair(CucmEvent.ShowNonSSOLogin, ""))
        Log.d(tag, "showUCNonSSOLoginView")
    }

    override fun onUCLoginFailed() {
        _cucmLiveData?.postValue(Pair(CucmEvent.OnUCLoginFailed, ""))
        Log.d(tag, "onUCLoginFailed")
        isCUCMServerLoggedIn = false
    }

    override fun onUCLoggedIn() {
        _cucmLiveData?.postValue(Pair(CucmEvent.OnUCLoggedIn, ""))
        Log.d(tag, "onUCLoggedIn")
        isCUCMServerLoggedIn = true
    }

    override fun onUCServerConnectionStateChanged(status: UCLoginServerConnectionStatus, failureReason: PhoneServiceRegistrationFailureReason) {
        _cucmLiveData?.postValue(Pair(CucmEvent.OnUCServerConnectionStateChanged, ""))
        Log.d(tag, "onUCServerConnectionStateChanged status: $status failureReason: $failureReason")
        ucServerConnectionStatus = status
        ucServerConnectionFailureReason = failureReason
    }
}