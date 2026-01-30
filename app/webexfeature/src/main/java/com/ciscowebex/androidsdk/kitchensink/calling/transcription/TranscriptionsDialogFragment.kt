package com.ciscowebex.androidsdk.kitchensink.calling.transcription

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexViewModel
import com.ciscowebex.androidsdk.kitchensink.calling.CallActivity
import com.ciscowebex.androidsdk.phone.WXA
import com.ciscowebex.androidsdk.phone.transcription.Transcription

class TranscriptionsDialogFragment : DialogFragment(),
    WXA.OnTranscriptionArrivedListener {

    private lateinit var close: ImageView
    private lateinit var transcriptsList: RecyclerView
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

        val view = inflater.inflate(R.layout.fragment_transcriptions, container, false)
        
        close = view.findViewById(R.id.close)
        transcriptsList = view.findViewById(R.id.transcriptsList)
        
        close.setOnClickListener { dismiss() }
        
        return view
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
        transcriptsList.adapter = adapter
        transcriptsList.smoothScrollToPosition(0)
    }

    class TranscriptionsAdapter : RecyclerView.Adapter<TranscriptionViewHolder>() {
        var transcriptions = mutableListOf<Transcription>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranscriptionViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.transcript_text_item, parent, false)
            return TranscriptionViewHolder(view)
        }

        override fun onBindViewHolder(holder: TranscriptionViewHolder, position: Int) {
            holder.bind(transcriptions[position])
        }

        override fun getItemCount(): Int {
            return transcriptions.size
        }
    }

    class TranscriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val arrivedAt: TextView = itemView.findViewById(R.id.arrivedAt)
        private val tvText: TextView = itemView.findViewById(R.id.tv_text)

        fun bind(transcription: Transcription) {
            tvName.text = transcription.personName
            arrivedAt.text = transcription.timestamp
            tvText.text = transcription.content
        }
    }

    override fun onTranscriptionArrived(transcription: Transcription) {
        Log.d("TranscriptionsBottomSheetFragment", "transcription: ${transcription.content} by ${transcription.personName}")
        adapter?.transcriptions?.add(transcription)
        adapter?.notifyItemInserted(adapter?.transcriptions?.size ?: 0 - 1)
        transcriptsList.smoothScrollToPosition(adapter?.transcriptions?.size ?: 0 - 1)

    }
}
