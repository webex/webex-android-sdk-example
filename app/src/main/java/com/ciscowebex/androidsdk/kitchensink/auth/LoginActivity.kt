package com.ciscowebex.androidsdk.kitchensink.auth

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkApp
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexViewModel
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityLoginBinding
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils.clearEmailPref
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils.getLoginTypePref
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils.saveEmailPref
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogForInputEmail
import com.ciscowebex.androidsdk.utils.AppConfiguration
import com.ciscowebex.androidsdk.utils.SettingsStore
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private val webexViewModel: WebexViewModel by viewModel()

    enum class LoginType(var value: String) {
        OAuth("OAuth"),
        JWT("JWT"),
        AccessToken("AccessToken")
    }

    private var loginTypeCalled = LoginType.OAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppConfiguration.setContext(applicationContext)
        DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
                .also { binding = it }
                .apply {

                    val type = getLoginTypePref(this@LoginActivity)
                    loadModules(type)

                    btnJwtLogin.setOnClickListener {
                        buttonClicked(LoginType.JWT)
                    }

                    btnOauthLogin.setOnClickListener {
                        buttonClicked(LoginType.OAuth)
                    }

                    btnAccessLogin.setOnClickListener {
                        buttonClicked(LoginType.AccessToken)
                    }

                    if(AppConfiguration.containsFedRampRestrictions()) {
                        fedrampToggle.isChecked = SettingsStore.isFedRAMPEmployee()
                        fedrampToggle.isClickable = false
                        fedrampToggle.alpha = 0.4f
                    } else {
                        fedrampToggle.isChecked = SharedPrefUtils.getFedrampPref(applicationContext)
                    }

                    fedrampToggle.setOnCheckedChangeListener { _, isChecked ->
                        SharedPrefUtils.saveFedrampPref(applicationContext, isChecked)
                        loadModules(type)
                    }
                }
    }

    private fun loadModules(type: String?) {
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
        enableBackgroundConnection()
        startActivity(Intent(this@LoginActivity, OAuthWebLoginActivity::class.java))
        finish()
    }

    private fun startJWTActivity() {
        (application as KitchenSinkApp).loadKoinModules(loginTypeCalled)
        enableBackgroundConnection()
        startActivity(Intent(this@LoginActivity, JWTLoginActivity::class.java))
        finish()
    }

    private fun startAccessTokenActivity() {
        (application as KitchenSinkApp).loadKoinModules(loginTypeCalled)
        enableBackgroundConnection()
        startActivity(Intent(this@LoginActivity, AccessTokenLoginActivity::class.java))
        finish()
    }

    private fun enableBackgroundConnection() {
        webexViewModel.enableBackgroundConnection(webexViewModel.enableBgConnectiontoggle)
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