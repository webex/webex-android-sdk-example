package com.ciscowebex.androidsdk.kitchensink.search

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val searchModule = module {
    viewModel {
        SearchViewModel(get(), get(), get())
    }
    single { SearchRepository(get()) }
}