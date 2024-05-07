package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members

import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.membersReadStatus.MembershipReadStatusModel
import com.ciscowebex.androidsdk.CompletionHandler
import io.reactivex.Observable
import io.reactivex.Single

class MembershipRepository(private val webex: Webex) {
    fun getMembersInSpace(spaceId: String?, max: Int?): Observable<List<MembershipModel>> {
        return Single.create<List<MembershipModel>> { emitter ->
            webex.memberships.list(spaceId, null, null, max, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data?.map {
                        MembershipModel.convertToMembershipModel(it)
                    } ?: emptyList())
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun getMembership(membershipId: String): Observable<MembershipModel> {
        return Single.create<MembershipModel> { emitter ->
            webex.memberships.get(membershipId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(MembershipModel.convertToMembershipModel(result.data))
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun updateMembershipWith(membershipId: String, isModerator: Boolean): Observable<MembershipModel> {
        return Single.create<MembershipModel> { emitter ->
            webex.memberships.update(membershipId, isModerator, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(MembershipModel.convertToMembershipModel(result.data))
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun delete(membershipId: String): Observable<Boolean> {
        return Single.create<Boolean> { emitter ->
            webex.memberships.delete(membershipId, CompletionHandler { result ->
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

    fun createMembershipWithId(spaceId: String, personId: String, isModerator: Boolean): Observable<MembershipModel> {
        return Single.create<MembershipModel> { emitter ->
            webex.memberships.create(spaceId, personId, null, false, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(MembershipModel.convertToMembershipModel(result.data))
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun createMembershipWithEmail(spaceId: String, emailId: String, isModerator: Boolean): Observable<MembershipModel> {
        return Single.create<MembershipModel> { emitter ->
            webex.memberships.create(spaceId, null, emailId, isModerator, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(MembershipModel.convertToMembershipModel(result.data))
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun listMembershipsWithReadStatus(spaceId: String): Observable<List<MembershipReadStatusModel>> {
        return Single.create<List<MembershipReadStatusModel>> { emitter ->
            webex.memberships.listWithReadStatus(spaceId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data?.map {
                        MembershipReadStatusModel.convertToMembershipReadStatusModel(it)
                    } ?: emptyList())
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

}