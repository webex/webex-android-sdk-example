package com.ciscowebex.androidsdk.kitchensink.base

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.reflect.full.createInstance

private const val TAG = "KitchenSinkBaseFCM"
private const val FCM_HELPER_CLASS = FCM_PROVIDER_CLASS

/**
 * Base FCM service that runs in the base app module.
 * This service receives all Firebase Cloud Messaging events and delegates them
 * to the dynamic feature module's FCM helper when the module is installed.
 * 
 * This architecture ensures that push notifications can be received even when
 * the app is killed, as the base module is always available.
 */
class KitchenSinkBaseFCMService : FirebaseMessagingService() {

    private var fcmProvider: IDynamicModule.IFCMHelper? = null

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "onMessageReceived from: ${remoteMessage.from}")
        
        if (KitchenSinkApp.isWebexSplitInstalled) {
            Log.d(TAG, "onMessageReceived: Webex module installed, delegating to feature module")
            
            if (fcmProvider == null) {
                try {
                    Log.d(TAG, "onMessageReceived: Creating FCM provider instance")
                    fcmProvider = Class.forName(FCM_HELPER_CLASS).kotlin.createInstance() as IDynamicModule.IFCMHelper
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create FCM provider: ${e.message}")
                    return
                }
            }
            
            fcmProvider?.onMessageReceived(this, remoteMessage)
        } else {
            Log.d(TAG, "onMessageReceived: Webex module not installed yet, ignoring message")
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "onNewToken: $token")
        
        if (KitchenSinkApp.isWebexSplitInstalled) {
            Log.d(TAG, "onNewToken: Webex module installed, delegating to feature module")
            
            if (fcmProvider == null) {
                try {
                    Log.d(TAG, "onNewToken: Creating FCM provider instance")
                    fcmProvider = Class.forName(FCM_HELPER_CLASS).kotlin.createInstance() as IDynamicModule.IFCMHelper
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create FCM provider: ${e.message}")
                    return
                }
            }
            
            fcmProvider?.onNewToken(this, token)
        } else {
            Log.d(TAG, "onNewToken: Webex module not installed yet, token will be set on next launch")
        }
    }
}
