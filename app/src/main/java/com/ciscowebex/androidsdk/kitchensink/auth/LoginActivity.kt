package com.ciscowebex.androidsdk.kitchensink.auth

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkApp
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityLoginBinding
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils.clearEmailPref
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils.getLoginTypePref
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils.saveEmailPref
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogForInputEmail

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    enum class LoginType(var value: String) {
        OAuth("OAuth"),
        JWT("JWT"),
        AccessToken("AccessToken")
    }

    private var loginTypeCalled = LoginType.OAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
                .also { binding = it }
                .apply {

                    val type = getLoginTypePref(this@LoginActivity)

                    when (type) {
                        LoginType.JWT.value -> {
                            loginTypeCalled = LoginType.JWT
                            (application as KitchenSinkApp).loadKoinModules(loginTypeCalled)
                            startActivity(Intent(this@LoginActivity, JWTLoginActivity::class.java))
                            finish()
                        }
                        LoginType.AccessToken.value -> {
                            loginTypeCalled = LoginType.AccessToken
                            (application as KitchenSinkApp).loadKoinModules(loginTypeCalled)
                            startActivity(Intent(this@LoginActivity, AccessTokenLoginActivity::class.java))
                            finish()
                        }
                        LoginType.OAuth.value -> {
                            loginTypeCalled = LoginType.OAuth
                            (application as KitchenSinkApp).loadKoinModules(loginTypeCalled)
                            startActivity(Intent(this@LoginActivity, OAuthWebLoginActivity::class.java))
                            finish()
                        }
                    }

                    btnJwtLogin.setOnClickListener {
                        buttonClicked(LoginType.JWT)
                    }

                    btnOauthLogin.setOnClickListener {
                        buttonClicked(LoginType.OAuth)
                    }

                    btnAccessLogin.setOnClickListener {
                        buttonClicked(LoginType.AccessToken)
                    }
                }
    }

    private fun buttonClicked(type: LoginType) {
        loginTypeCalled = type
        toggleButtonsVisibility(true)

        when (type) {
            LoginType.JWT -> {
                startJWTActivity()
            }
            LoginType.OAuth -> {
                showEmailDialog(type)
            }
            LoginType.AccessToken -> {
                startAccessTokenActivity()
            }
        }
    }

    private fun toggleButtonsVisibility(hide: Boolean) {
        if (hide) {
            binding.loginButtonLayout.visibility = View.GONE
            binding.loginFailedTextView.visibility = View.GONE
            binding.btnJwtLogin.visibility = View.GONE
        } else {
            binding.loginButtonLayout.visibility = View.VISIBLE
            binding.loginFailedTextView.visibility = View.GONE
            binding.btnJwtLogin.visibility = View.VISIBLE
        }
    }

    private fun startOAuthActivity() {
        (application as KitchenSinkApp).loadKoinModules(loginTypeCalled)
        startActivity(Intent(this@LoginActivity, OAuthWebLoginActivity::class.java))
        finish()
    }

    private fun startJWTActivity() {
        (application as KitchenSinkApp).loadKoinModules(loginTypeCalled)
        startActivity(Intent(this@LoginActivity, JWTLoginActivity::class.java))
        finish()
    }

    private fun startAccessTokenActivity() {
        (application as KitchenSinkApp).loadKoinModules(loginTypeCalled)
        startActivity(Intent(this@LoginActivity, AccessTokenLoginActivity::class.java))
        finish()
    }

    private fun showEmailDialog(type: LoginType) {
        showDialogForInputEmail(this, getString(R.string.enter_user_email_address), onPositiveButtonClick = { dialog: DialogInterface, email: String ->
            when (type) {
                LoginType.OAuth -> {
                    saveEmailPref(this, email)
                    startOAuthActivity()
                }
            }
            dialog.dismiss()
        }, onNegativeButtonClick = { dialog: DialogInterface, _: Int ->
            clearEmailPref(this)
            toggleButtonsVisibility(false)
            dialog.dismiss()
        })
    }
}