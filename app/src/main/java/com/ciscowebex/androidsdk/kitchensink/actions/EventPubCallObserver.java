/*
 * Copyright 2016-2017 Cisco Systems Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package com.ciscowebex.androidsdk.kitchensink.actions;

import com.ciscowebex.androidsdk.kitchensink.actions.events.OnCallMembershipEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnConnectEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnDisconnectEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnMediaChangeEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnRingingEvent;
import com.ciscowebex.androidsdk.phone.Call;
import com.ciscowebex.androidsdk.phone.CallObserver;

import org.greenrobot.eventbus.EventBus;


/**
 * Created on 18/09/2017.
 */

public class EventPubCallObserver implements CallObserver {
    @Override
    public void onRinging(Call call) {
        postEvent(new OnRingingEvent(call));
    }

    @Override
    public void onConnected(Call call) {
        postEvent(new OnConnectEvent(call));
    }

    @Override
    public void onDisconnected(CallDisconnectedEvent callDisconnectedEvent) {
        postEvent(new OnDisconnectEvent(callDisconnectedEvent));
    }

    @Override
    public void onMediaChanged(MediaChangedEvent mediaChangedEvent) {
        postEvent(new OnMediaChangeEvent(mediaChangedEvent));
    }

    @Override
    public void onCallMembershipChanged(CallMembershipChangedEvent event) {
        postEvent(new OnCallMembershipEvent(event));
    }

    private void postEvent(Object event) {
        EventBus.getDefault().post(event);
    }
}
