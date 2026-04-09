package com.ciscowebex.androidsdk.kitchensink.extras

import android.os.Process
import android.system.Os
import android.system.OsConstants
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

    fun triggerJavaCrashForTesting(): Nothing {
        Log.e(tag, "Triggering Java/Kotlin crash for testing from Extras screen")
        throw IllegalStateException("KitchenSink requested Java/Kotlin crash for testing")
    }

    fun triggerNativeCrashForTesting(): Nothing {
        Log.e(tag, "Triggering native crash for testing from Extras screen via SIGABRT")
        Os.kill(Process.myPid(), OsConstants.SIGABRT)
        throw IllegalStateException("Native crash signal did not terminate the process")
    }

    fun triggerOmniusServiceNativeCrashForTesting(): Nothing {
        Log.e(tag, "Triggering native crash for testing from OmniusService layer (null pointer dereference)")
        invokeTestCrashMethod("triggerOmniusServiceNativeCrashForTesting", 0)
        throw IllegalStateException("OmniusService native crash trigger returned unexpectedly")
    }

    fun triggerOmniusServiceStackOverflowForTesting(): Nothing {
        Log.e(tag, "Triggering stack overflow for testing from OmniusService layer")
        invokeTestCrashMethod("triggerOmniusServiceNativeCrashForTesting", 1)
        throw IllegalStateException("OmniusService stack overflow trigger returned unexpectedly")
    }

    fun triggerOmniusServiceAbortForTesting(): Nothing {
        Log.e(tag, "Triggering SIGABRT for testing from OmniusService layer")
        invokeTestCrashMethod("triggerOmniusServiceNativeCrashForTesting", 2)
        throw IllegalStateException("OmniusService abort trigger returned unexpectedly")
    }

    fun triggerBridgeNativeCrashForTesting(): Nothing {
        Log.e(tag, "Triggering native crash for testing from IOmniusServiceBridge JNI layer")
        invokeTestCrashMethod("triggerBridgeNativeCrashForTesting")
        throw IllegalStateException("Bridge native crash trigger returned unexpectedly")
    }

    fun triggerWrapperJniNativeCrashForTesting(): Nothing {
        Log.e(tag, "Triggering native crash for testing from OmniusWrapper JNI layer")
        invokeTestCrashMethod("triggerWrapperJniNativeCrashForTesting")
        throw IllegalStateException("Wrapper JNI native crash trigger returned unexpectedly")
    }

    /**
     * Invokes a test crash method on the SDK's internal OmniusWrapper via reflection.
     *
     * These methods are intentionally NOT on the public Webex API surface. Reflection is
     * the standard pattern for test/debug utilities that must cross module boundaries
     * without polluting the public SDK interface.
     */
    private fun invokeTestCrashMethod(methodName: String, vararg args: Any) {
        try {
            val instanceField = Webex::class.java.getDeclaredField("instance")
            instanceField.isAccessible = true
            val internal = instanceField.get(webex)

            val paramTypes = args.map { arg ->
                when (arg) {
                    is Int -> Int::class.javaPrimitiveType
                    else -> arg.javaClass
                }
            }.toTypedArray()

            val method = internal.javaClass.getMethod(methodName, *paramTypes)
            method.invoke(internal, *args)
        } catch (e: Exception) {
            Log.e(tag, "Failed to invoke test crash method $methodName via reflection", e)
        }
    }
}