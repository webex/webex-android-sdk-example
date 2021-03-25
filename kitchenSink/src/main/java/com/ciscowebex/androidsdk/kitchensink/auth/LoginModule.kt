package com.ciscowebex.androidsdk.kitchensink.auth

import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val loginModule = module {

    viewModel { LoginViewModel(get(), get()) }

    single { LoginRepository() }
}