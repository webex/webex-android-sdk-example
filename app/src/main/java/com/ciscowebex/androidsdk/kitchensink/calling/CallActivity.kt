package com.ciscowebex.androidsdk.kitchensink.calling

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.internal.ResultImpl
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.CallRejectService
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityCallBinding
import com.ciscowebex.androidsdk.kitchensink.utils.CallObjectStorage
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.phone.*
import com.ciscowebex.androidsdk.phone.closedCaptions.CaptionItem
import com.ciscowebex.androidsdk.phone.closedCaptions.ClosedCaptionsInfo
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class CallActivity : BaseActivity(), CallControlsFragment.OnCallActionListener, CallObserverInterface {

    lateinit var binding: ActivityCallBinding
    private var pictureInPictureParamsBuilder:PictureInPictureParams.Builder? = null
    lateinit var callQueueAdapter: CallQueueAdapter
    var calls : ArrayList<Call>  = ArrayList()
    //var fragmentMap : HashMap<String, CallControlsFragment> = HashMap()
    var argumentList : HashMap<String, Bundle> = HashMap()


    companion object {
        fun getOutgoingIntent(context: Context, callerName: String, callType: Boolean, moveMeeting: Boolean): Intent {
            val intent = Intent(context, CallActivity::class.java)
            intent.putExtra(Constants.Intent.CALLING_ACTIVITY_ID, 0)
            intent.putExtra(Constants.Intent.OUTGOING_CALL_CALLER_ID, callerName)
            intent.putExtra(Constants.Intent.CALL_TYPE, callType)
            intent.putExtra(Constants.Intent.MOVE_MEETING, moveMeeting)
            return intent
        }
        fun getIncomingIntent(context: Context, callId: String? = null): Intent {
            val intent = Intent(context, CallActivity::class.java)
            intent.putExtra(Constants.Intent.CALLING_ACTIVITY_ID, 1)
            intent.putExtra(Constants.Intent.CALL_ID, callId)
            return intent
        }

        fun getCallAcceptIntent(context: Context, callId: String? = null): Intent {
            val intent = Intent(context, CallActivity::class.java)
            intent.action = Constants.Action.WEBEX_CALL_ACCEPT_ACTION
            intent.putExtra(Constants.Intent.CALLING_ACTIVITY_ID, 1)
            intent.putExtra(Constants.Intent.CALL_ID, callId)
            intent.putExtra(Constants.Action.WEBEX_CALL_ACCEPT_ACTION, true)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "CallActivity"
        DataBindingUtil.setContentView<ActivityCallBinding>(this, R.layout.activity_call)
            .also { binding = it }
            .apply {
                webexViewModel.callObserverInterface = this@CallActivity
                val callingActivity = intent.getIntExtra(Constants.Intent.CALLING_ACTIVITY_ID, 0)
                val fragment = addNewFragment()

                Handler(Looper.getMainLooper()).postDelayed({
                    if (callingActivity == 0) {
                        val callerId = intent.getStringExtra(Constants.Intent.OUTGOING_CALL_CALLER_ID)
                        val switchToUcmOrBroadworksCall = intent.getBooleanExtra(Constants.Intent.CALL_TYPE, false)
                        val moveMeeting = intent.getBooleanExtra(Constants.Intent.MOVE_MEETING, false)
                        val companionMode: CompanionMode = if (moveMeeting) CompanionMode.MoveMeeting else CompanionMode.None
                        callerId?.let {
                            fragment.dialOutgoingCall(callerId, isCucmOrWxcCall = switchToUcmOrBroadworksCall, moveMeeting = companionMode)
                        }
                    } else if (intent.action == Constants.Action.WEBEX_CALL_ACTION){
                        intent?.getStringExtra(Constants.Intent.CALL_ID) ?.let { callId ->
                            handleIncomingWebexCallFromFCM(callId)
                        }
                    } else if (intent.action == Constants.Action.WEBEX_CALL_ACCEPT_ACTION) {
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                        notificationManager?.cancel(Constants.Notification.WEBEX_CALL)
                    }
                }, 100)

                setupCallQueueView()
                registerIncomingCallListener()
            }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            pictureInPictureParamsBuilder = PictureInPictureParams.Builder()
        }
    }

    private fun registerIncomingCallListener() {
        webexViewModel.setIncomingListener()
        webexViewModel.incomingListenerLiveData.observe(this, Observer {
            it?.let {
                Log.d(tag, "incomingListenerLiveData: ${it.getCallId()} :${it.getTitle()}")
                Handler(Looper.getMainLooper()).post {
                    if (!(it.isCUCMCall() || it.isWebexCallingOrWebexForBroadworks())) {
                        it.getCallId()?.let { callId ->
                            var title = it.getTitle() ?: "Unknown"
                            sendNotificationForCall(callId, title, title)
                        }
                    }
                }
            }
        })
        webexViewModel.hasConflictCalls.observe(this) { hasConflictCalls ->
            Handler(Looper.getMainLooper()).post {
                if (hasConflictCalls) {
                    Toast.makeText(this, "Answering this call will end any active calls", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getLastFragment() : CallControlsFragment? {
        // Only one CallControlFragment expected in the stack
        for(fragment in supportFragmentManager.fragments){
            if(fragment is CallControlsFragment){
                return fragment
            }
        }
        return null
    }

    private fun dismissExistingDialogFragments() {
        for(fragment in supportFragmentManager.fragments){
            if(fragment is BottomSheetDialogFragment){
                fragment.dismissAllowingStateLoss()
            }
        }
    }


    private fun saveCurrentState() {
        val currentFragment = getLastFragment()
        var currentCallId = currentFragment?.getCurrentActiveCallId()
        currentCallId?.let{
            var arguments = currentFragment?.requireArguments()
            if(arguments == null){
                arguments = Bundle()
            }
            arguments.putString(Constants.Intent.CALL_ID, currentCallId)
            argumentList[it] = arguments
        }
    }

    private fun addNewFragment() :CallControlsFragment {
        val callId = intent?.getStringExtra(Constants.Intent.CALL_ID) ?: "default"
        var transaction = supportFragmentManager.beginTransaction()
        val newCallControlFragment = CallControlsFragment()
        newCallControlFragment.arguments = intent?.extras
        transaction.replace(R.id.fragment_container_view, newCallControlFragment, "call-"+callId)
        transaction.commit()
        return newCallControlFragment
    }

    private fun resumeFragment(resumedCall : Call) {
        var transaction = supportFragmentManager.beginTransaction()
        val newCallControlFragment = CallControlsFragment()
        newCallControlFragment.arguments = argumentList[resumedCall.getCallId()]
        transaction.replace(
            R.id.fragment_container_view,
            newCallControlFragment,
            "call-" + resumedCall.getCallId()!!
        )
        transaction.commit()
    }

    private fun setupCallQueueView(){
        callQueueAdapter = CallQueueAdapter(calls, object : CallQueueAdapter.OnItemActionListener {
            override fun onCallResumed(callId: String) {
                var call = CallObjectStorage.getCallObject(callId)
                call?.let {resumedCall ->
                    // Remove the resumed call from the hold queue
                    calls.remove(resumedCall)

                    // Save current fragment state mainly arguments
                    dismissExistingDialogFragments()
                    saveCurrentState()

                    // add current call to the queue
                    val currentFragment : CallControlsFragment? = getLastFragment()
                    var currentCallId = currentFragment?.getCurrentActiveCallId()
                    currentCallId?.let {
                        val currentCallObject = CallObjectStorage.getCallObject(it)

                        currentCallObject?.let {
                            it.holdCall(true)
                            calls.add(currentCallObject)
                        }

                        // resume the call
                        resumeFragment(resumedCall)
                        resumedCall.holdCall(false)

                    }
                }
                binding.callQueue?.adapter?.notifyDataSetChanged()
            }
        })

        binding.callQueue.adapter = callQueueAdapter
        binding.callQueue.visibility = View.GONE
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent?.action == Constants.Action.WEBEX_CALL_ACCEPT_ACTION) {
            // Dismiss the notification
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            notificationManager?.cancel(Constants.Notification.WEBEX_CALL)

            // Get Call Id from the intent extra
            val incomingCallId = intent?.getStringExtra(Constants.Intent.CALL_ID) ?: ""
            var call: Call? = CallObjectStorage.getCallObject(incomingCallId)

            call?.let {
                // Set Observer for new call
                webexViewModel.setCallObserver(call)
                dismissExistingDialogFragments()

                // Save current fragments argument for resuming later
                saveCurrentState()

                // add current call to the queue
                val currentFragment = getLastFragment()
                var currentCallId = currentFragment?.getCurrentActiveCallId()
                currentCallId?.let {
                    val currentCallObject = CallObjectStorage.getCallObject(it)
                    currentCallObject?.let {
                        calls.add(currentCallObject)

                        it.holdCall(true)

                        // Set observer for the paused call. In case it gets ended we need to remove from queue
                        webexViewModel.setCallObserver(currentCallObject)
                    }
                }

                // resume the new call
                addNewFragment()

                binding.callQueue?.adapter?.notifyDataSetChanged()
                binding.callQueue.visibility = View.VISIBLE
            }

        }
    }

    private fun handleIncomingWebexCallFromFCM(callId: String) {
        val fragment = getLastFragment()
        fragment?.let{
            fragment.handleFCMIncomingCall(callId)
        }
    }

    override fun onBackPressed() {
        val fragment = getLastFragment()
        fragment?.let{
            if(fragment.needBackPressed()){
                fragment.onBackPressed()
                return
            }
        }
        super.onBackPressed()
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
                    val fragment = getLastFragment()
                    fragment?.let{
                        val call = CallObjectStorage.getCallObject(newCallId)
                        call?.let { fragment.answerCall(it) }
                        handler.onComplete(ResultImpl.success(true))
                    }  ?: run {
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

    private fun pictureInPictureMode(){
        Log.d(tag, "pictureInPictureMode: Try to enter PIP mode")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Log.d(tag, "pictureInPictureMode: Supports PIP")
            val fragment = getLastFragment()
            fragment?.let {
                val aspectRatio = fragment.aspectRatio()
                pictureInPictureParamsBuilder?.setAspectRatio(aspectRatio)?.build()
                pictureInPictureParamsBuilder?.build()?.let { enterPictureInPictureMode(it) }
            }
        }
        else{
            Log.d(tag, "pictureInPictureMode: Doesn't support PIP")
            Toast.makeText(this, "Your device doesn't support PIP", Toast.LENGTH_LONG).show()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            Log.d(tag, "onUserLeaveHint: was not in PIP")
            pictureInPictureMode()
        }
        else{
            Log.d(tag, "onUserLeaveHint: Already in PIP")
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        val fragment = getLastFragment()
        fragment?.let {
            if (isInPictureInPictureMode) {
                Log.d(tag, "onPictureInPictureModeChanged: Entered PIP")
                fragment.pipVisibility(View.GONE, isInPictureInPictureMode)
            } else {
                Log.d(tag, "onPictureInPictureModeChanged: Exited PIP")
                fragment.pipVisibility(View.VISIBLE, isInPictureInPictureMode)
            }
        }
    }

    override fun onDisconnected(call: Call?, event: CallObserver.CallDisconnectedEvent?) {
        runOnUiThread {
            if (calls.contains(call)) {
                calls.remove(call)
                //binding.callQueue?.adapter?.notifyItemChanged(0)
                binding.callQueue?.adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onMediaQualityInfoChanged(mediaQualityInfo: Call.MediaQualityInfo) {
        TODO("Not yet implemented")
    }

    override fun onBroadcastMessageReceivedFromHost(message: String) {
        TODO("Not yet implemented")
    }

    override fun onHostAskingReturnToMainSession() {
        TODO("Not yet implemented")
    }

    override fun onJoinableSessionUpdated(breakoutSessions: List<BreakoutSession>) {
        TODO("Not yet implemented")
    }

    override fun onJoinedSessionUpdated(breakoutSession: BreakoutSession) {
        TODO("Not yet implemented")
    }

    override fun onReturnedToMainSession() {
        TODO("Not yet implemented")
    }

    override fun onSessionClosing() {
        TODO("Not yet implemented")
    }

    override fun onSessionEnabled() {
        TODO("Not yet implemented")
    }

    override fun onSessionJoined(breakoutSession: BreakoutSession) {
        TODO("Not yet implemented")
    }

    override fun onSessionStarted(breakout: Breakout) {
        TODO("Not yet implemented")
    }

    override fun onBreakoutUpdated(breakout: Breakout) {
        TODO("Not yet implemented")
    }

    override fun onBreakoutError(error: BreakoutSession.BreakoutSessionError) {
        TODO("Not yet implemented")
    }

    override fun onReceivingNoiseInfoChanged(info: ReceivingNoiseInfo) {
        TODO("Not yet implemented")
    }

    override fun onStartRinging(call: Call?, ringerType: Call.RingerType) {
        TODO("Not yet implemented")
    }

    override fun onStopRinging(call: Call?, ringerType: Call.RingerType) {
        TODO("Not yet implemented")
    }

    override fun onClosedCaptionsArrived(closedCaptions: CaptionItem) {
        TODO("Not yet implemented")
    }

    override fun onClosedCaptionsInfoChanged(closedCaptionsInfo: ClosedCaptionsInfo) {
        TODO("Not yet implemented")
    }

    override fun onMoveMeetingFailed(call: Call?) {
        TODO("Not yet implemented")
    }

    override fun finish() {
        if(calls.size > 0){
            //Resume a queued call
            var resumedCall = calls.get(0)
            var transaction = supportFragmentManager.beginTransaction()
            val newCallControlFragment = CallControlsFragment()
            newCallControlFragment.arguments = argumentList[resumedCall.getCallId()]
            transaction.replace(
                R.id.fragment_container_view,
                newCallControlFragment,
                "call-" + resumedCall.getCallId()!!
            )
            transaction.commit()
            calls.remove(resumedCall)
            //binding.callQueue?.adapter?.notifyItemChanged(0)
            binding.callQueue?.adapter?.notifyDataSetChanged()
        }else {
            super.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webexViewModel.cleanup()
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
            .setContentText("Call from Webex")
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


}