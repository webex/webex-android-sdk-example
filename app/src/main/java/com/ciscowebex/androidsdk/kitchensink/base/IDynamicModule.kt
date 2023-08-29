package com.ciscowebex.androidsdk.kitchensink.base

import android.content.Context
import com.google.firebase.messaging.RemoteMessage

interface IDynamicModule {
    fun loadModules(context: Context): Boolean
    fun loadKoinModules(type: Any)
    fun unloadKoinModules()
    interface IFCMHelper {
        fun onMessageReceived(context: Context, remoteMessage: RemoteMessage)
        fun onNewToken(context: Context, token: String)
    }
}