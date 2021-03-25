package com.ciscowebex.androidsdk.kitchensink

import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.auth.OAuthWebViewAuthenticator
import com.ciscowebex.androidsdk.auth.Authenticator
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module

val OAuthWebexModule = module {
    single <Authenticator> (named("oAuth")) {
        val clientId = BuildConfig.CLIENT_ID
        val clientSecret = BuildConfig.CLIENT_SECRET
        val redirectUri = BuildConfig.REDIRECT_URI

        OAuthWebViewAuthenticator(clientId, clientSecret, redirectUri)
    }

    factory {
        Webex(androidApplication(), get(named("oAuth")))
    }
}