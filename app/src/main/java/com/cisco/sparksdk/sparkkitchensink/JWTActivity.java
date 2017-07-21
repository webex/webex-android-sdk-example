package com.cisco.sparksdk.sparkkitchensink;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class JWTActivity extends AppCompatActivity {

    private static final String TAG = "JWTActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jwt);

        Log.i(TAG, "onCreate: ->start");
    }
}
