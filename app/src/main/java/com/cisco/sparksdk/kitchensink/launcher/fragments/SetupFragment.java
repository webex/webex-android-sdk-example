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

package com.cisco.sparksdk.kitchensink.launcher.fragments;


import android.view.View;
import android.widget.RadioButton;
import android.widget.Switch;

import com.cisco.sparksdk.kitchensink.R;
import com.cisco.sparksdk.kitchensink.actions.SparkAgent;
import com.cisco.sparksdk.kitchensink.actions.commands.toggleSpeakerAction;
import com.cisco.sparksdk.kitchensink.ui.BaseFragment;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

/**
 * A simple {@link BaseFragment} subclass.
 */
public class SetupFragment extends BaseFragment {

    private SparkAgent agent;

    @BindView(R.id.preview)
    View preview;

    @BindView(R.id.audioVideoCall)
    RadioButton switchAudioVideo;

    @BindView(R.id.audioCallOnly)
    RadioButton switchAudioOnly;

    @BindView(R.id.setupLoudSpeaker)
    Switch switchLoudSpeaker;

    @BindView(R.id.backCamera)
    RadioButton switchBackCamera;

    @BindView(R.id.frontCamera)
    RadioButton switchFrontCamera;

    public SetupFragment() {
        // Required empty public constructor
        setLayout(R.layout.fragment_setup);
    }

    @Override
    public void onStart() {
        super.onStart();
        agent = SparkAgent.getInstance();
        setupWidgetStates();
    }

    private void setupWidgetStates() {
        if (agent.getCallCapability().equals(SparkAgent.CallCap.AUDIO_ONLY))
            switchAudioOnly.setChecked(true);
        else
            switchAudioVideo.setChecked(true);
        switchLoudSpeaker.setChecked(agent.getSpeakerPhoneOn());
        if (agent.getDefaultCamera().equals(SparkAgent.CameraCap.FRONT)) {
            switchFrontCamera.setChecked(true);
            setFrontCamera();
        } else {
            switchBackCamera.setChecked(true);
            setBackCamera();
        }
    }

    @OnClick({R.id.audioCallOnly, R.id.audioVideoCall})
    public void onSelectCallCapability(View v) {
        switch (v.getId()) {
            case R.id.audioCallOnly:
                agent.setCallCapability(SparkAgent.CallCap.AUDIO_ONLY);
                break;
            case R.id.audioVideoCall:
                agent.setCallCapability(SparkAgent.CallCap.AUDIO_VIDEO);
                break;
        }
    }

    @OnClick({R.id.closePreview, R.id.frontCamera, R.id.backCamera})
    public void onSelectCamera(View v) {
        switch (v.getId()) {
            case R.id.frontCamera:
                setFrontCamera();
                break;
            case R.id.backCamera:
                setBackCamera();
                break;
            case R.id.closePreview:
                stopPreview();
                break;
        }
    }

    @OnCheckedChanged(R.id.setupLoudSpeaker)
    public void onSetupLoadSpeakerChanged(Switch s) {
        new toggleSpeakerAction(getActivity(), s.isChecked() ? true : false).execute();
    }

    private void stopPreview() {
        agent.stopPreview();
        preview.setVisibility(View.GONE);
    }

    private void setBackCamera() {
        preview.setVisibility(View.VISIBLE);
        agent.setBackCamera(true);
        agent.startPreview(preview);
    }

    private void setFrontCamera() {
        preview.setVisibility(View.VISIBLE);
        agent.setFrontCamera(true);
        agent.startPreview(preview);
    }
}
