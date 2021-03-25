package com.ciscowebex.androidsdk.kitchensink.calling.participants

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.ParticipantsListItemBinding
import com.ciscowebex.androidsdk.phone.CallMembership

class ParticipantsAdapter(private val participants: ArrayList<CallMembership>, private val itemClickListener: OnItemActionListener, private val selfId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ParticipantsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ParticipantViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return participants.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ParticipantViewHolder).bind()
    }

    fun refreshData(list: ArrayList<CallMembership>) {
        participants.clear()
        participants.addAll(list)
    }

    inner class ParticipantViewHolder(private val binding: ParticipantsListItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(){
            binding.tvName.text = participants[adapterPosition].getName()
            binding.imgMute.setImageResource(R.drawable.ic_mic_off_24)
            binding.imgMute.visibility = if(!participants[adapterPosition].isSendingAudio()) View.VISIBLE else View.INVISIBLE

            val personId = participants[adapterPosition].getPersonId()

            if (personId == selfId) {
                binding.infoLabelView.visibility = View.VISIBLE
            }
            else {
                binding.infoLabelView.visibility = View.GONE
            }
            binding.root.setOnClickListener { itemClickListener.onParticipantMuted(personId)}
        }
    }

    interface OnItemActionListener{
        fun onParticipantMuted(participantId: String)
    }
}