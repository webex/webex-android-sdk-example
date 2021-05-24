package com.ciscowebex.androidsdk.kitchensink.search

import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceModel
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.space.Space
import io.reactivex.Observable
import io.reactivex.Single

class SearchRepository(private val webex: Webex) {

    fun getCallHistory(): Observable<List<SpaceModel>?> {
        val space = webex.phone.getCallHistory()

        return Observable.just(
                space?.map {
                    SpaceModel(it.id.orEmpty(), it.title.orEmpty(), it.type,
                            it.isLocked, it.lastActivity, it.created,
                            it.teamId.orEmpty(), it.sipAddress.orEmpty())
                } ?: emptyList()
        )
    }

    fun search(query: String): Observable<List<Space>> {
        return Single.create<List<Space>> { emitter ->
            webex.spaces.filter(query, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data ?: emptyList())
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }
}