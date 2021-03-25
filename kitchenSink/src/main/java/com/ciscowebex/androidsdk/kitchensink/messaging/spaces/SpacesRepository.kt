package com.ciscowebex.androidsdk.kitchensink.messaging.spaces

import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.kitchensink.messaging.MessagingRepository
import com.ciscowebex.androidsdk.space.SpaceClient.SortBy
import com.ciscowebex.androidsdk.space.Space.SpaceType
import com.ciscowebex.androidsdk.CompletionHandler
import io.reactivex.Observable
import io.reactivex.Single

class SpacesRepository(private val webex: Webex) : MessagingRepository(webex) {
    fun fetchSpacesList(teamId: String?, maxSpaces: Int): Observable<List<SpaceModel>> {
        return Single.create<List<SpaceModel>> { emitter ->
            webex.spaces.list(teamId, maxSpaces, SpaceType.NONE, SortBy.ID, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data?.map {
                        SpaceModel.convertToSpaceModel(it)
                    } ?: emptyList())
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
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
                    emitter.onError(Throwable(result.error?.errorMessage))
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
                    emitter.onError(Throwable(result.error?.errorMessage))
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
                    emitter.onError(Throwable(result.error?.errorMessage))
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
                    emitter.onError(Throwable(result.error?.errorMessage))
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
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }

    fun listMessages(spaceId: String): Observable<List<SpaceMessageModel>> {
        return Single.create<List<SpaceMessageModel>> { emitter ->
            webex.messages.list(spaceId, null, 50, null, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data?.map { SpaceMessageModel.convertToSpaceMessageModel(it) }.orEmpty())
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
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
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }

    fun listSpacesWithActiveCalls(): Observable<List<String>> {
        return Single.create<List<String>> { emitter ->
            webex.spaces.listWithActiveCalls( CompletionHandler { result ->
                if (result.isSuccessful) {
                     emitter.onSuccess(result.data.orEmpty())
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }
}