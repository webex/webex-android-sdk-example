package com.ciscowebex.androidsdk.kitchensink.calling

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ciscowebex.androidsdk.auth.UCLoginServerConnectionStatus
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkApp
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.WebexViewModel
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityCucmCallBinding
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.phone.Call
import com.ciscowebex.androidsdk.phone.NotificationCallType
import org.koin.androidx.viewmodel.ext.android.viewModel

class CucmCallActivity : BaseActivity() {
    lateinit var binding: ActivityCucmCallBinding
    private var mCallId: String? = null
    private var mPushId: String = ""
    private val TAG = "PUSHREST"

    companion object {
        fun getIncomingIntent(context: Context, pushId: String? = null): Intent {
            val intent = Intent(context, CucmCallActivity::class.java)
            intent.putExtra(Constants.Intent.CALLING_ACTIVITY_ID, 1)
            intent.putExtra(Constants.Intent.PUSH_ID, pushId)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.action = Constants.Action.WEBEX_CUCM_CALL_ACTION
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityCucmCallBinding>(this, R.layout.activity_cucm_call)
                .also { binding = it }
                .apply {
                    binding.callerinfo.text = getString(R.string.fetching_details)
                    binding.callerinfo.visibility = View.VISIBLE
                    binding.buttongroup.visibility = View.GONE
                    if (intent.action == Constants.Action.WEBEX_CUCM_CALL_ACTION) {
                        if (intent?.hasExtra(Constants.Intent.PUSH_ID) == true) {
                            intent?.getStringExtra(Constants.Intent.PUSH_ID)?.let { pushId ->
                                mPushId = pushId
                                Log.i(TAG, "Push call from CUCM $pushId")
                                handleIncomingCucmCallFromFCM(pushId)
                            }
                        } else {
                            this@CucmCallActivity.finish()
                        }

                        binding.accept.setOnClickListener {
                            val intent = CallActivity.getIncomingIntent(this@CucmCallActivity)
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            intent.putExtra(Constants.Intent.CALL_ID, mCallId)
                            intent.action = Constants.Action.WEBEX_CALL_ACTION
                            startActivity(intent)
                            this@CucmCallActivity.finishAfterTransition()
                        }

                        binding.decline.setOnClickListener {
                            this@CucmCallActivity.finish()
                        }

                    } else {
                        this@CucmCallActivity.finish()
                    }
                }
    }


    private fun handleIncomingCucmCallFromFCM(pushId: String) {
        // Initialize koin modules
        if (!KitchenSinkApp.isKoinModulesLoaded) {
            (application as KitchenSinkApp).loadModules()
        }

        if(webexViewModel.webex.authenticator?.isAuthorized() == false){
            webexViewModel.webex.initialize({ result ->
                Log.d(TAG, "isAuthorized : ${webexViewModel.webex.authenticator?.isAuthorized()}")
                if (result.error != null) {
                    Log.d(TAG, "errorCode : ${result.error?.errorCode}, errorMessage : ${result.error?.errorMessage}")
                    runOnUiThread {
                        finish()
                    }
                } else {
                    runOnUiThread {
                        fetchCallDetails(pushId)
                    }
                }
            })
        }else{
            runOnUiThread {
                fetchCallDetails(pushId)
            }
        }
    }


    private fun getCallFromPush(pushId: String): Call? {
        val actualCallId = webexViewModel.webex.getCallIdByNotificationId(pushId, NotificationCallType.Cucm)
        Log.d(TAG, "CallInfo $actualCallId")
        val callInfo = webexViewModel.getCall(actualCallId)
        Log.d(TAG, "CallInfo ${callInfo?.getCallId()} title ${callInfo?.getTitle()}")
        return callInfo
    }


    private fun fetchCallDetails(pushId: String) {
        Log.d(TAG, "fetchCallDetails for push $pushId")
        webexViewModel.ucLiveData.observe(this@CucmCallActivity, {
            if (it != null) {
                Log.d(TAG, "CUCM Event : ${it.first.name} ${webexViewModel.ucServerConnectionStatus}")
                when (WebexRepository.UCCallEvent.valueOf(it.first.name)) {
                    WebexRepository.UCCallEvent.OnUCLoggedIn -> {
                        Log.d(TAG, "UC Login completed")
                    }
                    WebexRepository.UCCallEvent.OnUCServerConnectionStateChanged -> {
                        handleUCConnectionStateChange()
                    }
                    else -> {
                        Log.d(TAG, "CUCM Event details : ${WebexRepository.UCCallEvent.valueOf(it.first.name)}")
                    }
                }
            } else {
                Log.d(TAG, "CUCM Event details : null")
            }
        })

        Handler(Looper.getMainLooper()).post {
            Log.d(TAG, "Starting UC services")
            webexViewModel.startUCServices()
            handleUCConnectionStateChange()
        }
    }

    private fun handleUCConnectionStateChange(){
        if(mPushId.isEmpty()){
            return
        }
        when (webexViewModel.ucServerConnectionStatus) {
            UCLoginServerConnectionStatus.Connected -> {
                Log.d(TAG, "Phone services connected")
                binding.ucServerConnectionStatusTextView.text = resources.getString(R.string.phone_service_connected)
                binding.ucServerConnectionStatusTextView.visibility = View.VISIBLE
                Handler(Looper.getMainLooper()).postDelayed({
                    val call = getCallFromPush(mPushId)
                    mCallId = call?.getCallId()
                    if (mCallId == null) {
                        binding.callerinfo.text = getString(R.string.caller_details_unavailable)
                        binding.callerinfo.visibility = View.VISIBLE
                        binding.buttongroup.visibility = View.VISIBLE
                        binding.accept.visibility = View.GONE
                    } else {
                        binding.callerinfo.text = call?.getTitle()
                        binding.callerinfo.visibility = View.VISIBLE
                        binding.buttongroup.visibility = View.VISIBLE
                        binding.accept.visibility = View.VISIBLE
                        binding.decline.visibility = View.VISIBLE
                    }
                }, 10)
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