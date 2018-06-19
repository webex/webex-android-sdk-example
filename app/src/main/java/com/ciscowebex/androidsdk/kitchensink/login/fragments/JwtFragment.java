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

package com.ciscowebex.androidsdk.kitchensink.login.fragments;


import android.app.Fragment;
import android.content.Intent;
import android.widget.EditText;

import com.ciscowebex.androidsdk.kitchensink.R;
import com.ciscowebex.androidsdk.kitchensink.actions.commands.AppIdLoginAction;
import com.ciscowebex.androidsdk.kitchensink.ui.BaseFragment;
import com.ciscowebex.androidsdk.kitchensink.launcher.LauncherActivity;
import com.ciscowebex.androidsdk.kitchensink.actions.events.LoginEvent;

import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JwtFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JwtFragment extends BaseFragment {

    @BindView(R.id.editTextJWT)
    public EditText editTextJwt;

    public JwtFragment() {
        // Required empty public constructor
        setLayout(R.layout.fragment_jwt_login);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment JwtFragment.
     */
    public static JwtFragment newInstance() {
        return new JwtFragment();
    }

    @OnClick(R.id.buttonLogin)
    public void login() {
        String jwt = editTextJwt.getText().toString();
        if (!jwt.isEmpty()) {
            showBusyIndicator("Login", "Waiting for login ...");
            //startLauncher();
            new AppIdLoginAction(jwt).execute();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onEventMainThread(LoginEvent event) {
        dismissBusyIndicator();
        if (event.isSuccessful()) {
            toast("AppID logged in.");
            startLauncher();
        } else {
            toast(event.getError().toString());
        }
    }

    private void startLauncher() {
        startActivity(new Intent(getActivity(), LauncherActivity.class));
    }
}
