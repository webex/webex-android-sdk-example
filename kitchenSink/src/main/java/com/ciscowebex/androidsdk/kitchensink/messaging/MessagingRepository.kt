package com.ciscowebex.androidsdk.kitchensink.messaging

import android.net.Uri
import android.util.Log
import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceMessageModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceModel
import com.ciscowebex.androidsdk.message.MessageClient
import com.ciscowebex.androidsdk.message.RemoteFile
import com.ciscowebex.androidsdk.CompletionHandler
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File

open class MessagingRepository(private val webex: Webex) {
    val tag = "MessagingRepository"

    enum class FileDownloadEvent {
        DOWNLOAD_COMPLETE,
        DOWNLOAD_FAILED
    }

    fun addSpace(spaceTitle: String, teamId: String?): Observable<SpaceModel> {
        return Single.create<SpaceModel> { emitter ->
            webex.spaces.create(spaceTitle, teamId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val space = result.data
                    emitter.onSuccess(SpaceModel.convertToSpaceModel(space))
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }


    fun deleteMessage(messageId: String): Observable<Boolean> {
        return Single.create<Boolean> { emitter ->
            webex.messages.delete(messageId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(true)
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }

    fun markMessageAsRead(spaceId: String, messageId: String? = null): Observable<Boolean> {
        return Single.create<Boolean> { emitter ->
            webex.messages.markAsRead(spaceId, messageId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(true)
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }

    fun getMessage(messageId: String): Observable<SpaceMessageModel>{
        return Single.create<SpaceMessageModel> { emitter ->
            webex.messages.get(messageId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(SpaceMessageModel.convertToSpaceMessageModel(result.data))
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }

            })
        }.toObservable()
    }

    fun downloadThumbnail(remoteFile: RemoteFile, file: File): Observable<Uri>{
        return Single.create<Uri> { emitter ->
            webex.messages.downloadThumbnail(remoteFile, file, CompletionHandler { result ->
                if (result.isSuccessful) {
                    if (result.data != null) {
                        emitter.onSuccess(result.data!!)
                    } else {
                        emitter.onError(Throwable("Unable to retrieve thumbnail"))
                    }
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }


    fun downloadFile(remoteFile: RemoteFile, file: File, progressEmitter: Emitter<Double>, completionEmitter: Emitter<Pair<FileDownloadEvent, String>>) {
        webex.messages.downloadFile(remoteFile, file,
                object : MessageClient.ProgressHandler {
                    override fun onProgress(bytes: Double) {
                        Log.d(tag, "downloadFile bytes: $bytes")
                        progressEmitter.onNext(bytes)
                    }
                },
                CompletionHandler { fileUrlResult ->
                    if (fileUrlResult.isSuccessful) {
                        Log.d(tag, "downloadFile onComplete success: ${fileUrlResult.data}")
                        fileUrlResult.data?.let {
                            completionEmitter.onNext(Pair(FileDownloadEvent.DOWNLOAD_COMPLETE, it.toString()))
                        } ?: run {
                            completionEmitter.onNext(Pair(FileDownloadEvent.DOWNLOAD_FAILED, "Download file error occurred"))
                        }
                    } else {
                        Log.d(tag, "downloadFile onComplete failed")
                        fileUrlResult.error?.let {
                            it.errorMessage?.let { errorMessage ->
                                completionEmitter.onNext(Pair(FileDownloadEvent.DOWNLOAD_FAILED, errorMessage))
                            } ?: run {
                                completionEmitter.onNext(Pair(FileDownloadEvent.DOWNLOAD_FAILED, "Download file error occurred"))
                            }
                        } ?: run {
                            completionEmitter.onNext(Pair(FileDownloadEvent.DOWNLOAD_FAILED, "Download file error occurred"))
                        }
                    }
                }
        )
    }
}