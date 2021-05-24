package com.ciscowebex.androidsdk.kitchensink.person

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.kitchensink.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers

class PersonViewModel(private val personRepo: PersonRepository) : BaseViewModel() {
    private val tag = "PersonViewModel"

    private val _person = MutableLiveData<PersonModel>()
    val person: LiveData<PersonModel> = _person

    private val _personList = MutableLiveData<List<PersonModel>>()
    val personList: LiveData<List<PersonModel>> = _personList

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
        }, { _personList.postValue(emptyList()) }).autoDispose()
    }
}