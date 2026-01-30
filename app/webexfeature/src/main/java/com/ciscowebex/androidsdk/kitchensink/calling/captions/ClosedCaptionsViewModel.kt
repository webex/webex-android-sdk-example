package com.ciscowebex.androidsdk.kitchensink.calling.captions

import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.kitchensink.BaseViewModel
import com.ciscowebex.androidsdk.phone.closedCaptions.CaptionItem
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ClosedCaptionsViewModel(private val repo: ClosedCaptionsRepository): BaseViewModel() {
    val captions = MutableLiveData<CaptionData>()
    private val disposables = CompositeDisposable()
    init {
        observeDataChanges()
    }

    private fun observeDataChanges() {
        val disposable = repo.dataStream
            .subscribeOn(Schedulers.io())
            .subscribe { data ->
                captions.postValue(data)
            }
        disposables.add(disposable)
    }

    fun updateData(newCaption: CaptionItem) {
        val caption = CaptionData(newCaption.getDisplayName(), newCaption.getTimeStamp(), newCaption.getContent())
        repo.setCaption(caption)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}