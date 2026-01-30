package com.ciscowebex.androidsdk.kitchensink.base

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Base ViewModel class that provides automatic disposal of RxJava subscriptions.
 * All ViewModels in the app should extend this class.
 */
abstract class BaseViewModel : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    /**
     * Extension function to automatically dispose of Disposable when ViewModel is cleared.
     */
    fun Disposable.autoDispose() = compositeDisposable.add(this)
}
