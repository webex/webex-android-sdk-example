package com.cisco.sparksdk.sparkkitchensink;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ciscospark.common.SparkError;
import com.ciscospark.phone.Call;
import com.ciscospark.phone.CallOption;
import com.ciscospark.phone.DialObserver;
import com.ciscospark.phone.IncomingCallObserver;
import com.ciscospark.phone.Phone;
import com.webex.wseclient.WseSurfaceView;

import java.util.List;

public class CallActivity extends AppCompatActivity {

    private static final String TAG = "CallActivity";
    public static final String IS_WAITING_CALL = "isWaitingCall";

    WseSurfaceView localView;
    WseSurfaceView remoteView;

    String dialCallee;
    public boolean isAudioCall;

    public Call mActiveCall;
    private MyCallObserver mActiveCallObserver;

    private KitchenSinkApplication myapplication;

    Button hangupButton;

    TextView callStatus;

    private Fragment mWaitingFragment;
    private boolean mWaitingCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_call);

        localView = (WseSurfaceView) findViewById(R.id.localView);
        remoteView = (WseSurfaceView) findViewById(R.id.remoteView);

        myapplication = (KitchenSinkApplication)getApplication();

        mActiveCallObserver = new MyCallObserver(this);

        callStatus = (TextView) findViewById(R.id.textViewStatus);

        HandleHangupButton();

        mWaitingCall = getIntent().getBooleanExtra(IS_WAITING_CALL, false);
        if (mWaitingCall) {
            setupWaitingCallFragment();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume " + mWaitingCall);
        if (!mWaitingCall) {
            call();
        } else {
            waitForIncomingCall();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");

        if (mActiveCall != null) {
            mActiveCall.hangup();
        }
    }

    private void setupWaitingCallFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mWaitingFragment = InComingCallFragment.newInstance();
        fragmentTransaction.add(R.id.call_layout, mWaitingFragment);
        fragmentTransaction.commit();
    }

    private void waitForIncomingCall() {
        KitchenSinkApplication application = (KitchenSinkApplication) getApplication();
        if (application.mSpark.getStrategy().isAuthorized()) {
            Phone phone = application.mSpark.phone();
            phone.setIncomingCallObserver(new IncomingCallObserver() {
                @Override
                public void onIncomingCall(Call call) {
                    mActiveCall = call;
                    if (mWaitingFragment != null) {
                        ((InComingCallFragment) mWaitingFragment).setButtonVisibility(View.VISIBLE);
                    }
                }
            });
        }

    }

    public void reject() {
        if (mWaitingFragment != null) {
            ((InComingCallFragment)mWaitingFragment).setButtonVisibility(View.INVISIBLE);
        }
        if (mActiveCall != null) {
            mActiveCall.reject();
        }
    }

    public void answerCall() {
        if (mActiveCall != null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(mWaitingFragment);
            fragmentTransaction.commit();
            mActiveCall.setObserver(mActiveCallObserver);
            mActiveCall.answer(new CallOption(CallOption.CallType.VIDEO, remoteView, localView));
        }
    }

    private void call(){
        Log.i(TAG, "call: ->start");

        this.dialCallee = myapplication.callee;
        this.isAudioCall = myapplication.isAudioCall;

        if(this.isAudioCall){
            this.audioCall(this.dialCallee);
        }else{
            this.videoCall(this.dialCallee);

        }

        this.callStatus.setText("Dialing");

    }

    private void HandleHangupButton(){
        Log.i(TAG, "HandleHangupButton: ->start");
        hangupButton = (Button) findViewById(R.id.buttonHangup);
        hangupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "HandleHangupButton.onClick: ->start");

                if (CallActivity.this.mActiveCall != null) {
                    CallActivity.this.mActiveCall.hangup();
                }
                CallActivity.this.callStatus.setText("Hangup");
            }
        });
    }

    private void videoCall(String dialstring) {
        Log.i(TAG, "videoCall: ->start");

        CallOption options = new CallOption(CallOption.CallType.VIDEO, this.remoteView, this.localView);

        this.myapplication.mSpark.phone().dial(dialstring, options, new DialObserver() {
            @Override
            public void onSuccess(Call call) {
                Log.i(TAG, "DialObserver-> onSuccess");
                CallActivity.this.mActiveCall = call;
                call.setObserver(CallActivity.this.mActiveCallObserver);
                Toast.makeText(CallActivity.this, "DialObserver-> onSuccess", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(SparkError error) {
                Log.i(TAG, "DialObserver-> onFailed");
                Log.i(TAG, "error code is " + error.toString());
                Toast.makeText(CallActivity.this, "DialObserver-> onFailed", Toast.LENGTH_SHORT).show();
                CallActivity.this.callStatus.setText("Dialing failed for "+ error.toString());
            }

            @Override
            public void onPermissionRequired(List<String> list) {
                Log.i(TAG, "onPermissionRequired: ->start");

                CallActivity.this.setPermission(list);
            }
        });
    }

    private void audioCall(String dialstring) {
        Log.i(TAG, "audioCall: ->start");
        CallOption options = new CallOption(CallOption.CallType.AUDIO, null, null);
        this.myapplication.mSpark.phone().dial(dialstring, options, new DialObserver() {
            @Override
            public void onSuccess(Call call) {
                Log.i(TAG, "DialObserver-> onSuccess");
                CallActivity.this.mActiveCall = call;
                call.setObserver(CallActivity.this.mActiveCallObserver);
                Toast.makeText(CallActivity.this, "DialObserver-> onSuccess", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onFailed(SparkError error) {
                Log.i(TAG, "DialObserver-> onFailed");
                Log.i(TAG, "error code is " + error.toString());
                Toast.makeText(CallActivity.this, "DialObserver-> onFailed", Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onPermissionRequired(List<String> list) {
                Log.i(TAG, "onPermissionRequired: ->start");

                CallActivity.this.setPermission(list);
            }


        });
        Log.i(TAG, "audioCall: ->end");
    }

    public void setPermission(List<String> permissions) {
        Log.i(TAG, "setPermission: ->start");
        String[] permissionStrings = new String[permissions.size()];
        permissionStrings = permissions.toArray(permissionStrings);
        ActivityCompat.requestPermissions(this, permissionStrings, 0);
        Log.i(TAG, "setPermission: ->end");
    }
}
