package com.ciscowebex.androidsdk.kitchensink.calling

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityCallBinding
import com.ciscowebex.androidsdk.kitchensink.utils.Constants


class CallActivity : BaseActivity() {

    lateinit var binding: ActivityCallBinding

    companion object {
        fun getOutgoingIntent(context: Context, callerName: String): Intent {
            val intent = Intent(context, CallActivity::class.java)
            intent.putExtra(Constants.Intent.CALLING_ACTIVITY_ID, 0)
            intent.putExtra(Constants.Intent.OUTGOING_CALL_CALLER_ID, callerName)
            return intent
        }
        fun getIncomingIntent(context: Context): Intent {
            val intent = Intent(context, CallActivity::class.java)
            intent.putExtra(Constants.Intent.CALLING_ACTIVITY_ID, 1)
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

    override fun onStop() {
        super.onStop()
        val fragment = supportFragmentManager.findFragmentById(R.id.containerFragment)
        if (fragment is CallControlsFragment){
            fragment.onFragmentStop()
            Log.d(CallActivity::class.java.name, "fragment.stopVideoStreaming() called")
        } else {
            Log.d(CallActivity::class.java.name, "fragment is null")
        }
    }

    override fun onStart() {
        super.onStart()
        val fragment = supportFragmentManager.findFragmentById(R.id.containerFragment)
        if (fragment is CallControlsFragment){
            fragment.onFragmentStart()
            Log.d(CallActivity::class.java.name, "fragment.resumeVideoStreaming() called")
        } else {
            Log.d(CallActivity::class.java.name, "fragment is null")
        }
    }
}