package com.ciscowebex.androidsdk.kitchensink.calling.participants

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.phone.CallMembership

class ParticipantsAdapter(private val participants: ArrayList<Any>, private val itemClickListener: OnItemActionListener, private val selfId: String, private val isSelfModerator: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val viewTypeHeader = 0
    private val viewTypeParticipant = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            viewTypeHeader -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.participants_header_item, parent, false)
                HeaderViewHolder(view)
            }

            viewTypeParticipant -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.participants_list_item, parent, false)
                ParticipantViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.participants_list_item, parent, false)
                ParticipantViewHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (participants[position] is String) {
            viewTypeHeader
        } else viewTypeParticipant
    }

    override fun getItemCount(): Int {
       return participants.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(participants[position] is String) {
            (holder as HeaderViewHolder).bind()
        } else {
            (holder as ParticipantViewHolder).bind()
        }
    }

    fun refreshData(list: List<Any>) {
        participants.clear()
        participants.addAll(list)
    }

    inner class ParticipantViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val imgMute: ImageView = itemView.findViewById(R.id.imgMute)
        private val infoDeviceType: TextView = itemView.findViewById(R.id.infoDeviceType)
        private val presenter: TextView = itemView.findViewById(R.id.presenter)
        private val host: TextView = itemView.findViewById(R.id.host)
        private val cohost: TextView = itemView.findViewById(R.id.cohost)
        private val makeHost: Button = itemView.findViewById(R.id.makeHost)
        private val infoLabelView: TextView = itemView.findViewById(R.id.infoLabelView)

        fun bind(){
            val participant = participants[adapterPosition] as CallMembership
            tvName.text = participant.getDisplayName()
            imgMute.setImageResource(R.drawable.ic_mic_off_24)
            imgMute.visibility = if(!participant.isSendingAudio()) View.VISIBLE else View.INVISIBLE
            infoDeviceType.text = participant.getDeviceType().name
            presenter.visibility = if(participant.isPresenter()) View.VISIBLE else View.GONE
            host.visibility = if(participant.isHost()) View.VISIBLE else View.GONE
            cohost.visibility = if(participant.isCohost()) View.VISIBLE else View.GONE
            makeHost.visibility = if(!participant.isSelf() && isSelfModerator) View.VISIBLE else View.GONE

            val personId = participant.getPersonId()

            if (personId == selfId) {
                infoLabelView.visibility = View.VISIBLE
            }
            else {
                infoLabelView.visibility = View.GONE
            }
            itemView.setOnClickListener {
                val pairedMembership = participant.getPairedMemberships()
                itemClickListener.onParticipantMuted(personId, pairedMembership?.isNotEmpty() == true)
            }
            itemView.setOnLongClickListener {
                itemClickListener.onLetInClicked(participant)
                true
            }
            makeHost.setOnClickListener {
                itemClickListener.onMakeHostClicked(participant.getPersonId())
            }
        }
    }

    inner class HeaderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)

        fun bind(){
            tvName.text = participants[adapterPosition] as String
            itemView.setOnClickListener(null)
        }
    }

    interface OnItemActionListener{
        fun onParticipantMuted(participantId: String, hasPairedParticipant: Boolean)

        fun onLetInClicked(callMembership: CallMembership)

        fun onMakeHostClicked(participantId: String)
    }
}
