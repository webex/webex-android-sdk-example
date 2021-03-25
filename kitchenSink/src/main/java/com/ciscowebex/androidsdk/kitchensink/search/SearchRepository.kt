package com.ciscowebex.androidsdk.kitchensink.search

import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceModel
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.search.SearchResult
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

    fun search(query: String): Observable<List<SearchResult>> {
        return Single.create<List<SearchResult>> { emitter ->
            webex.getSearchResult(query, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data ?: emptyList())
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }
}