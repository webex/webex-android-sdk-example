package com.ciscowebex.androidsdk.kitchensink.messaging.teams.membership

import android.util.Log
import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.CompletionHandler
import io.reactivex.Observable
import io.reactivex.Single

class TeamMembershipRepository(private val webex: Webex) {
    fun getTeamMemberships(teamId: String, max: Int): Observable<List<TeamMembershipModel>> {
        return Single.create<List<TeamMembershipModel>> { emitter ->
            webex.teamMembershipClient.list(teamId, max, CompletionHandler { result ->
                Log.d(TeamMembershipRepository::class.java.name, "result: " + result.data)
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data?.map {
                        TeamMembershipModel.convertToMembershipModel(it)
                    } ?: emptyList())
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }

    fun getTeamMembership(teamMembershipId: String): Observable<TeamMembershipModel> {
        return Single.create<TeamMembershipModel> { emitter ->
            webex.teamMembershipClient.get(teamMembershipId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(TeamMembershipModel.convertToMembershipModel(result.data))
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }

    fun delete(teamMembershipId: String): Observable<Boolean> {
        return Single.create<Boolean> { emitter ->
            webex.teamMembershipClient.delete(teamMembershipId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(true)
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }

    fun updateMembership(teamMembershipId: String, isModerator: Boolean): Observable<TeamMembershipModel> {
        return Single.create<TeamMembershipModel> { emitter ->
            webex.teamMembershipClient.update(teamMembershipId, isModerator, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(TeamMembershipModel.convertToMembershipModel(result.data))
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }

    fun createMembershipWithId(teamId: String, personId: String, isModerator: Boolean): Observable<TeamMembershipModel> {
        return Single.create<TeamMembershipModel> { emitter ->
            webex.teamMembershipClient.create(teamId, personId, null, isModerator, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(TeamMembershipModel.convertToMembershipModel(result.data))
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }

    fun createMembershipWithEmail(teamId: String, emailId: String, isModerator: Boolean): Observable<TeamMembershipModel> {
        return Single.create<TeamMembershipModel> { emitter ->
            webex.teamMembershipClient.create(teamId, null, emailId, isModerator, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(TeamMembershipModel.convertToMembershipModel(result.data))
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }

}