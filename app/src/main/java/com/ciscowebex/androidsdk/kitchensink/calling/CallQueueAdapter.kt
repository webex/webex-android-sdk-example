package com.ciscowebex.androidsdk.kitchensink.calling

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.databinding.CallQueueItemBinding
import com.ciscowebex.androidsdk.phone.Call

class CallQueueAdapter(private val calls: ArrayList<Call>, private val itemClickListener: OnItemActionListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CallItemViewHolder(CallQueueItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return calls.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CallItemViewHolder).bind()
    }

    fun refreshData(list: List<Call>) {
        calls.clear()
        calls.addAll(list)
    }

    inner class CallItemViewHolder(private val binding: CallQueueItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(){
            val call = calls[adapterPosition] as Call
            binding.callTitle.text = call.getTitle()

            binding.callResume.setOnClickListener {
                call.getCallId()?.let{
                    itemClickListener.onCallResumed(it)
                }
            }
        }
    }

    interface OnItemActionListener{
        fun onCallResumed(callId: String)
    }
}