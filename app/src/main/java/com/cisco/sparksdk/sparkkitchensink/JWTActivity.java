package com.cisco.sparksdk.sparkkitchensink;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ciscospark.CompletionHandler;
import com.ciscospark.SparkError;
import com.ciscospark.auth.Authenticator;
import com.ciscospark.auth.JWTAuthenticator;

public class JWTActivity extends AppCompatActivity {

    private static final String TAG = "JWTActivity";

    private Button buttonLogin;
    private EditText editTextJWT;

    private Authenticator strategy;

    private KitchenSinkApplication myApplication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jwt);

        myApplication = (KitchenSinkApplication) getApplication();

        Log.i(TAG, "onCreate: ->start");

        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        editTextJWT = (EditText) findViewById(R.id.editTextJWT);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "onClick: ->start");

                String jwtKey = editTextJWT.getText().toString();
                if (jwtKey.isEmpty()) {
                    /*
                    Toast.makeText(JWTActivity.this, "JWT Token cannot be empty", Toast
                            .LENGTH_SHORT).show();
                    return;
                    */
                    jwtKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMSIsIm5hbWUiOiJ1c2VyICMxIiwiaXNzIjoiY2Q1YzlhZjctOGVkMy00ZTE1LTk3MDUtMDI1ZWYzMGIxYjZhIn0.nQTlT_WwkHdWZTCNi4tVl2IA476nAWo34oxtuTlLSDk";
                }

                Toast.makeText(JWTActivity.this, "JWT authoring", Toast.LENGTH_SHORT).show();

                JWTAuthenticator authenticator = new JWTAuthenticator(jwtKey);
                strategy = authenticator;

                myApplication.mSpark.setAuthenticator(strategy);

                myApplication.mSpark.authorize(new CompletionHandler<String>() {
                    @Override
                    public void onComplete(String s) {
                        Log.d(TAG, s);
                        Toast.makeText(JWTActivity.this, "JWT auth success", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(JWTActivity.this, RegistryActivity.class);
                        JWTActivity.this.startActivity(intent);
                    }

                    @Override
                    public void onError(SparkError sparkError) {
                        Toast.makeText(JWTActivity.this, "JWT auth failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }


        });


    }

    private boolean exit = false;

    @Override
    public void onBackPressed() {
        if (exit) {
            this.finish();
        } else {
            Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }
}
