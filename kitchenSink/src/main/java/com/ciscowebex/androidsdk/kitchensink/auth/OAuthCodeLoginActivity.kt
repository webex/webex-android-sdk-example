package com.ciscowebex.androidsdk.kitchensink.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.kitchensink.HomeActivity
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkApp
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityLoginWithTokenBinding
import org.koin.android.viewmodel.ext.android.viewModel

class OAuthCodeLoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginWithTokenBinding
    private val loginViewModel: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityLoginWithTokenBinding>(this, R.layout.activity_login_with_token)
                .also { binding = it }
                .apply {
                    title.text = getString(R.string.login_oauth_code)
                    progressLayout.visibility = View.VISIBLE

                    loginViewModel.isAuthorized.observe(this@OAuthCodeLoginActivity, Observer { isAuthorized ->
                        progressLayout.visibility = View.GONE
                        isAuthorized?.let {
                            if (it) {
                                onLoggedIn()
                            } else {
                                onLoginFailed()
                            }
                        }
                    })

                    loginViewModel.isAuthorizedCached.observe(this@OAuthCodeLoginActivity, Observer { isAuthorizedCached ->
                        progressLayout.visibility = View.GONE
                        isAuthorizedCached?.let {
                            if (it) {
                                onLoggedIn()
                            } else {
                                loginButton.visibility = View.VISIBLE
                                jwtTokenText.visibility = View.VISIBLE
                                loginFailedTextView.visibility = View.GONE
                            }
                        }
                    })

                    loginButton.setOnClickListener {
                        if (jwtTokenText.text.isNullOrEmpty())
                            return@setOnClickListener

                        appBarLayout.visibility = View.GONE
                        loginButton.visibility = View.GONE
                        loginFailedTextView.visibility = View.GONE
                        progressLayout.visibility = View.VISIBLE

                        loginViewModel.authorizeOAuthCode(jwtTokenText.text.toString().trim())
                    }

                    loginViewModel.attemptToLoginWithCachedUser()
                }
    }

    override fun onBackPressed() {
        (application as KitchenSinkApp).closeApplication()
    }

    private fun onLoggedIn() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun onLoginFailed() {
        binding.loginButton.visibility = View.VISIBLE
        binding.loginFailedTextView.visibility = View.VISIBLE
    }
}