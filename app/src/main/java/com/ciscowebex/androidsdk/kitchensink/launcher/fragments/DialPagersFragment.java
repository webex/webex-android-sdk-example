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

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import com.ciscowebex.androidsdk.kitchensink.R;
import com.ciscowebex.androidsdk.kitchensink.launcher.fragments.pagers.DialFragment;
import com.ciscowebex.androidsdk.kitchensink.launcher.fragments.pagers.HistoryFragment;
import com.ciscowebex.androidsdk.kitchensink.launcher.fragments.pagers.PeopleFragment;
import com.ciscowebex.androidsdk.kitchensink.launcher.fragments.pagers.SpaceFragment;
import com.ciscowebex.androidsdk.kitchensink.ui.BaseFragment;
import com.ciscowebex.androidsdk.kitchensink.ui.ListPageAdapter;

import butterknife.BindView;

import static android.R.drawable.ic_menu_call;
import static android.R.drawable.ic_menu_myplaces;
import static android.R.drawable.ic_menu_recent_history;
import static android.R.drawable.ic_menu_search;

public class DialPagersFragment extends BaseFragment {

    @BindView(R.id.dialViewPager)
    ViewPager pager;

    @BindView(R.id.dialTabs)
    TabLayout tabs;

    public DialPagersFragment() {
        // Required empty public constructor
        setLayout(R.layout.fragment_dial_pagers);
    }

    @Override
    public void onActivityCreated(Bundle saved) {
        super.onActivityCreated(saved);
        setupAdapter();
        tabs.setupWithViewPager(pager);
        setupTabIcons();
    }

    public void gotoDialPage(String dialString) {
        pager.setCurrentItem(2);
        Fragment fm = ((ListPageAdapter) pager.getAdapter()).getItem(2);
        ((DialFragment) fm).setDialString(dialString);
    }

    private void setupAdapter() {
        ListPageAdapter adapter;
        adapter = new ListPageAdapter(getFragmentManager());
        adapter.add(new HistoryFragment());
        adapter.add(new PeopleFragment());
        adapter.add(new DialFragment());
        adapter.add(new SpaceFragment());
        pager.setAdapter(adapter);
    }

    private void setupTabIcons() {
        tabs.getTabAt(0).setIcon(ic_menu_recent_history);
        tabs.getTabAt(1).setIcon(ic_menu_search);
        tabs.getTabAt(2).setIcon(ic_menu_call);
        tabs.getTabAt(3).setIcon(ic_menu_myplaces);
    }
}