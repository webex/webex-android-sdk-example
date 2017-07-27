package com.cisco.sparksdk.sparkkitchensink;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.cisco.spark.android.authenticator.OAuth2AccessToken;
import com.ciscospark.auth.AuthorizeListener;
import com.ciscospark.auth.OAuth2Authenticator;
import com.ciscospark.common.SparkError;

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
                String clientId = "Cc580d5219555f0df8b03d99f3e020381eae4eee0bad1501ad187480db311cce4";
                String clientSec = "d4e9385b2e5828eef376077995080ea4aa42b5c92f1b6af8f3a59fc6a4e79f6a";
                String redirect = "AndroidDemoApp://response";
                String scope = "spark:all spark:kms";
                WebView webView = (WebView) findViewById(R.id.OAuthWebView);
                OAuth2Authenticator authenticator = new OAuth2Authenticator(clientId,clientSec,redirect,scope);
                ((KitchenSinkApplication)getApplication()).mSpark.setStrategy(authenticator);
                if (!authenticator.isAuthorized()) {
                    authenticator.authorize(webView, new AuthorizeListener() {
                        @Override
                        public void onSuccess(OAuth2AccessToken oAuth2AccessToken) {
                            startActivity(new Intent(EnterActivity.this, RegistryActivity.class));
                            finish();
                        }

                        @Override
                        public void onFailed(SparkError<AuthError> sparkError) {
                            Toast.makeText(EnterActivity.this, "Authorize error: " + sparkError.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
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
