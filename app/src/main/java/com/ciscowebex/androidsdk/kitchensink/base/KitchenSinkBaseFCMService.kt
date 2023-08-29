package com.ciscowebex.androidsdk.kitchensink.base

import android.util.Log
import com.ciscowebex.androidsdk.kitchensink.base.KitchenSinkApp.Companion.isWebexSplitInstalled
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.reflect.full.createInstance

const val FCM_TAG: String = "onDemand::FCM"
class KitchenSinkBaseFCMService :  FirebaseMessagingService() {

    var fcmProvider: IDynamicModule.IFCMHelper? = null
    override fun onMessageReceived(msg: RemoteMessage) {
        Log.d(FCM_TAG, "onMessageReceived")
        if(isWebexSplitInstalled) {
            Log.d(FCM_TAG, "onMessageReceived: Webex installed")
            if(fcmProvider == null) {
                Log.d(FCM_TAG, "onMessageReceived: provider null")
                fcmProvider =
                    Class.forName(FCM_PROVIDER_CLASS).kotlin.createInstance() as IDynamicModule.IFCMHelper
            }
            Log.d(FCM_TAG, "onMessageReceived: provider memory allotted")
            fcmProvider?.onMessageReceived(this, msg)
        } else {
            Log.d(FCM_TAG, "onMessageReceived: webex split not installed yet")
        }

    }

    override fun onNewToken(token: String) {
        Log.d(FCM_TAG, "onNewToken")
        if(isWebexSplitInstalled) {
            Log.d(FCM_TAG, "onNewToken :  installed ")
            if (fcmProvider == null) {
                Log.d(FCM_TAG, "onNewToken :  fcmProvider null ")
                fcmProvider =
                    Class.forName(FCM_PROVIDER_CLASS).kotlin.createInstance() as IDynamicModule.IFCMHelper
            }

            Log.d(FCM_TAG, "onNewToken :  fcmProvider memory allotted ")
            fcmProvider?.onNewToken(this, token)
        } else {
            Log.d(FCM_TAG, "onNewToken : webex split Not installed yet")
        }
    }

}