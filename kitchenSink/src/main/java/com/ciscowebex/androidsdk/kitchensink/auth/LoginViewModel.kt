package com.ciscowebex.androidsdk.kitchensink.auth

import android.webkit.WebView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.auth.JWTAuthenticator
import com.ciscowebex.androidsdk.auth.OAuthAuthenticator
import com.ciscowebex.androidsdk.auth.OAuthWebViewAuthenticator
import com.ciscowebex.androidsdk.kitchensink.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers

class LoginViewModel(private val webex: Webex, private val loginRepository: LoginRepository) : BaseViewModel() {
    private val _isAuthorized = MutableLiveData<Boolean>()
    val isAuthorized: LiveData<Boolean> = _isAuthorized

    private val _isAuthorizedCached = MutableLiveData<Boolean>()
    val isAuthorizedCached: LiveData<Boolean> = _isAuthorizedCached

    fun authorizeOAuth(loginWebview: WebView) {
        val oAuthAuthenticator = webex.authenticator as? OAuthWebViewAuthenticator
        oAuthAuthenticator?.let { auth ->
            loginRepository.authorizeOAuth(loginWebview, auth).observeOn(AndroidSchedulers.mainThread()).subscribe({
                _isAuthorized.postValue(it)
            }, {_isAuthorized.postValue(false)}).autoDispose()
        } ?: run {
            _isAuthorized.postValue(false)
        }
    }

    fun attemptToLoginWithCachedUser() {
        loginRepository.attemptToLoginWithCachedUser(webex).observeOn(AndroidSchedulers.mainThread()).subscribe({
            _isAuthorizedCached.postValue(it)
        }, {_isAuthorizedCached.postValue(false)}).autoDispose()
    }

    fun loginWithJWT(token: String) {
        val jwtAuthenticator = webex.authenticator as? JWTAuthenticator
        jwtAuthenticator?.let { auth ->
            loginRepository.loginWithJWT(token, auth).observeOn(AndroidSchedulers.mainThread()).subscribe({
                _isAuthorized.postValue(it)
            }, {_isAuthorized.postValue(false)}).autoDispose()
        } ?: run {
            _isAuthorized.postValue(false)
        }
    }

    fun authorizeOAuthCode(code: String) {
        val oAuthAuthenticator = webex.authenticator as? OAuthAuthenticator
        oAuthAuthenticator?.let { auth ->
            loginRepository.authorizeOAuthCode(code, auth).observeOn(AndroidSchedulers.mainThread()).subscribe({
                _isAuthorized.postValue(it)
            }, {_isAuthorized.postValue(false)}).autoDispose()
        } ?: run {
            _isAuthorized.postValue(false)
        }
    }
}