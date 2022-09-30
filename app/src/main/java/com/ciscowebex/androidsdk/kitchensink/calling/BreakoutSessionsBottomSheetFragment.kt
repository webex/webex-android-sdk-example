package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetWithRecyclerViewBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemJoinBreakoutSessionBinding
import com.ciscowebex.androidsdk.phone.BreakoutSession
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BreakoutSessionsBottomSheetFragment(): BottomSheetDialogFragment() {
    companion object {
        const val TAG = "BreakoutSessionsBottomSheetFragment"
    }

    private lateinit var binding: BottomSheetWithRecyclerViewBinding
    var adapter: BreakoutSessionsAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return BottomSheetWithRecyclerViewBinding.inflate(inflater, container, false)
            .also { binding = it }.apply {
                binding.recyclerView.adapter = adapter
                imgClose.setOnClickListener { dismiss() }
                binding.heading.text = getString(R.string.breakout_session)
            }.root
    }

    class BreakoutSessionsAdapter(val onJoinSessionClicked:(BreakoutSession) -> Unit) : RecyclerView.Adapter<BreakoutSessionsViewHolder>() {
        var sessions: MutableList<BreakoutSession> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreakoutSessionsViewHolder {
            return BreakoutSessionsViewHolder(ListItemJoinBreakoutSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false), onJoinSessionClicked)
        }

        override fun getItemCount(): Int = sessions.size

        override fun onBindViewHolder(holder: BreakoutSessionsViewHolder, position: Int) {
            holder.bind(sessions[position])
        }
    }

    class BreakoutSessionsViewHolder(private val binding: ListItemJoinBreakoutSessionBinding, private val onJoinSessionClicked: (BreakoutSession) -> Unit)
        : RecyclerView.ViewHolder(binding.root) {
        lateinit var item: BreakoutSession
        val tag = "BreakoutSessionsViewHolder"
        init {
            binding.meetingJoinButton.setOnClickListener {
                onJoinSessionClicked(item)
            }
        }

        fun bind(model: BreakoutSession) {
            item = model
            binding.titleTextView.text = model.getName()
            binding.executePendingBindings()
        }
    }
}