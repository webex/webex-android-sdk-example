package com.ciscowebex.androidsdk.kitchensink.webhooks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.kitchensink.BaseViewModel
import com.ciscowebex.androidsdk.webhook.Webhook
import io.reactivex.android.schedulers.AndroidSchedulers


class WebhooksViewModel(private val webhookRepo: WebhooksRepository) : BaseViewModel() {
    private val tag = "WebhooksViewModel"

    enum class WebhookEvent {
        CREATE,
        GET,
        UPDATE
    }

    private val _webhooksList = MutableLiveData<List<Webhook>>()
    val webhooksList: LiveData<List<Webhook>> = _webhooksList

    private val _webhooksError = MutableLiveData<String>()
    val webhooksError: LiveData<String> = _webhooksError

    private val _webhookData = MutableLiveData<Pair<WebhookEvent, Webhook>>()
    val webhookData: LiveData<Pair<WebhookEvent, Webhook>> = _webhookData

    private val _deleteWebhook = MutableLiveData<Boolean>()
    val deleteWebhook: LiveData<Boolean> = _deleteWebhook

    fun list(max: Int) {
        webhookRepo.list(max).observeOn(AndroidSchedulers.mainThread()).subscribe({
            _webhooksList.postValue(it)
        }, { error -> _webhooksError.postValue(error.message) }).autoDispose()
    }

    fun create(name: String, targetUrl: String, resource: String, event: String, filter: String?, secret: String?) {
        webhookRepo.create(name, targetUrl, resource, event, filter, secret).observeOn(AndroidSchedulers.mainThread()).subscribe({
            _webhookData.postValue(Pair(WebhookEvent.CREATE, it))
        }, { error -> _webhooksError.postValue(error.message) }).autoDispose()
    }

    fun get(webhookId: String) {
        webhookRepo.get(webhookId).observeOn(AndroidSchedulers.mainThread()).subscribe({
            _webhookData.postValue(Pair(WebhookEvent.GET, it))
        }, { error -> _webhooksError.postValue(error.message) }).autoDispose()
    }

    fun update(webhookId: String, name: String, targetUrl: String, secret: String?, status: String?) {
        webhookRepo.update(webhookId, name, targetUrl, secret, status).observeOn(AndroidSchedulers.mainThread()).subscribe({
            _webhookData.postValue(Pair(WebhookEvent.UPDATE, it))
        }, { error -> _webhooksError.postValue(error.message) }).autoDispose()
    }

    fun delete(webhookId: String) {
        webhookRepo.delete(webhookId).observeOn(AndroidSchedulers.mainThread()).subscribe({
            _deleteWebhook.postValue(it)
        }, { error -> _webhooksError.postValue(error.message) }).autoDispose()
    }
}