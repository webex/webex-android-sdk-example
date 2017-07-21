package com.cisco.sparksdk.sparkkitchensink;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class RegistryActivity extends AppCompatActivity {

    private static final String TAG = "RegistryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registry);

        Log.i(TAG, "onCreate: ->start");
    }
}
