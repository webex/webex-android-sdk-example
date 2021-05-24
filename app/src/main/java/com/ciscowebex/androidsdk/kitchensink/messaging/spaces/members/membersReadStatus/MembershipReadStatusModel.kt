package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.membersReadStatus

import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.MembershipModel
import com.ciscowebex.androidsdk.membership.MembershipReadStatus

data class MembershipReadStatusModel(val member: MembershipModel, val lastSeenId: String, val lastSeenDate: Long) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MembershipModel

        return member.membershipId == other.membershipId
    }

    override fun hashCode(): Int {
        var result = member.membershipId.hashCode()
        result = 31 * result + member.personId.hashCode()
        result = 31 * result + member.spaceId.hashCode()
        result = 31 * result + member.personDisplayName.hashCode()
        result = 31 * result + member.created.hashCode()
        result = 31 * result + member.personOrgId.hashCode()
        result = 31 * result + member.isModerator.hashCode()
        result = 31 * result + member.isMonitor.hashCode()
        result = 31 * result + member.personEmail.hashCode()
        return result
    }

    companion object {
        fun convertToMembershipReadStatusModel(membershipReadStatus: MembershipReadStatus?): MembershipReadStatusModel {
            return MembershipReadStatusModel(MembershipModel.convertToMembershipModel(membershipReadStatus?.membership),
                    membershipReadStatus?.lastSeenId.orEmpty(), membershipReadStatus?.lastSeenDate
                    ?: 0)
        }
    }
}