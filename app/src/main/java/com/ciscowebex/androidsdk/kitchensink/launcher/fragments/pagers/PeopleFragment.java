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
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.ciscowebex.androidsdk.kitchensink.R;
import com.ciscowebex.androidsdk.kitchensink.actions.commands.SearchPeopleAction;
import com.ciscowebex.androidsdk.kitchensink.actions.events.SearchPersonCompleteEvent;
import com.ciscowebex.androidsdk.kitchensink.launcher.LauncherActivity;
import com.ciscowebex.androidsdk.kitchensink.launcher.fragments.DialPagersFragment;
import com.ciscowebex.androidsdk.kitchensink.ui.BaseFragment;
import com.ciscowebex.androidsdk.kitchensink.ui.CircleTransform;
import com.ciscowebex.androidsdk.people.Person;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

public class PeopleFragment extends BaseFragment {

    @BindView(R.id.people_list)
    public ListView listView;

    @BindView(R.id.search_people)
    public SearchView searchView;

    private List<Person> persons;

    public PeopleFragment() {
        // Required empty public constructor
        setLayout(R.layout.fragment_people);
        persons = new ArrayList<>();
    }


    @OnItemClick(R.id.people_list)
    public void peopleListItemClicked(int position) {
        final Person p = persons.get(position);
        Fragment fm = ((LauncherActivity)getActivity()).getFragment();
        ((DialPagersFragment) fm).gotoDialPage(p.getEmails()[0]);
    }

    @Override
    public void onActivityCreated(Bundle saved) {
        super.onActivityCreated(saved);
        PersonAdapter adapter = new PersonAdapter(getActivity(), R.layout.listview_person, persons);
        listView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryString) {
                resetSearchView();
                showBusyIndicator("Searching", "searching people ...");
                query(queryString);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void query(String query) {
        if (query.isEmpty()) return;

        if (isValidEmail(query)) {
            new SearchPeopleAction(query, null).execute();
        } else {
            new SearchPeopleAction(null, query).execute();
        }
    }

    private void resetSearchView() {
        searchView.setEnabled(false);
        searchView.setIconified(true);
        searchView.clearFocus();
        searchView.setInputType(InputType.TYPE_NULL);
    }

    class PersonAdapter extends ArrayAdapter<Person> {

        private int resourceId;


        public PersonAdapter(Context context, int textViewResourceId,
                             List<Person> objects) {
            super(context, textViewResourceId, objects);
            resourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            ViewHolder holder = new ViewHolder(view);

            Person person = getItem(position);

            Picasso.with(getContext()).load(person.getAvatar()).transform(new CircleTransform()).into(holder.avatar);
            holder.name.setText(person.getDisplayName());
            holder.email.setText(person.getEmails()[0]);
            return view;
        }
    }

    static class ViewHolder {
        @BindView(R.id.person_name)
        TextView name;

        @BindView(R.id.person_email)
        TextView email;

        @BindView(R.id.person_icon)
        ImageView avatar;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onEventMainThread(SearchPersonCompleteEvent event) {
        dismissBusyIndicator();
        if (event.isSuccessful()) {
            List<Person> result = (List<Person>) event.getResult().getData();
            persons.clear();
            persons.addAll(result);
            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
        }
    }
}
