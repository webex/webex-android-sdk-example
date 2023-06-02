package com.ciscowebex.androidsdk.kitchensink.calling

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val callModule = module {
    viewModel {
        CallViewModel(get())
    }
}