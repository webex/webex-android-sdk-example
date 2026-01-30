package com.ciscowebex.androidsdk.kitchensink.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.kitchensink.HomeActivity
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkApp
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexViewModel
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogWithMessage
import com.google.android.play.core.splitcompat.SplitCompat
import org.koin.androidx.viewmodel.ext.android.viewModel

class JWTLoginActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
    }

    // View references (using findViewById due to DataBinding issues with dynamic feature modules in AGP 8+)
    private lateinit var titleView: TextView
    private lateinit var progressLayout: View
    private lateinit var loginButton: Button
    private lateinit var tokenText: EditText
    private lateinit var loginFailedTextView: TextView
    
    private val loginViewModel: LoginViewModel by viewModel()
    private val webexViewModel: WebexViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_with_token)
        
        // Initialize views using findViewById
        titleView = findViewById(R.id.title)
        progressLayout = findViewById(R.id.progressLayout)
        loginButton = findViewById(R.id.loginButton)
        tokenText = findViewById(R.id.tokenText)
        loginFailedTextView = findViewById(R.id.loginFailedTextView)
        
        titleView.text = getString(R.string.login_jwt)
        progressLayout.visibility = View.VISIBLE
        
        loginButton.setOnClickListener {
            loginFailedTextView.visibility = View.GONE
            if (tokenText.text.isEmpty()) {
                showDialogWithMessage(this@JWTLoginActivity, R.string.error_occurred, resources.getString(R.string.login_token_empty_error))
            } else {
                loginButton.visibility = View.GONE
                progressLayout.visibility = View.VISIBLE
                val token = tokenText.text.toString()
                loginViewModel.loginWithJWT(token)
            }
        }

        loginViewModel.isAuthorized.observe(this@JWTLoginActivity, Observer { isAuthorized ->
            progressLayout.visibility = View.GONE
            isAuthorized?.let {
                if (it) {
                    onLoggedIn()
                } else {
                    onLoginFailed()
                }
            }
        })

        loginViewModel.isAuthorizedCached.observe(this@JWTLoginActivity, Observer { isAuthorizedCached ->
            progressLayout.visibility = View.GONE
            isAuthorizedCached?.let {
                if (it) {
                    onLoggedIn()
                } else {
                    tokenText.visibility = View.VISIBLE
                    loginButton.visibility = View.VISIBLE
                    loginFailedTextView.visibility = View.GONE
                }
            }
        })

        loginViewModel.errorData.observe(this@JWTLoginActivity, Observer { errorMessage ->
            progressLayout.visibility = View.GONE
            onLoginFailed(errorMessage)
        })

        // Set up auth observer to handle authentication events from WebexRepository
        webexViewModel.authLiveData.observe(this@JWTLoginActivity, Observer { authEvent ->
            android.util.Log.d("JWTLoginActivity", "Auth event received: $authEvent")
            when (authEvent) {
                Constants.Callbacks.LOGIN_FAILED -> {
                    android.util.Log.d("JWTLoginActivity", "Login failed event received")
                    progressLayout.visibility = View.GONE
                    onLoginFailed()
                }
            }
        })

        loginViewModel.initialize()
    }

    override fun onBackPressed() {
        (application as KitchenSinkApp).closeApplication()
    }

    private fun onLoggedIn() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun onLoginFailed(failureMessage: String = getString(R.string.jwt_login_failed)) {
        loginButton.visibility = View.VISIBLE
        loginFailedTextView.visibility = View.VISIBLE
        loginFailedTextView.text = failureMessage
    }
}