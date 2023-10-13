package com.ciscowebex.androidsdk.kitchensink.person

import android.graphics.drawable.Drawable
import android.os.Parcelable
import com.ciscowebex.androidsdk.people.Person
import com.ciscowebex.androidsdk.people.PersonRole
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class PersonModel(val personId: String, val encodedId: String, val emails: List<String>, val displayName: String,
                       val nickName: String, val firstName: String, val lastName: String,
                       val avatar: String, val orgId: String, val created: Date,
                       val lastActivity: String, val status: String, val type: String,
                       val licenses: List<String>, val siteUrls: List<String>,
                       val roles: List<PersonRole>) : Parcelable {

    val createdString: String = created.toString()
    val emailList = emails.joinToString()
    var presenceStatusText: String = ""
    var presenceStatusDrawable: Drawable? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersonModel

        return personId == other.personId
    }

    override fun hashCode(): Int {
        var result = personId.hashCode()
        result = 31 * result + encodedId.hashCode()
        result = 31 * result + emails.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + nickName.hashCode()
        result = 31 * result + firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + avatar.hashCode()
        result = 31 * result + orgId.hashCode()
        result = 31 * result + created.hashCode()
        result = 31 * result + lastActivity.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    companion object {
        fun convertToPersonModel(person: Person?): PersonModel {
            return PersonModel(person?.id.orEmpty(), person?.encodedId.orEmpty(), person?.emails.orEmpty(), person?.displayName.orEmpty(),
                    person?.nickName.orEmpty(), person?.firstName.orEmpty(), person?.lastName.orEmpty(),
                    person?.avatar.orEmpty(), person?.orgId.orEmpty(), person?.created ?: Date(),
                    person?.lastActivity.orEmpty(), person?.status.orEmpty(), person?.type.orEmpty(),
                    person?.licenses.orEmpty(), person?.siteUrls.orEmpty(), person?.roles.orEmpty())
        }
    }
}