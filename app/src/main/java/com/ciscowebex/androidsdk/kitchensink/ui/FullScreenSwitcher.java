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

import android.app.ActionBar;
import android.app.Activity;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.view.View;
import android.view.WindowManager;

/**
 * Created on 27/09/2017.
 */

public class FullScreenSwitcher {
    private ConstraintSet originSet = new ConstraintSet();
    private ConstraintSet fullScreenSet = new ConstraintSet();
    private ConstraintLayout layout;
    private View view;
    private boolean isFullScreen;

    private Activity activity;

    public FullScreenSwitcher(Activity activity, ConstraintLayout layout, View view) {
        this.activity = activity;
        this.layout = layout;
        this.view = view;
        this.isFullScreen = false;

        originSet.clone(layout);
        fullScreenSet.clone(layout);
        fullScreenSet.setMargin(view.getId(), ConstraintSet.START, 0);
        fullScreenSet.setMargin(view.getId(), ConstraintSet.END, 0);
        fullScreenSet.setMargin(view.getId(), ConstraintSet.LEFT, 0);
        fullScreenSet.setMargin(view.getId(), ConstraintSet.RIGHT, 0);
        fullScreenSet.setMargin(view.getId(), ConstraintSet.TOP, 0);
        fullScreenSet.setMargin(view.getId(), ConstraintSet.BOTTOM, 0);
        fullScreenSet.setElevation(view.getId(), 0);
        fullScreenSet.connect(view.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
    }

    public void toggleFullScreen() {
        isFullScreen = !isFullScreen;
        if (isFullScreen) {
            fullScreen();
        } else {
            restore();
        }
    }

    public boolean isFullScreen(){
        return isFullScreen;
    }

    private void fullScreen() {
        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) actionBar.hide();
        updateOnRotation();
    }

    private void restore() {
        activity.getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) actionBar.show();

        originSet.applyTo(layout);
    }

    public void updateOnRotation() {
        if (isFullScreen) {
            int width = activity.getResources().getDisplayMetrics().widthPixels;
            int height = activity.getResources().getDisplayMetrics().heightPixels;

            view.bringToFront();
            fullScreenSet.constrainWidth(view.getId(), width);
            fullScreenSet.constrainHeight(view.getId(), height);
            fullScreenSet.applyTo(layout);
        }
    }
}
