package com.ciscowebex.androidsdk.kitchensink.calling

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.CallRejectService
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.phone.CallObserver
import org.koin.android.ext.android.inject

class LockScreenActivity : BaseActivity() {

    private lateinit var callerinfo: TextView
    private lateinit var accept: ImageView
    private lateinit var decline: ImageView
    private val repository: WebexRepository by inject()

    companion object {
        fun getLockScreenIntent(context: Context, callId: String? = null): Intent {
            val intent = Intent(context, LockScreenActivity::class.java)
            intent.putExtra(Constants.Intent.CALLING_ACTIVITY_ID, 1)
            intent.putExtra(Constants.Intent.CALL_ID, callId)
            intent.action = Constants.Action.WEBEX_CALL_ACTION
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)
        
        callerinfo = findViewById(R.id.callerinfo)
        accept = findViewById(R.id.accept)
        decline = findViewById(R.id.decline)
        
        val callId = intent.getStringExtra(Constants.Intent.CALL_ID)

        if(callId != null) {
            val call = repository.getCall(callId)
            callerinfo.text = call?.getTitle()

            call?.let{
                repository.setCallObserver(it, object : CallObserver {
                    override fun onDisconnected(event: CallObserver.CallDisconnectedEvent?) {
                        super.onDisconnected(event)
                        finish()
                    }
                })
            }

            accept.setOnClickListener {
                val intent = CallActivity.getCallAcceptIntent(this@LockScreenActivity, callId)
                startActivity(intent)
                finishAfterTransition()
            }

            decline.setOnClickListener {
                startService(CallRejectService.getCallRejectIntent(this@LockScreenActivity, callId))
                finish()
            }
        }
    }
}