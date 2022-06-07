package com.ciscowebex.androidsdk.kitchensink.messaging

import com.ciscowebex.androidsdk.kitchensink.messaging.composer.MessageComposerRepository
import com.ciscowebex.androidsdk.kitchensink.messaging.composer.MessageComposerViewModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpacesRepository
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpacesViewModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.detail.MessageViewModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.detail.SpaceDetailViewModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.MembershipRepository
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.MembershipViewModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.membersReadStatus.MembershipReadStatusViewModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.readStatusDetails.SpaceReadStatusDetailViewModel
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.TeamsRepository
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.TeamsViewModel
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.detail.TeamDetailViewModel
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.membership.TeamMembershipRepository
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.membership.TeamMembershipViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val messagingModule = module {
    viewModel { TeamsViewModel(get(), get()) }
    viewModel { TeamDetailViewModel(get()) }
    viewModel { TeamMembershipViewModel(get()) }

    single { TeamsRepository(get()) }


    viewModel { SpacesViewModel(get(), get(), get(), get()) }
    viewModel { SpaceDetailViewModel(get(), get(), get()) }

    single { SpacesRepository(get()) }

    viewModel { MembershipViewModel(get(), get()) }

    single { MembershipRepository(get()) }

    single { TeamMembershipRepository(get()) }

    viewModel { SpaceReadStatusDetailViewModel(get()) }

    viewModel { MessageViewModel(get()) }

    viewModel { MembershipReadStatusViewModel(get(), get()) }

    single { MessageComposerRepository(get()) }

    viewModel { MessageComposerViewModel(get(), get(), get()) }
}