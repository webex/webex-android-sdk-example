package com.ciscowebex.androidsdk.kitchensink.extras

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val extrasModule = module {
    viewModel { ExtrasViewModel(get()) }

    single { ExtrasRepository(get()) }
}