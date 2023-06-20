package com.ciscowebex.androidsdk.kitchensink.messaging.teams.membership

import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceModel
import com.ciscowebex.androidsdk.team.TeamMembership
import java.util.*

data class TeamMembershipModel(val teamMembershipId: String, val personId: String, val personEmail: String,
                               val personDisplayName: String, val isModerator: Boolean, val created: Date,
                               val personOrgId: String) {

    val createdDateTimeString: String = created.toString()
    val isModeratorString: String = isModerator.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpaceModel

        return teamMembershipId == other.id
    }

    override fun hashCode(): Int {
        var result = teamMembershipId.hashCode()
        result = 31 * result + personId.hashCode()
        result = 31 * result + personEmail.hashCode()
        result = 31 * result + personDisplayName.hashCode()
        result = 31 * result + isModerator.hashCode()
        result = 31 * result + created.hashCode()
        result = 31 * result + personOrgId.hashCode()
        return result
    }

    companion object {
        fun convertToMembershipModel(membership: TeamMembership?): TeamMembershipModel {
            return TeamMembershipModel(membership?.id.orEmpty(), membership?.personId.orEmpty(), membership?.personEmail.orEmpty(),
                                    membership?.personDisplayName.orEmpty(), membership?.isModerator ?: false,
                                    membership?.created ?: Date(), membership?.personOrgId.orEmpty())
        }
    }
}