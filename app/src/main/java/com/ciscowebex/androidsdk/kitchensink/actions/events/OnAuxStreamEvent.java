package com.ciscowebex.androidsdk.kitchensink.actions.events;

import com.ciscowebex.androidsdk.phone.MultiStreamObserver;

/**
 * Created by qimdeng on 8/10/18.
 */

public class OnAuxStreamEvent {
    public MultiStreamObserver.AuxStreamChangedEvent callEvent;

    public OnAuxStreamEvent(MultiStreamObserver.AuxStreamChangedEvent event) {
        this.callEvent = event;
    }
}
