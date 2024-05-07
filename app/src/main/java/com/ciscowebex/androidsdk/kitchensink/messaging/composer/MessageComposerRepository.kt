package com.ciscowebex.androidsdk.kitchensink.messaging.composer

import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.message.LocalFile
import com.ciscowebex.androidsdk.message.Mention
import com.ciscowebex.androidsdk.message.Message
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.utils.EmailAddress
import io.reactivex.Observable
import io.reactivex.Single


open class MessageComposerRepository(private val webex: Webex) {

    fun postToSpace(spaceId: String, message: Message.Text, mentions: ArrayList<Mention>?, files: ArrayList<LocalFile>?): Observable<Message> {
        return Single.create<Message> { emitter ->
            webex.messages.postToSpace(spaceId, message, mentions, files, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data!!)
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun postToPerson(email: EmailAddress, message: Message.Text, files: ArrayList<LocalFile>?): Observable<Message> {
        return Single.create<Message> { emitter ->
            webex.messages.postToPerson(email, message, files, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data!!)
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun postToPerson(id: String, message: Message.Text, files: ArrayList<LocalFile>?): Observable<Message> {
        return Single.create<Message> { emitter ->
            webex.messages.postToPerson(id, message, files, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data!!)
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun postMessageDraft(target: String, draft: Message.Draft): Observable<Message> {
        return Single.create<Message> { emitter ->
            webex.messages.post(target, draft, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data!!)
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }
}