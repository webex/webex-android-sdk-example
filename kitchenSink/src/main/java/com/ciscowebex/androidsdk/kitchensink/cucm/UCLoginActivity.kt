package com.ciscowebex.androidsdk.kitchensink.cucm


import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
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
import com.ciscowebex.androidsdk.auth.UCLoginServerConnectionStatus


class UCLoginActivity : BaseActivity() {
    lateinit var binding: ActivityCucmLoginBinding

    private var nonSSOAlertDialog: AlertDialog? = null
    private var ucSettingsAlertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "UCLoginActivity"
        DataBindingUtil.setContentView<ActivityCucmLoginBinding>(this, R.layout.activity_cucm_login)
                .also { binding = it }
                .apply {
                    webexViewModel.cucmLiveData.observe(this@UCLoginActivity, Observer {
                        if (it != null) {
                            when (WebexRepository.CucmEvent.valueOf(it.first.name)) {
                                WebexRepository.CucmEvent.ShowSSOLogin -> {

                                    progressBar.visibility = View.GONE
                                    ssologinWebview.visibility = View.VISIBLE

                                    nonSSOAlertDialog?.dismiss()
                                    ucSettingsAlertDialog?.dismiss()

                                    UCSSOWebViewAuthenticator.launchWebView(ssologinWebview, it.second, CompletionHandler { result ->
                                        if (result.isSuccessful) {
                                            Log.d(tag, "UCLoginActivity SSO login Successful")

                                            Handler(Looper.getMainLooper()).post {
                                                ssologinWebview.visibility = View.GONE
                                                progressBar.visibility = View.VISIBLE
                                            }
                                        } else {
                                            Log.d(tag, "UCLoginActivity SSO login Failed")
                                            ucLoginEvent(getString(R.string.uc_login_failed))
                                        }
                                    })
                                }
                                WebexRepository.CucmEvent.ShowNonSSOLogin -> {
                                    showUCNonSSOLoginDialog()
                                }
                                WebexRepository.CucmEvent.OnUCLoggedIn -> {
                                    ucLoginEvent(getString(R.string.uc_login_success))
                                }
                                WebexRepository.CucmEvent.OnUCLoginFailed -> {
                                    ucLoginEvent(getString(R.string.uc_login_failed))
                                }
                                WebexRepository.CucmEvent.OnUCServerConnectionStateChanged -> {
                                    processServerConnectionStatus(webexViewModel.getUCServerConnectionStatus())
                                }
                            }
                        }
                    })

                    progressBar.visibility = View.VISIBLE

                    //Testing purpose, don't process if x86 arch.
                    val supportedABIs = android.os.Build.SUPPORTED_ABIS
                    val isx86Supported = (supportedABIs?.count { it.startsWith("x86") } ?: 0) > 0

                    if (!isx86Supported) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (webexViewModel.isUCLoggedIn()) {
                                ucLoginEvent(getString(R.string.uc_login_success))
                            } else {
                                showUCLoginSettingsDialog()
                            }
                        }, 1000)
                    } else {
                        showToast(getString(R.string.uc_arch_x86_not_supported))
                    }
                }
    }

    private fun processServerConnectionStatus(status: UCLoginServerConnectionStatus) {
        Log.d(tag, "processServerConnectionStatus status: $status")
        when (status) {
            UCLoginServerConnectionStatus.Idle -> {}
            UCLoginServerConnectionStatus.Connecting -> {}
            UCLoginServerConnectionStatus.Connected -> {
                ucLoginEvent(getString(R.string.uc_server_connected))
            }
            UCLoginServerConnectionStatus.Disconnected -> {}
            UCLoginServerConnectionStatus.Failed -> {}
        }
    }

    private fun ucLoginEvent(message: String) {
        Handler(Looper.getMainLooper()).post {
            binding.ssologinWebview.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            showToast(message)
        }
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast.show()
        updateUCData()
    }

    private fun setUCDomainServerUrl(domain: String, serverUrl: String) {
        Log.d(tag, "setUCDomainServerUrl domain: $domain, serverUrl: $serverUrl")
        webexViewModel.setUCDomainServerUrl(domain, serverUrl)
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
                    webexViewModel.setCUCMCredential(username, password)
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

    private fun updateUCData() {
        Log.d(tag, "updateUCData isCUCMServerLoggedIn: ${webexViewModel.repository.isCUCMServerLoggedIn} ucServerConnectionStatus: ${webexViewModel.repository.ucServerConnectionStatus}")
        if (webexViewModel.isCUCMServerLoggedIn) {
            binding.ucLoginStatusTextView.visibility = View.VISIBLE
        } else {
            binding.ucLoginStatusTextView.visibility = View.GONE
        }

        when (webexViewModel.ucServerConnectionStatus) {
            UCLoginServerConnectionStatus.Connected -> {
                binding.ucServerConnectionStatusTextView.text = resources.getString(R.string.phone_service_connected)
                binding.ucServerConnectionStatusTextView.visibility = View.VISIBLE
            }
            UCLoginServerConnectionStatus.Failed -> {
                val text = resources.getString(R.string.phone_service_failed) + " " + webexViewModel.ucServerConnectionFailureReason
                binding.ucServerConnectionStatusTextView.text = text
                binding.ucServerConnectionStatusTextView.visibility = View.VISIBLE
            }
            else -> {
                binding.ucServerConnectionStatusTextView.visibility = View.GONE
            }
        }
    }
}
