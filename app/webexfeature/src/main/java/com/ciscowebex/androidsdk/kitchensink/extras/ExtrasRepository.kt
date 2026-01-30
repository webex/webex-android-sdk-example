package com.ciscowebex.androidsdk.kitchensink.extras

import android.util.Log
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.auth.JWTAuthenticator
import io.reactivex.Observable
import io.reactivex.Single
import java.util.Date

class ExtrasRepository(private val webex: Webex) {
    private val tag = "ExtrasRepository"
    fun getAccessToken(): Observable<String?> {
        return Single.create<String> { emitter ->
            webex.authenticator?.getToken(CompletionHandler { result ->
                if (result.isSuccessful) {
                    val token = result.data
                    emitter.onSuccess(token ?: "No Access Token yet")
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }

    fun getRefreshToken(): Observable<String?> {
        return Single.create<String> { emitter ->
            if (webex.authenticator is JWTAuthenticator) {
                (webex.authenticator as JWTAuthenticator).refreshToken(CompletionHandler { result ->
                    if (result.isSuccessful) {
                        val token = result.data
                        emitter.onSuccess(token ?: "No Access Token yet")
                    } else {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                })
            } else {
                emitter.onError(Throwable("Authenticator should be an instance of JWTAuthenticator"))
            }
        }.toObservable()
    }

    fun getJwtAccessTokenExpiration(): Date? {
        Log.d(tag, "isAuthorized : ${webex.authenticator?.isAuthorized()}")
        if (webex.authenticator is JWTAuthenticator) {
            return (webex.authenticator as JWTAuthenticator).getExpiration()
        }
        return null
    }
}