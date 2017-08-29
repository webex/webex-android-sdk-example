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

import com.ciscospark.SparkError;
import com.ciscospark.phone.DeregisterListener;
import com.ciscospark.phone.RegisterListener;

public class RegistryActivity extends AppCompatActivity {

    private static final String TAG = "RegistryActivity";
    public static final String IS_REGISTERED = "isRegistered";

    private Button buttonLogout;

    private Button buttonDial;

    private Button buttonWaiting;

    private boolean isRegistered = false;

    private KitchenSinkApplication myApplication;

    private TextView viewStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registry);


        myApplication = (KitchenSinkApplication)getApplication();

        viewStatus = (TextView) findViewById(R.id.textViewStatus);

        if (savedInstanceState != null) {
            isRegistered = savedInstanceState.getBoolean(IS_REGISTERED, false);
        }

        HandleLogoutButton();

        HandleDialButton();

        HandleWaitingCallButton();

        Log.i(TAG, "onCreate: ->start");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putBoolean(IS_REGISTERED, isRegistered);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void HandleWaitingCallButton() {
        buttonWaiting = (Button) findViewById(R.id.buttonWaitingCall);
        buttonWaiting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(RegistryActivity.this.isRegistered){
                    Intent intent = new Intent(RegistryActivity.this, CallActivity.class);
                    intent.putExtra(CallActivity.IS_WAITING_CALL, true);
                    startActivity(intent);
                }else{
                    Toast.makeText(RegistryActivity.this, "Please wait for registration", Toast
                            .LENGTH_SHORT).show();
                }
            }
        });
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

        this.myApplication.mSpark.phone().deregister(new DeregisterListener() {
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

            myApplication.mPhone = myApplication.mSpark.phone();
            this.myApplication.mPhone.register(new RegisterListener() {
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
