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

package com.cisco.sparksdk.kitchensink.actions;

import android.view.View;

import com.cisco.sparksdk.kitchensink.actions.events.AnswerEvent;
import com.cisco.sparksdk.kitchensink.actions.events.DialEvent;
import com.cisco.sparksdk.kitchensink.actions.events.HangupEvent;
import com.cisco.sparksdk.kitchensink.actions.events.LoginEvent;
import com.cisco.sparksdk.kitchensink.actions.events.OnIncomingCallEvent;
import com.cisco.sparksdk.kitchensink.actions.events.RejectEvent;
import com.ciscospark.androidsdk.Spark;
import com.ciscospark.androidsdk.auth.Authenticator;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.CallObserver;
import com.ciscospark.androidsdk.phone.CallOption;
import com.ciscospark.androidsdk.phone.Phone;

import static com.cisco.sparksdk.kitchensink.KitchenSinkApp.getApplication;
import static com.cisco.sparksdk.kitchensink.actions.events.SparkAgentEvent.postEvent;


/**
 * Created on 13/09/2017.
 */

public class SparkAgent {

    public enum CallCap {
        AUDIO_ONLY,
        AUDIO_VIDEO
    }

    private Spark spark;
    private Phone phone;
    private Call activeCall;
    private Call incomingCall;

    private static SparkAgent instance;
    private final CallObserver callObserver = new EventPubCallObserver();
    private CallCap callCap = CallCap.AUDIO_VIDEO;

    public static SparkAgent getInstance() {
        if (instance == null) {
            instance = new SparkAgent();
        }
        return instance;
    }

    public Spark getSpark() {
        return spark;
    }

    public void setSpark(Spark spark) {
        this.spark = spark;
    }

    public Phone getPhone() {
        return phone;
    }

    public void register(Authenticator authenticator) {
        spark = new Spark(getApplication(), authenticator);
        phone = spark.phone();
        phone.register(r -> {
            if (r.isSuccessful()) {
                setupIncomingCallListener();
            }
            new LoginEvent(r).post();
        });
    }

    private void setupIncomingCallListener() {
        phone.setIncomingCallListener(call -> {
            incomingCall = call;
            postEvent(new OnIncomingCallEvent(call));
        });
    }

    public void setCallCapability(CallCap cap) {
        callCap = cap;
    }

    public void dial(String callee, View localView, View remoteView) {
        phone.dial(callee, genCallOption(localView, remoteView), (result) -> {
            if (result.isSuccessful()) {
                activeCall = result.getData();
                activeCall.setObserver(callObserver);
            }
            new DialEvent(result).post();
        });
    }

    private CallOption genCallOption(View localView, View remoteView) {
        if (callCap.equals(CallCap.AUDIO_ONLY))
            return CallOption.audioOnly();
        else
            return CallOption.audioVideo(localView, remoteView);
    }

    public void reject() {
        incomingCall.reject(r -> new RejectEvent(r).post());
    }

    public void hangup() {
        activeCall.hangup(r -> new HangupEvent(r).post());
    }

    public void answer(View localView, View remoteView) {
        activeCall = incomingCall;
        activeCall.setObserver(callObserver);
        activeCall.answer(CallOption.audioVideo(localView, remoteView), r -> new AnswerEvent(r).post());
    }

    public void startPreview(View view) {
        phone.startPreview(view);
    }

    public void stopPreview() {
        phone.stopPreview();
    }

    public void setFrontCamera(boolean isDefault) {
        if (isDefault)
            phone.setDefaultFacingMode(Phone.FacingMode.USER);
        else
            activeCall.setFacingMode(Phone.FacingMode.USER);
    }

    public void setBackCamera(boolean isDefault) {
        if (isDefault)
            phone.setDefaultFacingMode(Phone.FacingMode.ENVIROMENT);
        else
            activeCall.setFacingMode(Phone.FacingMode.ENVIROMENT);
    }

    public void sendVideo(boolean b) {
        activeCall.setSendingVideo(b);
    }

    public void sendAudio(boolean b) {
        activeCall.setSendingAudio(b);
    }

    public void receiveVideo(boolean b) {
        activeCall.setReceivingVideo(b);
    }

    public void receiveAudio(boolean b) {
        activeCall.setReceivingAudio(b);
    }
}
