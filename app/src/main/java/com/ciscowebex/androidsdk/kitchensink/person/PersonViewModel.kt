package com.ciscowebex.androidsdk.kitchensink.person

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.kitchensink.BaseViewModel
import com.ciscowebex.androidsdk.people.PersonRole
import io.reactivex.android.schedulers.AndroidSchedulers

class PersonViewModel(private val personRepo: PersonRepository) : BaseViewModel() {
    private val tag = "PersonViewModel"

    private val _person = MutableLiveData<PersonModel>()
    val person: LiveData<PersonModel> = _person

    private val _personList = MutableLiveData<List<PersonModel>>()
    val personList: LiveData<List<PersonModel>> = _personList

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getMe() {
        personRepo.getMe().observeOn(AndroidSchedulers.mainThread()).subscribe({
            _person.postValue(it)
        }, { _person.postValue(null) }).autoDispose()
    }

    fun getPersonDetail(personId: String) {
        personRepo.getPersonDetail(personId).observeOn(AndroidSchedulers.mainThread()).subscribe({
            _person.postValue(it)
        }, { _person.postValue(null) }).autoDispose()
    }

    fun getPeopleList(email: String?, displayName: String?, id: String?, orgId: String?, max: Int) {
        personRepo.getPeopleList(email, displayName, id, orgId, max).observeOn(AndroidSchedulers.mainThread()).subscribe({ personModels ->
            _personList.postValue(personModels)
        }, {
            _personList.postValue(emptyList())
        }).autoDispose()
    }

    fun createPerson(email: String, displayName: String?, firstName: String?, lastName: String?, avatar: String?, orgId: String?, roles: List<PersonRole>, licenses: List<String>, siteUrls: List<String>) {
        personRepo.createPerson(email, displayName, firstName, lastName, avatar, orgId, roles, licenses, siteUrls).observeOn(AndroidSchedulers.mainThread()).subscribe({ personModels ->
            _personList.postValue(listOf(personModels))
        }, {
            _error.postValue(it.message)
        }).autoDispose()
    }

    fun updatePerson(personId: String, email: String, displayName: String?, firstName: String?, lastName: String?, avatar: String?, orgId: String?, roles: List<PersonRole>, licenses: List<String>, siteUrls: List<String>) {
        personRepo.updatePerson(personId, email, displayName, firstName, lastName, avatar, orgId, roles, licenses, siteUrls).observeOn(AndroidSchedulers.mainThread()).subscribe({ personModels ->
            _personList.postValue(listOf(personModels))
        }, {
            _error.postValue(it.message)
        }).autoDispose()
    }

    fun deletePerson(personId: String) {
        personRepo.deletePerson(personId).observeOn(AndroidSchedulers.mainThread()).subscribe({ personModels ->
            _personList.postValue(listOf())
        }, {
            _error.postValue(it.message)
        }).autoDispose()
    }
}