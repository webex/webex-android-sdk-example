package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.kitchensink.BaseViewModel
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.membership.Membership
import io.reactivex.android.schedulers.AndroidSchedulers

class MembershipViewModel(private val membershipRepo: MembershipRepository, private val webexRepository: WebexRepository) : BaseViewModel() {
    private val _memberships = MutableLiveData<List<MembershipModel>>()
    val memberships: LiveData<List<MembershipModel>> = _memberships

    private val _membershipDetail = MutableLiveData<MembershipModel>()
    val membershipDetail: LiveData<MembershipModel> = _membershipDetail

    private val _deleteMembership = MutableLiveData<Pair<Boolean, Int>>()
    val deleteMembership: LiveData<Pair<Boolean, Int>> = _deleteMembership

    private val _membershipError = MutableLiveData<String>()
    val membershipError: LiveData<String> = _membershipError

    private val _membershipEventLiveData = MutableLiveData<Pair<WebexRepository.MembershipEvent, Membership?>>()
    val membershipEventLiveData: LiveData<Pair<WebexRepository.MembershipEvent, Membership?>> = _membershipEventLiveData

    init {
        webexRepository._membershipEventLiveData = _membershipEventLiveData
    }

    override fun onCleared() {
        super.onCleared()
        webexRepository._membershipEventLiveData = null
    }

    fun getMembersIn(spaceId: String?, max: Int?) {
        membershipRepo.getMembersInSpace(spaceId, max).observeOn(AndroidSchedulers.mainThread()).subscribe({ memberships ->
            _memberships.postValue(memberships)
        }, { _memberships.postValue(emptyList()) }).autoDispose()
    }

    fun getMembership(membershipId: String) {
        membershipRepo.getMembership(membershipId).observeOn(AndroidSchedulers.mainThread()).subscribe({ membership ->
            _membershipDetail.postValue(membership)
        }, { error -> _membershipError.postValue(error.message) }).autoDispose()
    }

    fun updateMembershipWith(membershipId: String, isModerator : Boolean) {
        membershipRepo.updateMembershipWith(membershipId, isModerator).observeOn(AndroidSchedulers.mainThread()).subscribe({ membership ->
            _membershipDetail.postValue(membership)
        }, { error -> _membershipError.postValue(error.message) }).autoDispose()
    }

    fun deleteMembership(itemPosition: Int, membershipId: String) {
        membershipRepo.delete(membershipId).observeOn(AndroidSchedulers.mainThread()).subscribe({ response ->
            _deleteMembership.postValue(Pair(response, itemPosition))
        }, { error -> _membershipError.postValue(error.message) }).autoDispose()
    }

}