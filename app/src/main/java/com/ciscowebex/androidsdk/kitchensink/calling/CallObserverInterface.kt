package com.ciscowebex.androidsdk.kitchensink.calling

import com.ciscowebex.androidsdk.phone.Breakout
import com.ciscowebex.androidsdk.phone.BreakoutSession
import com.ciscowebex.androidsdk.phone.Call
import com.ciscowebex.androidsdk.phone.CallObserver
import com.ciscowebex.androidsdk.phone.BreakoutSession.BreakoutSessionError
import com.ciscowebex.androidsdk.phone.ReceivingNoiseInfo
import com.ciscowebex.androidsdk.phone.closedCaptions.CaptionItem
import com.ciscowebex.androidsdk.phone.closedCaptions.ClosedCaptionsInfo

/*
* This interface is written to overcome the limitation of live data postValue.
* When SDK pushes media events continuously then some events were getting lost.
* When post value gets trigger continuously then the latest value replaces the previous one and then the previous value doesn't reach to the UI observer.
* To overcome that limitation, the interface registration happens from UI and the all events now directly reaches to UI without any postValue.
* */
interface CallObserverInterface {
    fun onConnected(call: Call?) {}
    fun onRinging(call: Call?) {}
    fun onStartRinging(call:Call? , ringerType: Call.RingerType)
    fun onStopRinging(call: Call?, ringerType: Call.RingerType)
    fun onWaiting(call: Call?) {}
    fun onDisconnected(call: Call?, event: CallObserver.CallDisconnectedEvent?) {}
    fun onInfoChanged(call: Call?) {}
    fun onMediaChanged(call: Call?, event: CallObserver.MediaChangedEvent?) {}
    fun onCallMembershipChanged(call: Call?, event: CallObserver.CallMembershipChangedEvent?) {}
    fun onScheduleChanged(call: Call?) {}
    fun onCpuHitThreshold() {}
    fun onPhotoCaptured(imageData: ByteArray?) {}
    fun onMediaQualityInfoChanged(mediaQualityInfo: Call.MediaQualityInfo)

    // Breakout Sessions
    fun onBroadcastMessageReceivedFromHost(message: String)
    fun onHostAskingReturnToMainSession()
    fun onJoinableSessionUpdated(breakoutSessions: List<BreakoutSession>)
    fun onJoinedSessionUpdated(breakoutSession: BreakoutSession)
    fun onReturnedToMainSession()
    fun onSessionClosing()
    fun onSessionEnabled()
    fun onSessionJoined(breakoutSession: BreakoutSession)
    fun onSessionStarted(breakout: Breakout)
    fun onBreakoutUpdated(breakout: Breakout)
    fun onBreakoutError(error: BreakoutSessionError)
    fun onReceivingNoiseInfoChanged(info: ReceivingNoiseInfo)

    // Closedcaption
    fun onClosedCaptionsArrived(closedCaptions: CaptionItem)
    fun onClosedCaptionsInfoChanged(closedCaptionsInfo: ClosedCaptionsInfo)
    fun onMoveMeetingFailed(call: Call?)
}