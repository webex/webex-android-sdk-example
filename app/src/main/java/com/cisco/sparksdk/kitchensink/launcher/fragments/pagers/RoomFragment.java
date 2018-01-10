package com.cisco.sparksdk.kitchensink.launcher.fragments.pagers;

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

import com.cisco.sparksdk.kitchensink.R;
import com.cisco.sparksdk.kitchensink.actions.commands.SearchRoomAction;
import com.cisco.sparksdk.kitchensink.actions.events.SearchRoomCompleteEvent;
import com.cisco.sparksdk.kitchensink.launcher.LauncherActivity;
import com.cisco.sparksdk.kitchensink.launcher.fragments.DialPagersFragment;
import com.cisco.sparksdk.kitchensink.ui.BaseFragment;
import com.ciscospark.androidsdk.room.Room;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;


public class RoomFragment extends BaseFragment {

	@BindView(R.id.room_list)
	public ListView listView;

	private List<Room> roomList;

	public RoomFragment() {
		// Required empty public constructor
		setLayout(R.layout.fragment_room);
		roomList = new ArrayList<>();
	}

	@Override
	public void onStart() {
		super.onStart();
		new SearchRoomAction().execute();
	}

	@OnItemClick(R.id.room_list)
	public void roomListItemClicked(int position) {
		final Room p = roomList.get(position);
		Fragment fm = ((LauncherActivity) getActivity()).getFragment();
		((DialPagersFragment) fm).gotoDialPage(p.getId());
	}

	@Override
	public void onActivityCreated(Bundle saved) {
		super.onActivityCreated(saved);
		RoomFragment.RoomAdapter adapter = new RoomFragment.RoomAdapter(getActivity(), R.layout.listview_person, roomList);
		listView.setAdapter(adapter);
	}

	class RoomAdapter extends ArrayAdapter<Room> {

		private int resourceId;


		public RoomAdapter(Context context, int textViewResourceId,
						   List<Room> objects) {
			super(context, textViewResourceId, objects);
			resourceId = textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
			RoomFragment.ViewHolder holder = new RoomFragment.ViewHolder(view);

			Room room = getItem(position);

			holder.name.setText(room.getTitle());
			holder.email.setText(room.getType().toString());
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
	public void onEventMainThread(SearchRoomCompleteEvent event) {
		dismissBusyIndicator();
		if (event.isSuccessful()) {
			List<Room> result = (List<Room>) event.getResult().getData();
			roomList.clear();
			roomList.addAll(result);
			((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
		}
	}
}
