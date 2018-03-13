package com.cisco.sparksdk.kitchensink.actions.events;

import com.ciscospark.androidsdk.phone.CallObserver;

/**
 * Created by qimdeng on 3/13/18.
 */

public class OnCallMembershipEvent {
    public CallObserver.CallMembershipChangedEvent callEvent;

    public OnCallMembershipEvent(CallObserver.CallMembershipChangedEvent event) {
        this.callEvent = event;
    }
}
