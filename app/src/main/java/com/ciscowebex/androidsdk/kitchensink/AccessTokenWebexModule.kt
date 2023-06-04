package com.ciscowebex.androidsdk.kitchensink

import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.auth.JWTAuthenticator
import com.ciscowebex.androidsdk.auth.TokenAuthenticator
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val AccessTokenWebexModule = module {

    factory {
        val isFedrampEnabled = SharedPrefUtils.getFedrampPref(androidApplication())
        Webex(androidApplication(), TokenAuthenticator(isFedrampEnabled))
    }
}