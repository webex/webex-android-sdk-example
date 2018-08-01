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

package com.ciscowebex.androidsdk.kitchensink.launcher;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import com.ciscowebex.androidsdk.kitchensink.R;
import com.ciscowebex.androidsdk.kitchensink.actions.commands.RequirePermissionAction;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnCallMembershipEvent;
import com.ciscowebex.androidsdk.kitchensink.actions.events.OnMediaChangeEvent;
import com.ciscowebex.androidsdk.kitchensink.launcher.fragments.LauncherFragment;
import com.ciscowebex.androidsdk.kitchensink.ui.BaseFragment;
import com.ciscowebex.androidsdk.phone.CallObserver;
import com.github.benoitdion.ln.Ln;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        BaseFragment fragment = new LauncherFragment();
        replace(fragment);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        goBackStack();
    }

    public void goBackStack() {
        FragmentManager manager = getFragmentManager();
        BaseFragment fm = (BaseFragment) getFragment();
        fm.onBackPressed();
        if (manager.getBackStackEntryCount() == 1) {
            manager.popBackStackImmediate();
            super.onBackPressed();
        } else {
            //manager.popBackStackImmediate();
            startActivity(new Intent(this, LauncherActivity.class));
            finish();
        }
    }

    public void replace(BaseFragment fragment) {
        fragment.replace(this, R.id.launcher);
    }

    public Fragment getFragment() {
        return getFragmentManager().findFragmentById(R.id.launcher);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        RequirePermissionAction.PermissionsRequired(requestCode, grantResults);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnMediaChangeEvent event) {
        Ln.d("OnMediaChangeEvent: " + event.callEvent);
        if (event.callEvent instanceof CallObserver.SendingSharingEvent) {
            Ln.d("Activity SendingSharingEvent: " + ((CallObserver.SendingSharingEvent) event.callEvent).isSending());
            if (!((CallObserver.SendingSharingEvent) event.callEvent).isSending()) {
                cancelNotification();
                moveToFront();
                updateSharingSwitch(false);
                Toast.makeText(this, "Stop to share content", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OnCallMembershipEvent event) {
        if (event.callEvent instanceof CallObserver.MembershipSendingSharingEvent) {
            Ln.d("Activity CallMembership email: " + event.callEvent.getCallMembership().getEmail() +
                    "  isSendingSharing: " + event.callEvent.getCallMembership().isSendingSharing());
        }
    }

    protected void moveToFront() {
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (int i = 0; i < recentTasks.size(); i++) {
            // bring to front
            if (recentTasks.get(i).baseActivity.toShortString().indexOf("com.ciscowebex.androidsdk.kitchensink") > -1) {
                activityManager.moveTaskToFront(recentTasks.get(i).id, ActivityManager.MOVE_TASK_WITH_HOME);
            }
        }
    }

    private void cancelNotification() {
        NotificationManager notifyManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.cancel(1);
    }

    private void updateSharingSwitch(boolean flag) {
        Switch shareSwitch = (Switch) findViewById(R.id.switchShareContent);
        if (shareSwitch != null && shareSwitch.isChecked())
            shareSwitch.setChecked(flag);
    }
}
