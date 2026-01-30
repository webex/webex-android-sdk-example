package com.ciscowebex.androidsdk.kitchensink.messaging.spaces

import com.ciscowebex.androidsdk.space.SpaceMeetingInfo


data class SpaceMeetingInfoModel(val spaceId: String, val meetingLink: String, val sipAddress: String, val meetingNumber: String, val callInTollFreeNumber: String, val callInTollNumber: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpaceMeetingInfo

        return spaceId == other.spaceId
    }

    override fun hashCode(): Int {
        var result = spaceId.hashCode()
        result = 31 * result + meetingLink.hashCode()
        result = 31 * result + sipAddress.hashCode()
        result = 31 * result + meetingNumber.hashCode()
        result = 31 * result + callInTollFreeNumber.hashCode()
        result = 31 * result + callInTollNumber.hashCode()
        return result
    }

    override fun toString(): String {
        return "Space Id: $spaceId\n\nMeeting Link: $meetingLink\n\nSIP Address: $sipAddress\n\nMeeting Number: $meetingNumber\n\nCall In Toll Free Number: $callInTollFreeNumber\n\nCall In Toll Number: $callInTollNumber"
    }

    companion object {
        fun convertToSpaceMeetingInfoModel(spaceMeetingInfo: SpaceMeetingInfo?): SpaceMeetingInfoModel {
            return SpaceMeetingInfoModel(spaceMeetingInfo?.spaceId.orEmpty(),
                    spaceMeetingInfo?.meetingLink.orEmpty(), spaceMeetingInfo?.sipAddress.orEmpty(),
                    spaceMeetingInfo?.meetingNumber.orEmpty(), spaceMeetingInfo?.callInTollFreeNumber.orEmpty(),
                    spaceMeetingInfo?.callInTollNumber.orEmpty())
        }
    }

}