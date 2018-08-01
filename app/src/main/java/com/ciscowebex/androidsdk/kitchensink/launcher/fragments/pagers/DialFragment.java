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

package com.ciscowebex.androidsdk.kitchensink.launcher.fragments.pagers;


import android.text.TextUtils;
import android.widget.EditText;

import com.ciscowebex.androidsdk.kitchensink.R;
import com.ciscowebex.androidsdk.kitchensink.launcher.LauncherActivity;
import com.ciscowebex.androidsdk.kitchensink.launcher.fragments.CallFragment;
import com.ciscowebex.androidsdk.kitchensink.launcher.fragments.MessageFragment;
import com.ciscowebex.androidsdk.kitchensink.ui.BaseFragment;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * A simple {@link BaseFragment} subclass.
 */
public class DialFragment extends BaseFragment {

    @BindView(R.id.editCallee)
    public EditText callee;

    public DialFragment() {
        // Required empty public constructor
        setLayout(R.layout.fragment_dial);
    }

    @OnClick(R.id.callButton)
    public void makeCall() {
        String id = callee.getText().toString();
        if (!TextUtils.isEmpty(id)) {
            CallFragment fragment = CallFragment.newInstance(id);
            ((LauncherActivity)getActivity()).replace(fragment);
        }
    }

    @OnClick(R.id.messageButton)
    public void sendMessage() {
        String id = callee.getText().toString();
        if (!TextUtils.isEmpty(id)) {
            MessageFragment fragment = MessageFragment.newInstance(id);
            ((LauncherActivity) getActivity()).replace(fragment);
        }
    }

    public void setDialString(String dialString) {
        callee.setText(dialString);
    }
}
