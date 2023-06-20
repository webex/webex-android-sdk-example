package com.ciscowebex.androidsdk.kitchensink

import com.ciscowebex.androidsdk.kitchensink.calling.RingerManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val webexModule = module(createdAtStart = true) {
    single { WebexRepository(get()) }
    single { RingerManager(get()) }

    viewModel {
        WebexViewModel(get(), get())
    }
}