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

import android.content.Intent;

import com.ciscowebex.androidsdk.kitchensink.R;
import com.ciscowebex.androidsdk.kitchensink.actions.commands.LogoutAction;
import com.ciscowebex.androidsdk.kitchensink.actions.events.LogoutEvent;
import com.ciscowebex.androidsdk.kitchensink.launcher.LauncherActivity;
import com.ciscowebex.androidsdk.kitchensink.login.LoginActivity;
import com.ciscowebex.androidsdk.kitchensink.ui.BaseFragment;

import org.greenrobot.eventbus.Subscribe;

import butterknife.OnClick;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

/**
 * A simple {@link BaseFragment} subclass.
 */
public class LauncherFragment extends BaseFragment {

    private BaseFragment setupFragment;
    private BaseFragment dialFragment;
    private BaseFragment waitingCallFragment;
    private BaseFragment feedbackFragment;

    public LauncherFragment() {
        // Required empty public constructor
        setLayout(R.layout.fragment_launcher);
        setupFragment = new SetupFragment();
        dialFragment = new DialPagersFragment();
        waitingCallFragment = new WaitingCallFragment();
        feedbackFragment = new FeedbackFragment();
    }

    @OnClick(R.id.buttonSetup)
    public void setup() {
        ((LauncherActivity) getActivity()).replace(setupFragment);
    }

    @OnClick({R.id.buttonDial, R.id.buttonMessaging})
    public void dial() {
        ((LauncherActivity) getActivity()).replace(dialFragment);
    }

    @OnClick(R.id.buttonWaitingCall)
    public void waitingCall() {
        ((LauncherActivity) getActivity()).replace(waitingCallFragment);
    }

    @OnClick(R.id.buttonFeedback)
    public void sendFeedback() {
        ((LauncherActivity) getActivity()).replace(feedbackFragment);
    }

    @OnClick(R.id.buttonLogout)
    public void logout() {
        showBusyIndicator("Logout", "Wait for logout ...");
        new LogoutAction().execute();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onEventMainThread(LogoutEvent event) {
        dismissBusyIndicator();
        toast("Logout success");
        Intent i = new Intent(getActivity(), LoginActivity.class);
        i.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        getActivity().finish();
    }
}
