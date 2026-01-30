package com.ciscowebex.androidsdk.kitchensink.person

import android.util.Log
import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.CompletionHandler
import com.ciscowebex.androidsdk.people.PersonRole
import io.reactivex.Observable
import io.reactivex.Single

class PersonRepository(private val webex: Webex) {

    private val tag = javaClass.name
    fun getMe(): Observable<PersonModel> {
        return Single.create<PersonModel> { emitter ->
            webex.people.getMe(CompletionHandler { result ->
                if (result.isSuccessful) {
                    val person = result.data
                    emitter.onSuccess(PersonModel.convertToPersonModel(person))
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun getPersonDetail(personId: String): Observable<PersonModel> {
        return Single.create<PersonModel> { emitter ->
            webex.people.get(personId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val person = result.data
                    emitter.onSuccess(PersonModel.convertToPersonModel(person))
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                }
            })
        }.toObservable()
    }

    fun getPeopleList(email: String?, displayName: String?, id: String?, orgId: String?, max: Int): Observable<List<PersonModel>> {
        return Single.create<List<PersonModel>> { emitter ->
            webex.people.list(email, displayName, id, orgId, max, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data?.map { PersonModel.convertToPersonModel(it) }.orEmpty())
                    Log.d(tag, "Listed persons successfully");
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                    Log.d(tag, result.error?.errorMessage ?: "");
                }
            })
        }.toObservable()
    }

    fun createPerson(email: String, displayName: String?, firstName: String?, lastName: String?, avatar: String?, orgId: String?, roles: List<PersonRole>, licenses: List<String>, siteUrls: List<String>): Observable<PersonModel> {
        return Single.create<PersonModel> { emitter ->
            webex.people.create(email, displayName, firstName, lastName, avatar, orgId, roles, licenses, siteUrls, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val person = result.data
                    emitter.onSuccess(PersonModel.convertToPersonModel(person))
                    Log.d(tag, "Created person successfully");
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                    Log.d(tag, result.error?.errorMessage ?: "");
                }
            })
        }.toObservable()
    }

    fun updatePerson(personId: String, email: String?, displayName: String?, firstName: String?, lastName: String?, avatar: String?, orgId: String?, roles: List<PersonRole>, licenses: List<String>, siteUrls: List<String>): Observable<PersonModel> {
        return Single.create<PersonModel> { emitter ->
            webex.people.update(personId, email, displayName, firstName, lastName, avatar, orgId, roles, licenses, siteUrls, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val person = result.data
                    emitter.onSuccess(PersonModel.convertToPersonModel(person))
                    Log.d(tag, "Updated person details successfully");
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                    Log.d(tag, result.error?.errorMessage ?: "");
                }
            })
        }.toObservable()
    }

    fun deletePerson(personId: String): Observable<Boolean> {
        return Single.create<Boolean> { emitter ->
            webex.people.delete(personId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(true)
                    Log.d(tag, "Deleted person successfully");
                } else {
                    if (!emitter.isDisposed) {
                        emitter.onError(Throwable(result.error?.errorMessage))
                    }
                    Log.d(tag, result.error?.errorMessage ?: "");
                }
            })
        }.toObservable()
    }
}