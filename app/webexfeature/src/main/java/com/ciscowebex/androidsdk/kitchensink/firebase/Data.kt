package com.ciscowebex.androidsdk.kitchensink.firebase

import com.google.gson.annotations.SerializedName

data class Data(

        @SerializedName("id") val id: String?,
        @SerializedName("roomId") val roomId: String?,
        @SerializedName("callId") val callId: String?,
        @SerializedName("state") val state: String?,
        @SerializedName("roomType") val roomType: String?,
        @SerializedName("personId") val personId: String?,
        @SerializedName("personEmail") val personEmail: String?,
        @SerializedName("created") val created: String?
)