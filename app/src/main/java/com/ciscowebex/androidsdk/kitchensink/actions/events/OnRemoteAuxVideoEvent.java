package com.ciscowebex.androidsdk.kitchensink.actions.events;

import com.ciscowebex.androidsdk.phone.CallObserver;

/**
 * Created by qimdeng on 8/10/18.
 */

public class OnRemoteAuxVideoEvent {
    public CallObserver.RemoteAuxVideoChangeEvent callEvent;

    public OnRemoteAuxVideoEvent(CallObserver.RemoteAuxVideoChangeEvent event) {
        this.callEvent = event;
    }
}
