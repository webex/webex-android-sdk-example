package com.cisco.sparksdk.sparkkitchensink;

import android.util.Log;

import com.ciscospark.Spark;
import com.ciscospark.core.SparkApplication;
import com.ciscospark.phone.Phone;

/**
 * Created by lm on 7/21/17.
 */

public class KitchenSinkApplication extends SparkApplication {

    public Spark mSpark;
    private static final String TAG = "KitchenSinkApplication";

    @Override
    public void onCreate() {

        Log.i(TAG, "onCreate: ->start");

        super.onCreate();

        mSpark = new Spark();

        Log.i(TAG, "onCreate: ->end");
    }







}
