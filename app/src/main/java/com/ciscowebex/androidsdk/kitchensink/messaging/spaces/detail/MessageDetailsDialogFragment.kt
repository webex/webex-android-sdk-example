package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.detail

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.DialogMessageDetailsBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemAttachmentsBinding
import com.ciscowebex.androidsdk.kitchensink.messaging.BaseDialogFragment
import com.ciscowebex.androidsdk.kitchensink.messaging.RemoteModel
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.message.Message
import com.ciscowebex.androidsdk.message.RemoteFile
import kotlinx.android.synthetic.main.dialog_message_details.*
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        messageId = arguments?.getString(Constants.Bundle.MESSAGE_ID) ?: ""

        return DialogMessageDetailsBinding.inflate(inflater, container, false)
                .apply {
                    progressLayout.visibility = View.VISIBLE

                    messageViewModel.message.observe(viewLifecycleOwner, Observer { _msg ->
                        _msg?.let {
                            progressLayout.visibility = View.GONE
                            message = it
                            setMessageBody(it.messageBody)
                            setUpAttachments(it.attachments)
                        }
                    })

                    close.setOnClickListener { dialog?.dismiss() }
                }.root
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