package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.membersReadStatus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.kitchensink.BaseViewModel
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.MembershipRepository
import com.ciscowebex.androidsdk.membership.Membership
import io.reactivex.android.schedulers.AndroidSchedulers

class MembershipReadStatusViewModel(private val membershipRepo: MembershipRepository, private val webexRepository: WebexRepository) : BaseViewModel() {
    private val _membershipsReadStatus = MutableLiveData<List<MembershipReadStatusModel>>()
    val membershipsReadStatus: LiveData<List<MembershipReadStatusModel>> = _membershipsReadStatus

    private val _membershipReadStatusError = MutableLiveData<String>()
    val membershipReadStatusError: LiveData<String> = _membershipReadStatusError

    private val _membershipEventLiveData = MutableLiveData<Pair<WebexRepository.MembershipEvent, Membership?>>()
    val membershipEventLiveData: LiveData<Pair<WebexRepository.MembershipEvent, Membership?>> = _membershipEventLiveData

    init {
        webexRepository._membershipEventLiveData = _membershipEventLiveData
    }

    override fun onCleared() {
        super.onCleared()
        webexRepository._membershipEventLiveData = null
    }

    fun getMembershipsWithReadStatus(spaceId: String?) {
        membershipRepo.listMembershipsWithReadStatus(spaceId
                ?: "").observeOn(AndroidSchedulers.mainThread()).subscribe({ membershipsReadStatus ->
            _membershipsReadStatus.postValue(membershipsReadStatus)
        }, { _membershipReadStatusError.postValue(it.message) }).autoDispose()
    }
}