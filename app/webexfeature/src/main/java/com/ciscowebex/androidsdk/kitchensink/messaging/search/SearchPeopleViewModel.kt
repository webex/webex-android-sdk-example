package com.ciscowebex.androidsdk.kitchensink.messaging.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.kitchensink.BaseViewModel
import com.ciscowebex.androidsdk.kitchensink.person.PersonModel
import com.ciscowebex.androidsdk.kitchensink.person.PersonRepository
import com.ciscowebex.androidsdk.kitchensink.utils.extensions.isValidEmail
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

class SearchPeopleViewModel(private val peopleRepository: PersonRepository) : BaseViewModel() {
    private val tag = "SearchPeopleViewModel"
    private val _persons = MutableLiveData<List<PersonModel>>()
    val persons: LiveData<List<PersonModel>> = _persons
    private val _peopleError = MutableLiveData<String>()
    val peopleError: LiveData<String> = _peopleError

    private val _searchResult = MutableLiveData<List<PersonModel>>()

    fun loadData(key: String?, maxPeopleCount: Int) {
        val observable: Observable<List<PersonModel>> = if (key.isValidEmail())
            peopleRepository.getPeopleList(key, null, null, null, maxPeopleCount)
        else
            peopleRepository.getPeopleList(null, key, null, null, maxPeopleCount)
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe({
            _persons.postValue(it)
        }, {
            _peopleError.postValue(it.message)
        }).autoDispose()
    }

}