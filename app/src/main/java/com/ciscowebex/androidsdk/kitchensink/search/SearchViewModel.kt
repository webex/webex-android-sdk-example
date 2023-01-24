package com.ciscowebex.androidsdk.kitchensink.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.kitchensink.BaseViewModel
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpacesRepository
import com.ciscowebex.androidsdk.phone.CallHistoryRecord
import com.ciscowebex.androidsdk.space.Space
import com.ciscowebex.androidsdk.space.SpaceClient
import io.reactivex.android.schedulers.AndroidSchedulers

class SearchViewModel(private val searchRepo: SearchRepository, private val spacesRepo: SpacesRepository, private val webexRepo: WebexRepository) : BaseViewModel() {
    private val tag = "SearchViewModel"
    private val _spaces = MutableLiveData<List<SpaceModel>>()
    val spaces: LiveData<List<SpaceModel>> = _spaces

    private val _callHistoryRecords = MutableLiveData<List<CallHistoryRecord>>()
    val callHistoryRecords: LiveData<List<CallHistoryRecord>> = _callHistoryRecords

    private val _searchResult = MutableLiveData<List<Space>>()
    val searchResult: LiveData<List<Space>> = _searchResult

    private val _spaceEventLiveData = MutableLiveData<Pair<WebexRepository.SpaceEvent, Any?>>()

    private val _spacesSyncCompletedLiveData = MutableLiveData<Boolean>()
    val spacesSyncCompletedLiveData: LiveData<Boolean> = _spacesSyncCompletedLiveData

    private val _initialSpacesSyncCompletedLiveData = MutableLiveData<Boolean>()
    val initialSpacesSyncCompletedLiveData: LiveData<Boolean> = _initialSpacesSyncCompletedLiveData

    val titles =
            listOf("Call", "Search", "History", "Spaces", "Meetings")

    init {
        webexRepo._spaceEventLiveData = _spaceEventLiveData
    }

    fun getSpaceEvent() = webexRepo._spaceEventLiveData

    fun isSpaceCallStarted() = webexRepo.isSpaceCallStarted
    fun spaceCallId() = webexRepo.spaceCallId

    fun loadData(taskType: String, maxSpaceCount: Int) {
        when (taskType) {
            SearchCommonFragment.Companion.TaskType.TaskCallHistory -> {
                searchRepo.getCallHistory().observeOn(AndroidSchedulers.mainThread()).subscribe({
                    Log.d(tag, "Size of $taskType is ${it?.size?.or(0)}")
                    _callHistoryRecords.postValue(it)
                }, {
                    _callHistoryRecords.postValue(emptyList())
                }).autoDispose()
            }
            SearchCommonFragment.Companion.TaskType.TaskSearchSpace -> {
                search("")
            }
            SearchCommonFragment.Companion.TaskType.TaskListSpaces -> {
                spacesRepo.fetchSpacesList(null, maxSpaceCount, SpaceClient.SortBy.NONE).observeOn(AndroidSchedulers.mainThread()).subscribe({ spacesList ->
                    _spaces.postValue(spacesList)
                }, { _spaces.postValue(emptyList()) }).autoDispose()
            }
        }
    }

    fun search(query: String?) {
        query?.let { searchQuery ->
            searchRepo.search(searchQuery).observeOn(AndroidSchedulers.mainThread()).subscribe({
                _searchResult.postValue(it)
            }, {
                _searchResult.postValue(emptyList())
            }).autoDispose()
        }
    }

    fun setSpacesSyncCompletedListener() {
        spacesRepo.setSpacesSyncCompletedListener().observeOn(AndroidSchedulers.mainThread()).subscribe { isSyncing ->
            _spacesSyncCompletedLiveData.postValue(isSyncing)
        }.autoDispose()
    }

    fun setOnInitialSpacesSyncCompletedListener() {
        webexRepo.setOnInitialSpacesSyncCompletedListener() {
            _initialSpacesSyncCompletedLiveData.postValue(true)
        }
    }

    fun isSpacesSyncCompleted(): Boolean {
        return webexRepo.webex.spaces.isSpacesSyncCompleted()
    }
}