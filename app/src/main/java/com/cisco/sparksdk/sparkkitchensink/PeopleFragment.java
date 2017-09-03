package com.cisco.sparksdk.sparkkitchensink;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PeopleFragment extends Fragment {

    private static final String TAG = "PeopleFragment";

    public PeopleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: ->start");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ->start");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_people, container, false);
    }
}
