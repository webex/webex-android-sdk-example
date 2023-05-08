package com.ciscowebex.androidsdk.kitchensink.calling.transcription

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.WebexViewModel
import com.ciscowebex.androidsdk.kitchensink.calling.CallActivity
import com.ciscowebex.androidsdk.kitchensink.databinding.FragmentTranscriptionsBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.TranscriptTextItemBinding
import com.ciscowebex.androidsdk.phone.WXA
import com.ciscowebex.androidsdk.phone.transcription.Transcription

class TranscriptionsDialogFragment : DialogFragment(),
    WXA.OnTranscriptionArrivedListener {

    private lateinit var binding: FragmentTranscriptionsBinding
    private lateinit var viewModel: WebexViewModel
    private var adapter: TranscriptionsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = (activity as? CallActivity)?.webexViewModel!!

        viewModel.currentCallId?.let { callId ->
            viewModel.getCall(callId)?.getWXA()?.setOnTranscriptionArrivedListener(this)
        }

        return FragmentTranscriptionsBinding.inflate(inflater, container, false).also { binding = it }.apply {
            binding.close.setOnClickListener { dismiss() }
        }.root
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TranscriptionsAdapter()
        binding.transcriptsList.adapter = adapter
        binding.transcriptsList.smoothScrollToPosition(0)
    }

    class TranscriptionsAdapter : RecyclerView.Adapter<TranscriptionViewHolder>() {
        var transcriptions = mutableListOf<Transcription>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranscriptionViewHolder {
            return TranscriptionViewHolder(
                TranscriptTextItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: TranscriptionViewHolder, position: Int) {
            holder.bind(transcriptions[position])
        }

        override fun getItemCount(): Int {
            return transcriptions.size
        }
    }

    class TranscriptionViewHolder(val binding: TranscriptTextItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transcription: Transcription) {
            binding.tvName.text = transcription.personName
            binding.arrivedAt.text = transcription.timestamp
            binding.tvText.text = transcription.content
            binding.executePendingBindings()
        }
    }

    override fun onTranscriptionArrived(transcription: Transcription) {
        Log.d("TranscriptionsBottomSheetFragment", "transcription: ${transcription.content} by ${transcription.personName}")
        adapter?.transcriptions?.add(transcription)
        adapter?.notifyItemInserted(adapter?.transcriptions?.size ?: 0 - 1)
        binding.transcriptsList.smoothScrollToPosition(adapter?.transcriptions?.size ?: 0 - 1)

    }
}