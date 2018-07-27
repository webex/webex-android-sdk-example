package com.ciscowebex.androidsdk.kitchensink.launcher.fragments.pagers;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ciscowebex.androidsdk.kitchensink.R;
import com.ciscowebex.androidsdk.kitchensink.actions.commands.SearchSpaceAction;
import com.ciscowebex.androidsdk.kitchensink.actions.events.SearchSpaceCompleteEvent;
import com.ciscowebex.androidsdk.kitchensink.launcher.LauncherActivity;
import com.ciscowebex.androidsdk.kitchensink.launcher.fragments.DialPagersFragment;
import com.ciscowebex.androidsdk.kitchensink.ui.BaseFragment;
import com.ciscowebex.androidsdk.space.Space;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;


public class SpaceFragment extends BaseFragment {

    @BindView(R.id.space_list)
    public ListView listView;

    private List<Space> spaceList;

    public SpaceFragment() {
        // Required empty public constructor
        setLayout(R.layout.fragment_space);
        spaceList = new ArrayList<>();
    }

    @Override
    public void onStart() {
        super.onStart();
        //new SearchSpaceAction().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        new SearchSpaceAction().execute();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @OnItemClick(R.id.space_list)
    public void spaceListItemClicked(int position) {
        final Space p = spaceList.get(position);
        Fragment fm = ((LauncherActivity) getActivity()).getFragment();
        ((DialPagersFragment) fm).gotoDialPage(p.getId());
    }

    @Override
    public void onActivityCreated(Bundle saved) {
        super.onActivityCreated(saved);
        SpaceAdapter adapter = new SpaceAdapter(getActivity(), R.layout.listview_person, spaceList);
        listView.setAdapter(adapter);
    }

    class SpaceAdapter extends ArrayAdapter<Space> {

        private int resourceId;


        public SpaceAdapter(Context context, int textViewResourceId,
                            List<Space> objects) {
            super(context, textViewResourceId, objects);
            resourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            SpaceFragment.ViewHolder holder = new SpaceFragment.ViewHolder(view);

            Space space = getItem(position);

            holder.name.setText(space.getTitle());
            holder.email.setText(space.getType().toString());
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
    public void onEventMainThread(SearchSpaceCompleteEvent event) {
        dismissBusyIndicator();
        if (event.isSuccessful()) {
            List<Space> result = (List<Space>) event.getResult().getData();
            spaceList.clear();
            spaceList.addAll(result);
            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
        }
    }
}
