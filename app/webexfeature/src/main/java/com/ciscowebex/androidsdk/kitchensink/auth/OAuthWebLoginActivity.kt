package com.ciscowebex.androidsdk.kitchensink.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.kitchensink.HomeActivity
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkApp
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexViewModel
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.google.android.play.core.splitcompat.SplitCompat
import org.koin.androidx.viewmodel.ext.android.viewModel

class OAuthWebLoginActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
    }

    // View references (using findViewById due to DataBinding issues with dynamic feature modules in AGP 8+)
    private lateinit var progressLayout: View
    private lateinit var appBarLayout: View
    private lateinit var exitButton: Button
    private lateinit var loginFailedTextView: TextView
    private lateinit var loginWebview: WebView
    
    private val loginViewModel: LoginViewModel by viewModel()
    private val webexViewModel: WebexViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth)
        
        // Initialize views using findViewById
        progressLayout = findViewById(R.id.progressLayout)
        appBarLayout = findViewById(R.id.appBarLayout)
        exitButton = findViewById(R.id.exitButton)
        loginFailedTextView = findViewById(R.id.loginFailedTextView)
        loginWebview = findViewById(R.id.loginWebview)
        
        progressLayout.visibility = View.VISIBLE

        loginViewModel.isAuthorized.observe(this@OAuthWebLoginActivity, Observer { isAuthorized ->
            progressLayout.visibility = View.GONE
            isAuthorized?.let {
                if (it) {
                    onLoggedIn()
                } else {
                    onLoginFailed()
                }
            }
        })

        loginViewModel.isAuthorizedCached.observe(this@OAuthWebLoginActivity, Observer { isAuthorizedCached ->
            progressLayout.visibility = View.GONE
            isAuthorizedCached?.let {
                if (it) {
                    onLoggedIn()
                } else {
                    appBarLayout.visibility = View.GONE
                    exitButton.visibility = View.GONE
                    loginFailedTextView.visibility = View.GONE
                    loginWebview.visibility = View.VISIBLE
                    loginViewModel.authorizeOAuth(loginWebview)
                }
            }
        })

        loginViewModel.errorData.observe(this@OAuthWebLoginActivity, Observer { errorMessage ->
            onLoginFailed(errorMessage)
        })

        // Set up auth observer to handle authentication events from WebexRepository
        webexViewModel.authLiveData.observe(this@OAuthWebLoginActivity, Observer { authEvent ->
            Log.d("OAuthWebLoginActivity", "Auth event received: $authEvent")
            when (authEvent) {
                Constants.Callbacks.LOGIN_FAILED -> {
                    Log.d("OAuthWebLoginActivity", "Login failed event received")
                    onLoginFailed()
                }
            }
        })

        exitButton.setOnClickListener {
            // close application as user needs to reload koin modules, currently unloading and reloading of koin modules doesn't work
            (application as KitchenSinkApp).closeApplication()
        }

        loginViewModel.initialize()
    }

    override fun onBackPressed() {
        (application as KitchenSinkApp).closeApplication()
    }

    private fun onLoggedIn() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun onLoginFailed(failureMessage: String = getString(R.string.login_failed)) {
        Log.d("auth : ", "onLoginFailed, updating ui")
        loginWebview.visibility = View.GONE
        appBarLayout.visibility = View.VISIBLE
        exitButton.visibility = View.VISIBLE
        loginFailedTextView.visibility = View.VISIBLE
        loginFailedTextView.text = failureMessage
    }
}