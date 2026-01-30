package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members

import android.graphics.drawable.Drawable
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceModel
import com.ciscowebex.androidsdk.membership.Membership
import java.util.*

data class MembershipModel(val membershipId: String, val personId: String, val personEmail: String,
                           val personDisplayName: String, val spaceId: String, val isModerator: Boolean,
                           val isMonitor: Boolean, val created: Date, val personOrgId: String, val personFirstName: String, val personLastName: String) {

    val createdDateTimeString: String = created.toString()
    val isModeratorString: String = isModerator.toString()
    val isMonitorString: String = isMonitor.toString()
    var presenceStatusText: String = ""
    var presenceStatusDrawable: Drawable? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MembershipModel

        return membershipId == other.membershipId
    }

    override fun hashCode(): Int {
        var result = membershipId.hashCode()
        result = 31 * result + personId.hashCode()
        result = 31 * result + personEmail.hashCode()
        result = 31 * result + personDisplayName.hashCode()
        result = 31 * result + spaceId.hashCode()
        result = 31 * result + isModerator.hashCode()
        result = 31 * result + isMonitor.hashCode()
        result = 31 * result + created.hashCode()
        result = 31 * result + personOrgId.hashCode()
        return result
    }

    companion object {
        fun convertToMembershipModel(membership: Membership?): MembershipModel {
            return MembershipModel(membership?.id.orEmpty(), membership?.personId.orEmpty(), membership?.personEmail.orEmpty(),
                    membership?.personDisplayName.orEmpty(), membership?.spaceId.orEmpty(), membership?.isModerator ?: false,
                    membership?.isMonitor ?: false, membership?.created ?: Date(), membership?.personOrgId.orEmpty(),
                    membership?.personFirstName.orEmpty(), membership?.personLastName.orEmpty())
        }
    }
}