package com.ciscowebex.androidsdk.kitchensink.person

import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val personModule = module {
    viewModel { PersonViewModel(get()) }

    single { PersonRepository(get()) }
}