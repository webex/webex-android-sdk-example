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


import android.app.Fragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ciscowebex.androidsdk.kitchensink.R;
import com.ciscowebex.androidsdk.kitchensink.launcher.LauncherActivity;
import com.ciscowebex.androidsdk.kitchensink.launcher.fragments.DialPagersFragment;
import com.ciscowebex.androidsdk.kitchensink.models.CallHistory;
import com.ciscowebex.androidsdk.kitchensink.models.CallHistoryDao;
import com.ciscowebex.androidsdk.kitchensink.models.DaoSession;
import com.ciscowebex.androidsdk.kitchensink.ui.BaseFragment;
import com.ciscowebex.androidsdk.kitchensink.ui.CircleTransform;
import com.ciscowebex.androidsdk.people.Person;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnItemClick;

import static android.R.drawable.sym_call_incoming;
import static android.R.drawable.sym_call_outgoing;
import static com.ciscowebex.androidsdk.kitchensink.KitchenSinkApp.getApplication;
import static com.ciscowebex.androidsdk.kitchensink.R.drawable.google_contacts_android;

public class HistoryFragment extends BaseFragment {

    private CallHistoryDao dao;

    private List<CallHistory> callList = new ArrayList<>();

    @BindView(R.id.callhistory)
    public ListView listView;

    public HistoryFragment() {
        // Required empty public constructor
        setLayout(R.layout.fragment_history);
    }


    @Override
    public void onStart() {
        super.onStart();
        DaoSession daoSession = getApplication().getDaoSession();
        dao = daoSession.getCallHistoryDao();
        PersonAdapter adapter = new PersonAdapter(getActivity(), R.layout.listview_person, callList);
        listView.setAdapter(adapter);
        update();
    }

    public void update() {
        List<CallHistory> list = dao.queryBuilder().orderDesc(CallHistoryDao.Properties.Date).list();
        callList.clear();
        callList.addAll(list);
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

    @OnItemClick(R.id.callhistory)
    public void historyItemClicked(int position) {
        final CallHistory history = callList.get(position);
        Fragment fm = ((LauncherActivity) getActivity()).getFragment();
        ((DialPagersFragment) fm).gotoDialPage(history.getEmail());
    }

    class PersonAdapter extends ArrayAdapter<CallHistory> {

        private int resourceId;

        public PersonAdapter(Context context, int textViewResourceId,
                             List<CallHistory> objects) {
            super(context, textViewResourceId, objects);
            resourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CallHistory history = getItem(position);
            View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            TextView name = view.findViewById(R.id.person_name);
            name.setText(history.getEmail());
            TextView email = view.findViewById(R.id.person_email);
            SimpleDateFormat spf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss");
            email.setText(spf.format(history.getDate()));

            ImageView image = view.findViewById(R.id.person_icon);
            if (history.getPerson() != null && !history.getPerson().isEmpty()) {
                Gson gson = new Gson();
                Person person = gson.fromJson(history.getPerson(), Person.class);
                if (person.getAvatar() != null && !person.getAvatar().isEmpty()) {
                    Picasso.with(getContext()).load(person.getAvatar()).transform(new CircleTransform()).into(image);
                }
            } else {
                image.setImageResource(google_contacts_android);
            }
            if (history.getDirection().equals("in")) {
                ImageView icon = view.findViewById(R.id.person_call_icon);
                icon.setImageResource(sym_call_incoming);
            }
            if (history.getDirection().equals("out")) {
                ImageView icon = view.findViewById(R.id.person_call_icon);
                icon.setImageResource(sym_call_outgoing);
            }
            return view;
        }
    }
}
