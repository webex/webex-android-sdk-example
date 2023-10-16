package com.ciscowebex.androidsdk.kitchensink.messaging.spaces

import android.graphics.drawable.Drawable
import com.ciscowebex.androidsdk.space.Space
import com.ciscowebex.androidsdk.space.Space.SpaceType
import java.util.*

data class SpaceModel(
    val id: String,
    val title: String,
    val spaceType: SpaceType?,
    val isLocked: Boolean,
    val lastActivity: Date,
    val created: Date,
    val teamId: String,
    val sipAddress: String,
    val isExternallyOwned: Boolean?
) {

    val createdDateTimeString: String = created.toString()
    val lastActivityTimestampString: String = lastActivity.toString()
    var presenceStatus: Drawable? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpaceModel

        return id == other.id
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + spaceType.hashCode()
        result = 31 * result + isLocked.hashCode()
        result = 31 * result + lastActivity.hashCode()
        result = 31 * result + created.hashCode()
        result = 31 * result + teamId.hashCode()
        result = 31 * result + sipAddress.hashCode()
        result = 31 * result + createdDateTimeString.hashCode()
        result = 31 * result + lastActivityTimestampString.hashCode()
        result = 31 * result + isExternallyOwned.hashCode()
        return result
    }

    companion object {
        fun convertToSpaceModel(space: Space?): SpaceModel {
            return SpaceModel(space?.id.orEmpty(), space?.title.orEmpty(), space?.type,
                    space?.isLocked ?: false, space?.lastActivity
                    ?: Date(), space?.created ?: Date(),
                    space?.teamId.orEmpty(), space?.sipAddress.orEmpty(), space?.isExternallyOwned)
        }
    }

}
