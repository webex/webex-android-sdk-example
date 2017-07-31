package com.cisco.sparksdk.sparkkitchensink;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class EnterActivity extends AppCompatActivity {

    Button buttonJWT;

    Button buttonSpark;

    private static final String TAG = "EnterActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);

        Log.i(TAG, "onCreate: ->start");

        buttonSpark = (Button) findViewById(R.id.buttonSpark);
        buttonSpark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "spark id authorize");
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                OAuth2Fragment fragment = OAuth2Fragment.newInstance();
                fragmentTransaction.replace(R.id.enter_activity_layout, fragment);
                fragmentTransaction.commit();
            }
        });

        buttonJWT = (Button) findViewById(R.id.buttonJWT);
        buttonJWT.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "onClick: ->start");
                // Perform action on click
                Intent intent = new Intent(EnterActivity.this, JWTActivity.class);

                // currentContext.startActivity(activityChangeIntent);

                EnterActivity.this.startActivity(intent);

                KitchenSinkApplication myapplication = (KitchenSinkApplication) getApplication();

                if (myapplication.mSpark != null) {

                    Log.i(TAG, "mSpark is created ");

                    //prevent go back
                    EnterActivity.this.finish();

                    //Toast.makeText(EnterActivity.this, "Spark version is " + myapplication.mSpark.version(), Toast.LENGTH_SHORT).show();

                    //Toast.makeText(EnterActivity.this, "mSpark is created", Toast.LENGTH_SHORT).show();

                } else {

                    Log.i(TAG, "mSpark is null ");

                    //prevent go back
                    EnterActivity.this.finish();

                    //Toast.makeText(EnterActivity.this, "mSpark is null", Toast.LENGTH_SHORT).show();

                }

                Log.i(TAG, "onClick: ->end");
            }
        });
    }
}
