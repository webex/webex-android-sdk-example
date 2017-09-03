package com.cisco.sparksdk.sparkkitchensink;

import com.ciscospark.Spark;
import com.ciscospark.core.SparkApplication;
import com.ciscospark.phone.Phone;

public class KitchenSinkApplication extends SparkApplication {

    public Spark mSpark;
    public Phone mPhone;

    public String callee;
    public boolean isAudioCall;

    @Override
    public void onCreate() {
        super.onCreate();
        mSpark = new Spark();
    }
}
