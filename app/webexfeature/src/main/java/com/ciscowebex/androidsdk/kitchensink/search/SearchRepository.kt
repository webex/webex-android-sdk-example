package com.ciscowebex.androidsdk.kitchensink.search

import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.phone.CallHistoryRecord
import com.ciscowebex.androidsdk.space.Space
import io.reactivex.Observable
import io.reactivex.Single

class SearchRepository(private val webex: Webex) {

    fun getCallHistory(): Observable<List<CallHistoryRecord>?> {
        val callHistoryRecords = webex.phone.getCallHistory()
        return Observable.just(callHistoryRecords ?: emptyList())
    }

    fun search(query: String): Observable<List<Space>> {
        return Single.create<List<Space>> { emitter ->
            webex.spaces.filter(query, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data ?: emptyList())
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }
}