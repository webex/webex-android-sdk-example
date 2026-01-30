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

    fun updateCallObject(callId: String, call: Call) {
        synchronized(this) {
            val callIndex = getCallObjectIndex(callId)
            if (callIndex != -1) {
                callObjects[callIndex] = call
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

    fun getCallObjectIndex(callId: String): Int {
        synchronized(this) {
            for (call in callObjects) {
                if (call.getCallId() == callId) {
                    return callObjects.indexOf(call)
                }
            }
            return -1
        }
    }

    fun getCallObjectFromIndex(index: Int): Call? {
        synchronized(this) {
            if (index >= 0 && index < callObjects.size) {
                return callObjects[index]
            }
            return null
        }
    }

    fun clearStorage() {
        synchronized(this) {
            callObjects.clear()
        }
    }

    fun size() : Int{
        return callObjects.size
    }
}