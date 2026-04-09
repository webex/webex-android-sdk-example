package com.ciscowebex.androidsdk.kitchensink

import com.ciscowebex.androidsdk.auth.JWTAuthenticator
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val JWTWebexModule = module {

    factory {
        buildCrashEnabledWebex(androidApplication(), JWTAuthenticator())
    }
}