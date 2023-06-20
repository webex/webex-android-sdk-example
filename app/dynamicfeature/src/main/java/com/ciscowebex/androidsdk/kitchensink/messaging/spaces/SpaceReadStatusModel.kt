package com.ciscowebex.androidsdk.kitchensink.messaging.spaces

import com.ciscowebex.androidsdk.space.SpaceReadStatus
import com.ciscowebex.androidsdk.space.Space.SpaceType
import java.util.*

data class SpaceReadStatusModel(val spaceId: String, val spaceType: SpaceType?, val lastActivityDate: Date, val lastSeenDate: Date) {
    val spaceTypeString: String = spaceType?.name.orEmpty()
    val lastSeenDateTimeString: String = lastSeenDate.toString()
    val lastActivityTimestampString: String = lastActivityDate.toString()
    val isSpaceUnread: Boolean = lastActivityDate > lastSeenDate

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpaceReadStatus

        return spaceId == other.id
    }

    override fun hashCode(): Int {
        var result = spaceId.hashCode()
        result = 31 * result + spaceType.hashCode()
        result = 31 * result + lastActivityDate.hashCode()
        result = 31 * result + lastSeenDate.hashCode()
        return result
    }

    companion object {
        fun convertToSpaceReadStatusModel(spaceReadStatus: SpaceReadStatus?): SpaceReadStatusModel {
            return SpaceReadStatusModel(spaceReadStatus?.id.orEmpty(), spaceReadStatus?.type, spaceReadStatus?.lastActivityDate ?: Date(),
            spaceReadStatus?.lastSeenDate ?: Date())
        }
    }
}