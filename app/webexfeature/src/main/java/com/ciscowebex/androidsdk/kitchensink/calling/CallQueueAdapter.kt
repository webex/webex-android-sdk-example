package com.ciscowebex.androidsdk.kitchensink.calling

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.phone.Call

class CallQueueAdapter(private val calls: ArrayList<Call>, private val itemClickListener: OnItemActionListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.call_queue_item, parent, false)
        return CallItemViewHolder(view)
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

    inner class CallItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val callTitle: TextView = itemView.findViewById(R.id.callTitle)
        private val callResume: Button = itemView.findViewById(R.id.callResume)

        fun bind(){
            val call = calls[adapterPosition] as Call
            callTitle.text = call.getTitle()

            callResume.setOnClickListener {
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
