package com.ciscowebex.androidsdk.kitchensink.messaging.teams

import java.util.Date

data class TeamModel(val id : String, val name : String, val createdDateTime : Date){

    val createdDateTimeString : String = createdDateTime.toString()

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(javaClass != other?.javaClass) return false

        other as TeamModel

        return id == other.id
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + createdDateTime.hashCode()
        result = 31 * result + createdDateTimeString.hashCode()
        return result
    }

}
