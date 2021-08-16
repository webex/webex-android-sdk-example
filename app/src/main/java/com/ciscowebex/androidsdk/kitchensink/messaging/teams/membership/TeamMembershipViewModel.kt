package com.ciscowebex.androidsdk.kitchensink.messaging.teams.membership

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.kitchensink.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers

class TeamMembershipViewModel(private val membershipRepo: TeamMembershipRepository) : BaseViewModel() {
    var teamId: String = ""
    private val _memberships = MutableLiveData<List<TeamMembershipModel>>()
    val memberships: LiveData<List<TeamMembershipModel>> = _memberships

    private val _membershipDetail = MutableLiveData<TeamMembershipModel>()
    val membershipDetails: LiveData<TeamMembershipModel> = _membershipDetail

    private val _membershipError = MutableLiveData<String>()
    val membershipError: LiveData<String> = _membershipError

    fun getTeamMembersIn(max: Int) {
        membershipRepo.getTeamMemberships(teamId, max).observeOn(AndroidSchedulers.mainThread()).subscribe({ memberships ->
            _memberships.postValue(memberships)
        }, { _memberships.postValue(emptyList()) }).autoDispose()
    }

    fun getTeamMembership(teamMembershipId: String){
        membershipRepo.getTeamMembership(teamMembershipId).observeOn(AndroidSchedulers.mainThread()).subscribe({ membership ->
            _membershipDetail.postValue(membership)
        }, { error -> _membershipError.postValue(error.message)}).autoDispose()
    }

    fun deleteMembership(teamMembershipId: String, max: Int) {
        membershipRepo.delete(teamMembershipId).observeOn(AndroidSchedulers.mainThread()).subscribe({
            // refresh list
            getTeamMembersIn(max)
        }, {error -> _membershipError.postValue(error.message)}).autoDispose()
    }

    fun updateMembership(teamMembershipId: String, isModerator: Boolean) {
        membershipRepo.updateMembership(teamMembershipId, isModerator).observeOn(AndroidSchedulers.mainThread()).subscribe({ membership ->
            _membershipDetail.postValue(membership)
        }, {error -> _membershipError.postValue(error.message)}).autoDispose()
    }

}