package com.ciscowebex.androidsdk.kitchensink.messaging.spaces

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.kitchensink.BaseViewModel
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.listeners.SpaceEventListener
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.MembershipModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.MembershipRepository
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.TeamsRepository
import com.ciscowebex.androidsdk.space.SpaceClient
import io.reactivex.android.schedulers.AndroidSchedulers

class SpacesViewModel(private val spacesRepo: SpacesRepository,
                      private val membershipRepo: MembershipRepository,
                      private val messagingRepo: TeamsRepository, private val webexRepository: WebexRepository) : BaseViewModel() {
    private val _spaces = MutableLiveData<List<SpaceModel>>()
    val spaces: LiveData<List<SpaceModel>> = _spaces

    private val _readStatusList = MutableLiveData<List<SpaceReadStatusModel>>()
    val readStatusList: LiveData<List<SpaceReadStatusModel>> = _readStatusList

    private val _addSpace = MutableLiveData<SpaceModel>()
    val addSpace: LiveData<SpaceModel> = _addSpace

    private val _spaceMeetingInfo = MutableLiveData<SpaceMeetingInfoModel>()
    val spaceMeetingInfo: LiveData<SpaceMeetingInfoModel> = _spaceMeetingInfo

    private val _spaceError = MutableLiveData<String>()
    val spaceError: LiveData<String> = _spaceError

    private val _createMemberData = MutableLiveData<MembershipModel>()
    val createMemberData: LiveData<MembershipModel> = _createMemberData

    private val _markSpaceRead = MutableLiveData<Boolean>()
    val markSpaceRead: LiveData<Boolean> = _markSpaceRead

    private val _deleteSpace = MutableLiveData<String>()
    val deleteSpace: LiveData<String> = _deleteSpace

    private val _spacesSyncCompletedLiveData = MutableLiveData<Boolean>()
    val spacesSyncCompletedLiveData: LiveData<Boolean> = _spacesSyncCompletedLiveData

    private val _initialSpacesSyncCompletedLiveData = MutableLiveData<Boolean>()
    val initialSpacesSyncCompletedLiveData: LiveData<Boolean> = _initialSpacesSyncCompletedLiveData

    private val addOnCallSuffix = " (On Call)"

    private val TAG = "SpacesViewModel"

    override fun onCleared() {
        webexRepository.clearSpaceData()
    }

    private fun getSpacesWithActiveCalls() {
        val allSpaces = arrayListOf<SpaceModel>()
        spacesRepo.listSpacesWithActiveCalls().observeOn(AndroidSchedulers.mainThread()).subscribe({ spaceIds ->
            Log.d(TAG, "listSpacesWithActiveCalls spaceIds.size = ${spaceIds.size}")
            if (spaceIds.isNotEmpty()) {
                spaces.value?.forEach { space ->
                    if(spaceIds.contains(space.id)) {
                        val tempSpace = SpaceModel(
                            space.id,
                            space.title + addOnCallSuffix,
                            space.spaceType,
                            space.isLocked,
                            space.lastActivity,
                            space.created,
                            space.teamId,
                            space.sipAddress,
                            space?.isExternallyOwned
                        )
                        allSpaces.add(tempSpace)
                    } else {
                        allSpaces.add(space)
                    }
                }
                _spaces.postValue(allSpaces)
            }
        })
        {
            Log.e(TAG, "error in listSpacesWithActiveCalls : ${it.message}")
        }.autoDispose()
    }

    fun setSpaceEventListener(listener : SpaceEventListener) {
        webexRepository.spaceEventListener = listener
    }

    fun getSpacesList(maxSpaces: Int, sortBy: SpaceClient.SortBy, teamId: String? = null) {
        spacesRepo.fetchSpacesList(teamId, maxSpaces, sortBy).observeOn(AndroidSchedulers.mainThread()).subscribe({ spacesList ->
            _spaces.postValue(spacesList)
            getSpacesWithActiveCalls()
        }, { _spaces.postValue(emptyList()) }).autoDispose()
    }

    fun addSpace(title: String, teamId: String?) {
        spacesRepo.addSpace(title, teamId).observeOn(AndroidSchedulers.mainThread()).subscribe({ createdSpace ->
            _addSpace.postValue(createdSpace)
        }, { _addSpace.postValue(null) }).autoDispose()

    }

    fun getSpaceReadStatusList(maxSpaces: Int) {
        spacesRepo.fetchSpaceReadStatusList(maxSpaces).observeOn(AndroidSchedulers.mainThread()).subscribe({ listReadStatus ->
            _readStatusList.postValue(listReadStatus)
        }, { _readStatusList.postValue(null) }).autoDispose()
    }

    fun updateSpace(spaceId: String, spaceName: String) {
        spacesRepo.updateSpace(spaceId, spaceName).observeOn(AndroidSchedulers.mainThread()).subscribe({
            Log.d(SpacesViewModel::class.java.simpleName, "Space title is updated")
        }, { error -> _spaceError.postValue(error.message) }).autoDispose()
    }

    fun delete(spaceId: String) {
        spacesRepo.delete(spaceId).observeOn(AndroidSchedulers.mainThread()).subscribe({
            _deleteSpace.postValue(spaceId)
        }, {error -> _spaceError.postValue(error.message) }).autoDispose()
    }

    fun getMeetingInfo(spaceId: String) {
        spacesRepo.getMeetingInfo(spaceId).observeOn(AndroidSchedulers.mainThread()).subscribe({ meetingInfo ->
            _spaceMeetingInfo.postValue(meetingInfo)
        }, { error -> _spaceError.postValue(error.message) }).autoDispose()
    }

    fun createMembershipWithId(spaceId: String, personId: String, isModerator: Boolean = false) {
        membershipRepo.createMembershipWithId(spaceId, personId, isModerator).observeOn(AndroidSchedulers.mainThread()).subscribe({ membership ->
            _createMemberData.postValue(membership)
        }, { error -> _spaceError.postValue(error.message) }).autoDispose()
    }

    fun createMembershipWithEmailId(spaceId: String, emailId: String, isModerator: Boolean = false) {
        membershipRepo.createMembershipWithEmail(spaceId, emailId, isModerator).observeOn(AndroidSchedulers.mainThread()).subscribe({ membership ->
            _createMemberData.postValue(membership)
        }, { error -> _spaceError.postValue(error.message) }).autoDispose()
    }

    fun markSpaceRead(spaceId: String) {
        messagingRepo.markMessageAsRead(spaceId).observeOn(AndroidSchedulers.mainThread()).subscribe({ success ->
            _markSpaceRead.postValue(success)
        }, { error -> _spaceError.postValue(error.message) }).autoDispose()
    }

    fun setOnInitialSpacesSyncCompletedListener() {
        webexRepository.setOnInitialSpacesSyncCompletedListener() {
            _initialSpacesSyncCompletedLiveData.postValue(true)
        }
    }

    fun setSpacesSyncCompletedListener() {
        spacesRepo.setSpacesSyncCompletedListener().observeOn(AndroidSchedulers.mainThread()).subscribe { isSyncing ->
            _spacesSyncCompletedLiveData.postValue(isSyncing)
        }.autoDispose()
    }

    fun isSpacesSyncCompleted(): Boolean {
        return webexRepository.webex.spaces.isSpacesSyncCompleted()
    }

    private fun refreshSpaces() {
        getSpacesList(maxSpaces = 100, sortBy = SpaceClient.SortBy.NONE)
    }
}

