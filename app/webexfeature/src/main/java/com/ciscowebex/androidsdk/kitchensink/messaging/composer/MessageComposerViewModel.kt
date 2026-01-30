package com.ciscowebex.androidsdk.kitchensink.messaging.composer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ciscowebex.androidsdk.kitchensink.BaseViewModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceMessageModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpacesRepository
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.MembershipModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.members.MembershipRepository
import com.ciscowebex.androidsdk.message.LocalFile
import com.ciscowebex.androidsdk.message.Mention
import com.ciscowebex.androidsdk.message.Message
import com.ciscowebex.androidsdk.utils.EmailAddress
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlin.collections.ArrayList
import java.util.Date


class MessageComposerViewModel(private val composerRepo: MessageComposerRepository, private val membershipRepo: MembershipRepository,
                               private val spacesRepository: SpacesRepository) : BaseViewModel() {

    companion object {
        val MINIMUM_MEMBERS_REQUIRED_FOR_MENTIONS = 2
    }
    private val tag = "MessageComposerViewModel"

    private val _postMessages = MutableLiveData<Message>()
    val postMessages: LiveData<Message> = _postMessages

    private val _postMessageError = MutableLiveData<String?>()
    val postMessageError: LiveData<String?> = _postMessageError

    private val _fetchMembershipsLiveData = MutableLiveData<List<MembershipModel>>()
    val fetchMembershipsLiveData: LiveData<List<MembershipModel>> = _fetchMembershipsLiveData

    private val _editMessage = MutableLiveData<SpaceMessageModel>()
    val editMessage: LiveData<SpaceMessageModel> = _editMessage

    private var membersList = mutableListOf<MembershipModel>()

    val labelAll = "All"

    fun postToSpace(spaceId: String, message: Message.Text, mentions: ArrayList<Mention>?, files: ArrayList<LocalFile>? = null) {
        composerRepo.postToSpace(spaceId, message, mentions, files).observeOn(AndroidSchedulers.mainThread()).subscribe({ result ->
            _postMessages.postValue(result)
        }, { error -> _postMessageError.postValue(error.message) }).autoDispose()
    }

    fun postToPerson(email: EmailAddress, message: Message.Text, files: ArrayList<LocalFile>? = null) {
        composerRepo.postToPerson(email, message, files).observeOn(AndroidSchedulers.mainThread()).subscribe({ result ->
            _postMessages.postValue(result)
        }, { error -> _postMessageError.postValue(error.message) }).autoDispose()
    }

    fun postToPerson(id: String, message: Message.Text, files: ArrayList<LocalFile>? = null) {
        composerRepo.postToPerson(id, message, files).observeOn(AndroidSchedulers.mainThread()).subscribe({ result ->
            _postMessages.postValue(result)
        }, { error -> _postMessageError.postValue(error.message) }).autoDispose()
    }

    fun postMessageDraft(target: String, draft: Message.Draft) {
        composerRepo.postMessageDraft(target, draft).observeOn(AndroidSchedulers.mainThread()).subscribe({ result ->
            _postMessages.postValue(result)
        }, { error -> _postMessageError.postValue(error.message) }).autoDispose()
    }

    fun editMessage(messageId: String, messageText: Message.Text, mentions: ArrayList<Mention>?) {
        spacesRepository.editMessage(messageId, messageText, mentions).observeOn(AndroidSchedulers.mainThread()).subscribe({ result ->
            _editMessage.postValue(result)
        }, { error -> _postMessageError.postValue(error.message) }).autoDispose()
    }

    fun fetchAllMembersInSpace(spaceId: String?, max: Int? = null) {
        membershipRepo.getMembersInSpace(spaceId, max).observeOn(AndroidSchedulers.mainThread()).subscribe({ memberships ->
            // Crete a membership model indicating all members
            val allMember = MembershipModel(labelAll, "", "", labelAll, "", false, false, Date(), "", labelAll, "")
            membersList.add(allMember)
            membersList.addAll(memberships)
            _fetchMembershipsLiveData.postValue(memberships)
        }, { membersList.clear() }).autoDispose()
    }

    fun search(filter: String): List<MembershipModel> {
        return membersList.filter {
            if (filter.isNotEmpty() && filter[0] == '@') {
                it.personDisplayName.startsWith(filter.substring(1))
            } else {
                it.personDisplayName.startsWith(filter)
            }
        }
    }

}