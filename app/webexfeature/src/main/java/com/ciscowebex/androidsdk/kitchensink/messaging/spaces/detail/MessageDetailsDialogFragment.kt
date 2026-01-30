package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.detail

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemAttachmentsBinding
import com.ciscowebex.androidsdk.kitchensink.messaging.BaseDialogFragment
import com.ciscowebex.androidsdk.kitchensink.messaging.RemoteModel
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.message.Message
import com.ciscowebex.androidsdk.message.RemoteFile
import org.koin.android.ext.android.inject

class MessageDetailsDialogFragment : BaseDialogFragment() {

    companion object {
        fun newInstance(messageId: String): MessageDetailsDialogFragment {
            val args = Bundle()
            args.putString(Constants.Bundle.MESSAGE_ID, messageId)

            val fragment = MessageDetailsDialogFragment()
            fragment.arguments = args

            return fragment
        }
    }

    private val messageViewModel: MessageViewModel by inject()
    private lateinit var messageId: String
    
    private lateinit var progressLayout: RelativeLayout
    private lateinit var close: ImageView
    private lateinit var messageBodyTextView: TextView
    private lateinit var attachmentTextView: TextView
    private lateinit var attachmentList: RecyclerView
    private lateinit var msgIdTextView: TextView
    private lateinit var membershipDateCreatedTextView: TextView
    private lateinit var msgPersonIdTextView: TextView
    private lateinit var msgPersonEmailTextView: TextView
    private lateinit var msgToPersonId: TextView
    private lateinit var toPersonEmailText: TextView
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        messageId = arguments?.getString(Constants.Bundle.MESSAGE_ID) ?: ""

        val view = inflater.inflate(R.layout.dialog_message_details, container, false)
        
        // Initialize views
        progressLayout = view.findViewById(R.id.progressLayout)
        close = view.findViewById(R.id.close)
        messageBodyTextView = view.findViewById(R.id.messageBodyTextView)
        attachmentTextView = view.findViewById(R.id.attachmentTextView)
        attachmentList = view.findViewById(R.id.attachmentList)
        msgIdTextView = view.findViewById(R.id.msgIdTextView)
        membershipDateCreatedTextView = view.findViewById(R.id.membershipDateCreatedTextView)
        msgPersonIdTextView = view.findViewById(R.id.msgPersonIdTextView)
        msgPersonEmailTextView = view.findViewById(R.id.msgPersonEmailTextView)
        msgToPersonId = view.findViewById(R.id.msgToPersonId)
        toPersonEmailText = view.findViewById(R.id.toPersonEmailText)
        
        progressLayout.visibility = View.VISIBLE

        messageViewModel.message.observe(viewLifecycleOwner, Observer { _msg ->
            _msg?.let {
                progressLayout.visibility = View.GONE
                // Manually set all TextView values from SpaceMessageModel
                msgIdTextView.text = it.messageId
                membershipDateCreatedTextView.text = it.createdDateTimeString
                msgPersonIdTextView.text = it.personId
                msgPersonEmailTextView.text = it.personEmail
                msgToPersonId.text = it.toPersonId
                toPersonEmailText.text = it.toPersonEmail
                setMessageBody(it.messageBody)
                setUpAttachments(it.attachments)
            }
        })

        close.setOnClickListener { dialog?.dismiss() }
        
        return view
    }

    private fun setMessageBody(msg: Message.Text) {
        var text = ""
        when {
            msg.getMarkdown() != null -> {
                text = msg.getMarkdown()!!
            }
            msg.getPlain() != null -> {
                text = msg.getPlain()!!
            }
            msg.getHtml() != null -> {
                text = msg.getHtml()!!
            }
        }
        messageBodyTextView.text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
    }

    private fun setUpAttachments(attachments: List<RemoteFile>) {
        attachmentTextView.text = getString(R.string.attachments_label, attachments.size)

        val dividerItemDecoration = DividerItemDecoration(requireContext(),
                LinearLayoutManager.VERTICAL)
        attachmentList.addItemDecoration(dividerItemDecoration)
        val onAttachmentClick: (RemoteFile) -> Unit = { remoteFile ->
            val remoteModel = RemoteModel(remoteFile.getDisplayName().orEmpty(),
                    remoteFile.getMimeType(),
                    remoteFile.getSize(),
                    remoteFile.getUrl(),
                    remoteFile.getConversationId(),
                    remoteFile.getMessageId(),
                    remoteFile.getContentIndex(),
                    remoteFile.getThumbnail()?.getWidth(),
                    remoteFile.getThumbnail()?.getHeight(),
                    remoteFile.getThumbnail()?.getMimeType(),
                    remoteFile.getThumbnail()?.getUrl())
            activity?.startActivity(FileViewerActivity.getIntent(requireContext(), remoteModel))
        }
        attachmentList.adapter = MessageAttachmentsAdapter(attachments, onAttachmentClick)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        messageViewModel.getMessageDetail(messageId)
    }

    class MessageAttachmentsAdapter(private val attachments: List<RemoteFile>, private val onAttachmentClick: (RemoteFile) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val binding = ListItemAttachmentsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return AttachmentViewHolder(binding, onAttachmentClick)
        }

        override fun getItemCount(): Int {
            return attachments.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as AttachmentViewHolder).bind(attachments[position])
        }

        inner class AttachmentViewHolder(private val binding: ListItemAttachmentsBinding, private val onAttachmentClick: (RemoteFile) -> Unit) : RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener {
                    onAttachmentClick(attachments[adapterPosition])
                }
            }

            fun bind(remoteFile: RemoteFile) {
                binding.remoteFile = remoteFile
                binding.executePendingBindings()
            }
        }
    }
}