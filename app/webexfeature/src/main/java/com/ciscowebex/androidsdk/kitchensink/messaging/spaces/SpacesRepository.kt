package com.ciscowebex.androidsdk.kitchensink.messaging.spaces

import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.kitchensink.messaging.MessagingRepository
import com.ciscowebex.androidsdk.message.Before
import com.ciscowebex.androidsdk.space.Space.SpaceType
import com.ciscowebex.androidsdk.space.SpaceClient.SortBy
import io.reactivex.Observable
import io.reactivex.Single
import java.util.Date

class SpacesRepository(private val webex: Webex) : MessagingRepository(webex) {
    fun fetchSpacesList(teamId: String?, maxSpaces: Int, sortBy: SortBy): Observable<List<SpaceModel>> {
        return Single.create<List<SpaceModel>> { emitter ->
            webex.spaces.list(teamId, maxSpaces, null, sortBy, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data?.map {
                        SpaceModel.convertToSpaceModel(it)
                    } ?: emptyList())
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun fetchSpaceById(spaceId: String): Observable<SpaceModel> {
        return Single.create<SpaceModel> { emitter ->
            webex.spaces.get(spaceId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val space = result.data
                    emitter.onSuccess(SpaceModel.convertToSpaceModel(space))
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun updateSpace(spaceId: String, spaceName: String): Observable<SpaceModel> {
        return Single.create<SpaceModel> { emitter ->
            webex.spaces.update(spaceId, spaceName, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val space = result.data
                    emitter.onSuccess(SpaceModel.convertToSpaceModel(space))
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun fetchSpaceReadStatusList(maxSpaces: Int): Observable<List<SpaceReadStatusModel>> {
        return Single.create<List<SpaceReadStatusModel>> { emitter ->
            webex.spaces.listWithReadStatus(maxSpaces, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data?.map {
                        SpaceReadStatusModel.convertToSpaceReadStatusModel(it)
                    } ?: emptyList())
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun getMeetingInfo(spaceId: String): Observable<SpaceMeetingInfoModel> {
        return Single.create<SpaceMeetingInfoModel> { emitter ->
            webex.spaces.getMeetingInfo(spaceId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(SpaceMeetingInfoModel.convertToSpaceMeetingInfoModel(result.data))
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun getSpaceReadStatusById(spaceId: String): Observable<SpaceReadStatusModel> {
        return Single.create<SpaceReadStatusModel> { emitter ->
            webex.spaces.getWithReadStatus(spaceId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(SpaceReadStatusModel.convertToSpaceReadStatusModel(result.data))
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun listMessages(spaceId: String, beforeMessageId: String? = null, beforeMessageDate: Long? = null): Observable<List<SpaceMessageModel>> {
        return Single.create<List<SpaceMessageModel>> { emitter ->
            var before: Before? = null
            beforeMessageId?.let { before = Before.Message(it) }
            beforeMessageDate?.let { before = Before.Date(Date(it)) }
            webex.messages.list(spaceId, before, 100, null, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data?.map { SpaceMessageModel.convertToSpaceMessageModel(it) }.orEmpty())
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun delete(spaceId: String): Observable<Boolean> {
        return Single.create<Boolean> { emitter ->
            webex.spaces.delete(spaceId, CompletionHandler { result ->
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

    fun listSpacesWithActiveCalls(): Observable<List<String>> {
        return Single.create<List<String>> { emitter ->
            webex.spaces.listWithActiveCalls(CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data.orEmpty())
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun setSpacesSyncCompletedListener(): Observable<Boolean> {
        return Single.create<Boolean> { emitter ->
            webex.spaces.setOnSpaceSyncingStatusChangedListener() { result ->
                emitter.onSuccess(result.data ?: false)
            }
        }.toObservable()
    }

    // Commenting out but keeping this code for now in order to test encoding/decoding related code changes.
    // We can delete once encoding/decoding code is put in proper format and returns proper error state(IN FORM OF ENUM) from Omnius layer
    /*fun encodeDecodeTest() {
        webex.base64Encode(ResourceType.Memberships, "Rohit Sharma", CompletionHandler { result ->
            if(result.isSuccessful){
                Log.d("Enc/Dec Test", "Encoded String : ${result.data}")
                val decodedString = webex.base64Decode(result.data?: "Y2lzY29zcGFyazovL3VzL09SR0FOSVpBVElPTi9lZGI2OWJlOS1hMDNiLTQ4YzUtYWFmYi1lMmE2MjE0N2Q0NmM")
                Log.d("Enc/Dec Test", "Decoded String : $decodedString")
            }else {
                Log.d("Enc/Dec Test", "Error in encoding : ${result.error?.errorMessage}")
            }
        })
    }*/
}