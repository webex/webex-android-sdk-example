package com.ciscowebex.androidsdk.kitchensink

import android.app.Application
import android.util.Log
import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.auth.Authenticator
import com.ciscowebex.androidsdk.kitchensink.calling.RingerManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal fun buildCrashEnabledWebex(application: Application, authenticator: Authenticator): Webex {
    return Webex(application, authenticator).apply {
        try {
            enableCrashReporting(true)
        } catch (e: Exception) {
            Log.e("WebexModule", "buildCrashEnabledWebex exception: ${e.printStackTrace()}")
        }
    }
}

val webexModule = module(createdAtStart = true) {
    single { WebexRepository(get()) }
    single { RingerManager(get()) }

    viewModel {
        WebexViewModel(get(), get())
    }
}