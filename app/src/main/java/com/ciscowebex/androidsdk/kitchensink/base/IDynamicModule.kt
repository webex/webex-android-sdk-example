package com.ciscowebex.androidsdk.kitchensink.base

import android.content.Context
import com.google.firebase.messaging.RemoteMessage

/**
 * Interface for communication between the base app module and the dynamic feature module.
 * The dynamic feature module implements this interface to provide its functionality
 * to the base app without requiring compile-time dependencies.
 */
interface IDynamicModule {
    /**
     * Attempts to load Koin modules based on saved login type preference.
     * @param context Application context
     * @return true if modules were loaded successfully, false if no saved login type exists
     */
    fun loadModules(context: Context): Boolean

    /**
     * Loads Koin dependency injection modules for the specified login type.
     * @param type The login type (should be LoginActivity.LoginType)
     */
    fun loadKoinModules(type: Any)

    /**
     * Unloads all Koin dependency injection modules.
     */
    fun unloadKoinModules()

    /**
     * Interface for FCM (Firebase Cloud Messaging) helper functionality.
     * Implemented in the dynamic feature module to handle push notifications.
     */
    interface IFCMHelper {
        /**
         * Called when a push notification message is received.
         * @param context Service context
         * @param remoteMessage The received FCM message
         */
        fun onMessageReceived(context: Context, remoteMessage: RemoteMessage)

        /**
         * Called when a new FCM token is generated.
         * @param context Service context
         * @param token The new FCM token
         */
        fun onNewToken(context: Context, token: String)
    }
}
