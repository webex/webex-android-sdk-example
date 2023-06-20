package com.ciscowebex.androidsdk.kitchensink.messaging.spaces

import android.os.Parcelable
import com.ciscowebex.androidsdk.message.Message
import kotlinx.android.parcel.Parcelize

class InternalMessage(private val model: ReplyMessageModel) : Message() {
    override fun getId(): String? {
        return model.messageId
    }

    override fun getParentId(): String? {
        return model.parentId
    }

    override fun getSpaceId(): String? {
        return model.spaceId
    }

    override fun getCreated(): Long {
        return model.created
    }

    override fun isSelfMentioned(): Boolean {
        return model.isSelfMentioned
    }

    override fun isReply(): Boolean {
        return model.isReply
    }

    override fun getPersonId(): String? {
        return model.personId
    }

    override fun getPersonEmail(): String? {
        return model.personEmail
    }

    override fun getToPersonId(): String? {
        return model.toPersonId
    }

    override fun getToPersonEmail(): String? {
        return model.toPersonEmail
    }
}

@Parcelize
class ReplyMessageModel(val spaceId: String, val messageId: String,
                        val created: Long, val isSelfMentioned: Boolean, val parentId: String,
                        val isReply: Boolean, val personId: String,
                        val personEmail: String, val toPersonId: String, val toPersonEmail: String) : Parcelable {

    fun getMessage(): Message {
        return InternalMessage(this)
    }
}
