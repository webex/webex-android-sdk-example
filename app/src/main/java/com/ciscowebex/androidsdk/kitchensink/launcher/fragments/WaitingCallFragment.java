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

package com.ciscowebex.androidsdk.kitchensink.launcher.fragments;


import android.support.design.widget.FloatingActionButton;
import android.view.View;

import com.ciscowebex.androidsdk.kitchensink.R;
import com.ciscowebex.androidsdk.kitchensink.actions.WebexAgent;
import com.ciscowebex.androidsdk.kitchensink.actions.commands.AddCallHistoryAction;
import com.ciscowebex.androidsdk.kitchensink.actions.events.AnswerEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnDisconnectEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnIncomingCallEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.RejectEvent;
import com.ciscowebex.androidsdk.kitchensink.launcher.LauncherActivity;
import com.ciscowebex.androidsdk.kitchensink.ui.BaseFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link WaitingCallFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WaitingCallFragment extends BaseFragment {

    @BindView(R.id.answer)
    public FloatingActionButton answerButton;

    @BindView(R.id.reject)
    public FloatingActionButton rejectButton;

    public WaitingCallFragment() {
        // Required empty public constructor
        setLayout(R.layout.fragment_waiting_call);
    }

    @OnClick(R.id.answer)
    public void answerCall() {
        if (WebexAgent.getInstance().isCallIncoming()) {
            showButton(false);
            ((LauncherActivity) getActivity()).replace(CallFragment.newAnswerCallInstance());
        }
    }

    @OnClick(R.id.reject)
    public void rejectCall() {
        showButton(false);
        WebexAgent.getInstance().reject();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (WebexAgent.getInstance().isCallIncoming()) {
            showButton(true);
        } else {
            showButton(false);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnIncomingCallEvent event) {
        showButton(true);
        new AddCallHistoryAction(event.call.getFrom().getEmail(), "in").execute();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AnswerEvent event) {
        // if (event.isSuccessful())
        showButton(false);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RejectEvent event) {
        // if (event.isSuccessful())
        showButton(false);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnDisconnectEvent event) {
        showButton(false);
    }

    private void showButton(boolean show) {
        int visible = show ? View.VISIBLE : View.INVISIBLE;
        answerButton.setVisibility(visible);
        rejectButton.setVisibility(visible);
    }
}
