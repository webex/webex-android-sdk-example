package com.ciscowebex.androidsdk.kitchensink

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.auth.OAuthWebViewAuthenticator
import com.ciscowebex.androidsdk.auth.TokenAuthenticator
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
import com.ciscowebex.androidsdk.kitchensink.calling.CallActivity
import com.ciscowebex.androidsdk.kitchensink.extras.ExtrasActivity
import com.ciscowebex.androidsdk.kitchensink.search.SearchActivity
import com.ciscowebex.androidsdk.kitchensink.setup.SetupActivity
import com.ciscowebex.androidsdk.kitchensink.utils.FileUtils
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils
import com.ciscowebex.androidsdk.message.LocalFile
import com.ciscowebex.androidsdk.phone.Phone
import org.koin.android.ext.android.inject

class HomeActivity : BaseActivity() {

    lateinit var binding: ActivityHomeBinding
    private val personViewModel : PersonViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "HomeActivity"

        val authenticator = webexViewModel.webex.authenticator

        webexViewModel.enableBackgroundConnection(webexViewModel.enableBgConnectiontoggle)
        webexViewModel.setLogLevel(webexViewModel.logFilter)
        webexViewModel.enableConsoleLogger(webexViewModel.isConsoleLoggerEnabled)

        if(SharedPrefUtils.isAppBackgroundRunningPreferred(this)) {
            KitchenSinkForegroundService.startForegroundService(this)
        }

        Log.d(tag, "Service URls METRICS: ${webexViewModel.getServiceUrl(Phone.ServiceUrlType.METRICS)}" +
                "\nCLIENT_LOGS: ${webexViewModel.getServiceUrl(Phone.ServiceUrlType.CLIENT_LOGS)}" +
                "\nKMS: ${webexViewModel.getServiceUrl(Phone.ServiceUrlType.KMS)}")

        authenticator?.let {
            when (it) {
                is OAuthWebViewAuthenticator -> {
                    saveLoginTypePref(this, LoginActivity.LoginType.OAuth)
                }
                is TokenAuthenticator -> {
                    saveLoginTypePref(this, LoginActivity.LoginType.AccessToken)
                    webexViewModel.setOnTokenExpiredListener()
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
                    KitchenSinkForegroundService.stopForegroundService(this)
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

        webexViewModel.incomingListenerLiveData.observe(this@HomeActivity, Observer {
            it?.let {
                Log.d(tag, "incomingListenerLiveData: ${it.getCallId()}")
                Handler(Looper.getMainLooper()).post {
                    startActivity(CallActivity.getIncomingIntent(this, it.getCallId()))
                }
            }
        })

        DataBindingUtil.setContentView<ActivityHomeBinding>(this, R.layout.activity_home)
                .also { binding = it }
                .apply {

                    ivStartCall.setOnClickListener {
                        startActivity(Intent(this@HomeActivity, SearchActivity::class.java))
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
                        val recipient = "webex-mobile-sdk@cisco.com"
                        val subject = resources.getString(R.string.feedbackLogsSubject)

                        val emailIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            type = "text/plain"
//                            data = Uri.parse("mailto:")
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

                    ivExtras.setOnClickListener {
                        startActivity(Intent(this@HomeActivity, ExtrasActivity::class.java))
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
        webexViewModel.setMessageObserver()
        webexViewModel.setCalendarMeetingObserver()
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
        webexViewModel.setIncomingListener()
        addVirtualBackground()
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

    private fun addVirtualBackground() {
        if (SharedPrefUtils.isVirtualBgAdded(this)) {
            Log.d(tag, "Virtual Bg is already added")
        } else {

            val thumbnailFile = FileUtils.getFileFromResource(this, "nature-thumb")
            val file = FileUtils.getFileFromResource(this, "nature")
            val thumbnail = LocalFile.Thumbnail(thumbnailFile, null,
                resources.getInteger(R.integer.virtual_bg_thumbnail_width),
                resources.getInteger(R.integer.virtual_bg_thumbnail_height))

            val localFile = LocalFile(file, null, thumbnail, null)
            webexViewModel.addVirtualBackground(localFile, CompletionHandler {
                if (it.isSuccessful && it.data != null) {
                    SharedPrefUtils.setVirtualBgAdded(this, true)
                }
            })
        }
    }
}