package com.cisco.sparksdk.sparkkitchensink;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ciscospark.common.SparkError;
import com.ciscospark.phone.DeregisterListener;
import com.ciscospark.phone.RegisterListener;

public class RegistryActivity extends AppCompatActivity {

    private static final String TAG = "RegistryActivity";

    private Button buttonLogout;

    private Button buttonDial;

    private boolean isRegistered;

    private KitchenSinkApplication myapplication;

    private TextView viewStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registry);


        myapplication = (KitchenSinkApplication)getApplication();

        viewStatus = (TextView) findViewById(R.id.textViewStatus);

        HandleLogoutButton();

        HandleDialButton();

        Log.i(TAG, "onCreate: ->start");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "onStart: ->start");

        this.registerToLocus();
    }

    private boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit) {
            logout();
        }
        else {
            Toast.makeText(this, "Press Back again to Exit.",Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }


    private void logout(){
        Log.i(TAG, "logout: ->start");

        this.myapplication.mSpark.phone().deregister(new DeregisterListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(RegistryActivity.this, "Deregister successfully", Toast.LENGTH_SHORT)
                        .show();
                //MainActivity.this.callStatus.setText(R.string.call_status_Deregistered);

                RegistryActivity.this.finishAffinity();

                System.exit(0);
            }

            @Override
            public void onFailed(SparkError error) {
                Toast.makeText(RegistryActivity.this, "Deregister failed", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void HandleLogoutButton(){
        Log.i(TAG, "HandleLogoutButton: ->start");
        buttonLogout = (Button) findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "buttonLogout.onClick: ->start");
                // Perform action on click
                Toast.makeText(RegistryActivity.this, "Logout & Quit", Toast.LENGTH_SHORT)
                        .show();

                RegistryActivity.this.logout();

                }
        });
    }

    private void HandleDialButton(){
        Log.i(TAG, "HandleDialButton: ->start");
        buttonDial = (Button) findViewById(R.id.buttonDial);
        buttonDial.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "buttonDial.onClick: ->start");

                if(RegistryActivity.this.isRegistered){
                    Intent intent = new Intent(RegistryActivity.this, dialActivity.class);

                    RegistryActivity.this.startActivity(intent);

                }else{

                    Toast.makeText(RegistryActivity.this, "Please wait for registration", Toast
                            .LENGTH_SHORT).show();

                }




            }
        });
    }

    private void registerToLocus(){
        Log.i(TAG, "registerToLocus: ->start");

        if(!this.isRegistered){

            Log.i(TAG, "begin to register");

            this.myapplication.mSpark.phone().register(new RegisterListener() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "register successfully");
                    Toast.makeText(RegistryActivity.this, "register successfully", Toast
                            .LENGTH_SHORT).show();
                    RegistryActivity.this.viewStatus.setText("Registered");

                    RegistryActivity.this.isRegistered = true;

                }

                @Override
                public void onFailed(SparkError error) {
                    Log.i(TAG, "register failed");
                    Toast.makeText(RegistryActivity.this, "register failed" + error.toString(), Toast.LENGTH_SHORT).show();
                    RegistryActivity.this.viewStatus.setText("Not registered");
                    RegistryActivity.this.isRegistered = false;
                }
            });

            Toast.makeText(RegistryActivity.this, "registering", Toast.LENGTH_SHORT).show();
            RegistryActivity.this.viewStatus.setText("registering");

        }else{

            Log.i(TAG, "already register");

        }




    }
}
