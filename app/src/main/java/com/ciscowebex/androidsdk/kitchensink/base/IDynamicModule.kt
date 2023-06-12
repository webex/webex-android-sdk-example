package com.ciscowebex.androidsdk.kitchensink.base

import android.content.Context

interface IDynamicModule {
    fun loadModules(context: Context): Boolean
    fun loadKoinModules(type: Any)
    fun unloadKoinModules()
}