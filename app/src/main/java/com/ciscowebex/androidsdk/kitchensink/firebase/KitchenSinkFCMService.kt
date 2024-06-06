package com.ciscowebex.androidsdk.kitchensink.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.internal.ResultImpl
import com.ciscowebex.androidsdk.kitchensink.BuildConfig
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkApp
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.CallRejectService
import com.ciscowebex.androidsdk.kitchensink.HomeActivity
import com.ciscowebex.androidsdk.kitchensink.calling.CallActivity
import com.ciscowebex.androidsdk.kitchensink.calling.CucmCallActivity
import com.ciscowebex.androidsdk.kitchensink.calling.LockScreenActivity
import com.ciscowebex.androidsdk.kitchensink.firebase.KitchenSinkFCMService.WebhookResources.CALL_MEMBERSHIPS
import com.ciscowebex.androidsdk.kitchensink.firebase.KitchenSinkFCMService.WebhookResources.MESSAGES
import com.ciscowebex.androidsdk.kitchensink.utils.Base64Utils
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.decryptPushRESTPayload
import com.ciscowebex.androidsdk.message.Message
import com.ciscowebex.androidsdk.phone.Call
import com.ciscowebex.androidsdk.phone.CallObserver
import com.ciscowebex.androidsdk.phone.NotificationCallType
import com.ciscowebex.androidsdk.phone.Phone
import com.ciscowebex.androidsdk.phone.PushNotificationResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import kotlin.random.Random

class KitchenSinkFCMService : FirebaseMessagingService() {

    private val repository: WebexRepository by inject()
    private var payloadPushRest: PushRestPayloadModel? = null

    private fun processPushMessageInternal(msg :String, handler: CompletionHandler<PushNotificationResult>) {
        if(!repository.isIncomingCallListenerSet("fcmservice")) {
            repository.setIncomingCallListener("fcmservice", object : Phone.IncomingCallListener {
                override fun onIncomingCall(call: Call?, hasActiveConflictCalls : Boolean) {
                    call?.let{
                        // Only incoming call notification of CUCM or Broadworks calling is handled here
                        // For both these cases we would get push notification for incoming call
                        if(!(it.isCUCMCall() || it.isWebexCallingOrWebexForBroadworks())){
                            Log.d(TAG, "Not notifying as calltype is not CUCM or WxC")
                            return
                        }
                    }
                    GlobalScope.launch(Dispatchers.Main) {
                        call?.let {
                            Log.d(TAG, "onIncomingCall processFCMMessage Call object  ${it.getCallId()} ${it.getTitle()}")
                            it.getCallId()?.let { callId ->
                                var title = it.getTitle() ?: "Unknown"
                                if(title.contains("Unknown")){
                                    title = getDisplayNameFromPushForCallId(callId)
                                }
                                sendNotificationForCall(callId, title, title)
                            }

                        } ?: run {
                            Log.d(TAG, "processFCMMessage Call object null")
                        }

                        call?.let{
                            repository.setCallObserver(it, object : CallObserver {
                                override fun onDisconnected(event: CallObserver.CallDisconnectedEvent?) {
                                    super.onDisconnected(event)
                                    val notificationManager =
                                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                                    notificationManager?.cancel(Constants.Notification.WEBEX_CALL)
                                    Handler(Looper.getMainLooper()).post {
                                        it.getCallId()?.let {callId->
                                            repository.removeCallObserver(
                                                callId,
                                                this
                                            )
                                        }
                                    }
                                }
                            })
                        }

                    }
                }
            })
        }
        repository.webex.phone.processPushNotification(msg) {
            if (it.isSuccessful) {
                if (it.data == PushNotificationResult.NoError) {
                    Log.d(TAG, it.data.toString())
                    handler.onComplete(ResultImpl.success(PushNotificationResult.NoError))
                }
            } else {
                Log.e(TAG, "Error code: ${it.error?.errorCode}, message: ${it.error?.errorMessage}")
                handler.onComplete(ResultImpl.error(it.error?.errorMessage ?: "InternalError"))
            }
        }
    }

