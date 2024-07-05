package com.ciscowebex.androidsdk.kitchensink.utils

import android.util.Log
import com.ciscowebex.androidsdk.phone.Call
import com.ciscowebex.androidsdk.phone.CallObserver
import com.ciscowebex.androidsdk.kitchensink.utils.CallObjectStorage
import kotlin.system.exitProcess

internal class GlobalExceptionHandler : Thread.UncaughtExceptionHandler {
    private val tag = "GlobalExceptionHandler"
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Log.e(tag, "Uncaught exception", throwable)
        Log.d(tag, "Size of call object storage : ${CallObjectStorage.size()}")
        // Get the call that is in connected state inside the CallObjectStorage and hangup the call
        var call: Call? = null

        for (i in 0 until CallObjectStorage.size()) {
            val callObj = CallObjectStorage.getCallObjectFromIndex(i)
            if (callObj?.getStatus() == Call.CallStatus.CONNECTED) {
                Log.d(tag, "Call with id = ${callObj.getCallId()} found in connected state. ")
                call = callObj
                break
            }
        }
        CallObjectStorage.clearStorage()

        if (call == null) {
            Log.d(tag, "No call found in connected state. Proceeding to kill app!")
            killApp()
        }
        call?.setObserver(object : CallObserver {
            override fun onDisconnected(event: CallObserver.CallDisconnectedEvent?) {
                Log.d(tag, "Call disconnected fired, killing app!")
                killApp()
            }
        })
        call?.hangup {result ->
            if (result.isSuccessful) {
                Log.d(tag, "Call hung up success")
            } else {
                Log.e(tag, "Call hangup failed, reason : ${result.error?.errorMessage}")
                killApp()
            }
        } ?: run {
            Log.d(tag, "Crash detected & no active call found. Proceeding to kill app!")
            killApp()
        }
    }

    private fun killApp() {
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(1)
    }
}
