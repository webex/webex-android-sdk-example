package com.cisco.sparksdk.sparkkitchensink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DialFragment extends Fragment {
    private static final String TAG = "DialFragment";

    public DialFragment() {
        // Required empty public constructor
    }

    private Button buttonAudio;
    private Button buttonVideo;
    private EditText textCallee;
    private KitchenSinkApplication myapplication;

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
        View view = inflater.inflate(R.layout.fragment_dial, container, false);

        textCallee = (EditText) view.findViewById(R.id.editCallee);
        buttonVideo = (Button)view.findViewById(R.id.buttonVideoCall);
        buttonAudio = (Button)view.findViewById(R.id.buttonAudioCall);

        myapplication = (KitchenSinkApplication)getActivity().getApplicationContext();

        handleAudioButton();
        handleVideoButton();

        Log.i(TAG, "onCreateView: ->end");

        return view;
    }

    private void handleCallee(){


    }

    private void handleAudioButton(){

        Log.i(TAG, "handleAudioButton: ->start");

        buttonAudio.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "buttonAudio->onClick: ->start");

                String callee = textCallee.getText().toString();
                if (callee.isEmpty()) {
                    Toast.makeText(getActivity(), "Callee cannot be empty", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }else{
                    Log.i(TAG, "Audio call is sent out");
                    DialFragment.this.myapplication.callee = callee;
                    DialFragment.this.myapplication.isAudioCall = true;
                    Intent intent = new Intent(getActivity(), CallActivity.class);

                    getActivity().startActivity(intent);
                }
            }
        });

        Log.i(TAG, "handleAudioButton: ->end");

    }

    private void handleVideoButton(){
        Log.i(TAG, "handleVideoButton: ->start");

        buttonVideo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "buttonVideo->onClick: ->start");

                String callee = textCallee.getText().toString();
                if (callee.isEmpty()) {
                    Toast.makeText(getActivity(), "Callee cannot be empty", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }else{
                    Log.i(TAG, "Video call is sent out");
                    DialFragment.this.myapplication.callee = callee;
                    DialFragment.this.myapplication.isAudioCall = false;

                    Intent intent = new Intent(getActivity(), CallActivity.class);

                    getActivity().startActivity(intent);
                }
            }
        });

        Log.i(TAG, "handleVideoButton: ->end");

    }

}