package com.ciscowebex.androidsdk.kitchensink

import android.content.Context
import com.ciscowebex.androidsdk.kitchensink.auth.LoginActivity
import com.ciscowebex.androidsdk.kitchensink.auth.loginModule
import com.ciscowebex.androidsdk.kitchensink.base.IDynamicModule
import com.ciscowebex.androidsdk.kitchensink.calling.calendarMeeting.calendarMeetingsModule
import com.ciscowebex.androidsdk.kitchensink.calling.callModule
import com.ciscowebex.androidsdk.kitchensink.extras.extrasModule
import com.ciscowebex.androidsdk.kitchensink.messaging.messagingModule
import com.ciscowebex.androidsdk.kitchensink.messaging.search.searchPeopleModule
import com.ciscowebex.androidsdk.kitchensink.person.personModule
import com.ciscowebex.androidsdk.kitchensink.search.searchModule
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils
import com.ciscowebex.androidsdk.kitchensink.webhooks.webhooksModule
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

/**
 * Provider class that implements IDynamicModule interface.
 * This class is instantiated via reflection when the dynamic feature module is installed.
 * It provides access to the feature module's Koin dependency injection modules.
 */
class DynamicModuleProvider : IDynamicModule {

    /**
     * Attempts to load Koin modules based on saved login type preference.
     * @param context Application context
     * @return true if modules were loaded successfully, false if no saved login type exists
     */
    override fun loadModules(context: Context): Boolean {
        val type = SharedPrefUtils.getLoginTypePref(context.applicationContext)
        if (type != null) {
            // Use 'this.' to call the local method, not the imported Koin function
            this.loadKoinModules(LoginActivity.LoginType.valueOf(type))
            return true
        }
        return false
    }

    /**
     * Loads Koin dependency injection modules for the specified login type.
     * @param type The login type (should be LoginActivity.LoginType)
     */
    override fun loadKoinModules(type: Any) {
        when (type) {
            LoginActivity.LoginType.JWT -> {
                org.koin.core.context.loadKoinModules(getKoinModulesForJWT())
            }
            LoginActivity.LoginType.AccessToken -> {
                org.koin.core.context.loadKoinModules(getKoinModulesForAccessToken())
            }
            else -> {
                org.koin.core.context.loadKoinModules(getKoinModulesForOAuth())
            }
        }
    }

    /**
     * Unloads all Koin dependency injection modules.
     */
    override fun unloadKoinModules() {
        unloadKoinModules(getAllKoinModules())
    }

    private fun getKoinModulesForJWT() = listOf(
        mainAppModule,
        webexModule,
        loginModule,
        JWTWebexModule,
        searchModule,
        callModule,
        messagingModule,
        personModule,
        searchPeopleModule,
        webhooksModule,
        extrasModule,
        calendarMeetingsModule
    )

    private fun getKoinModulesForAccessToken() = listOf(
        mainAppModule,
        webexModule,
        loginModule,
        AccessTokenWebexModule,
        searchModule,
        callModule,
        messagingModule,
        personModule,
        searchPeopleModule,
        webhooksModule,
        extrasModule,
        calendarMeetingsModule
    )

    private fun getKoinModulesForOAuth() = listOf(
        mainAppModule,
        webexModule,
        loginModule,
        OAuthWebexModule,
        searchModule,
        callModule,
        messagingModule,
        personModule,
        searchPeopleModule,
        webhooksModule,
        extrasModule,
        calendarMeetingsModule
    )

    private fun getAllKoinModules() = listOf(
        mainAppModule,
        webexModule,
        loginModule,
        JWTWebexModule,
        AccessTokenWebexModule,
        OAuthWebexModule,
        searchModule,
        callModule,
        messagingModule,
        personModule,
        searchPeopleModule,
        webhooksModule,
        extrasModule,
        calendarMeetingsModule
    )
}
