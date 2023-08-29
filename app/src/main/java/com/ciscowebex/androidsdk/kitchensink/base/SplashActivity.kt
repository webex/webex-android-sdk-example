package com.ciscowebex.androidsdk.kitchensink.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bumptech.glide.Glide
import com.ciscowebex.androidsdk.kitchensink.base.KitchenSinkApp.Companion.dmProvider
import com.ciscowebex.androidsdk.kitchensink.base.KitchenSinkApp.Companion.isWebexSplitInstalled
import com.google.android.play.core.splitinstall.*
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlin.reflect.full.createInstance

private const val packageName = "com.ciscowebex.androidsdk.kitchensink.auth"
private const val loginActivityClassname = "$packageName.LoginActivity"

fun SplashActivity.toastAndLog(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    Log.d(TAG, text)
}

private const val TAG = "DynamicFeatures"

class SplashActivity : Activity() {
    private var isSplitInstalledChecked = false
    private val module by lazy { "dynamicfeature" }
    private lateinit var manager: SplitInstallManager

    private lateinit var statusText: TextView
    private lateinit var progress: ProgressBar

    /** Listener used to handle changes in state for install requests. */
    private val listener = SplitInstallStateUpdatedListener { state ->
        val multiInstall = state.moduleNames().size > 1
        val names = state.moduleNames().joinToString(" - ")
        when (state.status()) {
            SplitInstallSessionStatus.DOWNLOADING -> {
                //  In order to see this, the application has to be uploaded to the Play Store.
                displayLoadingState(state, "Downloading $names")
            }
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                /*
                  This may occur when attempting to download a sufficiently large module.

                  In order to see this, the application has to be uploaded to the Play Store.
                  Then features can be requested until the confirmation path is triggered.
                 */
                startIntentSender(state.resolutionIntent()?.intentSender, null, 0, 0, 0)
            }
            SplitInstallSessionStatus.INSTALLED -> {
                onSuccessfulLoad(names, launch = !multiInstall)
            }

            SplitInstallSessionStatus.INSTALLING -> displayLoadingState(state, "Installing $names")
            SplitInstallSessionStatus.FAILED -> {
                toastAndLog("Error: ${state.errorCode()} for module ${state.moduleNames()}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Set the layout for the content view.
        setContentView(R.layout.activity_splash)
        manager = SplitInstallManagerFactory.create(this)


        // Set up an OnPreDrawListener to the root view.
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // Check whether the initial data is ready.
                    return if (isSplitInstalledChecked) {
                        // The content is ready. Start drawing.
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        // The content isn't ready. Suspend.
                        false
                    }
                }
            }
        )

        statusText = findViewById(R.id.statusText)
        statusText.isClickable = true
        statusText.setOnClickListener() {loadAndLaunchModule(module) }
        progress = findViewById(R.id.installationProgress)
        Glide.with(this)
            .asGif()
            .load(R.raw.giphy)
            .into(findViewById<ImageView>(R.id.gifAnimationView));

        // Free up the onCreate and post split check on main thread.
        Handler().post(){ loadAndLaunchModule(module) }
    }

    override fun onResume() {
        // Listener can be registered even without directly triggering a download.
        manager.registerListener(listener)
        super.onResume()
    }

    override fun onPause() {
        // Make sure to dispose of the listener once it's no longer needed.
        manager.unregisterListener(listener)
        super.onPause()
    }

    private fun loadAndLaunchModule(name: String) {
        updateProgressMessage("Loading module $name")
        // Skip loading if the module already is installed. Perform success action directly.
        if (manager.installedModules.contains(name)) {
            updateProgressMessage("Already installed")
            onSuccessfulLoad(name, launch = true)
            return
        }

        // Create request to install a feature module by name.
        val request = SplitInstallRequest.newBuilder()
            .addModule(name)
            .build()

        // Load and install the requested feature module.
        manager.startInstall(request)

        updateProgressMessage("Starting install for $name")
        isSplitInstalledChecked = true
    }

    private fun onSuccessfulLoad(moduleName: String, launch: Boolean) {

        dmProvider = Class.forName(PROVIDER_CLASS).kotlin.createInstance() as IDynamicModule
        isWebexSplitInstalled = true

        if (launch) {
            when (moduleName) {
                module -> launchActivity(loginActivityClassname)

            }
        }

        displayButtons()
        this@SplashActivity.finish()
    }

    /** Launch an activity by its class name. */
    private fun launchActivity(className: String) {
        Intent().setClassName(packageName, className)
            .also {
                startActivity(it)
            }
    }

    /** Display a loading state to the user. */
    private fun displayLoadingState(state: SplitInstallSessionState, message: String) {
        displayProgress()

        progress.max = state.totalBytesToDownload().toInt()
        progress.progress = state.bytesDownloaded().toInt()

        updateProgressMessage(message)
    }

    private fun updateProgressMessage(message: String) {
        if (progress.visibility != View.VISIBLE) displayProgress()
        statusText.text = message
    }

    /** Display progress bar and text. */
    private fun displayProgress() {
        progress.visibility = View.VISIBLE
    }

    /** Display buttons to accept user input. */
    private fun displayButtons() {
        progress.visibility = View.GONE
    }
}