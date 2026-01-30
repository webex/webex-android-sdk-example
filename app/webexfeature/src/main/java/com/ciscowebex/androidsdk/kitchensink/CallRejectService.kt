package com.ciscowebex.androidsdk.kitchensink

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import org.koin.android.ext.android.inject

class CallRejectService : Service() {

    private val repository: WebexRepository by inject()
    val tag = "CallRejectService"

    companion object {
        fun getCallRejectIntent(context: Context, callId: String? = null, accept : Boolean = true): Intent {
            val intent = Intent(context, CallRejectService::class.java)
            intent.action = Constants.Action.WEBEX_CALL_REJECT_ACTION
            intent.putExtra(Constants.Intent.CALLING_ACTIVITY_ID, 1)
            intent.putExtra(Constants.Intent.CALL_ID, callId)
            return intent
        }

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null){
            if(intent.action == Constants.Action.WEBEX_CALL_REJECT_ACTION){
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                notificationManager?.cancel(Constants.Notification.WEBEX_CALL)
                val callId = intent.getStringExtra(Constants.Intent.CALL_ID)
                if(!callId.isNullOrBlank()) {
                    val call = repository.getCall(callId)
                    call?.let {
                        call.reject { result ->
                            if (result.isSuccessful) {
                                Log.d(tag, "rejectCall successful")
                            } else {
                                Log.d(tag, "rejectCall error: ${result.error?.errorMessage}")
                            }
                        }
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}