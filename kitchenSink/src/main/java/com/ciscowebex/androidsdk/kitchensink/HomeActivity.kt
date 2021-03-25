package com.ciscowebex.androidsdk.kitchensink

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.auth.OAuthAuthenticator
import com.ciscowebex.androidsdk.auth.OAuthWebViewAuthenticator
import com.ciscowebex.androidsdk.kitchensink.auth.LoginActivity
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityHomeBinding
import com.ciscowebex.androidsdk.kitchensink.messaging.MessagingActivity
import com.ciscowebex.androidsdk.kitchensink.cucm.UCLoginActivity
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.detail.MessageDetailsDialogFragment
import com.ciscowebex.androidsdk.kitchensink.person.PersonDialogFragment
import com.ciscowebex.androidsdk.kitchensink.person.PersonViewModel
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils.clearLoginTypePref
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils.saveLoginTypePref
import com.ciscowebex.androidsdk.kitchensink.webhooks.WebhooksActivity
import com.ciscowebex.androidsdk.auth.UCLoginServerConnectionStatus
import com.ciscowebex.androidsdk.kitchensink.setup.SetupActivity
import org.koin.android.ext.android.inject

class HomeActivity : BaseActivity() {

    lateinit var binding: ActivityHomeBinding
    private val personViewModel : PersonViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "HomeActivity"

        val authenticator = webexViewModel.webex.authenticator

        webexViewModel.enableBackgroundConnection(true)

        authenticator?.let {
            when (it) {
                is OAuthWebViewAuthenticator -> {
                    saveLoginTypePref(this, LoginActivity.LoginType.OAuth)
                }
                is OAuthAuthenticator -> {
                    saveLoginTypePref(this, LoginActivity.LoginType.AuthCode)
                }
                else -> {
                    saveLoginTypePref(this, LoginActivity.LoginType.JWT)
                }
            }
        }

        webexViewModel.signOutListenerLiveData.observe(this@HomeActivity, Observer {
            it?.let {
                if (it) {
                    clearLoginTypePref(this)
                    (application as KitchenSinkApp).unloadKoinModules()
                    finish()
                }
                else {
                    binding.progressLayout.visibility = View.GONE
                }
            }
        })


        webexViewModel.cucmLiveData.observe(this@HomeActivity, Observer {
            if (it != null) {
                when (WebexRepository.CucmEvent.valueOf(it.first.name)) {
                    WebexRepository.CucmEvent.OnUCServerConnectionStateChanged -> {
                        updateUCData()
                    }
                    else -> {}
                }
            }
        })

        DataBindingUtil.setContentView<ActivityHomeBinding>(this, R.layout.activity_home)
                .also { binding = it }
                .apply {

                    ivStartCall.setOnClickListener {
                        checkCallingFeature(false)
                    }

                    ivWaitingCall.setOnClickListener {
                        checkCallingFeature(true)
                    }

                    ivMessaging.setOnClickListener {
                        startActivity(Intent(this@HomeActivity, MessagingActivity::class.java))
                    }

                    ivUcLogin.setOnClickListener {
                        startActivity(Intent(this@HomeActivity, UCLoginActivity::class.java))
                    }

                    ivWebhook.setOnClickListener {
                        startActivity(Intent(this@HomeActivity, WebhooksActivity::class.java))
                    }

                    ivLogout.setOnClickListener {
                        progressLayout.visibility = View.VISIBLE
                        webexViewModel.signOut()
                    }

                    ivGetMe.setOnClickListener {
                        PersonDialogFragment().show(supportFragmentManager, getString(R.string.person_detail))
                    }

                    ivFeedback.setOnClickListener {
                        val fileUri = webexViewModel.getlogFileUri(false)
                        val recipient = ""
                        val subject = resources.getString(R.string.feedbackLogsSubject)

                        val emailIntent = Intent().apply {
                            action = Intent.ACTION_SENDTO
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            type = "text/plain"
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
                            putExtra(Intent.EXTRA_SUBJECT, subject)
                            putExtra(Intent.EXTRA_STREAM, fileUri)
                        }

                        try {
                            startActivity(Intent.createChooser(emailIntent, "Send mail..."))
                        }
                        catch (e: Exception) {
                            Log.e(tag, "Send mail exception: $e")
                        }
                    }

                    ivSetup.setOnClickListener {
                        startActivity(Intent(this@HomeActivity, SetupActivity::class.java))
                    }
                }

        //used some delay because sometimes it gives empty stuff in personDetails
        Handler().postDelayed(Runnable {
            personViewModel.getMe()
        }, 1000)
        observeData()
        showMessageIfCameFromNotification()
        webexViewModel.setSpaceObserver()
        webexViewModel.setMembershipObserver()
        webexViewModel.setLogLevel()
        webexViewModel.setMessageObserver()
    }

    override fun onBackPressed() {
        (application as KitchenSinkApp).closeApplication()
    }

    private fun showMessageIfCameFromNotification() {

        if("ACTION" == intent?.action){
            val messageId = intent?.getStringExtra(Constants.Bundle.MESSAGE_ID)
            MessageDetailsDialogFragment.newInstance(messageId.orEmpty()).show(supportFragmentManager, "MessageDetailsDialogFragment")
        }
    }

    override fun onNewIntent(intent: Intent?) {
        val messageId = intent?.getStringExtra(Constants.Bundle.MESSAGE_ID)
        MessageDetailsDialogFragment.newInstance(messageId.orEmpty()).show(supportFragmentManager, "MessageDetailsDialogFragment")
        super.onNewIntent(intent)
    }

    private fun observeData() {
        personViewModel.person.observe(this, Observer { person ->
            person?.let {
                webexViewModel.getFCMToken(it)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        updateUCData()
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