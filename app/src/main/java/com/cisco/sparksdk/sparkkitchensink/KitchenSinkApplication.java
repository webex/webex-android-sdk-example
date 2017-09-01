package com.cisco.sparksdk.sparkkitchensink;

import com.ciscospark.Spark;
import com.ciscospark.core.SparkApplication;
import com.ciscospark.phone.Phone;

import static com.ciscospark.Spark.LogLevel.RELEASE;

public class KitchenSinkApplication extends SparkApplication {

    public Spark mSpark;
    public Phone mPhone;

    public String callee;
    public boolean isAudioCall;

    @Override
    public void onCreate() {
        super.onCreate();
        mSpark = new Spark();

        // This may not effect wme for wme may not initialized
        // as permission reason when start the app for the first time.
        mSpark.setLogLevel(RELEASE);
    }
}
