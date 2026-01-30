package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.detail

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.kitchensink.BaseViewModel
import com.ciscowebex.androidsdk.kitchensink.messaging.MessagingRepository
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceMessageModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpacesRepository
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import com.ciscowebex.androidsdk.message.RemoteFile
import java.io.File

class MessageViewModel(private val spaceRepo: SpacesRepository) : BaseViewModel() {
    private val tag = "MessageViewModel"

    private val _message = MutableLiveData<SpaceMessageModel>()
    val message: LiveData<SpaceMessageModel> = _message

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _uri = MutableLiveData<Uri>()
    val thumbnailUri: LiveData<Uri> = _uri

    private val _downloadFileCompletionLiveData = MutableLiveData<Pair<MessagingRepository.FileDownloadEvent, String>>()
    val downloadFileCompletionLiveData: LiveData<Pair<MessagingRepository.FileDownloadEvent, String>> = _downloadFileCompletionLiveData

    private val _downloadFileProgressLiveData = MutableLiveData<Double>()
    val downloadFileProgressLiveData: LiveData<Double> = _downloadFileProgressLiveData

    fun getMessageDetail(messageId: String) {
        spaceRepo.getMessage(messageId).observeOn(AndroidSchedulers.mainThread()).subscribe({
            _message.postValue(it)
        }, { error -> _error.postValue(error.message) }).autoDispose()
    }

    fun getMessage(messageId: String): SpaceMessageModel? {
        return spaceRepo.getMessageWithoutObserver(messageId)
    }

    fun downloadThumbnail(remoteFile: RemoteFile, file: File) {
        spaceRepo.downloadThumbnail(remoteFile, file).observeOn(AndroidSchedulers.mainThread()).subscribe({
            _uri.postValue(it)
        }, { error -> _error.postValue(error.message) }).autoDispose()
    }

    fun downloadFile(remoteFile: RemoteFile, file: File) {
        lateinit var progressEmitter: Emitter<Double>
        val progressObserver = Observable.create(ObservableOnSubscribe<Double> { emitter ->
            progressEmitter = emitter
        })

        lateinit var completionEmitter: Emitter<Pair<MessagingRepository.FileDownloadEvent, String>>
        val completionObserver = Observable.create(ObservableOnSubscribe<Pair<MessagingRepository.FileDownloadEvent, String>> { emitter ->
            completionEmitter = emitter
        })

        progressObserver.observeOn(AndroidSchedulers.mainThread()).subscribe {
            it?.let {
                _downloadFileProgressLiveData.postValue(it)
            }
        }.autoDispose()

        completionObserver.observeOn(AndroidSchedulers.mainThread()).subscribe {
            it?.let {
                _downloadFileCompletionLiveData.postValue(it)
            }
        }.autoDispose()

        spaceRepo.downloadFile(remoteFile, file, progressEmitter, completionEmitter)
    }
}