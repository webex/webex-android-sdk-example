package com.ciscowebex.androidsdk.kitchensink

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import android.Manifest
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.ResourceType
import com.ciscowebex.androidsdk.auth.OAuthWebViewAuthenticator
import com.ciscowebex.androidsdk.auth.TokenAuthenticator
import com.ciscowebex.androidsdk.auth.UCLoginServerConnectionStatus
import com.ciscowebex.androidsdk.kitchensink.auth.LoginActivity
import com.ciscowebex.androidsdk.kitchensink.calling.CallActivity
import com.ciscowebex.androidsdk.kitchensink.cucm.UCLoginActivity
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityHomeBinding
import com.ciscowebex.androidsdk.kitchensink.extras.ExtrasActivity
import com.ciscowebex.androidsdk.kitchensink.messaging.MessagingActivity
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.detail.MessageDetailsDialogFragment
import com.ciscowebex.androidsdk.kitchensink.person.PersonDialogFragment
import com.ciscowebex.androidsdk.kitchensink.person.PersonViewModel
import com.ciscowebex.androidsdk.kitchensink.search.SearchActivity
import com.ciscowebex.androidsdk.kitchensink.setup.SetupActivity
import com.ciscowebex.androidsdk.kitchensink.utils.CallObjectStorage
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.FileUtils
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils.saveLoginTypePref
import com.ciscowebex.androidsdk.kitchensink.webhooks.WebhooksActivity
import com.ciscowebex.androidsdk.message.LocalFile
import com.ciscowebex.androidsdk.people.ProductCapability
import com.ciscowebex.androidsdk.phone.Phone
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import java.io.FileDescriptor
import java.io.PrintWriter

class HomeActivity : BaseActivity() {

    lateinit var binding: ActivityHomeBinding
    private val personViewModel : PersonViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "HomeActivity"

        val authenticator = webexViewModel.webex.authenticator

