package com.ciscowebex.androidsdk.kitchensink.extras

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.kitchensink.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.Date

class ExtrasViewModel(private val extrasRepository: ExtrasRepository) : BaseViewModel() {
    private val tag = "ExtrasViewModel"

    private val _accessToken = MutableLiveData<String>()
    val accessToken: LiveData<String> = _accessToken

    private val _refreshToken = MutableLiveData<String>()
    val refreshToken: LiveData<String> = _refreshToken

    fun getAccessToken() {
        extrasRepository.getAccessToken().observeOn(AndroidSchedulers.mainThread()).subscribe({
            _accessToken.postValue(it)
        }, { _accessToken.postValue(null) }).autoDispose()
    }

    fun getRefreshToken() {
        extrasRepository.getRefreshToken().observeOn(AndroidSchedulers.mainThread()).subscribe({
            _refreshToken.postValue(it)
        }, {
            _refreshToken.postValue(it.message)
        }).autoDispose()
    }

    fun getJwtAccessTokenExpiration(): Date? {
        return extrasRepository.getJwtAccessTokenExpiration()
    }

}