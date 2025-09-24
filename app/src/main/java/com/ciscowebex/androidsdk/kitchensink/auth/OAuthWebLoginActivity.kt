package com.ciscowebex.androidsdk.kitchensink.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.kitchensink.HomeActivity
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkApp
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexViewModel
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityOauthBinding
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import org.koin.androidx.viewmodel.ext.android.viewModel

class OAuthWebLoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityOauthBinding
    private val loginViewModel: LoginViewModel by viewModel()
    private val webexViewModel: WebexViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityOauthBinding>(this, R.layout.activity_oauth)
                .also { binding = it }
                .apply {
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
                                binding.exitButton.visibility = View.GONE
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
        binding.loginWebview.visibility = View.GONE
        binding.appBarLayout.visibility = View.VISIBLE
        binding.exitButton.visibility = View.VISIBLE
        binding.loginFailedTextView.visibility = View.VISIBLE
        binding.loginFailedTextView.text = failureMessage
    }
}