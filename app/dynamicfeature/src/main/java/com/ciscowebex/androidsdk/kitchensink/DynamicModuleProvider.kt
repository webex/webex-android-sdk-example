package com.ciscowebex.androidsdk.kitchensink

import android.content.Context
import com.ciscowebex.androidsdk.kitchensink.base.IDynamicModule
import com.ciscowebex.androidsdk.kitchensink.auth.LoginActivity
import com.ciscowebex.androidsdk.kitchensink.auth.loginModule
import com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting.calendarMeetingsModule
import com.ciscowebex.androidsdk.kitchensink.calling.callModule
import com.ciscowebex.androidsdk.kitchensink.extras.extrasModule
import com.ciscowebex.androidsdk.kitchensink.messaging.messagingModule
import com.ciscowebex.androidsdk.kitchensink.messaging.search.searchPeopleModule
import com.ciscowebex.androidsdk.kitchensink.person.personModule
import com.ciscowebex.androidsdk.kitchensink.search.searchModule
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils
import com.ciscowebex.androidsdk.kitchensink.webhooks.webhooksModule
import org.koin.core.context.unloadKoinModules


class DynamicModuleProvider: IDynamicModule {
    override fun loadModules(context: Context): Boolean {
        val type = SharedPrefUtils.getLoginTypePref(context.applicationContext)
        if(type != null) {
            loadKoinModules(LoginActivity.LoginType.valueOf(type))
            return true
        }
        return false
    }
    override fun loadKoinModules(type: Any) {
        when (type) {
            LoginActivity.LoginType.JWT -> {
                org.koin.core.context.loadKoinModules(listOf(mainAppModule, webexModule, loginModule, JWTWebexModule, searchModule, callModule, messagingModule, personModule, searchPeopleModule, webhooksModule, extrasModule, calendarMeetingsModule))
            }
            LoginActivity.LoginType.AccessToken -> {
                org.koin.core.context.loadKoinModules(listOf(mainAppModule, webexModule, loginModule, AccessTokenWebexModule, searchModule, callModule, messagingModule, personModule, searchPeopleModule, webhooksModule, extrasModule, calendarMeetingsModule))
            }
            else -> {
                org.koin.core.context.loadKoinModules(listOf(mainAppModule, webexModule, loginModule, OAuthWebexModule, searchModule, callModule, messagingModule, personModule, searchPeopleModule, webhooksModule, extrasModule, calendarMeetingsModule))
            }
        }
    }

    override fun unloadKoinModules() {
        unloadKoinModules(listOf(mainAppModule, webexModule, loginModule, JWTWebexModule, AccessTokenWebexModule, OAuthWebexModule, searchModule, callModule, messagingModule, personModule, searchPeopleModule, webhooksModule, extrasModule, calendarMeetingsModule))
    }
}