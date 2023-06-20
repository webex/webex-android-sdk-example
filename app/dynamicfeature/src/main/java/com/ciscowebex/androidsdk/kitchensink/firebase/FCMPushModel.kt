package com.ciscowebex.androidsdk.kitchensink.firebase

import com.google.gson.annotations.SerializedName

data class FCMPushModel(

        @SerializedName("id") val id: String?,
        @SerializedName("name") val name: String?,
        @SerializedName("targetUrl") val targetUrl: String?,
        @SerializedName("resource") val resource: String?,
        @SerializedName("event") val event: String?,
        @SerializedName("orgId") val orgId: String?,
        @SerializedName("createdBy") val createdBy: String?,
        @SerializedName("appId") val appId: String?,
        @SerializedName("ownedBy") val ownedBy: String?,
        @SerializedName("status") val status: String?,
        @SerializedName("created") val created: String?,
        @SerializedName("actorId") val actorId: String?,
        @SerializedName("data") val data: Data?
)