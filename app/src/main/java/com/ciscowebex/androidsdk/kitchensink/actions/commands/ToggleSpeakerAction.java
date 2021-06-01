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

package com.ciscowebex.androidsdk.kitchensink.actions.commands;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import com.ciscowebex.androidsdk.kitchensink.actions.IAction;
import com.ciscowebex.androidsdk.kitchensink.actions.WebexAgent;
import com.ciscowebex.androidsdk.phone.Call;
import com.ciscowebex.androidsdk.phone.Phone;
import com.ciscowebex.androidsdk.phone.internal.CallImpl;


/**
 * Created on 19/09/2017.
 */

public class ToggleSpeakerAction implements IAction {
    private boolean on;
    private Context context;
    private CallImpl call;

    public ToggleSpeakerAction(Context context, CallImpl call, boolean on) {
        this.context = context;
        this.on = on;
        this.call = call;
    }

    @Override
    public void execute() {
        if (call != null) {
            android.media.AudioManager am = (android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (on) {
                call.switchAudioOutput(Call.AudioOutputMode.SPEAKER);
            } else {
                if (isBluetoothHeadsetConnected()) {
                    call.switchAudioOutput(Call.AudioOutputMode.BLUETOOTH_HEADSET);
                } else if (am.isWiredHeadsetOn()) {
                    call.switchAudioOutput(Call.AudioOutputMode.HEADSET);
                } else {
                    call.switchAudioOutput(Call.AudioOutputMode.PHONE);
                }
            }
        } else {
            WebexAgent.getInstance().setLoudSpeakerState(on ? Phone.LoudSpeakerState.ON : Phone.LoudSpeakerState.OFF);
        }

    }

    private boolean isBluetoothHeadsetConnected() {
        return BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(android.bluetooth.BluetoothProfile.HEADSET)
                != android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;
    }
}
