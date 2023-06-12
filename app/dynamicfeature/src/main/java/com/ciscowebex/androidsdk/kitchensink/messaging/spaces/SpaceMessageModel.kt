package com.ciscowebex.androidsdk.kitchensink.messaging.spaces

import com.ciscowebex.androidsdk.space.Space.SpaceType
import com.ciscowebex.androidsdk.message.Message
import com.ciscowebex.androidsdk.message.RemoteFile
import java.util.Date

data class SpaceMessageModel(val spaceId: String, val messageId: String, val messageBody: Message.Text,
                             val created: Long, val isSelfMentioned: Boolean, val parentId: String,
                             val isReply: Boolean, val conversationType: SpaceType, val personId: String,
                             val personEmail: String, val toPersonId: String, val toPersonEmail: String, val attachments : List<RemoteFile>) {

    val createdDateTimeString: String = Date(created).toString()
    var mMessage: Message? = null
    companion object {
        fun convertToSpaceMessageModel(message: Message?): SpaceMessageModel {

            val model = SpaceMessageModel(message?.getSpaceId().orEmpty(), message?.getId().orEmpty(), message?.getTextAsObject()?: Message.Text(),
                    message?.getCreated() ?: 0, message?.isSelfMentioned() ?: false, message?.getParentId().orEmpty(),
                    message?.isReply() ?: false, SpaceType.valueOf(message?.getSpaceType().toString()), message?.getPersonId().orEmpty(),
                    message?.getPersonEmail().orEmpty(), message?.getToPersonId().orEmpty(), message?.getToPersonEmail().orEmpty(), message?.getFiles().orEmpty())
            model.mMessage = message
            return model
        }
    }
}