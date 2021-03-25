package com.ciscowebex.androidsdk.kitchensink

import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.auth.Authenticator
import com.ciscowebex.androidsdk.auth.OAuthAuthenticator
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module

val OAuthCodeWebexModule = module {
    single <Authenticator> (named("oAuthCode")) {
        val clientId = BuildConfig.CLIENT_ID
        val clientSecret = BuildConfig.CLIENT_SECRET
        val redirectUri = BuildConfig.REDIRECT_URI

        OAuthAuthenticator(clientId, clientSecret, redirectUri)
    }

    factory {
        Webex(androidApplication(), get(named("oAuthCode")))
    }
}