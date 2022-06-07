package com.ciscowebex.androidsdk.kitchensink.webhooks

import com.ciscowebex.androidsdk.kitchensink.person.PersonRepository
import com.ciscowebex.androidsdk.kitchensink.person.PersonViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val webhooksModule = module {
    single { WebhooksRepository(get()) }

    viewModel { WebhooksViewModel(get()) }
}