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
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.CallObserver;
import com.ciscospark.androidsdk.phone.MediaOption;
import com.ciscospark.androidsdk.phone.Phone;

import static com.cisco.sparksdk.kitchensink.actions.events.SparkAgentEvent.postEvent;


/**
 * Created on 13/09/2017.
 */

public class SparkAgent {

    public enum CallCap {AUDIO_ONLY, AUDIO_VIDEO}

    public enum CameraCap {FRONT, BACK, CLOSE}

    private Spark spark;
    private Phone phone;
    private Call activeCall;
    private Call incomingCall;
    private boolean isSpeakerOn = true;
    private boolean isDialing = false;

    private static SparkAgent instance;
    private final CallObserver callObserver = new EventPubCallObserver();
    private CallCap callCap = CallCap.AUDIO_VIDEO;
    private CameraCap cameraCap = CameraCap.FRONT;

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

    public boolean getSpeakerPhoneOn() {
        return isSpeakerOn;
    }

    public void setSpeakerPhoneOn(boolean on) {
        isSpeakerOn = on;
    }

    public void register() {
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
            incomingCall.setObserver(callObserver);
            postEvent(new OnIncomingCallEvent(call));
        });
    }

    public void setCallCapability(CallCap cap) {
        callCap = cap;
    }

    public CallCap getCallCapability() {
        return callCap;
    }

    public void dial(String callee, View localView, View remoteView) {
        isDialing = true;
        phone.dial(callee, genCallOption(localView, remoteView), (result) -> {
            if (result.isSuccessful()) {
                activeCall = result.getData();
                if (isDialing == false) {
                    hangup();
                } else {
                    activeCall.setObserver(callObserver);
                }
            }
            isDialing = false;
            new DialEvent(result).post();
        });
    }

    private MediaOption genCallOption(View localView, View remoteView) {
        if (callCap.equals(CallCap.AUDIO_ONLY))
            return MediaOption.audioOnly();
        else
            return MediaOption.audioVideo(localView, remoteView);
    }

    public void reject() {
        incomingCall.reject(r -> new RejectEvent(r).post());
        incomingCall = null;
    }

    public void hangup() {
        isDialing = false;
        if (activeCall != null) {
            activeCall.hangup(r -> new HangupEvent(r).post());
            activeCall = null;
        }
    }

    public void answer(View localView, View remoteView) {
        activeCall = incomingCall;
        incomingCall = null;
        activeCall.setObserver(callObserver);
        activeCall.answer(MediaOption.audioVideo(localView, remoteView), r -> new AnswerEvent(r).post());
    }

    public void startPreview(View view) {
        cameraCap = getDefaultCamera();
        phone.startPreview(view);
    }

    public void stopPreview() {
        phone.stopPreview();
    }

    public void setFrontCamera() {
        if (activeCall != null) activeCall.setFacingMode(Phone.FacingMode.USER);
    }

    public void setBackCamera() {
        if (activeCall != null) activeCall.setFacingMode(Phone.FacingMode.ENVIROMENT);
    }

    public void setDefaultCamera(CameraCap cap) {
        cameraCap = cap;
        switch (cap) {
            case FRONT:
                phone.setDefaultFacingMode(Phone.FacingMode.USER);
                break;
            case BACK:
                phone.setDefaultFacingMode(Phone.FacingMode.ENVIROMENT);
                break;
            case CLOSE:
                break;
        }
    }

    public CameraCap getDefaultCamera() {
        if (cameraCap.equals(CameraCap.CLOSE))
            return CameraCap.CLOSE;
        if (phone.getDefaultFacingMode().equals(Phone.FacingMode.USER))
            return CameraCap.FRONT;
        else
            return CameraCap.BACK;
    }

    public void sendVideo(boolean b) {
        if (activeCall != null)
            activeCall.setSendingVideo(b);
    }

    public void sendAudio(boolean b) {
        if (activeCall != null)
            activeCall.setSendingAudio(b);
    }

    public void receiveVideo(boolean b) {
        if (activeCall != null)
            activeCall.setReceivingVideo(b);
    }

    public void receiveAudio(boolean b) {
        if (activeCall != null)
            activeCall.setReceivingAudio(b);
    }

    public boolean isSendingVideo() {
        if (activeCall != null)
            return activeCall.isSendingVideo();
        return false;
    }

    public boolean isSendingAudio() {
        if (activeCall != null)
            return activeCall.isSendingAudio();
        return false;
    }

    public boolean isReceivingVideo() {
        if (activeCall != null)
            return activeCall.isReceivingVideo();
        return false;
    }

    public boolean isReceivingAudio() {
        if (activeCall != null)
            return activeCall.isReceivingAudio();
        return false;
    }
}
