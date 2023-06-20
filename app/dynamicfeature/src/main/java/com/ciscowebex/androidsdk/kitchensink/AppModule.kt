package com.ciscowebex.androidsdk.kitchensink

import com.ciscowebex.androidsdk.kitchensink.base.utils.PermissionsHelper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val mainAppModule = module {
    single { PermissionsHelper(androidContext()) }
}