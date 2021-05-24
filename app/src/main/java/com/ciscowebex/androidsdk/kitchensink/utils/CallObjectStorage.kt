package com.ciscowebex.androidsdk.kitchensink.utils

import com.ciscowebex.androidsdk.phone.Call

object CallObjectStorage {
    private var callObjects: ArrayList<Call> = ArrayList()

    fun addCallObject(call: Call) {
        synchronized(this) {
            val callObj = getCallObject(call.getCallId() ?: "")
            if (callObj == null) {
                callObjects.add(call)
            }
        }
    }

    fun removeCallObject(callId: String) {
        synchronized(this) {
            val itr = callObjects.iterator()
            while (itr.hasNext()) {
                val call = itr.next()
                if (call.getCallId() == callId) {
                    itr.remove()
                }
            }
        }
    }

    fun getCallObject(callId: String): Call? {
        synchronized(this) {
            for (call in callObjects) {
                if (call.getCallId() == callId) {
                    return call
                }
            }

            return null
        }
    }

    fun clearStorage() {
        synchronized(this) {
            callObjects.clear()
        }
    }
}