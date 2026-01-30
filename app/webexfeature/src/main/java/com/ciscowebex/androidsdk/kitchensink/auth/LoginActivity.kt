package com.ciscowebex.androidsdk.kitchensink.auth

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkApp
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexViewModel
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils.clearEmailPref
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils.getLoginTypePref
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils.saveEmailPref
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogForInputEmail
import com.ciscowebex.androidsdk.utils.AppConfiguration
import com.ciscowebex.androidsdk.utils.SettingsStore
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.ciscowebex.androidsdk.kitchensink.base.BaseActivity as BaseBaseActivity

/**
 * LoginActivity extends the base module's BaseActivity which provides SplitCompat support.
 * webexViewModel is injected lazily (by viewModel()) and only accessed AFTER Koin modules 
 * are loaded via loadKoinModules() call.
 */
class LoginActivity : BaseBaseActivity() {
    
    // WebexViewModel is injected lazily and only used after loadKoinModules() is called
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
        
        // Use setContentView with layout inflation, then find views manually
        // DataBinding has issues with dynamic feature modules in AGP 8+
        setContentView(R.layout.activity_login)
        
        // Get views using findViewById as workaround for DataBinding issues
        val jwtBtn = findViewById<View>(R.id.btn_jwt_login)
        val oauthBtn = findViewById<View>(R.id.btn_oauth_login)
        val accessBtn = findViewById<View>(R.id.btn_access_login)
        val fedToggle = findViewById<android.widget.Switch>(R.id.fedrampToggle)
        
        val type = getLoginTypePref(this@LoginActivity)
        loadModules(type)

        jwtBtn.setOnClickListener {
            buttonClicked(LoginType.JWT)
        }

        oauthBtn.setOnClickListener {
            buttonClicked(LoginType.OAuth)
        }

        accessBtn.setOnClickListener {
            buttonClicked(LoginType.AccessToken)
        }

        if(AppConfiguration.containsFedRampRestrictions()) {
            fedToggle.isChecked = SettingsStore.isFedRAMPEmployee()
            fedToggle.isClickable = false
            fedToggle.alpha = 0.4f
        } else {
            fedToggle.isChecked = SharedPrefUtils.getFedrampPref(applicationContext)
        }

        fedToggle.setOnCheckedChangeListener { _, isChecked ->
            SharedPrefUtils.saveFedrampPref(applicationContext, isChecked)
            loadModules(type)
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
        val loginButtonLayout = findViewById<View>(R.id.loginButtonLayout)
        val loginFailedTextView = findViewById<View>(R.id.loginFailedTextView)
        val btnJwtLogin = findViewById<View>(R.id.btn_jwt_login)
        
        if (hide) {
            loginButtonLayout.visibility = View.GONE
            loginFailedTextView.visibility = View.GONE
            btnJwtLogin.visibility = View.GONE
        } else {
            loginButtonLayout.visibility = View.VISIBLE
            loginFailedTextView.visibility = View.GONE
            btnJwtLogin.visibility = View.VISIBLE
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
                else -> {}
            }
            dialog.dismiss()
        }, onNegativeButtonClick = { dialog: DialogInterface, _: Int ->
            clearEmailPref(this)
            toggleButtonsVisibility(false)
            dialog.dismiss()
        })
    }
}