package com.ciscowebex.androidsdk.kitchensink.utils

import android.util.Base64
import android.util.Log
import com.ciscowebex.androidsdk.kitchensink.firebase.KitchenSinkFCMService

object Base64Utils {
    const val TAG = "Base64Utils"

    fun decodeString(encodedId: String?): String {
        val decodedBytes: ByteArray = Base64.decode(encodedId, Base64.DEFAULT)
        val decodedString = String(decodedBytes)
        val decodedId = decodedString.substring(decodedString.lastIndexOf("/") + 1)
        Log.d(TAG, "decodedString: $decodedString, decodedString: $decodedString, originalRoomId: $decodedId")
        return decodedId
    }
}