    private fun processFCMMessage(remoteMessage: RemoteMessage) {
        GlobalScope.launch(Dispatchers.Main) {
            val payload = remoteMessage.data

            var fcmDataPayload = "none"
            for ((key, value) in payload) {
                Log.i(TAG, "$key = $value")
                // If its from webhook unpack and use. else regular flow
                if(key == "pinpoint.jsonBody") {
                    val jsonValue = JSONObject(value)
                    Log.i(TAG, "" + jsonValue["webhookTelephonyPush"])
                    fcmDataPayload = (jsonValue["webhookTelephonyPush"] as String)
                    break
                }
            }

            if(fcmDataPayload == "none"){
                fcmDataPayload = repository.webex.phone.buildNotificationPayload(remoteMessage.data, remoteMessage.messageId.orEmpty())
            }
            val parsedFcmPayload = repository.webex.parseVoipPayload(fcmDataPayload)
            Log.i(TAG, "Parsed FCM Payload: Name: ${parsedFcmPayload.getName()}, Number: ${parsedFcmPayload.getNumber()}, pushId: ${parsedFcmPayload.getPushId()}, TrackingId: ${parsedFcmPayload.getExternalTrackingId()}, version:${parsedFcmPayload.getVersion()}")
            val authorised = repository.webex.authenticator?.isAuthorized()
            if (authorised == true) {
                processPushMessageInternal(fcmDataPayload) {
                    if (!it.isSuccessful && it.error?.errorMessage == PushNotificationResult.NotWebexCallingPush.name) {
                        cucmPushRestFlow(remoteMessage)
                    }
                }
            }
            else {
                repository.webex.initialize {
                    if (repository.webex.authenticator?.isAuthorized() == true) {
                        processPushMessageInternal(fcmDataPayload) {
                            if (!it.isSuccessful && it.error?.errorMessage == PushNotificationResult.NotWebexCallingPush.name) {
                                cucmPushRestFlow(remoteMessage)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getDisplayNameFromPushForCallId(callId: String): String {
        payloadPushRest?.let {
            it.pushid?.let { pushId ->
                run{
                    val actualCallId = repository.webex.getCallIdByNotificationId(pushId, NotificationCallType.Cucm)
                    if(actualCallId == callId){
                        // Cached payloadPushRest belongs to current call. Return display name
                        return it.displayname ?: "Unknown"
                    }
                }
            }
        }
        return "Unknown"
    }

    private fun getDisplayNameFromPush(): String {
        // For CUCM initially the title is null/unknown initially
        // so using the display name that comes in push
        return payloadPushRest?.displayname?: "Unknown"
    }

    private fun getCallFromPush(pushId: String): Call? {
        val actualCallId = repository.webex.getCallIdByNotificationId(pushId, NotificationCallType.Cucm)
        val callInfo = repository.getCall(actualCallId)
        return callInfo
    }

    private fun cucmPushRestFlow(remoteMessage: RemoteMessage) {
        var notificationData: FCMPushModel?
        if (remoteMessage.data.isNotEmpty()) {
            // CUCM Push Rest Flow
            val map = remoteMessage.data
            val pushRestPayload = map["body"]
            if (!pushRestPayload.isNullOrEmpty()) {
                GlobalScope.launch(Dispatchers.Main) {
                    Log.d(TAG, "Starting UC services in FCM service")
                    repository.webex.startUCServices()
                    Log.d(TAG, "Payload from PushREST : $pushRestPayload")
                    val decryptedPayload = repository.webex.phone.decryptCUCMPushMessage(pushRestPayload)
                    Log.d(TAG, "Decrypted payload : ${decryptedPayload.getPushId()}, ${decryptedPayload.getDisplayName()}")
                    if (decryptedPayload.getPushId().isNotEmpty()) {
                        payloadPushRest = PushRestPayloadModel(null, decryptedPayload.getPushId(), decryptedPayload.getDisplayName(), null, null, null, null)
                        run {
                            val call = getCallFromPush(decryptedPayload.getPushId())
                            call?.let {_call ->
                                _call.getCallId()?.let { callId ->
                                    sendNotificationForCall(callId, decryptedPayload.getDisplayName(), decryptedPayload.getDisplayName())
                                }
                            }
                        }
                    }
                }
            } else {
                // FCM triggered via webhook from push notification server
                val data = map["data"]
                data?.let {
                    val jsonObject = JSONObject(it)
                    Log.d(TAG, "Message data payload: remoteMessage.data -> $jsonObject")
                    notificationData = getFCMModel(jsonObject.toString())
                    when (notificationData?.resource) {
                        MESSAGES.value -> {
                            buildMessageNotification(notificationData)
                        }
                        CALL_MEMBERSHIPS.value -> {
                            //send call notification
                            notificationData?.let { data ->
                                buildCallNotification(data)
                            }
                        }
                        else -> {
                            Log.d(
                                TAG,
                                "Unknown resource found : Resource: ${notificationData?.resource}"
                            )
                        }
                    }
                }
            }
        }
    }



    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.from)
        Log.d(TAG, "APP chg isInForeground: " + KitchenSinkApp.inForeground)

        if(!(application as KitchenSinkApp).loadModules()){
            Log.w(TAG, "Login type unknown")
            return
        }
        processFCMMessage(remoteMessage)
    }

    private fun buildCallNotification(data: FCMPushModel) {
        val callId = Base64Utils.decodeString(data.data?.callId) //locus sessionId returned
        Handler(Looper.getMainLooper()).postDelayed({
            val actualCallId = repository.getCallIdByNotificationId(callId, NotificationCallType.Webex)
            val callInfo = repository.getCall(actualCallId)
            Log.d(TAG, "CallInfo ${callInfo?.getCallId()} title ${callInfo?.getTitle()}")
            sendCallNotification(callInfo)
        }, 100)
    }

    private fun sendCallNotification(callInfo: Call?, caller: String? = null) {
        val notificationId = Random.nextInt(10000)
        val requestCode = Random.nextInt(10000)
        val intent = CallActivity.getIncomingIntent(this)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(Constants.Intent.CALL_ID, callInfo?.getCallId())
        intent.action = Constants.Action.WEBEX_CALL_ACTION

        val pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)
        val channelId: String = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.app_notification_icon)
                .setContentTitle("$caller is calling")
                .setContentText(getString(R.string.call_description))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    WEBEX_CALL_CHANNEL,
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager?.createNotificationChannel(channel)
        }
        notificationManager?.notify(notificationId, notificationBuilder.build())
    }

    private fun sendCucmCallNotification(pushId: String?, caller: String? = null) {
        val notificationId = Random.nextInt(10000)
        val requestCode = Random.nextInt(10000)
        val intent = CucmCallActivity.getIncomingIntent(this, pushId)

        val pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)
        val channelId: String = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.app_notification_icon)
                .setContentTitle("$caller is calling")
                .setContentText(getString(R.string.call_description))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    WEBEX_CALL_CHANNEL,
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager?.createNotificationChannel(channel)
        }
        notificationManager?.notify(notificationId, notificationBuilder.build())
    }

    private fun buildMessageNotification(notificationData: FCMPushModel?) {
        val roomId = Base64Utils.decodeString(notificationData?.data?.roomId)
        repository.listMessages(roomId) {
            Log.d(TAG, "message size: ${it.data?.size}")
            val size = it.data?.size ?: 0
            if (size > 0) {
                val message = it.data?.get(size - 1)
                Log.d(TAG, "last message: ${message?.getText()}")

                Log.d(TAG, "Fetching person details")
                repository.getPerson(
                    Base64Utils.decodeString(notificationData?.data?.personId)
                ) { personResult ->
                    Log.d(TAG, "Fetching space details")
                    repository.getSpace(
                        Base64Utils.decodeString(notificationData?.data?.roomId)
                    ) { spaceResult ->
                        sendNotification(
                            personResult.data?.displayName.orEmpty(),
                            spaceResult.data?.title.orEmpty(),
                            message
                        )
                    }
                }
            } else {
                Log.d(TAG, "message not found")
            }
        }
    }

    private fun getFCMModel(data: String): FCMPushModel {
        return Gson().fromJson(data, FCMPushModel::class.java)
    }

    /**
     * Called if FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve
     * the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        if(!(application as KitchenSinkApp).loadModules()){
            Log.w(TAG, "Login type unknown")
            return
        }

        val setToken = {
            FirebaseInstallations.getInstance().id.addOnCompleteListener(object : OnCompleteListener<String?> {
                override fun onComplete(task: Task<String?>) {
                    if (!task.isSuccessful) {
                        Log.w(TAG, "Fetching FCM registration id failed", task.exception)
                        return
                    }
                    val mId = task.result
                    mId?.let {
                        if(BuildConfig.WEBHOOK_URL.isEmpty()) {
                            repository.webex.phone.setPushTokens(
                                KitchenSinkApp.applicationContext().packageName,
                                it,
                                token,
                                Constants.Keys.appId
                            )
                        }
                    }
                }
            })
        }

        val authorised = repository.webex.authenticator?.isAuthorized()
        if (authorised == true) {
            setToken()
        }
        else {
            repository.webex.initialize {
                if (repository.webex.authenticator?.isAuthorized() == true) {
                    setToken()
                }
            }
        }

    }

    private fun sendNotificationForCall(callId:String, number: String, spaceTitle: String) {
        val notificationId = Constants.Notification.WEBEX_CALL

        val acceptIntent = CallActivity.getCallAcceptIntent(this, callId = callId)
        val rejectIntent = CallRejectService.getCallRejectIntent(this,callId = callId)
        val fullScreenIntent = LockScreenActivity.getLockScreenIntent(this, callId = callId)

        val intentFlag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val acceptPendingIntent = PendingIntent.getActivity(this, Constants.Intent.ACCEPT_REQUEST_CODE, acceptIntent, intentFlag)
        val rejectPendingIntent = PendingIntent.getService(this, Constants.Intent.REJECT_REQUEST_CODE, rejectIntent, intentFlag)
        val fullScreenPendingIntent = PendingIntent.getActivity(this, Constants.Intent.FULLSCREEN_REQUEST_CODE, fullScreenIntent, intentFlag)
        val channelId = "Calls"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.app_notification_icon)
                .setContentTitle(spaceTitle)
                .setContentText(number)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .addAction(0, "Accept", acceptPendingIntent)
                .addAction(0, "Reject", rejectPendingIntent)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    "Calls",
                    NotificationManager.IMPORTANCE_HIGH)
            notificationManager?.createNotificationChannel(channel)
        }
        notificationManager?.notify(notificationId, notificationBuilder.build())
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     */
    private fun sendNotification(personTitle: String, spaceTitle: String, message: Message?) {
        val notificationId = Random.nextInt(10000)
        val requestCode = Random.nextInt(10000)
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(Constants.Bundle.MESSAGE_ID, message?.getId().orEmpty())
        intent.action = Constants.Action.MESSAGE_ACTION

        val pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)
        val channelId: String = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.app_notification_icon)
                .setContentTitle(spaceTitle)
                .setContentText(personTitle)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(
                        NotificationCompat.BigTextStyle()
                                .bigText(Html.fromHtml(message?.getText().orEmpty(), Html.FROM_HTML_MODE_LEGACY))
                )
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                    MESSAGE_CHANNEL,
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager?.createNotificationChannel(channel)
        }
        notificationManager?.notify(notificationId, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "PUSHREST"
        private const val WEBEX_CALL_CHANNEL = "WebexCallChannel"
        private const val CUCM_CALL_CHANNEL = "CUCMCallChannel"
        private const val MESSAGE_CHANNEL = "MessageChannel"
    }

    enum class WebhookResources(var value: String) {
        MESSAGES("messages"), CALL_MEMBERSHIPS("callMemberships")
    }
}