package com.ciscowebex.androidsdk.kitchensink.cucm

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.auth.UCSSOWebViewAuthenticator
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityCucmLoginBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.DialogUcloginNonssoBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.DialogUcloginSettingsBinding
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.auth.PhoneServiceRegistrationFailureReason
import com.ciscowebex.androidsdk.auth.UCLoginServerConnectionStatus
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkApp.Companion.isUCSSOLogin
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogWithMessage
import com.ciscowebex.androidsdk.phone.Phone


class UCLoginActivity : BaseActivity() {
    lateinit var binding: ActivityCucmLoginBinding

    private var nonSSOAlertDialog: AlertDialog? = null
    private var ucSettingsAlertDialog: AlertDialog? = null

    var isUCSSOLoginSuccessful = false

    companion object {
        enum class OnActivityStartAction {
            ShowSSOLogin,
            ShowNonSSOLogin
        }

        fun getIntent(context: Context, onActivityStartAction: String? = null, ssoUrl: String = ""): Intent {
            val intent = Intent(context, UCLoginActivity::class.java)
            intent.putExtra(Constants.Intent.KEY_UC_LOGIN_PAGE_ACTION, onActivityStartAction)
            intent.putExtra(Constants.Intent.KEY_SSO_URL, ssoUrl)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "UCLoginActivity"
        DataBindingUtil.setContentView<ActivityCucmLoginBinding>(this, R.layout.activity_cucm_login)
                .also { binding = it }
                .apply {
                    // If HomeActivity starts the UC login process
                    val onActivityStartAction = intent.getStringExtra(Constants.Intent.KEY_UC_LOGIN_PAGE_ACTION)
                    onActivityStartAction?.let {
                        if (it == OnActivityStartAction.ShowSSOLogin.name) {
                            onActionShowSSOLogin(intent.getStringExtra(Constants.Intent.KEY_SSO_URL).orEmpty())
                        } else if (it == OnActivityStartAction.ShowNonSSOLogin.name) {
                            onActionShowNonSSOLogin()
                        }
                    }

                    if (webexViewModel.getCallingType() == Phone.CallingType.WebexCalling || webexViewModel.getCallingType() == Phone.CallingType.WebexForBroadworks) {
                        binding.connectphoneservices.setOnClickListener {
                            webexViewModel.connectPhoneServices{ result ->
                                if (!result.isSuccessful) {
                                    Log.d(tag, "PhoneServices connection result : ${result.error?.errorMessage}")
                                }
                            }
                        }

                        binding.disconnectphoneservices.setOnClickListener {
                            webexViewModel.disconnectPhoneServices{ result ->
                                if (!result.isSuccessful) {
                                    Log.d(tag, "PhoneServices disconnection result : ${result.error?.errorMessage}")
                                }
                            }
                        }
                    } else {
                        binding.connectphoneservices.visibility = View.GONE
                        binding.disconnectphoneservices.visibility = View.GONE
                    }

                    webexViewModel.ucLiveData.observe(this@UCLoginActivity, Observer {
                        if (it != null) {
                            when (WebexRepository.UCCallEvent.valueOf(it.first.name)) {
                                WebexRepository.UCCallEvent.ShowSSOLogin -> {
                                    onActionShowSSOLogin(it.second)
                                }
                                WebexRepository.UCCallEvent.ShowNonSSOLogin -> {
                                    onActionShowNonSSOLogin()
                                }
                                WebexRepository.UCCallEvent.OnUCLoggedIn -> {
                                    Log.d(tag, "Callback : Uc logged in")
                                    Handler(Looper.getMainLooper()).post {
                                        binding.ssologinWebview.visibility = View.GONE
                                        updateUCLoginStatusUI(getString(R.string.uc_login_success))
                                    }
                                }
                                WebexRepository.UCCallEvent.OnUCLoginFailed -> {
                                    Log.d(tag, "Callback : Uc login failed, reason: ${it.second}")
                                    Handler(Looper.getMainLooper()).post {
                                        binding.ssologinWebview.visibility = View.GONE
                                        binding.progressBar.visibility = View.GONE
                                        val failureText = "${getString(R.string.uc_login_failed)} : ${it.second}"
                                        updateUCLoginStatusUI(failureText)
                                    }
                                }
                                WebexRepository.UCCallEvent.OnUCServerConnectionStateChanged -> {
                                    Log.d(tag, "Callback : Uc server connection state changed")
                                    processServerConnectionStatus(webexViewModel.getUCServerConnectionStatus(), webexViewModel.getUCServerFailureReason())
                                }
                                WebexRepository.UCCallEvent.ShowUCSSOBrowser -> {
                                    Log.d(tag, "ShowUCSSOBrowser")
                                    ssologinWebview.visibility = View.VISIBLE
                                }
                                WebexRepository.UCCallEvent.HideUCSSOBrowser -> {
                                    Log.d(tag, "HideUCSSOBrowser")
                                    isUCSSOLoginSuccessful = true
                                    ssologinWebview.visibility = View.GONE
                                }
                                WebexRepository.UCCallEvent.OnSSOLoginFailed -> {
                                    Log.d(tag, "Callback : OnSSOLoginFailed")
                                    showDialogWithMessage(this@UCLoginActivity, getString(R.string.login_failed), "Reason : ${it.second}", R.string.retry, true,
                                        { dialog, _ ->
                                            dialog.dismiss()
                                            webexViewModel.retryUCSSOLogin()
                                        },
                                        R.string.ok,
                                        { dialog, _ ->
                                            dialog.dismiss()
                                        })
                                }
                            }
                        }
                    })

                    progressBar.visibility = View.VISIBLE


                    Handler(Looper.getMainLooper()).post {
                        webexViewModel.startUCServices()
                        if (isUCSSOLogin && !webexViewModel.isUCLoggedIn()) {
                            Log.d(tag, "isUCSSOLogin && !webexViewModel.isUCLoggedIn() -> retrying sso login")
                            // To handle the case, when user starts UCSSOLogin but stops midway before success and presses back
                            webexViewModel.retryUCSSOLogin()
                        } else if (webexViewModel.isUCLoggedIn()) {
                            Handler(Looper.getMainLooper()).post {
                                binding.ssologinWebview.visibility = View.GONE
                                binding.progressBar.visibility = View.GONE
                                if(webexViewModel.getCallingType() == Phone.CallingType.WebexCalling || webexViewModel.getCallingType() == Phone.CallingType.WebexForBroadworks) {
                                    binding.buttongroup.visibility = View.VISIBLE
                                }
                                updateUCLoginStatusUI(getString(R.string.uc_login_success))

                                updatePhoneServiceConnectionUI("Phone services state : ${webexViewModel.getUCServerConnectionStatus().name}")
                            }
                        } else {
                            showUCLoginSettingsDialog()
                        }
                    }
                }
    }

    private fun onActionShowSSOLogin(ssoUrl: String) {
        Log.d(tag, "Callback : Show sso login with url : $ssoUrl")
        binding.progressBar.visibility = View.GONE
        binding.ssologinWebview.visibility = View.VISIBLE

        nonSSOAlertDialog?.dismiss()
        ucSettingsAlertDialog?.dismiss()

        isUCSSOLogin = true
        UCSSOWebViewAuthenticator.launchWebView(binding.ssologinWebview, ssoUrl, CompletionHandler { result ->
            if (result.isSuccessful) {
                Log.d(tag, "UCLoginActivity SSO login Successful")

                Handler(Looper.getMainLooper()).post {
                    binding.ssologinWebview.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                    updateUCLoginStatusUI(getString(R.string.uc_login_success))
                }
            } else {
                Log.d(tag, "UCLoginActivity SSO login Failed")
                Handler(Looper.getMainLooper()).post {
                    binding.ssologinWebview.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    updateUCLoginStatusUI(getString(R.string.uc_login_failed))
                }
            }
        })
    }

    private fun onActionShowNonSSOLogin() {
        Log.d(tag, "Callback : Show non sso login")
        showUCNonSSOLoginDialog()
    }

    override fun onBackPressed() {
        if (isUCSSOLogin && !isUCSSOLoginSuccessful) {
            Log.d(tag, "ucCancelSSOLogin()")
            webexViewModel.ucCancelSSOLogin()
        }
        super.onBackPressed()
    }

    private fun processServerConnectionStatus(
        status: UCLoginServerConnectionStatus,
        ucServerFailureReason: PhoneServiceRegistrationFailureReason
    ) {
        Log.d(tag, "processServerConnectionStatus status: $status, failureReason : $ucServerFailureReason")
        if (ucServerFailureReason != PhoneServiceRegistrationFailureReason.None) {
            if (ucServerFailureReason == PhoneServiceRegistrationFailureReason.RegisteredElsewhere) {
                showDialogWithMessage(this@UCLoginActivity, getString(R.string.force_register), getString(R.string.force_register_dialog_message), R.string.yes, true,
                    { dialog, _ ->
                        dialog.dismiss()
                        webexViewModel.forceRegisterPhoneServices()
                    },
                    R.string.no,
                    { dialog, _ ->
                        dialog.dismiss()
                    })
            }
            Handler(Looper.getMainLooper()).post {
                updatePhoneServiceConnectionUI("Phone services connection failed : ${ucServerFailureReason.name}")
                binding.ssologinWebview.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }

        } else {
            Handler(Looper.getMainLooper()).post {
                updatePhoneServiceConnectionUI("Phone services state : ${status.name}")
            }
            when (status) {
                UCLoginServerConnectionStatus.Connected -> {
                    Handler(Looper.getMainLooper()).post {
                        binding.ssologinWebview.visibility = View.GONE
                        binding.progressBar.visibility = View.GONE
                        if(webexViewModel.getCallingType() == Phone.CallingType.WebexCalling || webexViewModel.getCallingType() == Phone.CallingType.WebexForBroadworks) {
                            binding.buttongroup.visibility = View.VISIBLE
                        }
                        ucSettingsAlertDialog?.dismiss()
                    }
                }
                UCLoginServerConnectionStatus.Failed -> {
                    Handler(Looper.getMainLooper()).post {
                        binding.progressBar.visibility = View.GONE
                    }
                }
                else -> {}
            }
        }
    }

    private fun updatePhoneServiceConnectionUI(status: String) {
        binding.ucServerConnectionStatusTextView.text = status
    }

    private fun updateUCLoginStatusUI(status: String) {
        binding.ucLoginStatusTextView.text = status
    }

    private fun setUCDomainServerUrl(domain: String, serverUrl: String) {
        Log.d(tag, "setUCDomainServerUrl domain: $domain, serverUrl: $serverUrl")
        webexViewModel.setUCDomainServerUrl(ucDomain = domain, serverUrl = serverUrl)
    }

    private fun showUCLoginSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.uc_login_settings)
        DialogUcloginSettingsBinding.inflate(layoutInflater).apply {
            builder.setView(this.root)
            builder.setPositiveButton(android.R.string.ok) { dialog, _ ->

                Handler(Looper.getMainLooper()).postDelayed({
                    setUCDomainServerUrl(domain.text.toString(), server.text.toString())
                }, 200)
                dialog.dismiss()
            }
            builder.setNeutralButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            builder.setOnDismissListener {
                ucSettingsAlertDialog = null
            }
        }
        ucSettingsAlertDialog = builder.create()
        ucSettingsAlertDialog?.setCanceledOnTouchOutside(false)
        ucSettingsAlertDialog?.show()
    }

    private fun showUCNonSSOLoginDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.uc_login_settings)
        DialogUcloginNonssoBinding.inflate(layoutInflater).apply {
            builder.setView(this.root)
            builder.setPositiveButton(android.R.string.ok) { dialog, _ ->

                Handler(Looper.getMainLooper()).postDelayed({
                    val username = username.text.toString()
                    val password = password.text.toString()
                    webexViewModel.setCallServiceCredential(username, password)
                }, 200)

                dialog.dismiss()
            }
            builder.setNeutralButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            builder.setOnDismissListener {
                nonSSOAlertDialog = null
            }
        }

        nonSSOAlertDialog = builder.create()
        nonSSOAlertDialog?.setCanceledOnTouchOutside(false)
        nonSSOAlertDialog?.show()
    }
}
