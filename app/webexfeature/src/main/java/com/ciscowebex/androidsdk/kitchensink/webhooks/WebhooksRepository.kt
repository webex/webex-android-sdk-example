package com.ciscowebex.androidsdk.kitchensink.webhooks

import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.webhook.Webhook
import io.reactivex.Observable
import io.reactivex.Single


class WebhooksRepository(private val webex: Webex) {

    fun list(max: Int) : Observable<List<Webhook>> {
        return Single.create<List<Webhook>> { emitter ->
            webex.webhooks.list(max, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val webhooksList = result.data
                    webhooksList?.let {
                        emitter.onSuccess(it)
                    } ?: run {
                        emitter.onSuccess(emptyList())
                    }
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun create(name: String, targetUrl: String, resource: String, event: String, filter: String?, secret: String?) : Observable<Webhook> {
        return Single.create<Webhook> { emitter ->
            webex.webhooks.create(name, targetUrl, resource, event, filter, secret, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val webhook = result.data
                    webhook?.let {
                        emitter.onSuccess(it)
                    } ?: run {
                        emitter.onError(Throwable(""))
                    }
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun get(webhookId: String) : Observable<Webhook> {
        return Single.create<Webhook> { emitter ->
            webex.webhooks.get(webhookId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val webhook = result.data
                    webhook?.let {
                        emitter.onSuccess(it)
                    } ?: run {
                        emitter.onError(Throwable(""))
                    }
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun update(webhookId: String, name: String, targetUrl: String, secret: String?, status: String?) : Observable<Webhook> {
        return Single.create<Webhook> { emitter ->
            webex.webhooks.update(webhookId, name, targetUrl, secret, status, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val webhook = result.data
                    webhook?.let {
                        emitter.onSuccess(it)
                    } ?: run {
                        emitter.onError(Throwable(""))
                    }
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun delete(webhookId: String) : Observable<Boolean> {
        return Single.create<Boolean> { emitter ->
            webex.webhooks.delete(webhookId, CompletionHandler { result ->
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