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

import android.net.Uri;
import android.util.Pair;
import android.view.View;

import com.ciscowebex.androidsdk.CompletionHandler;
import com.ciscowebex.androidsdk.Result;
import com.ciscowebex.androidsdk.Webex;
import com.ciscowebex.androidsdk.kitchensink.actions.events.AnswerEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.DialEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.HangupEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.LoginEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnIncomingCallEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.RejectEvent;
import com.ciscowebex.androidsdk.membership.MembershipClient;
import com.ciscowebex.androidsdk.message.LocalFile;
import com.ciscowebex.androidsdk.message.Mention;
import com.ciscowebex.androidsdk.message.Message;
import com.ciscowebex.androidsdk.message.MessageClient;
import com.ciscowebex.androidsdk.message.RemoteFile;
import com.ciscowebex.androidsdk.phone.Call;
import com.ciscowebex.androidsdk.phone.CallObserver;
import com.ciscowebex.androidsdk.phone.MediaOption;
import com.ciscowebex.androidsdk.phone.Phone;
import com.github.benoitdion.ln.Ln;

import java.io.File;

import static com.ciscowebex.androidsdk.kitchensink.actions.events.WebexAgentEvent.postEvent;


/**
 * Created on 13/09/2017.
 */

public class WebexAgent {

    public enum CallCap {AUDIO_ONLY, AUDIO_VIDEO}

    public enum CameraCap {FRONT, BACK, CLOSE}

    private Webex webex;
    private Phone phone;
    private Call activeCall;
    private Call incomingCall;
    private boolean isSpeakerOn = true;
    private boolean isDialing = false;

    private static WebexAgent instance;
    private final CallObserver callObserver = new EventPubCallObserver();
    private CallCap callCap = CallCap.AUDIO_VIDEO;
    private CameraCap cameraCap = CameraCap.FRONT;

    public static WebexAgent getInstance() {
        if (instance == null) {
            instance = new WebexAgent();
        }
        return instance;
    }

    public Webex getWebex() {
        return webex;
    }

    public void setWebex(Webex webex) {
        this.webex = webex;
    }

    public Phone getPhone() {
        return phone;
    }

    public Call getActiveCall() {
        return activeCall;
    }

    public boolean getSpeakerPhoneOn() {
        return isSpeakerOn;
    }

    public void setSpeakerPhoneOn(boolean on) {
        isSpeakerOn = on;
    }

    public void setMaxBandWidth(int maxBandWidth) {
        if (phone != null) {
            phone.setVideoMaxBandwidth(maxBandWidth);
        }
    }

    public int getMaxBandWidth() {
        if (phone != null) {
            return phone.getVideoMaxBandwidth();
        }
        return -1;
    }

    public void register() {
        phone = webex.phone();
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

    public void getMembership(String roomId, CompletionHandler handler) {
        MembershipClient client = webex.memberships();
        client.list(roomId, null, null, 0, handler);
    }

    public boolean isCallIncoming() {
        return incomingCall != null && !incomingCall.getStatus().equals(Call.CallStatus.DISCONNECTED);
    }

    public void setCallCapability(CallCap cap) {
        callCap = cap;
    }

    public CallCap getCallCapability() {
        return callCap;
    }

    public MessageClient getMessageClient() {
        return webex.messages();
    }

    public void sendMessage(String idOrEmail, String message, Mention[] mentions, LocalFile[] files,
                            CompletionHandler<Message> handler) {
        getMessageClient().post(idOrEmail, message, mentions, files, handler);
    }


    public void downloadThumbnail(RemoteFile file, File saveTo, MessageClient.ProgressHandler handler, CompletionHandler<Uri> completionHandler) {
        getMessageClient().downloadThumbnail(file, saveTo == null ? null : saveTo.getPath(), handler, completionHandler);
    }

    public void downloadFile(RemoteFile file, File saveTo, MessageClient.ProgressHandler handler, CompletionHandler<Uri> completionHandler) {
        getMessageClient().downloadFile(file, saveTo == null ? null : saveTo.getPath(), handler, completionHandler);
    }

    public void dial(String callee, View localView, View remoteView, View screenSharing) {
        isDialing = true;
        phone.dial(callee, getMediaOption(localView, remoteView, screenSharing), (Result<Call> result) -> {
            if (result.isSuccessful()) {
                activeCall = result.getData();
                if (!isDialing || activeCall == null) {
                    hangup();
                } else {
                    if (activeCall != null) activeCall.setObserver(callObserver);
                }
            }
            isDialing = false;
            new DialEvent(result).post();
        });
    }

    private MediaOption getMediaOption(View localView, View remoteView, View screenSharing) {
        if (callCap.equals(CallCap.AUDIO_ONLY))
            return MediaOption.audioOnly();
        else
            return MediaOption.audioVideoSharing(new Pair<>(localView, remoteView), screenSharing);
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

    public void answer(View localView, View remoteView, View screenShare) {
        if (isCallIncoming()) {
            activeCall = incomingCall;
            incomingCall = null;
            activeCall.setObserver(callObserver);
            activeCall.answer(getMediaOption(localView, remoteView, screenShare), r -> new AnswerEvent(r).post());
        }
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

    public boolean isScreenSharing() {
        if (activeCall != null) {
            return activeCall.isRemoteSendingSharing();
        }
        return false;
    }
}
