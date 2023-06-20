package com.ciscowebex.androidsdk.kitchensink.messaging.search

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val searchPeopleModule = module {
    viewModel { SearchPeopleViewModel(get()) }
}