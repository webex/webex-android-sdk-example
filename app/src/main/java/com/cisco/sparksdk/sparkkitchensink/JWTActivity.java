package com.cisco.sparksdk.sparkkitchensink;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cisco.spark.android.authenticator.OAuth2AccessToken;
import com.ciscospark.auth.Authenticator;
import com.ciscospark.auth.AuthorizeListener;
import com.ciscospark.auth.JWTAuthenticator;
import com.ciscospark.common.SparkError;

public class JWTActivity extends AppCompatActivity {

    private static final String TAG = "JWTActivity";

    private Button buttonLogin;
    private EditText editTextJWT;

    private Authenticator strategy;

    private KitchenSinkApplication myapplication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jwt);

        myapplication = (KitchenSinkApplication)getApplication();

        Log.i(TAG, "onCreate: ->start");

        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        editTextJWT = (EditText) findViewById(R.id.editTextJWT);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "onClick: ->start");

                String jwtKey = editTextJWT.getText().toString();
                if (jwtKey.isEmpty()) {
                    Toast.makeText(JWTActivity.this, "JWT Token cannot be empty", Toast
                            .LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(JWTActivity.this, "JWT authoring", Toast.LENGTH_SHORT).show();

                JWTAuthenticator authenticator = new JWTAuthenticator();
                strategy = authenticator;



                JWTActivity.this.myapplication.mSpark.init(strategy);

                authenticator.authorize(jwtKey);
                if (strategy.isAuthorized()) {
                    strategy.accessToken(new AuthorizeListener() {
                        @Override
                        public void onSuccess(OAuth2AccessToken token) {
                            Log.d(TAG, token.getAccessToken());
                            Toast.makeText(JWTActivity.this, "JWT auth success", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(JWTActivity.this, RegistryActivity.class);

                            JWTActivity.this.startActivity(intent);

                        }

                        @Override
                        public void onFailed(SparkError<AuthError> error) {
                            Toast.makeText(JWTActivity.this, "JWT auth failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }



        });


    }

    private boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit) {
            this.finish();
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
}
