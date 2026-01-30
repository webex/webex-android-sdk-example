package com.ciscowebex.androidsdk.kitchensink.messaging.teams

import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.kitchensink.messaging.MessagingRepository
import com.ciscowebex.androidsdk.CompletionHandler
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

class TeamsRepository(private val webex: Webex) : MessagingRepository(webex) {
    fun fetchTeamsList(maxTeams: Int): Observable<List<TeamModel>> {
        return Single.create<List<TeamModel>> { emitter ->
            webex.teams.list(maxTeams, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data?.filter { !it.isDeleted }?.map { TeamModel(it.id.orEmpty(), it.name.orEmpty(), it.created) }
                            ?: emptyList())
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun fetchTeamById(teamId: String): Observable<TeamModel> {
        return Single.create<TeamModel> { emitter ->
            webex.teams.get(teamId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val team = result.data
                    emitter.onSuccess(TeamModel(team?.id.orEmpty(), team?.name.orEmpty(), team?.created
                            ?: Date()))
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun createTeam(teamName: String): Observable<TeamModel> {
        return Single.create<TeamModel> { emitter ->
            webex.teams.create(teamName, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val team = result.data
                    emitter.onSuccess(TeamModel(team?.id.orEmpty(), team?.name.orEmpty(), team?.created
                            ?: Date()))
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun updateTeam(teamId: String, teamName: String): Observable<TeamModel> {
        return Single.create<TeamModel> { emitter ->
            webex.teams.update(teamId, teamName, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val team = result.data
                    emitter.onSuccess(TeamModel(team?.id.orEmpty(), team?.name.orEmpty(), team?.created
                            ?: Date()))
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun deleteTeamWithId(teamId: String): Observable<Boolean> {
        return Single.create<Boolean> { emitter ->
            webex.teams.delete(teamId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(true)
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }
}