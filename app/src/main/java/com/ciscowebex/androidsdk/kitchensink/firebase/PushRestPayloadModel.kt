package com.ciscowebex.androidsdk.kitchensink.firebase

import com.google.gson.annotations.SerializedName

data class PushRestPayloadModel (
        @SerializedName("type") val type: String?,
        @SerializedName("pushid") val pushid: String?,
        @SerializedName("displayname") val displayname: String?,
        @SerializedName("displaynumber") val displaynumber: String?,
        @SerializedName("payloadversion") val payloadversion: String?,
        @SerializedName("huntpilotdn") val huntpilotdn: String?,
        @SerializedName("ringexpiretime") val ringexpiretime: String?
)