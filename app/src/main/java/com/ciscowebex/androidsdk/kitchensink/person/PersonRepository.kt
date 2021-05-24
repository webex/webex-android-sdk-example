package com.ciscowebex.androidsdk.kitchensink.person

import android.util.Log
import com.ciscowebex.androidsdk.Webex
import com.ciscowebex.androidsdk.CompletionHandler
import io.reactivex.Observable
import io.reactivex.Single

class PersonRepository(private val webex: Webex) {

    fun getMe(): Observable<PersonModel> {
        return Single.create<PersonModel> { emitter ->
            webex.people.getMe(CompletionHandler { result ->
                if (result.isSuccessful) {
                    val person = result.data
                    emitter.onSuccess(PersonModel.convertToPersonModel(person))
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
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
                    emitter.onError(Throwable(result.error?.errorMessage))
                }
            })
        }.toObservable()
    }

    fun getPeopleList(email: String?, displayName: String?, id: String?, orgId: String?, max: Int): Observable<List<PersonModel>> {
        return Single.create<List<PersonModel>> { emitter ->
            webex.people.list(email, displayName, id, orgId, max, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(result.data?.map { PersonModel.convertToPersonModel(it) }.orEmpty())
                    Log.d("CRUD_TEST", "Listed persons successfully");
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                    Log.d("CRUD_TEST", result.error?.errorMessage ?: "");
                }
            })
        }.toObservable()
    }

    fun createPerson(email: String, displayName: String?, firstName: String?, lastName: String?, avatar: String?, orgId: String?, roles: String?, licenses: String?): Observable<PersonModel> {
        return Single.create<PersonModel> { emitter ->
            webex.people.create(email, displayName, firstName, lastName, avatar, orgId, roles, licenses, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val person = result.data
                    emitter.onSuccess(PersonModel.convertToPersonModel(person))
                    Log.d("CRUD_TEST", "Created person successfully");
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                    Log.d("CRUD_TEST", result.error?.errorMessage ?: "");
                }
            })
        }.toObservable()
    }

    fun updatePerson(personId: String, email: String?, displayName: String?, firstName: String?, lastName: String?, avatar: String?, orgId: String?, roles: String?, licenses: String?): Observable<PersonModel> {
        return Single.create<PersonModel> { emitter ->
            webex.people.update(personId, email, displayName, firstName, lastName, avatar, orgId, roles, licenses, CompletionHandler { result ->
                if (result.isSuccessful) {
                    val person = result.data
                    emitter.onSuccess(PersonModel.convertToPersonModel(person))
                    Log.d("CRUD_TEST", "Updated person details successfully");
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                    Log.d("CRUD_TEST", result.error?.errorMessage ?: "");
                }
            })
        }.toObservable()
    }

    fun deletePerson(personId: String): Observable<Boolean> {
        return Single.create<Boolean> { emitter ->
            webex.people.delete(personId, CompletionHandler { result ->
                if (result.isSuccessful) {
                    emitter.onSuccess(true)
                    Log.d("CRUD_TEST", "Deleted person successfully");
                } else {
                    emitter.onError(Throwable(result.error?.errorMessage))
                    Log.d("CRUD_TEST", result.error?.errorMessage ?: "");
                }
            })
        }.toObservable()
    }
}