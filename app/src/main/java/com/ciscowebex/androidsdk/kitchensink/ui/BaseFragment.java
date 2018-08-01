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

package com.ciscowebex.androidsdk.kitchensink.ui;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BaseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BaseFragment extends Fragment {

    protected static final String LAYOUT = "layout";
    protected int layout;
    private ProgressDialog dialog;

    public BaseFragment() {
        // Required empty public constructor
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param cls: Fragment class
     * @return A new instance of fragment depends on cls.
     */
    public static BaseFragment newInstance(Class cls, int layout) {
        BaseFragment fragment = null;
        try {
            fragment = (BaseFragment) cls.newInstance();
            fragment.setLayout(layout);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return fragment;
    }

    /**
     * Use this method to set Fragment's layout
     * this method is not dynamic, should call after fragment created.
     *
     * @param layout
     */
    public void setLayout(int layout) {
        Bundle args = new Bundle();
        args.putInt(LAYOUT, layout);
        setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            layout = getArguments().getInt(LAYOUT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(layout, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void onBackPressed() {

    }

    /* for activity */
    public void replace(Activity activity, int resourceId) {
        FragmentTransaction transaction;
        transaction = activity.getFragmentManager().beginTransaction();
        transaction.replace(resourceId, this);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // TODO: extract progressDialog out
    public void showBusyIndicator(String title, String message) {
        dialog = ProgressDialog.show(getActivity(), title, message);
    }

    public void dismissBusyIndicator() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public void toast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    /* Only use for debug */
    @SuppressWarnings("unused")
    @Subscribe
    public void onEvent(Object event) {
    }
}
