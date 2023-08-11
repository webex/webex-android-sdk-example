package com.ciscowebex.androidsdk.kitchensink

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ciscowebex.androidsdk.kitchensink.auth.LoginActivity

class KitchenSinkForegroundService : Service() {

    private var mNotificationManager: NotificationManager? = null;


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (IsForegroundServiceSupported()) {
            startForeground(0x111111, getServiceOngoingNotification(this))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            if (it.getBooleanExtra(KitchenSinkForegroundService.STOP_REQUEST, false)) {
                stopSelf()
            }
        }
        return START_STICKY
    }

    fun IsForegroundServiceSupported(): Boolean {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    }


    override fun onDestroy() {
        if (IsForegroundServiceSupported()) {
            stopForeground(true)
        }
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getServiceOngoingNotification(context: Context): Notification {
        val notificationChannel =
                NotificationChannel("ks_01", "Service Notifications",
                        NotificationManager.IMPORTANCE_MIN)
        notificationChannel.enableLights(false)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
        mNotificationManager?.createNotificationChannel(notificationChannel)

        val title = context.getString(R.string.app_running_in_background_notification_title)
        val mainActivity = Intent(this, LoginActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, mainActivity, PendingIntent.FLAG_IMMUTABLE or 0)
        return NotificationCompat.Builder(
                context,
                "ks_01"
        ).setSmallIcon(R.drawable.app_notification_icon)
                .setWhen(0)
                .setContentTitle(title)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build()
    }


    companion object {

        fun startForegroundService(context: Context) {
            Log.d(TAG, "Starting foreground service")
            if (SERVICE_START_CALLED) {
                return
            }
            SERVICE_START_CALLED = true
            val intent = Intent(context, KitchenSinkForegroundService::class.java)
            intent.putExtra(STOP_REQUEST, false)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopForegroundService(context: Context) {
            Log.d(TAG, "Stopping foreground service")
            if (SERVICE_START_CALLED) {
                val intent = Intent(context, KitchenSinkForegroundService::class.java)
                intent.putExtra(STOP_REQUEST, true)
                ContextCompat.startForegroundService(context, intent)
                SERVICE_START_CALLED = false
            }
        }

        private var SERVICE_START_CALLED = false
        private const val STOP_REQUEST = "STOP_REQUEST"
        private val TAG = "KitchenSinkForegroundService"
    }
}