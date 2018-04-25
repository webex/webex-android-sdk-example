/*
 * Copyright 2016-2017 Cisco Systems Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package com.cisco.sparksdk.kitchensink.actions.commands;

import android.webkit.WebView;

import com.cisco.sparksdk.kitchensink.BuildConfig;
import com.cisco.sparksdk.kitchensink.actions.IAction;
import com.cisco.sparksdk.kitchensink.actions.SparkAgent;
import com.cisco.sparksdk.kitchensink.actions.events.LoginEvent;
import com.ciscospark.androidsdk.Spark;
import com.ciscospark.androidsdk.auth.OAuthWebViewAuthenticator;

import static com.cisco.sparksdk.kitchensink.KitchenSinkApp.getApplication;

/**
 * Created on 19/09/2017.
 */

public class SparkIdLoginAction implements IAction {

    private WebView view;

    private static final String clientId = BuildConfig.CLIENT_ID;

    private static final String clientSec = BuildConfig.CLIENT_SEC;

    private static final String redirect = BuildConfig.REDIRECT_URL;

    private static final String scope = BuildConfig.SCOPE;


    public SparkIdLoginAction(WebView view) {
        this.view = view;
    }

    @Override
    public void execute() {
        OAuthWebViewAuthenticator oAuth2;
        oAuth2 = new OAuthWebViewAuthenticator(clientId, clientSec, scope, redirect);
        Spark spark = new Spark(getApplication(), oAuth2);
        SparkAgent.getInstance().setSpark(spark);
        oAuth2.authorize(view, result -> {
            if (result.isSuccessful()) {
                new RegisterAction(oAuth2).execute();
            } else {
                new LoginEvent(result).post();
            }
        });
    }
}
