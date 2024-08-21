package com.ciscowebex.androidsdk.kitchensink

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.WebexUCLoginDelegate
import com.ciscowebex.androidsdk.message.Message
import com.ciscowebex.androidsdk.people.Person
import com.ciscowebex.androidsdk.space.Space
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.WebexAuthDelegate
import com.ciscowebex.androidsdk.auth.PhoneServiceRegistrationFailureReason
import com.ciscowebex.androidsdk.auth.UCLoginFailureReason
import com.ciscowebex.androidsdk.auth.UCLoginServerConnectionStatus
import com.ciscowebex.androidsdk.auth.UCSSOFailureReason
import com.ciscowebex.androidsdk.kitchensink.utils.CallObjectStorage
import com.ciscowebex.androidsdk.calendarMeeting.CalendarMeetingObserver
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.listeners.SpaceEventListener
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.membership.Membership
import com.ciscowebex.androidsdk.membership.MembershipObserver
import com.ciscowebex.androidsdk.message.LocalFile
import com.ciscowebex.androidsdk.message.MessageObserver
import com.ciscowebex.androidsdk.phone.Breakout
import com.ciscowebex.androidsdk.phone.BreakoutSession
import com.ciscowebex.androidsdk.phone.Call
import com.ciscowebex.androidsdk.phone.CallMembership
import com.ciscowebex.androidsdk.phone.MediaOption
import com.ciscowebex.androidsdk.phone.Phone
import com.ciscowebex.androidsdk.phone.VirtualBackground
import com.ciscowebex.androidsdk.phone.CallObserver
import com.ciscowebex.androidsdk.phone.NotificationCallType
import com.ciscowebex.androidsdk.phone.ReceivingNoiseInfo
import com.ciscowebex.androidsdk.phone.closedCaptions.CaptionItem
import com.ciscowebex.androidsdk.phone.closedCaptions.ClosedCaptionsInfo
import com.ciscowebex.androidsdk.space.SpaceObserver
import java.io.PrintWriter

class WebexRepository(val webex: Webex) : WebexUCLoginDelegate, WebexAuthDelegate {
    private val tag = "WebexRepository"

    enum class CallCap {
        Audio_Only,
        Audio_Video
    }

    enum class UCCallEvent {
        ShowSSOLogin,
        ShowNonSSOLogin,
        OnUCLoginFailed,
        OnUCLoggedIn,
        OnUCServerConnectionStateChanged,
        ShowUCSSOBrowser,
        HideUCSSOBrowser,
        OnSSOLoginFailed
    }

    enum class LogLevel {
        ALL,
        VERBOSE,
        INFO,
        WARNING,
        DEBUG,
        ERROR,
        NO
    }

    enum class BandWidthOptions {
        BANDWIDTH_90P,
        BANDWIDTH_180P,
        BANDWIDTH_360P,
        BANDWIDTH_720P,
        BANDWIDTH_1080P
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
        MessageThumbnailUpdated,
        Updated
    }

    enum class CallEvent {
        DialCompleted,
        DialFailed,
        AnswerCompleted,
        AnswerFailed,
        AssociationCallCompleted,
        AssociationCallFailed,
        MeetingPinOrPasswordRequired,
        CaptchaRequired,
        InCorrectPassword,
        InCorrectPasswordWithCaptcha,
        InCorrectPasswordOrHostKey,
        InCorrectPasswordOrHostKeyWithCaptcha,
        WrongApiCalled,
        CannotStartInstantMeeting
    }

    enum class CalendarMeetingEvent {
        Created,
        Updated,
        Deleted
    }

