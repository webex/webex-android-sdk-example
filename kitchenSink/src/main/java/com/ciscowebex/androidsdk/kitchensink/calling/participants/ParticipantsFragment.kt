package com.ciscowebex.androidsdk.kitchensink.calling.participants

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexViewModel
import com.ciscowebex.androidsdk.kitchensink.calling.CallActivity
import com.ciscowebex.androidsdk.kitchensink.databinding.FragmentParticipantsBinding
import kotlinx.android.synthetic.main.fragment_participants.*


class ParticipantsFragment : DialogFragment(), ParticipantsAdapter.OnItemActionListener {

    lateinit var binding: FragmentParticipantsBinding
    lateinit var adapter: ParticipantsAdapter
    private lateinit var webexViewModel: WebexViewModel
    private var currentCallId: String? = null
    private var selfId: String? = null

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
        adapter = ParticipantsAdapter(arrayListOf(), this, selfId.orEmpty())
        binding.participants.adapter = adapter

        val dividerItemDecoration = DividerItemDecoration(requireContext(),
                LinearLayoutManager.VERTICAL)
        binding.participants.addItemDecoration(dividerItemDecoration)

        webexViewModel.currentCallId?.let { _callId ->
            currentCallId = _callId
            webexViewModel.getParticipants(_callId)
        }

        webexViewModel.callMembershipsLiveData.observe(this, Observer {
            it?.let { data ->
                Log.d(tag, data.toString())
                adapter.refreshData(ArrayList(data))
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
            webexViewModel.currentCallId?.let {
                webexViewModel.muteAllParticipantAudio(it)
            }
        }

        binding.close.setOnClickListener { dismiss() }

    }

    override fun onParticipantMuted(participantId: String) {
        currentCallId?.let {
            webexViewModel.muteParticipant(it, participantId)
            adapter.notifyDataSetChanged()
        }

    }
}