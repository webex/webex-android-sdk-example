package com.ciscowebex.androidsdk.kitchensink.calling

import com.ciscowebex.androidsdk.kitchensink.calling.captions.ClosedCaptionsRepository
import com.ciscowebex.androidsdk.kitchensink.calling.captions.ClosedCaptionsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val callModule = module {
    viewModel {
        CallViewModel(get())
        ClosedCaptionsViewModel(get())
    }

    single { ClosedCaptionsRepository() }

}