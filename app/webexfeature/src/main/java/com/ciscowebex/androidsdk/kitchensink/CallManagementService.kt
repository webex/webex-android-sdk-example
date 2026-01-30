package com.ciscowebex.androidsdk.kitchensink

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.ciscowebex.androidsdk.kitchensink.utils.CallObjectStorage
import com.ciscowebex.androidsdk.phone.Call
import com.ciscowebex.androidsdk.phone.CallObserver


class CallManagementService : Service() {
    private val tag = "CallManagementService"
    private var receiver: BroadcastReceiver? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isForegroundServiceSupported()) {
            startForeground(1, getNotification())
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(tag, "onTaskRemoved called!")
        super.onTaskRemoved(rootIntent)
        val call = getOngoingCall()
        if (call != null) {
            endCall(call)
        } else {
            Log.d(tag, "No call found in connected state. Killing service!")
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (receiver != null) {
            unregisterReceiver(receiver)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotification(): Notification {
        // Create a notification for the foreground service
        val channelId = "ks_02"
        val notificationChannel =
            NotificationChannel(
                channelId, "Call Management Service Notifications",
                NotificationManager.IMPORTANCE_MIN
            )
        notificationChannel.enableLights(false)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.createNotificationChannel(notificationChannel)

        val title = getString(R.string.call_management_service_notification_title)
        return NotificationCompat.Builder(
            this,
            channelId
        ).setSmallIcon(R.drawable.app_notification_icon)
            .setWhen(0)
            .setContentTitle(title)
            .setOngoing(true)
            .build()
    }

    private fun getOngoingCall(): Call? {
        var call: Call? = null

        for (i in 0 until CallObjectStorage.size()) {
            val callObj = CallObjectStorage.getCallObjectFromIndex(i)
            val status = callObj?.getStatus()
            if (status == Call.CallStatus.CONNECTED || status == Call.CallStatus.RINGING
                || status == Call.CallStatus.WAITING || status == Call.CallStatus.INITIATED) {
                Log.d(tag, "Call with id = ${callObj.getCallId()} found in ${status.name} state. ")
                call = callObj
                break
            }
        }
        return call
    }

    private fun endCall(call: Call) {
        Log.d(tag, "Ending call with id = ${call.getCallId()}")
        call.setObserver(object : CallObserver {
            override fun onDisconnected(event: CallObserver.CallDisconnectedEvent?) {
                Log.d(tag, "Call disconnected fired, stopping callManagementService!")
                stopSelf()
            }

        })
        if (call.getDirection() == Call.Direction.INCOMING && call.getStatus() == Call.CallStatus.RINGING) {
            call.reject {
                if (it.isSuccessful) {
                    Log.d(tag, "Call rejected successfully. Waiting for call disconnected event")
                } else {
                    Log.e(tag, "Call reject failed, reason : ${it.error?.errorMessage}")
                    stopSelf()
                }
            }
        } else {
            call.hangup { result ->
                if (result.isSuccessful) {
                    Log.d(tag, "Call hung up success. Waiting for call disconnected event")
                } else {
                    Log.e(tag, "Call hangup failed, reason : ${result.error?.errorMessage}")
                    stopSelf()
                }
            }
        }

    }

    private fun isForegroundServiceSupported(): Boolean {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    }
}