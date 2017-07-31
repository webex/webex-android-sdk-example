package com.cisco.sparksdk.sparkkitchensink;


import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InComingCallFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InComingCallFragment extends Fragment {
    FloatingActionButton mAnswerButton;
    FloatingActionButton mRejectButton;

    public InComingCallFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InComingCallFragment.
     */
    public static InComingCallFragment newInstance() {
        InComingCallFragment fragment = new InComingCallFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_in_coming_call, container, false);
        mAnswerButton = (FloatingActionButton) rootView.findViewById(R.id.answer);
        mRejectButton = (FloatingActionButton) rootView.findViewById(R.id.reject);
        mAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CallActivity)(getActivity())).answerCall();
            }
        });
        mRejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CallActivity)(getActivity())).reject();
            }
        });
        return rootView;
    }

    public void setButtonVisibility(int visibility) {
        if (mAnswerButton != null) {
            mAnswerButton.setVisibility(visibility);
        }
        if (mRejectButton != null) {
            mRejectButton.setVisibility(visibility);
        }
    }
}
