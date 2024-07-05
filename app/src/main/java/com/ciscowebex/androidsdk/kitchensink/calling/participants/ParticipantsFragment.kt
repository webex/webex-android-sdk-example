package com.ciscowebex.androidsdk.kitchensink.calling.participants

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexViewModel
import com.ciscowebex.androidsdk.kitchensink.calling.CallActivity
import com.ciscowebex.androidsdk.kitchensink.databinding.FragmentParticipantsBinding
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogForTextBox
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogWithMessage
import com.ciscowebex.androidsdk.phone.MakeHostError
import com.ciscowebex.androidsdk.phone.InviteParticipantError
import com.ciscowebex.androidsdk.phone.CallMembership
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_participants.*


class ParticipantsFragment : DialogFragment(), ParticipantsAdapter.OnItemActionListener {

    lateinit var binding: FragmentParticipantsBinding
    lateinit var adapter: ParticipantsAdapter
    private lateinit var webexViewModel: WebexViewModel
    private var currentCallId: String? = null
    private var selfId: String? = null
    private var isSelfModerator: Boolean = false

    companion object {
        private const val CALL_KEY = "call_id"
        private const val SELF_ID_KEY = "self_id"

        fun newInstance(callid: String): ParticipantsFragment {
            val bundle = Bundle()
            bundle.putString(CALL_KEY, callid)
            val fragment = ParticipantsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val participantsBinding = DataBindingUtil.inflate<FragmentParticipantsBinding>(LayoutInflater.from(context), R.layout.fragment_participants, container,false).also { binding = it }.apply {
            webexViewModel = (activity as? CallActivity)?.webexViewModel!!
            Log.d(tag, "onCreateView webexViewModel: $webexViewModel")
            selfId = webexViewModel.selfPersonId
            setUpViews()
        }
        return participantsBinding.root
    }

    private fun setUpViews() {
        val callMembership = webexViewModel.getCall(webexViewModel.currentCallId.orEmpty())?.getMemberships()
        for(member in callMembership.orEmpty())
        {
            if(member.isSelf() && member.isHost())
            {
                isSelfModerator = true
                break
            }
        }
        adapter = ParticipantsAdapter(arrayListOf(), this, selfId.orEmpty(), isSelfModerator)
        binding.participants.adapter = adapter
        val dividerItemDecoration = DividerItemDecoration(requireContext(),
                LinearLayoutManager.VERTICAL)
        binding.participants.addItemDecoration(dividerItemDecoration)

        webexViewModel.currentCallId?.let { _callId ->
            currentCallId = _callId
            webexViewModel.getParticipants(_callId)
        }

        webexViewModel.callMembershipsLiveData.observe(this, Observer { it ->
            it?.let { callMemberships ->
                Log.d(tag, callMemberships.toString())
                val data = arrayListOf<Any>()
                val stateWiseMap = callMemberships.groupBy { it.getState() }
                stateWiseMap.keys.forEach { state ->
                    val memberships = stateWiseMap[state]
                    data.add(webexViewModel.getHeader(state))
                    data.addAll(memberships.orEmpty())
                }
                adapter.refreshData(data)
                adapter.notifyDataSetChanged()
            }
        })

        webexViewModel.muteAllLiveData.observe(this, Observer { shouldMuteAll ->
            if (shouldMuteAll != null) {
                tvMute.text = if(shouldMuteAll) getString(R.string.mute_all) else getString(R.string.unmute_all)
            }
        })

        binding.tvMute.text = getString(R.string.mute_all)
        binding.tvMute.setOnClickListener {
            if (webexViewModel.getCall(webexViewModel.currentCallId.orEmpty())?.isCUCMCall() == false) {
                webexViewModel.currentCallId?.let {
                    webexViewModel.muteAllParticipantAudio(it)
                }
            } else {
                showSnackbar(getString(R.string.mute_feature_is_not_available_for_cucm_calls))
            }
        }
        binding.inviteParticipantButton.setOnClickListener {
            inviteParticipant()
        }

        binding.close.setOnClickListener { dismiss() }

    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onParticipantMuted(participantId: String, hasPairedParticipant: Boolean) {
        currentCallId?.let {
            if (webexViewModel.getCall(webexViewModel.currentCallId.orEmpty())?.isCUCMCall() == false || webexViewModel.selfPersonId == participantId) {
                webexViewModel.muteParticipant(it, participantId)
                adapter.notifyDataSetChanged()
            } else {
                showSnackbar(getString(R.string.mute_feature_is_not_available_for_cucm_calls))
            }
            if(hasPairedParticipant) {
                showSnackbar(getString(R.string.mute_feature_paired_participant))
            }
        }
    }

    override fun onLetInClicked(callMembership: CallMembership) {
        if (callMembership.getState() == CallMembership.State.WAITING) {
            context?.let { ctx ->
                showDialogWithMessage(ctx, getString(R.string.message), getString(R.string.let_in_confirmation),
                        onPositiveButtonClick = { dialog, _ ->
                            currentCallId?.let {
                                webexViewModel.letIn(it, callMembership)
                            }
                            dialog.dismiss()
                        },
                        onNegativeButtonClick = { dialog, _ ->
                            dialog.dismiss()
                        })
            }
        }
    }

    private fun inviteParticipant() {
        showDialogForTextBox(requireContext(), getString(R.string.invite_participant), onPositiveButtonClick = { dialog: DialogInterface, invitee: String ->
            webexViewModel.inviteParticipant(invitee) {
                if (it.isSuccessful) {
                    showSnackbar("Invite Participant Successful")
                    Log.d(tag, "Invite Participant Successful")
                } else {
                    showSnackbar("Invite Participant failed ${it.error?.errorMessage}")
                    Log.d(tag, "Invite Participant failed ${it.error?.errorMessage}")
                }
            }
            dialog.dismiss()
        }, onNegativeButtonClick = { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        })
    }

    override fun onMakeHostClicked(participantId: String) {
        context?.let {
            ctx->
            showDialogWithMessage(ctx, getString(R.string.message), getString(R.string.assign_host_confirmation),
                    onPositiveButtonClick = { dialog, _ ->
                        currentCallId?.let {
                            webexViewModel.makeHost(participantId) {
                                if (it.isSuccessful) {
                                    showSnackbar(getString(R.string.assign_host_success))
                                } else {
                                    showSnackbar(it.error?.errorMessage ?: getString(R.string.assign_host_failure))
                                }

                            }
                        }
                        dialog.dismiss()
                    },
                    onNegativeButtonClick = { dialog, _ ->
                        dialog.dismiss()
                    })
        }
    }
}