        webexViewModel.setLogLevel(webexViewModel.logFilter)
        webexViewModel.enableConsoleLogger(webexViewModel.isConsoleLoggerEnabled)
        webexViewModel.setOnInitialSpacesSyncCompletedListener()

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
                    onSignedOut()
                }
                else {
                    binding.progressLayout.visibility = View.GONE
                }
            }
        })


        webexViewModel.ucLiveData.observe(this@HomeActivity, Observer {
            if (it != null) {
                when (WebexRepository.UCCallEvent.valueOf(it.first.name)) {
                    WebexRepository.UCCallEvent.OnUCServerConnectionStateChanged -> {
                        updateUCData()
                    }
                    else -> {}
                }
            }
        })

        webexViewModel.incomingListenerLiveData.observe(this@HomeActivity, Observer {
            it?.let {
                Log.d(tag, "incomingListenerLiveData: ${it.getCallId()}")
                val callId = it.getCallId()
                if(callId != null){
                    if(CallObjectStorage.getCallObject(callId) != null){
                        if(!it.isWebexCallingOrWebexForBroadworks() && !it.isCUCMCall()) {
                            // For Webex Calling call is notified in FCM service with accept decline button even for foreground case
                            // So not notifying here in home screen
                            Handler(Looper.getMainLooper()).post {
                                startActivity(CallActivity.getIncomingIntent(this, it.getCallId()))
                            }
                        }
                    }
                }
            }
        })

        webexViewModel.initialSpacesSyncCompletedLiveData.observe(this@HomeActivity) {
            Log.d(tag, getString(R.string.initial_spaces_sync_completed))
            Snackbar.make(binding.root, getString(R.string.initial_spaces_sync_completed), Snackbar.LENGTH_LONG).show()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        DataBindingUtil.setContentView<ActivityHomeBinding>(this, R.layout.activity_home)
                .also { binding = it }
                .apply {

                    binding.version.text = "Version : "+BuildConfig.VERSION_NAME

                    ivStartCall.setOnClickListener {
                        startActivity(Intent(this@HomeActivity, SearchActivity::class.java))
                    }

                    ivMessaging.setOnClickListener {
                        startActivity(Intent(this@HomeActivity, MessagingActivity::class.java))
                    }

                    ivUcLogin.setOnClickListener {
                        startActivity(UCLoginActivity.getIntent(this@HomeActivity))
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
                        val capability = getProductCapability()
                        Log.d(tag, "messaging capability: ${capability.isMessagingSupported()} calling capability: ${capability.isCallingSupported()} meeting capability: ${capability.isMeetingSupported()}")
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
        // UC Login
        webexViewModel.startUCServices()
        observeUCLoginData()
        

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(tag, "POST NOTIFICATION permission granted")
        } else {
            Log.e(tag, "POST NOTIFICATION permission denied")
            Toast.makeText(this, "POST NOTIFICATION permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeUCLoginData() {
        webexViewModel.ucLiveData.observe(this@HomeActivity, Observer {
            Log.d(tag, "uc login observer called : ${it.first.name}")
            if (it != null) {
                when (WebexRepository.UCCallEvent.valueOf(it.first.name)) {
                    WebexRepository.UCCallEvent.OnUCLoggedIn, WebexRepository.UCCallEvent.OnUCServerConnectionStateChanged -> {
                        updateUCData()
                    }
                    WebexRepository.UCCallEvent.ShowSSOLogin -> {
                        startActivity(UCLoginActivity.getIntent(this@HomeActivity,
                            UCLoginActivity.Companion.OnActivityStartAction.ShowSSOLogin.name,
                            it.second))
                    }

                    WebexRepository.UCCallEvent.ShowNonSSOLogin -> {
                        startActivity(UCLoginActivity.getIntent(this@HomeActivity, UCLoginActivity.Companion.OnActivityStartAction.ShowNonSSOLogin.name))
                    }
                    else -> {
                    }
                }
            }
        })
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
        checkForInitialSpacesSync()

        // Base64 testing
        Log.d(tag, "Testing Base64 encoding")
        testBase64Encoding()
        Log.d(tag, "Testing Base64 decoding")
        testBase64Decoding()
    }

    private fun checkForInitialSpacesSync() {
        if (!webexViewModel.isSpacesSyncCompleted()) {
            Snackbar.make(binding.root, getString(R.string.syncing_spaces), Snackbar.LENGTH_SHORT).show()
        }
    }


    private fun updateUCData() {
        Log.d(tag, "updateUCData isUCServerLoggedIn: ${webexViewModel.repository.isUCServerLoggedIn} ucServerConnectionStatus: ${webexViewModel.repository.ucServerConnectionStatus}")
        if (webexViewModel.isUCServerLoggedIn) {
            binding.ucLoginStatusTextView.visibility = View.VISIBLE
            if(webexViewModel.getCallingType() == Phone.CallingType.WebexCalling)  {
                binding.ucLoginStatusTextView.text = getString(R.string.wxc_loggedIn)
            } else if(webexViewModel.getCallingType() == Phone.CallingType.WebexForBroadworks)  {
                binding.ucLoginStatusTextView.text = getString(R.string.webexforbroadworks_loggedIn)
            } else if (webexViewModel.getCallingType() == Phone.CallingType.CUCM){
                binding.ucLoginStatusTextView.text = getString(R.string.uc_loggedIn)
            }
        } else {
            binding.ucLoginStatusTextView.visibility = View.GONE
        }

        when (webexViewModel.ucServerConnectionStatus) {
            UCLoginServerConnectionStatus.Failed -> {
                val text = resources.getString(R.string.phone_service_failed) + " " + webexViewModel.ucServerConnectionFailureReason
                binding.ucServerConnectionStatusTextView.text = text
                binding.ucServerConnectionStatusTextView.visibility = View.VISIBLE
            }
            UCLoginServerConnectionStatus.Connected, UCLoginServerConnectionStatus.Connecting, UCLoginServerConnectionStatus.Disconnected -> {
                val text = resources.getString(R.string.phone_services_connection_status) + webexViewModel.ucServerConnectionStatus.name
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

    private fun getProductCapability() : ProductCapability {
        return webexViewModel.getProductCapability()
    }

    override fun dump(
        prefix: String,
        fd: FileDescriptor?,
        writer: PrintWriter,
        args: Array<out String>?
    ) {
        super.dump(prefix, fd, writer, args)
        writer.println(" ")
        writer.println("Dump logs: ")
        webexViewModel.printObservers(writer)
    }

    private fun testBase64Encoding() {
        // Replace your personId with personUUIDString and comparison base64 string
        val personUUIDString = "5bd7167d-0072-4878-8c58-c1097b720aaa"

        Handler().postDelayed(Runnable {
            Log.d(tag, "Attempting to Base64 People encoded string:")

            webexViewModel.webex.base64Encode(ResourceType.People, personUUIDString, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val encodedString = result.data
                    Log.d(tag, "Base64 People encoded string: $encodedString")
                    if (encodedString == "Y2lzY29zcGFyazovL3VzL1BFT1BMRS81YmQ3MTY3ZC0wMDcyLTQ4NzgtOGM1OC1jMTA5N2I3MjBhYWE" || encodedString == "Y2lzY29zcGFyazovL3VzL1BFT1BMRS81YmQ3MTY3ZC0wMDcyLTQ4NzgtOGM1OC1jMTA5N2I3MjBhYWE=") {
                        Log.d(tag, "Base64 Encoding People ✅")
                    } else {
                        Log.d(tag, "Base64 Encoding People ❌. Please make sure to replace personId with personUUIDString and comparison base64 string in code")
                    }
                } else {
                    Log.e(tag, "Base64 Encoding People ❌: ${result.error}. Please make sure to replace personId with personUUIDString and comparison base64 string in code")
                }
            })
        }, 300)


        Handler().postDelayed(Runnable {
            Log.d(tag, "Attempting to Base64 Rooms encoded string:")

            // Replace with any roomId in your account with roomUUIDString and comparison base64 string
            val roomUUIDString = "55831920-9886-11f0-87b5-2b4f3f1744dc"
            webexViewModel.webex.base64Encode(ResourceType.Rooms, roomUUIDString, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val encodedString = result.data
                    Log.d(tag, "Base64 Rooms encoded string: $encodedString")
                    if (encodedString == "Y2lzY29zcGFyazovL3VzL1JPT00vNTU4MzE5MjAtOTg4Ni0xMWYwLTg3YjUtMmI0ZjNmMTc0NGRj" || encodedString == "Y2lzY29zcGFyazovL3VzL1JPT00vNTU4MzE5MjAtOTg4Ni0xMWYwLTg3YjUtMmI0ZjNmMTc0NGRj=") {
                        Log.d(tag, "Base64 Encoding Rooms ✅")
                    } else {
                        Log.d(tag, "Base64 Encoding Rooms ❌. Please make sure to replace roomId with roomUUIDString and comparison base64 string in code")
                    }
                } else {
                    Log.e(tag, "Base64 Encoding Rooms ❌: ${result.error}. Please make sure to replace roomId with roomUUIDString and comparison base64 string in code")
                }
            })
        }, 300)

        Handler().postDelayed(Runnable {
            Log.d(tag, "Attempting to Base64 Teams encoded string:")

            // Replace with any teamId in your account with teamUUIDString and comparison base64 string
            val teamUUIDString = "a811a0e0-3289-11ee-a006-b7b8084e553b"
            webexViewModel.webex.base64Encode(ResourceType.Teams, teamUUIDString, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val encodedString = result.data
                    Log.d(tag, "Base64 Teams encoded string: $encodedString")
                    if (encodedString == "Y2lzY29zcGFyazovL3VzL1RFQU0vYTgxMWEwZTAtMzI4OS0xMWVlLWEwMDYtYjdiODA4NGU1NTNi" || encodedString == "Y2lzY29zcGFyazovL3VzL1RFQU0vYTgxMWEwZTAtMzI4OS0xMWVlLWEwMDYtYjdiODA4NGU1NTNi=") {
                        Log.d(tag, "Base64 Encoding Teams ✅")
                    } else {
                        Log.d(tag, "Base64 Encoding Teams ❌. Please make sure to replace teamId with teamUUIDString and comparison base64 string in code")
                    }
                } else {
                    Log.e(tag, "Base64 Encoding Teams ❌: ${result.error}. Please make sure to replace teamId with teamUUIDString and comparison base64 string in code")
                }
            })
        }, 300)

    }

    private fun testBase64Decoding() {
        val encodedString = "Y2lzY29zcGFyazovL3VzL1BFT1BMRS81YmQ3MTY3ZC0wMDcyLTQ4NzgtOGM1OC1jMTA5N2I3MjBhYWE"
        val decodedString = webexViewModel.webex.base64Decode(encodedString)
        if (decodedString == "5bd7167d-0072-4878-8c58-c1097b720aaa") {
            Log.d(tag, "Base64 Decoding ✅")
        } else {
            Log.d(tag, "Base64 Decoding ❌: Expected '5bd7167d-0072-4878-8c58-c1097b720aaa', got '$decodedString'. Please make sure to replace encodedString and expected UUID in comparison")
        }
    }
}