    data class CallLiveData(val event: CallEvent,
                            val call: Call? = null,
                            val captcha: Phone.Captcha? = null,
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
    var enableBgStreamtoggle = true
    var enableBgConnectiontoggle = true
    var enablePhoneStatePermission = true
    var enableHWAcceltoggle = false
    var multiStreamNewApproach = true
    var logFilter = LogLevel.ALL.name
    var maxVideoBandwidth = BandWidthOptions.BANDWIDTH_720P.name
    var isConsoleLoggerEnabled = true
    var callCapability: CallCap = CallCap.Audio_Video
    var scalingMode: Call.VideoRenderMode = Call.VideoRenderMode.Fit
    var compositedVideoLayout: MediaOption.CompositedVideoLayout = MediaOption.CompositedVideoLayout.FILMSTRIP
    var streamMode: Phone.VideoStreamMode = Phone.VideoStreamMode.AUXILIARY
    var isSpaceCallStarted = false
    var spaceCallId:String? = null

    val participantMuteMap = hashMapOf<String, Boolean>()
    var isUCServerLoggedIn = false
    var ucServerConnectionStatus: UCLoginServerConnectionStatus = UCLoginServerConnectionStatus.Idle
    var ucServerConnectionFailureReason: PhoneServiceRegistrationFailureReason = PhoneServiceRegistrationFailureReason.Unknown

    var _callMembershipsLiveData: MutableLiveData<List<CallMembership>>? = null
    var _muteAllLiveData: MutableLiveData<Boolean>? = null
    var _ucLiveData: MutableLiveData<Pair<UCCallEvent, String>>? = null
    var _authLiveDataList: MutableList<MutableLiveData<String>?> = mutableListOf()
    var _callingLiveData: MutableLiveData<CallLiveData>? = null
    var _startAssociationLiveData: MutableLiveData<CallLiveData>? = null
    var _startShareLiveData: MutableLiveData<Boolean>? = null
    var _stopShareLiveData: MutableLiveData<Boolean>? = null
    var _startAudioDumpLiveData: MutableLiveData<Boolean>? = null
    var _stopAudioDumpLiveData: MutableLiveData<Boolean>? = null
    var _canStartAudioDumpLiveData: MutableLiveData<Boolean>? = null
    var _spaceEventLiveData: MutableLiveData<Pair<SpaceEvent, Any?>>? = null
    var spaceEventListener : SpaceEventListener? = null
    var _membershipEventLiveData: MutableLiveData<Pair<MembershipEvent, Membership?>>? = null
    var _messageEventLiveData: MutableLiveData<Pair<MessageEvent, Any?>>? = null
    var _calendarMeetingEventLiveData: MutableLiveData<Pair<CalendarMeetingEvent, Any>>? = null
    var _isIncomingCallListenerSet = false
    var _incomingCallListeners : HashMap<String, Phone.IncomingCallListener> = HashMap()
    var _callObservers : HashMap<String, MutableList<CallObserver>> = HashMap()

    init {
        webex.delegate = this
        webex.authDelegate = this
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
        _startAssociationLiveData = null
        _startShareLiveData = null
        _stopShareLiveData = null
        _startAudioDumpLiveData = null
        _stopAudioDumpLiveData = null
        _canStartAudioDumpLiveData = null
    }

    fun clearSpaceData(){
        spaceEventListener = null
    }

    fun setSpaceObserver() {
        webex.spaces.setSpaceObserver(object : SpaceObserver {
            override fun onEvent(event: SpaceObserver.SpaceEvent) {
                Log.d(tag, "onEvent: $event with actorID : ${event.getActorId().orEmpty()}")
                when (event) {
                    is SpaceObserver.SpaceCallStarted -> {
                        spaceEventListener?.onCallStarted(event.getSpaceId() ?: "")
                        isSpaceCallStarted = true
                        spaceCallId = event.getSpaceId()
                    }
                    is SpaceObserver.SpaceCallEnded -> {
                        spaceEventListener?.onCallEnded(event.getSpaceId() ?: "")
                        isSpaceCallStarted = false
                        spaceCallId = null
                    }
                    is SpaceObserver.SpaceCreated -> {
                        event.getSpace()?.let { spaceEventListener?.onCreate(it) }
                    }
                    is SpaceObserver.SpaceUpdated -> {
                        event.getSpace()?.let { spaceEventListener?.onUpdate(it) }
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
                    is MessageObserver.MessagesUpdated -> {
                        _messageEventLiveData?.postValue(Pair(MessageEvent.Updated, event.getMessages()))
                    }
                }
            }
        })
    }

    fun setCalendarMeetingObserver() {
        webex.calendarMeetings.setObserver(object : CalendarMeetingObserver
        {
            override fun onEvent(event: CalendarMeetingObserver.CalendarMeetingEvent) {
                Log.d(tag, "onCalendarMeetingEvent: $event")
                when (event) {
                    is CalendarMeetingObserver.CalendarMeetingAdded -> {
                        _calendarMeetingEventLiveData?.postValue(Pair(CalendarMeetingEvent.Created, event.getCalendarMeeting()))
                    }
                    is CalendarMeetingObserver.CalendarMeetingUpdated -> {
                        _calendarMeetingEventLiveData?.postValue(Pair(CalendarMeetingEvent.Updated, event.getCalendarMeeting()))
                    }
                    is CalendarMeetingObserver.CalendarMeetingRemoved -> {
                        _calendarMeetingEventLiveData?.postValue(Pair(CalendarMeetingEvent.Deleted, event.getCalendarMeetingId()))
                    }
                }
            }
        })
    }

    fun removeCalendarMeetingObserver() {
        _calendarMeetingEventLiveData = null
        webex.calendarMeetings.setObserver(null)
    }

    fun getCall(callId: String): Call? {
        return CallObjectStorage.getCallObject(callId)
    }

    fun getCallIdByNotificationId(notificationId: String, callType: NotificationCallType): String {
        return webex.getCallIdByNotificationId(notificationId, callType)
    }

    fun stopShare(callId: String) {
        getCall(callId)?.stopSharing(CompletionHandler { result ->
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

    fun getVirtualBackgrounds(handler: CompletionHandler<List<VirtualBackground>> ) {
        webex.phone.fetchVirtualBackgrounds(handler)
    }

    fun addVirtualBackground(imgFile: LocalFile, handler: CompletionHandler<VirtualBackground>) {
        webex.phone.addVirtualBackground(imgFile, handler)
    }

    fun applyVirtualBackground(background: VirtualBackground, mode: Phone.VirtualBackgroundMode, handler: CompletionHandler<Boolean>) {
        webex.phone.applyVirtualBackground(background, mode, handler)
    }

    fun removeVirtualBackground(background: VirtualBackground, handler: CompletionHandler<Boolean>) {
        webex.phone.removeVirtualBackground(background, handler)
    }

    fun setMaxVirtualBackgrounds(limit: Int) {
        webex.phone.setMaxVirtualBackgroundItems(limit)
    }

    fun getMaxVirtualBackgrounds(): Int {
        return webex.phone.getMaxVirtualBackgroundItems()
    }

    fun setOnInitialSpacesSyncCompletedListener(handler: CompletionHandler<Void>) {
        webex.spaces.setOnInitialSpacesSyncCompletedListener(handler)
    }

    // Callbacks
    override fun loadUCSSOViewInBackground(ssoUrl: String) {
        _ucLiveData?.postValue(Pair(UCCallEvent.ShowSSOLogin, ssoUrl))
        Log.d(tag, "showUCSSOLoginView")
    }

    override fun showUCNonSSOLoginView() {
        _ucLiveData?.postValue(Pair(UCCallEvent.ShowNonSSOLogin, ""))
        Log.d(tag, "showUCNonSSOLoginView")
    }

    override fun onUCLoginFailed(failureReason: UCLoginFailureReason) {
        isUCServerLoggedIn = false
        _ucLiveData?.postValue(Pair(UCCallEvent.OnUCLoginFailed, failureReason.name))
        Log.d(tag, "onUCLoginFailed with reason: $failureReason")
    }

    override fun onUCLoggedIn() {
        isUCServerLoggedIn = true
        _ucLiveData?.postValue(Pair(UCCallEvent.OnUCLoggedIn, ""))
        Log.d(tag, "onUCLoggedIn")
    }

    override fun onUCServerConnectionStateChanged(status: UCLoginServerConnectionStatus, failureReason: PhoneServiceRegistrationFailureReason) {
        Log.d(tag, "onUCServerConnectionStateChanged status: $status failureReason: $failureReason")
        ucServerConnectionStatus = status
        ucServerConnectionFailureReason = failureReason
        _ucLiveData?.postValue(Pair(UCCallEvent.OnUCServerConnectionStateChanged, ""))
    }

    override fun showUCSSOBrowser() {
        _ucLiveData?.postValue(Pair(UCCallEvent.ShowUCSSOBrowser, ""))
        Log.d(tag, "showUCSSOBrowser")
    }

    override fun hideUCSSOBrowser() {
        _ucLiveData?.postValue(Pair(UCCallEvent.HideUCSSOBrowser, ""))
        Log.d(tag, "hideUCSSOBrowser")
    }

    override fun onUCSSOLoginFailed(failureReason: UCSSOFailureReason) {
        _ucLiveData?.postValue(Pair(UCCallEvent.OnSSOLoginFailed, failureReason.name))
        Log.d(tag, "onUCSSOLoginFailed : reason = ${failureReason.name}")
    }

    override fun onReLoginRequired() {
        Log.d("onReAuthRequired", "live data list size : ${_authLiveDataList.size}")
        for (liveData in _authLiveDataList) {
            liveData?.postValue(Constants.Callbacks.RE_LOGIN_REQUIRED)
        }
        Log.d(tag, Constants.Callbacks.RE_LOGIN_REQUIRED)
    }

    private fun registerIncomingCallListener() {
        webex.phone.setIncomingCallListener(object : Phone.IncomingCallListener {
            override fun onIncomingCall(call: Call?, hasActiveConflictCalls : Boolean) {
                call?.let {
                    CallObjectStorage.addCallObject(call)
                    registerCallObserver(call)
                }
                for(listener in _incomingCallListeners.values){
                    listener.onIncomingCall(call)
                    listener.onIncomingCall(call, hasActiveConflictCalls)
                }
            }
        })
    }

    fun isIncomingCallListenerSet(type: String):Boolean {
        return _incomingCallListeners.containsKey(type)
    }

    fun setIncomingCallListener(type: String, incomingCallListener: Phone.IncomingCallListener) {
        if(!_isIncomingCallListenerSet){
            registerIncomingCallListener()
            _isIncomingCallListenerSet = true
        }
        _incomingCallListeners[type] = incomingCallListener
    }

    fun removeIncomingCallListener(type: String) {
        _incomingCallListeners.remove(type)
    }

    @Synchronized
    fun setCallObserver(call: Call, callObserver: CallObserver){
        val callId = call.getCallId() ?: return
        var observers = _callObservers[callId]
        var registerFirstTime = false
        if(observers == null){
            registerFirstTime = true
            observers = mutableListOf()
        }
        if(!observers.contains(callObserver)) {
            observers.add(callObserver)
        }
        _callObservers[callId] = observers
        if(registerFirstTime)
            registerCallObserver(call)
    }

    inner class WxCallObserver(private val _callId : String) : CallObserver {
        override fun onWaiting(call: Call?, reason: Call.WaitReason?) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onWaiting(call, reason)
                }
            }
        }

        override fun onScheduleChanged(call: Call?) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onScheduleChanged(call)
                }
            }
        }

