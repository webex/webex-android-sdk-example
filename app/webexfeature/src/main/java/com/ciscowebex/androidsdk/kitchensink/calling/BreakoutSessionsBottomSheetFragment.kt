package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.phone.BreakoutSession
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BreakoutSessionsBottomSheetFragment(): BottomSheetDialogFragment() {
    companion object {
        const val TAG = "BreakoutSessionsBottomSheetFragment"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var imgClose: ImageView
    private lateinit var heading: TextView
    var adapter: BreakoutSessionsAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.bottom_sheet_with_recycler_view, container, false)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        imgClose = view.findViewById(R.id.imgClose)
        heading = view.findViewById(R.id.heading)
        
        recyclerView.adapter = adapter
        imgClose.setOnClickListener { dismiss() }
        heading.text = getString(R.string.breakout_session)
        
        return view
    }

    class BreakoutSessionsAdapter(val onJoinSessionClicked:(BreakoutSession) -> Unit) : RecyclerView.Adapter<BreakoutSessionsViewHolder>() {
        var sessions: MutableList<BreakoutSession> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreakoutSessionsViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_join_breakout_session, parent, false)
            return BreakoutSessionsViewHolder(view, onJoinSessionClicked)
        }

        override fun getItemCount(): Int = sessions.size

        override fun onBindViewHolder(holder: BreakoutSessionsViewHolder, position: Int) {
            holder.bind(sessions[position])
        }
    }

    class BreakoutSessionsViewHolder(itemView: View, private val onJoinSessionClicked: (BreakoutSession) -> Unit)
        : RecyclerView.ViewHolder(itemView) {
        lateinit var item: BreakoutSession
        val tag = "BreakoutSessionsViewHolder"
        
        private val meetingJoinButton: Button = itemView.findViewById(R.id.meetingJoinButton)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        
        init {
            meetingJoinButton.setOnClickListener {
                onJoinSessionClicked(item)
            }
        }

        fun bind(model: BreakoutSession) {
            item = model
            titleTextView.text = model.getName()
        }
    }
}
