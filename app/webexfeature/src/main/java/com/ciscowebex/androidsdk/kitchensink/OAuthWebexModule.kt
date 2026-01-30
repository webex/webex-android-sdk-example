package com.ciscowebex.androidsdk.kitchensink

import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.auth.OAuthWebViewAuthenticator
import com.ciscowebex.androidsdk.auth.Authenticator
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils.getEmailPref
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils.getFedrampPref
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module

val OAuthWebexModule = module {
    single <Authenticator> (named("oAuth")) {
        val isFedrampEnabled = getFedrampPref(androidApplication())
        val clientId = if(isFedrampEnabled) BuildConfig.FEDRAMP_CLIENT_ID  else BuildConfig.CLIENT_ID
        val clientSecret =  if(isFedrampEnabled) BuildConfig.FEDRAMP_CLIENT_SECRET else BuildConfig.CLIENT_SECRET
        val redirectUri =  if(isFedrampEnabled) BuildConfig.FEDRAMP_REDIRECT_URI else BuildConfig.REDIRECT_URI
        val additionalScopes =  BuildConfig.SCOPE
        val email = getEmailPref(androidApplication()).orEmpty()

        OAuthWebViewAuthenticator(clientId, clientSecret, additionalScopes, redirectUri, email, isFedrampEnabled)
    }

    factory {
        Webex(androidApplication(), get(named("oAuth")))
    }
}