        override fun onRinging(call: Call?) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onRinging(call)
                }
            }
        }

        override fun onStopRinging(call: Call?, ringerType: Call.RingerType) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onStopRinging(call, ringerType)
                }
            }
        }

        override fun onStartRinging(call: Call?, ringerType: Call.RingerType) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    Log.d(tag, "start ringer repository")
                    observer.onStartRinging(call, ringerType)
                }
            }
        }

        override fun onConnected(call: Call?) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onConnected(call)
                }
            }
        }

        override fun onDisconnected(event: CallObserver.CallDisconnectedEvent?) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onDisconnected(event)
                }
            }
            CallObjectStorage.removeCallObject(_callId)
        }

        override fun onInfoChanged(call: Call?) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onInfoChanged(call)
                }
            }
        }

        override fun onCallMembershipChanged(event: CallObserver.CallMembershipChangedEvent?) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onCallMembershipChanged(event)
                }
            }
        }

        override fun onCpuHitThreshold() {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onCpuHitThreshold()
                }
            }
        }

        override fun onPhotoCaptured(imageData: ByteArray?) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onPhotoCaptured(imageData)
                }
            }
        }

        override fun onMediaQualityInfoChanged(mediaQualityInfo: Call.MediaQualityInfo) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onMediaQualityInfoChanged(mediaQualityInfo)
                }
            }
        }

        override fun onSessionEnabled() {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onSessionEnabled()
                }
            }
        }

        override fun onSessionStarted(breakout: Breakout) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onSessionStarted(breakout)
                }
            }
        }

        override fun onBreakoutUpdated(breakout: Breakout) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onBreakoutUpdated(breakout)
                }
            }
        }

        override fun onSessionJoined(breakoutSession: BreakoutSession) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onSessionJoined(breakoutSession)
                }
            }
        }

        override fun onJoinedSessionUpdated(breakoutSession: BreakoutSession) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onJoinedSessionUpdated(breakoutSession)
                }
            }
        }

        override fun onJoinableSessionUpdated(breakoutSessions: List<BreakoutSession>) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onJoinableSessionUpdated(breakoutSessions)
                }
            }
        }

        override fun onHostAskingReturnToMainSession() {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onHostAskingReturnToMainSession()
                }
            }
        }

        override fun onBroadcastMessageReceivedFromHost(message: String) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onBroadcastMessageReceivedFromHost(message)
                }
            }
        }

        override fun onSessionClosing() {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onSessionClosing()
                }
            }
        }

        override fun onReturnedToMainSession() {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onReturnedToMainSession()
                }
            }
        }

        override fun onBreakoutError(error: BreakoutSession.BreakoutSessionError) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onBreakoutError(error)
                }
            }
        }

        override fun onMediaChanged(event: CallObserver.MediaChangedEvent?) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onMediaChanged(event)
                }
            }
        }

        override fun onReceivingNoiseInfoChanged(info: ReceivingNoiseInfo) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onReceivingNoiseInfoChanged(info)
                }
            }
        }

        override fun onClosedCaptionsArrived(closedCaptions: CaptionItem) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onClosedCaptionsArrived(closedCaptions)
                }
            }
        }

        override fun onClosedCaptionsInfoChanged(closedCaptionsInfo: ClosedCaptionsInfo) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onClosedCaptionsInfoChanged(closedCaptionsInfo)
                }
            }
        }

        override fun onMoveMeetingFailed(call: Call?) {
            val observers: MutableList<CallObserver>? = _callObservers[_callId]
            observers?.let { it ->
                it.forEach { observer ->
                    observer.onMoveMeetingFailed(call)
                }
            }
        }
    }

    private fun registerCallObserver(call: Call) {
        call.getCallId()?.let {
            call.setObserver(WxCallObserver(it))
        }
    }

    fun removeCallObserver(callId : String, observer: CallObserver){
        var observers = _callObservers[callId]
        observers?.let{
            observers.remove(observer)
            if(it.size == 0)
                _callObservers.remove(callId)
        }
    }

    fun clearCallObservers(callId: String) {
        _callObservers.remove(callId)
    }

    fun printObservers(writer : PrintWriter) {
        writer.println("******** Incoming calls in Repository **********")
        _incomingCallListeners.forEach { (key, value) -> writer.println("$key = $value") }
        writer.println("******** Call Observers in Repository **********")
        _callObservers.forEach { (key, value) -> writer.println("$key = $value") }
        writer.println("******** Calls in storage *****")
        writer.println(CallObjectStorage.size())
    }
}