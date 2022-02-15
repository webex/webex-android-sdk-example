package com.ciscowebex.androidsdk.kitchensink.messaging.teams

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.kitchensink.BaseViewModel
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.membership.TeamMembershipModel
import com.ciscowebex.androidsdk.kitchensink.messaging.teams.membership.TeamMembershipRepository
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import io.reactivex.android.schedulers.AndroidSchedulers

class TeamsViewModel(private val teamsRepo: TeamsRepository, private val membershipRepo: TeamMembershipRepository) : BaseViewModel() {
    private val _teams = MutableLiveData<List<TeamModel>>()
    val teams: LiveData<List<TeamModel>> = _teams

    private val _teamAdded = MutableLiveData<TeamModel>()
    val teamAdded: LiveData<TeamModel> = _teamAdded

    private val _createMemberData = MutableLiveData<TeamMembershipModel>()
    val createMemberData: LiveData<TeamMembershipModel> = _createMemberData

    private val _teamError = MutableLiveData<String>()
    val teamError : LiveData<String> = _teamError

    fun getTeamsList(maxTeams: Int) {
        teamsRepo.fetchTeamsList(maxTeams).observeOn(AndroidSchedulers.mainThread()).subscribe({ teamsList ->
            _teams.postValue(teamsList)
        }, { _teams.postValue(emptyList()) }).autoDispose()
    }

    fun addTeam(teamName: String) {
        teamsRepo.createTeam(teamName).observeOn(AndroidSchedulers.mainThread()).subscribe({ addedTeam ->
            _teamAdded.postValue(addedTeam)
        }, { _teamAdded.postValue(null) }).autoDispose()
    }

    fun updateTeam(teamId: String, teamName: String) {
        teamsRepo.updateTeam(teamId, teamName).observeOn(AndroidSchedulers.mainThread()).subscribe({
            refreshTeams()
        }, { error -> _teamError.postValue(error.message) }).autoDispose()
    }

    fun deleteTeamWithId(teamId: String) {
        teamsRepo.deleteTeamWithId(teamId).observeOn(AndroidSchedulers.mainThread()).subscribe({
            refreshTeams()
        }, { error -> _teamError.postValue(error.message) }).autoDispose()
    }

    fun addSpaceFromTeam(spaceTitle: String, teamId: String){
        teamsRepo.addSpace(spaceTitle, teamId).observeOn(AndroidSchedulers.mainThread()).subscribe ({
            refreshTeams()
        }, {}).autoDispose()
    }

    private fun refreshTeams() {
        getTeamsList(Constants.DefaultMax.TEAM_MAX)
    }

    fun createMembershipWithEmailId(spaceId: String, emailId: String, isModerator: Boolean) {
        membershipRepo.createMembershipWithEmail(spaceId, emailId, isModerator).observeOn(AndroidSchedulers.mainThread()).subscribe({ membership ->
            _createMemberData.postValue(membership)
        }, { error -> _teamError.postValue(error.message) }).autoDispose()
    }

    fun createMembershipWithId(spaceId: String, personId: String, isModerator: Boolean) {
        membershipRepo.createMembershipWithId(spaceId, personId, isModerator).observeOn(AndroidSchedulers.mainThread()).subscribe({ membership ->
            _createMemberData.postValue(membership)
        }, { error -> _teamError.postValue(error.message) }).autoDispose()
    }
}