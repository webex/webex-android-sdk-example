package com.cisco.sparksdk.sparkkitchensink;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.cisco.spark.android.authenticator.OAuth2AccessToken;
import com.ciscospark.auth.AuthorizeListener;
import com.ciscospark.auth.OAuth2Authenticator;
import com.ciscospark.common.SparkError;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link OAuth2Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OAuth2Fragment extends Fragment {

    public OAuth2Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OAuth2Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OAuth2Fragment newInstance() {
        OAuth2Fragment fragment = new OAuth2Fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_oauth2, container, false);
        authorize(rootView);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void authorize(View rootView) {
        String clientId = "Cc580d5219555f0df8b03d99f3e020381eae4eee0bad1501ad187480db311cce4";
        String clientSec = "d4e9385b2e5828eef376077995080ea4aa42b5c92f1b6af8f3a59fc6a4e79f6a";
        String redirect = "AndroidDemoApp://response";
        String scope = "spark:all spark:kms";
        WebView webView = (WebView)rootView.findViewById(R.id.OAuthWebView);
        OAuth2Authenticator authenticator = new OAuth2Authenticator(clientId,clientSec,redirect,scope);
        ((KitchenSinkApplication)getActivity().getApplication()).mSpark.setStrategy(authenticator);
        if (!authenticator.isAuthorized()) {
            authenticator.authorize(webView, new AuthorizeListener() {
                @Override
                public void onSuccess(OAuth2AccessToken oAuth2AccessToken) {
                    startActivity(new Intent(getActivity(), RegistryActivity.class));
                    getActivity().finish();
                }

                @Override
                public void onFailed(SparkError<AuthError> sparkError) {
                    Toast.makeText(getActivity(), "Authorize error: " + sparkError.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
