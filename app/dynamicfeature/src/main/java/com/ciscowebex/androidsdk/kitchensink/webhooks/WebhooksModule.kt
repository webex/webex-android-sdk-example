package com.ciscowebex.androidsdk.kitchensink.webhooks

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val webhooksModule = module {
    single { WebhooksRepository(get()) }

    viewModel { WebhooksViewModel(get()) }
}