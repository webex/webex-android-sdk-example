package com.ciscowebex.androidsdk.kitchensink.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ciscowebex.androidsdk.kitchensink.base.R
import com.ciscowebex.androidsdk.kitchensink.base.KitchenSinkApp.Companion.dmProvider
import com.ciscowebex.androidsdk.kitchensink.base.KitchenSinkApp.Companion.isWebexSplitInstalled
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitinstall.SplitInstallHelper
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlin.reflect.full.createInstance

private const val TAG = "SplashActivity"
private const val LOGIN_ACTIVITY_PACKAGE = "com.ciscowebex.androidsdk.kitchensink.auth"
private const val LOGIN_ACTIVITY_CLASS = "$LOGIN_ACTIVITY_PACKAGE.LoginActivity"

/**
 * Splash screen activity that handles on-demand installation of the dynamic feature module.
 * This is the launcher activity that checks if the Webex feature module is installed,
 * downloads it if necessary, and then launches the LoginActivity.
 */
class SplashActivity : Activity() {

    private var isSplitInstalledChecked = false
    private val moduleName = "webexfeature"
    private lateinit var manager: SplitInstallManager

    private lateinit var statusText: TextView
    private lateinit var progress: ProgressBar

    /** Listener used to handle changes in state for install requests. */
    private val listener = SplitInstallStateUpdatedListener { state ->
        val multiInstall = state.moduleNames().size > 1
        val names = state.moduleNames().joinToString(" - ")
        
        when (state.status()) {
            SplitInstallSessionStatus.DOWNLOADING -> {
                // In order to see this, the application has to be uploaded to the Play Store.
                displayLoadingState(state, "Downloading $names")
            }
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                /*
                 * This may occur when attempting to download a sufficiently large module.
                 * In order to see this, the application has to be uploaded to the Play Store.
                 * Then features can be requested until the confirmation path is triggered.
                 */
                try {
                    startIntentSender(state.resolutionIntent()?.intentSender, null, 0, 0, 0)
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting resolution intent: ${e.message}")
                    toastAndLog("Error: Could not start confirmation dialog")
                }
            }
            SplitInstallSessionStatus.INSTALLED -> {
                onSuccessfulLoad(names, launch = !multiInstall)
            }
            SplitInstallSessionStatus.INSTALLING -> {
                displayLoadingState(state, "Installing $names")
            }
            SplitInstallSessionStatus.FAILED -> {
                toastAndLog("Error: ${state.errorCode()} for module ${state.moduleNames()}")
            }
            SplitInstallSessionStatus.PENDING -> {
                displayLoadingState(state, "Pending installation...")
            }
            SplitInstallSessionStatus.CANCELING -> {
                displayLoadingState(state, "Canceling...")
            }
            SplitInstallSessionStatus.CANCELED -> {
                toastAndLog("Installation canceled")
            }
            else -> {
                Log.d(TAG, "Unknown status: ${state.status()}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)
        manager = SplitInstallManagerFactory.create(this)

        // Set up an OnPreDrawListener to the root view to keep splash screen visible
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (isSplitInstalledChecked) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        false
                    }
                }
            }
        )

        statusText = findViewById(R.id.statusText)
        statusText.setOnClickListener { loadAndLaunchModule(moduleName) }
        progress = findViewById(R.id.installationProgress)

        // Post split check on main thread to free up onCreate
        Handler(Looper.getMainLooper()).post { loadAndLaunchModule(moduleName) }
    }

    override fun onResume() {
        super.onResume()
        manager.registerListener(listener)
    }

    override fun onPause() {
        manager.unregisterListener(listener)
        super.onPause()
    }

    private fun loadAndLaunchModule(name: String) {
        updateProgressMessage("Loading module $name")
        
        // Skip loading if the module is already installed
        if (manager.installedModules.contains(name)) {
            updateProgressMessage("Module already installed")
            onSuccessfulLoad(name, launch = true)
            return
        }

        // Create request to install the feature module
        val request = SplitInstallRequest.newBuilder()
            .addModule(name)
            .build()

        // Load and install the requested feature module
        manager.startInstall(request)
            .addOnSuccessListener { sessionId ->
                Log.d(TAG, "Install request started, session ID: $sessionId")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Install failed: ${exception.message}")
                toastAndLog("Failed to start installation: ${exception.message}")
            }

        updateProgressMessage("Starting install for $name")
        isSplitInstalledChecked = true
    }

    private fun onSuccessfulLoad(moduleName: String, launch: Boolean) {
        if (!manager.installedModules.contains(moduleName)) {
            toastAndLog("Module not installed yet")
            return
        }

        try {
            // IMPORTANT: include code so the classloader has split dex paths
            val splitContext = createPackageContext(
                packageName,
                Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY
            )
            Log.d(TAG, "installedModules=${manager.installedModules}")
            Log.d(TAG, "splitSourceDirs=${splitContext.applicationInfo.splitSourceDirs?.joinToString()}")

            // Make sure split paths are visible to the current process/component
            // Update app info for THIS context so splitSourceDirs includes the feature split
            /*SplitInstallHelper.updateAppInfo(splitContext)
            SplitCompat.installActivity(splitContext)*/

            // After module is installed
            SplitInstallHelper.updateAppInfo(this)
            SplitCompat.installActivity(this)
            // Now load classes using the Activity classloader
            dmProvider = Class.forName(PROVIDER_CLASS, true, this.classLoader)
                .kotlin.createInstance() as IDynamicModule

            Log.d(TAG, "splitSourceDirs=${splitContext.applicationInfo.splitSourceDirs?.joinToString()}")
            Log.d(TAG, "applicationInfo.splitSourceDirs=${this.applicationInfo.splitSourceDirs?.joinToString()}")

            //loadNativeLibraries(splitContext)
            // Update the app's library paths to include the split APK's native libraries
            //dmProvider = splitContext.classLoader.loadClass(PROVIDER_CLASS).kotlin.createInstance() as IDynamicModule
            Thread {
                //dmProvider.loadNativeLibraries(splitContext)

                runOnUiThread {
                    if (launch) {
                        when (moduleName) {
                            this.moduleName -> launchActivity(LOGIN_ACTIVITY_CLASS)
                        }
                    }
                    hideProgress()
                    finish()
                }
            }.start()

            isWebexSplitInstalled = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load dynamic module provider: ${e.message}")
            toastAndLog("Failed to initialize module: ${e.message}")
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
    }

    /** Launch an activity by its class name. */
    private fun launchActivity(className: String) {
        try {
            val clazz = Class.forName(className)
            Intent(this, clazz).also { startActivity(it) }
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "Could not find class: $className")
            toastAndLog("Could not launch activity: ${e.message}")
        }
    }

    /** Display a loading state to the user. */
    private fun displayLoadingState(state: SplitInstallSessionState, message: String) {
        showProgress()
        progress.max = state.totalBytesToDownload().toInt()
        progress.progress = state.bytesDownloaded().toInt()
        updateProgressMessage(message)
    }

    private fun updateProgressMessage(message: String) {
        if (progress.visibility != View.VISIBLE) showProgress()
        statusText.text = message
    }

    private fun showProgress() {
        progress.visibility = View.VISIBLE
        statusText.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress.visibility = View.GONE
    }

    private fun toastAndLog(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        Log.d(TAG, text)
    }
}
