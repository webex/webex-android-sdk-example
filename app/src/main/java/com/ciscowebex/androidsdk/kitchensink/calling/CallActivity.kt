package com.ciscowebex.androidsdk.kitchensink.calling

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.internal.ResultImpl
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityCallBinding
import com.ciscowebex.androidsdk.kitchensink.utils.CallObjectStorage
import com.ciscowebex.androidsdk.kitchensink.utils.Constants


class CallActivity : BaseActivity(), CallControlsFragment.OnCallActionListener {

    lateinit var binding: ActivityCallBinding

    companion object {
        fun getOutgoingIntent(context: Context, callerName: String): Intent {
            val intent = Intent(context, CallActivity::class.java)
            intent.putExtra(Constants.Intent.CALLING_ACTIVITY_ID, 0)
            intent.putExtra(Constants.Intent.OUTGOING_CALL_CALLER_ID, callerName)
            return intent
        }
        fun getIncomingIntent(context: Context, callId: String? = null): Intent {
            val intent = Intent(context, CallActivity::class.java)
            intent.putExtra(Constants.Intent.CALLING_ACTIVITY_ID, 1)
            intent.putExtra(Constants.Intent.CALL_ID, callId)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "CallActivity"
        DataBindingUtil.setContentView<ActivityCallBinding>(this, R.layout.activity_call)
                .also { binding = it }
                .apply {
                    val callingActivity = intent.getIntExtra(Constants.Intent.CALLING_ACTIVITY_ID, 0)

                    if (callingActivity == 0) {
                        val callerId = intent.getStringExtra(Constants.Intent.OUTGOING_CALL_CALLER_ID)
                        val fragment = supportFragmentManager.findFragmentById(R.id.containerFragment) as CallControlsFragment

                        callerId?.let {
                            fragment.dialOutgoingCall(callerId)
                        }
                    } else if (intent.action == Constants.Action.WEBEX_CALL_ACTION){
                        intent?.getStringExtra(Constants.Intent.CALL_ID) ?.let { callId ->
                            handleIncomingWebexCallFromFCM(callId)
                        }
                    }
                }
    }

    private fun handleIncomingWebexCallFromFCM(callId: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.containerFragment)
        if (fragment is CallControlsFragment){
            fragment.handleFCMIncomingCall(callId)
        } else {
            Log.d(CallActivity::class.java.name, "fragment is null")
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.containerFragment)
        if ( (fragment is CallControlsFragment) && (fragment.needBackPressed())) {
            fragment.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    fun alertDialog(shouldFinishActivity: Boolean, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.call_failed))
        builder.setMessage(message)

        builder.setPositiveButton("OK") { _, _ ->
            if(shouldFinishActivity) finish()
        }

        builder.show()
    }

    private fun toBeShownOnLockScreen() {
        window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
        } else {
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        toBeShownOnLockScreen()
    }

    override fun onEndAndAnswer(currentCallId: String, newCallId: String, handler: CompletionHandler<Boolean>) {
        Log.d(tag, "currentCall: $currentCallId newCallId $newCallId")
        //cut the current call
        webexViewModel.getCall(currentCallId)?.hangup(CompletionHandler { result ->
            if (result.isSuccessful) {
                Log.d(tag, "hangup successful")
                Handler(Looper.getMainLooper()).postDelayed( Runnable {
                    val fragment = supportFragmentManager.findFragmentById(R.id.containerFragment)
                    if (fragment is CallControlsFragment){
                        val call = CallObjectStorage.getCallObject(newCallId)
                        call?.let { fragment.answerCall(it) }
                        handler.onComplete(ResultImpl.success(true))
                    } else {
                        Log.d(CallActivity::class.java.name, "fragment is null")
                        handler.onComplete(ResultImpl.success(true))
                    }
                }, 2000)
            } else {
                handler.onComplete(ResultImpl.success(false))
                Log.d(tag, "hangup error: ${result.error?.errorMessage}")
            }
        })
    }
}