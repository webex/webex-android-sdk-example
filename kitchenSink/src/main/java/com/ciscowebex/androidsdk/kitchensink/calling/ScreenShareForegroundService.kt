package com.ciscowebex.androidsdk.kitchensink.calling

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.cisco.wme.appshare.ScreenShareContext
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkApp
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.utils.PermissionsHelper
import org.koin.android.ext.android.inject

class ScreenShareForegroundService : LifecycleService()  {
    private lateinit var notificationManager: NotificationManager
    private val repository: WebexRepository by inject()
    private var callId: String = ""

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val action = intent?.action
            val callId = intent?.getStringExtra(NOTIFICATION_EXTRA_CALL_ID) ?: ""
            Log.d(TAG,"Broadcast received, action:$action, callId:$callId")
            when (action) {
                CALL_NOTIFICATION_SCREEN_SHARING_STOP -> stopShare()
                CALL_NOTIFICATION_SCREEN_SHARING_UPDATE -> updateNotification(callId)
            }
        }
    }
    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(CALL_NOTIFICATION_SCREEN_SHARING_STOP)
        intentFilter.addAction(CALL_NOTIFICATION_SCREEN_SHARING_UPDATE)
        registerReceiver(receiver, intentFilter)
    }

    private fun stopShare() {
        Log.d(TAG, "Stop share, callId:$callId")
        if (callId.isNotEmpty()) {
            repository.stopShare(callId)
        }
        stopSelf()
    }

    private fun updateNotification(id: String) {
        Log.d(TAG, "updateNotification id: $id")
        callId = id
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(SHARE_SCREEN_FOREGROUND_SERVICE_NOTIFICATION_ID, buildScreenShareForegroundServiceNotification(true))
        } else {
            notificationManager.notify(SHARE_SCREEN_FOREGROUND_SERVICE_NOTIFICATION_ID, buildScreenShareForegroundServiceNotification(true))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        startForeground(SHARE_SCREEN_FOREGROUND_SERVICE_NOTIFICATION_ID, buildScreenShareForegroundServiceNotification())
        registerReceiver()
    }

    override fun onDestroy() {
        Log.d(TAG, "Destroy screen share foreground service")
        super.onDestroy()

        stopShare()
        unregisterReceiver(receiver)
        ScreenShareContext.getInstance().finit()
        stopForeground(true)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        service?.createNotificationChannel(chan)
        return channelId
    }

    private fun buildScreenShareForegroundServiceNotification(shareStarted: Boolean = false): Notification {
        Log.d(TAG, "buildScreenShareForegroundServiceNotification shareStarted: $shareStarted")
        val contentId = if (shareStarted) R.string.notification_share_foreground_text else R.string.notification_start_share_foreground_text

        val channelId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel("screen_share_service_v3_sdk", "Background Screen Share Service v3 SDK")
                } else { "" }


        val notificationBuilder =
                NotificationCompat.Builder(applicationContext, channelId)
                        .setSmallIcon(R.drawable.app_notification_icon)
                        .setContentTitle(getString(R.string.notification_share_foreground_title))
                        .setContentText(getString(contentId))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setTicker(getString(contentId))
                        .setDefaults(Notification.DEFAULT_SOUND)

        if (shareStarted) {
            notificationBuilder.addAction(buildStopShareAction())
            val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            mediaStyle.setShowActionsInCompactView(0)
            notificationBuilder.setStyle(mediaStyle)
        }

        return notificationBuilder.build()
    }

    private fun buildStopShareAction(): NotificationCompat.Action? {
        val intent = Intent(CALL_NOTIFICATION_SCREEN_SHARING_STOP).apply {
            putExtra(NOTIFICATION_EXTRA_CALL_ID, callId)
        }
        val pendingIntent = PendingIntent.getBroadcast(this, SHARE_SCREEN_FOREGROUND_SERVICE_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Action(R.drawable.ic_remote_end, getString(R.string.stop_share), pendingIntent)
    }

    companion object {
        private val TAG = "ScreenShareForegroundService"
        const val NOTIFICATION_EXTRA_CALL_ID = "callId"
        const val CALL_NOTIFICATION_SCREEN_SHARING_STOP = "screenSharingStop"
        const val CALL_NOTIFICATION_SCREEN_SHARING_UPDATE = "screenSharingUpdate"
        const val SHARE_SCREEN_FOREGROUND_SERVICE_NOTIFICATION_ID = 0xabc61

        fun startScreenShareForegroundService() {
            val context = KitchenSinkApp.applicationContext()
            val startCallForegroundServiceIntent = Intent(context, ScreenShareForegroundService::class.java)
            context.startService(startCallForegroundServiceIntent)
        }

        fun stopScreenShareForegroundService() {
            val context = KitchenSinkApp.applicationContext()
            val intent = Intent(context, ScreenShareForegroundService::class.java)
            context.stopService(intent)
        }

        fun updateScreenShareForegroundService(callId: String?) {
            Log.d(TAG, "updating teams screen share foreground service with callId: $callId")
            val context = KitchenSinkApp.applicationContext()
            val intent = Intent(CALL_NOTIFICATION_SCREEN_SHARING_UPDATE)
            callId?.let {
                intent.putExtra(NOTIFICATION_EXTRA_CALL_ID, it)
            }
            context.sendBroadcast(intent)
        }
    }
}