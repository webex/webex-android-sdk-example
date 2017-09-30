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

package com.cisco.sparksdk.kitchensink.launcher;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import com.cisco.sparksdk.kitchensink.R;
import com.cisco.sparksdk.kitchensink.actions.commands.RequirePermissionAction;
import com.cisco.sparksdk.kitchensink.ui.BaseFragment;
import com.cisco.sparksdk.kitchensink.launcher.fragments.LauncherFragment;
import com.github.benoitdion.ln.Ln;

public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        BaseFragment fragment = new LauncherFragment();
        replace(fragment);
    }

    @Override
    public void onBackPressed() {
        goBackStack();
    }

    public void goBackStack() {
        FragmentManager manager = getFragmentManager();
        BaseFragment fm = (BaseFragment)getFragment();
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Ln.e("permission required");
        RequirePermissionAction.PermissionsRequired(requestCode, grantResults);
    }
}
