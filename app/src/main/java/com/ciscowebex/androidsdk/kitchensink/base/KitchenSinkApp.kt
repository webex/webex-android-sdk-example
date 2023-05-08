package com.ciscowebex.androidsdk.kitchensink.base

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitcompat.SplitCompatApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

const val PROVIDER_CLASS = "com.ciscowebex.androidsdk.kitchensink.DynamicModuleProvider"
class KitchenSinkApp : SplitCompatApplication(), LifecycleObserver {



    companion object {
        lateinit var dmProvider: IDynamicModule
        lateinit var instance: KitchenSinkApp
            private set

        fun applicationContext(): Context {
            return instance.applicationContext
        }

        fun get(): KitchenSinkApp {
            return instance
        }

        var inForeground: Boolean = false


        // App level boolean to keep track of if the CUCM login is of type SSO Login
        var isUCSSOLogin = false

        var isKoinModulesLoaded : Boolean = false

    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@KitchenSinkApp)
        }
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        instance = this
        SplitCompat.install(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // app moved to foreground
        inForeground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        // app moved to background
        inForeground = false
    }

    fun closeApplication() {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    fun loadModules(): Boolean {
        return dmProvider.loadModules(this@KitchenSinkApp)

    }
    fun loadKoinModules(type: Any) {
        dmProvider.loadKoinModules(type)
        isKoinModulesLoaded = true
    }

    fun unloadKoinModules() {
        dmProvider.unloadKoinModules()
        isKoinModulesLoaded = false
    }
}