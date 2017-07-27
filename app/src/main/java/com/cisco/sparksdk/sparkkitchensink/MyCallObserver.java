package com.cisco.sparksdk.sparkkitchensink;

import android.util.Log;
import android.widget.Toast;

import com.ciscospark.common.SparkError;
import com.ciscospark.phone.Call;


public class MyCallObserver implements com.ciscospark.phone.CallObserver {

    public CallActivity mActivity;
    private static final String TAG = "MyCallObserver";

    public MyCallObserver(CallActivity activity) {
        super();
        mActivity = activity;
    }

    @Override
    public void onRinging(Call call) {
        Log.i(TAG, "onRinging: ->start");
        Toast.makeText(mActivity, "onRinging!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(Call call) {
        Log.i(TAG, "onConnected: ->start");
        Toast.makeText(mActivity, "call onConnected", Toast.LENGTH_SHORT).show();
        this.mActivity.callStatus.setText("In Call");
    }

    @Override
    public void onDisconnected(Call call, DisconnectedReason reason, SparkError errorInfo) {
        Log.i(TAG, "onDisconnected: ->start");

        Toast.makeText(mActivity, "call onDisconnected", Toast.LENGTH_SHORT).show();
        switch (reason) {

            case endForAndroidPermission: {
                Toast.makeText(mActivity, "call onDisconnected as endForAndroidPermission", Toast.LENGTH_SHORT).show();
                this.mActivity.callStatus.setText("call onDisconnected as endForAndroidPermission");

                Log.i(TAG, "reason is  endForAndroidPermission");
                break;
            }
            case selfHangUP: {
                Toast.makeText(mActivity, "call onDisconnected as selfHangUP", Toast.LENGTH_SHORT).show();
                this.mActivity.callStatus.setText("call onDisconnected as selfHangUP");
                Log.i(TAG, "reason is  selfHangUP");
                break;
            }
            case remoteHangUP: {
                Toast.makeText(mActivity, "call onDisconnected as remoteHangUP", Toast.LENGTH_SHORT).show();
                this.mActivity.callStatus.setText("call onDisconnected as remoteHangUP");
                Log.i(TAG, "reason is  remoteHangUP");
                break;
            }
            case remoteReject: {
                Toast.makeText(mActivity, "call onDisconnected as remoteReject", Toast.LENGTH_SHORT).show();
                this.mActivity.callStatus.setText("call onDisconnected as remoteReject");
                Log.i(TAG, "reason is  remoteReject");
                break;
            }
            case callEnd: {
                Toast.makeText(mActivity, "call onDisconnected as callEnd", Toast.LENGTH_SHORT).show();
                this.mActivity.callStatus.setText("call onDisconnected as callEnd");
                Log.i(TAG, "reason is  callEnd");
                break;
            }
            case Error_serviceFailed_CallJoinError: {
                Toast.makeText(mActivity, "call onDisconnected as Error_serviceFailed_CallJoinError", Toast.LENGTH_SHORT).show();
                this.mActivity.callStatus.setText("call onDisconnected as Error_serviceFailed_CallJoinError");
                Log.i(TAG, "reason is  Error_serviceFailed_CallJoinError");
                if (errorInfo != null) {
                    Log.i(TAG, "SparkError is +" + errorInfo.toString());
                    Toast.makeText(mActivity, "SparkError is +" + errorInfo.toString(), Toast.LENGTH_SHORT).show();

                }
                break;
            }
            default: {
                break;
            }

        }
    }

    @Override
    public void onMediaChanged(Call call, MediaChangeReason reason) {
        Log.i(TAG, "onMediaChanged: ->start");
        Log.i(TAG, "reason is  " + reason);
    }


}

