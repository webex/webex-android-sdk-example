package com.ciscowebex.androidsdk.kitchensink.calling

import com.ciscowebex.androidsdk.phone.Call
import com.ciscowebex.androidsdk.phone.CallObserver

/*
* This interface is written to overcome the limitation of live data postValue.
* When SDK pushes media events continuously then some events were getting lost.
* When post value gets trigger continuously then the latest value replaces the previous one and then the previous value doesn't reach to the UI observer.
* To overcome that limitation, the interface registration happens from UI and the all events now directly reaches to UI without any postValue.
* */
interface CallObserverInterface {
    fun onConnected(call: Call?) {}
    fun onRinging(call: Call?) {}
    fun onWaiting(call: Call?) {}
    fun onDisconnected(call: Call?, event: CallObserver.CallDisconnectedEvent?) {}
    fun onInfoChanged(call: Call?) {}
    fun onMediaChanged(call: Call?, event: CallObserver.MediaChangedEvent?) {}
    fun onCallMembershipChanged(call: Call?, event: CallObserver.CallMembershipChangedEvent?) {}
    fun onScheduleChanged(call: Call?) {}
    fun onCpuHitThreshold() {}
}