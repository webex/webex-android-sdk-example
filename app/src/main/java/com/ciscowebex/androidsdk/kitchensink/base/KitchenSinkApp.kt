package com.ciscowebex.androidsdk.kitchensink.base

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Application class for the Kitchen Sink app.
 * 
 * This class extends SplitCompatApplication to support dynamic feature modules.
 * It delegates Koin module loading to the dynamic feature module via the IDynamicModule interface.
 */
class KitchenSinkApp : SplitCompatApplication(), DefaultLifecycleObserver {

    companion object {
        /**
         * Provider for dynamic module functionality.
         * Initialized when the dynamic feature module is installed.
         */
        lateinit var dmProvider: IDynamicModule
        
        /**
         * Flag indicating whether the Webex dynamic feature module is installed.
         */
        var isWebexSplitInstalled = false
        
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

        var isKoinModulesLoaded: Boolean = false
    }

    override fun onCreate() {
        super<SplitCompatApplication>.onCreate()
        FirebaseApp.initializeApp(this)

        startKoin {
            androidLogger()
            androidContext(this@KitchenSinkApp)
        }
        
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        instance = this
        // Enable SplitCompat for dynamic feature module support
        SplitCompat.install(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        // App moved to foreground
        inForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        // App moved to background
        inForeground = false
    }

    fun closeApplication() {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    /**
     * Attempts to load Koin modules based on saved login type.
     * Delegates to the dynamic module provider.
     * 
     * @return true if modules were loaded, false if no saved login type exists
     */
    fun loadModules(): Boolean {
        if (!isWebexSplitInstalled) {
            return false
        }
        return dmProvider.loadModules(this@KitchenSinkApp)
    }

    /**
     * Loads Koin modules for the specified login type.
     * Delegates to the dynamic module provider.
     * 
     * @param type The login type to load modules for
     */
    fun loadKoinModules(type: Any) {
        if (!isWebexSplitInstalled) {
            return
        }
        dmProvider.loadKoinModules(type)
        isKoinModulesLoaded = true
    }

    /**
     * Unloads all Koin modules.
     * Delegates to the dynamic module provider.
     */
    fun unloadKoinModules() {
        if (!isWebexSplitInstalled) {
            return
        }
        dmProvider.unloadKoinModules()
        isKoinModulesLoaded = false
    }
}
