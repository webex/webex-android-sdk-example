package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivitySpaceDetailBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.ListItemSpaceMessageBinding
import com.ciscowebex.androidsdk.kitchensink.messaging.composer.MessageComposerActivity
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.ReplyMessageModel
import com.ciscowebex.androidsdk.kitchensink.messaging.spaces.SpaceMessageModel
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.showDialogWithMessage
import com.ciscowebex.androidsdk.message.Message
import com.ciscowebex.androidsdk.message.RemoteFile
import org.koin.android.ext.android.inject

class SpaceDetailActivity : BaseActivity() {

    companion object {
        fun getIntent(context: Context, spaceId: String): Intent {
            val intent = Intent(context, SpaceDetailActivity::class.java)
            intent.putExtra(Constants.Intent.SPACE_ID, spaceId)
            return intent
        }
    }

    lateinit var messageClientAdapter: MessageClientAdapter
    lateinit var binding: ActivitySpaceDetailBinding

    private val spaceDetailViewModel: SpaceDetailViewModel by inject()
    private val messageViewModel: MessageViewModel by inject()
    private lateinit var spaceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "SpaceDetailActivity"

        spaceId = intent.getStringExtra(Constants.Intent.SPACE_ID) ?: ""
        spaceDetailViewModel.spaceId = spaceId
        DataBindingUtil.setContentView<ActivitySpaceDetailBinding>(this, R.layout.activity_space_detail)
                .also { binding = it }
                .apply {
                    val messageActionBottomSheetFragment = MessageActionBottomSheetFragment({ message -> spaceDetailViewModel.deleteMessage(message) },
                            { message -> spaceDetailViewModel.markMessageAsRead(message) },
                            { message -> replyMessageListener(message) },
                            { message -> editMessage(message)},
                            { message -> fetchMessageBeforeMessageId(message)},
                            { message -> fetchMessageBeforeDate(message)})

                    messageClientAdapter = MessageClientAdapter(messageActionBottomSheetFragment, supportFragmentManager)
                    spaceMessageRecyclerView.adapter = messageClientAdapter

                    setUpObservers()

                    swipeContainer.setOnRefreshListener {
                        spaceDetailViewModel.getMessages()
                    }
                    postMessageFAB.setOnClickListener {
                        ContextCompat.startActivity(this@SpaceDetailActivity,
                                MessageComposerActivity.getIntent(this@SpaceDetailActivity, MessageComposerActivity.Companion.ComposerType.POST_SPACE, spaceDetailViewModel.spaceId, null), null)
                    }
                }
    }

    private fun replyMessageListener(message: SpaceMessageModel) {
        val parentMessage = messageViewModel.getMessage(message.parentId)
        val model = parentMessage?.let { ReplyMessageModel(it.spaceId, it.messageId, it.created, it.isSelfMentioned, it.parentId, it.isReply, it.personId, it.personEmail, it.toPersonId, it.toPersonEmail) }
        startActivity(MessageComposerActivity.getIntent(this@SpaceDetailActivity, MessageComposerActivity.Companion.ComposerType.POST_SPACE, spaceDetailViewModel.spaceId, model), null)
    }

    private fun editMessage(message: SpaceMessageModel) {
        startActivity(MessageComposerActivity.getIntent(this@SpaceDetailActivity, MessageComposerActivity.Companion.ComposerType.POST_SPACE,
                spaceDetailViewModel.spaceId, null, message.messageId))
    }

    private fun fetchMessageBeforeMessageId(message: SpaceMessageModel) {
        spaceDetailViewModel.getMessages(message.messageId)
    }

    private fun fetchMessageBeforeDate(message: SpaceMessageModel) {
        spaceDetailViewModel.getMessages(null, message.mMessage?.getCreated() ?: 0L)
    }

    override fun onResume() {
        super.onResume()
        spaceDetailViewModel.getSpaceById()
        getMessages()
    }

    private fun getMessages() {
        binding.noMessagesLabel.visibility = View.GONE
        binding.progressLayout.visibility = View.VISIBLE
        spaceDetailViewModel.getMessages()
    }

    private fun setUpObservers() {
        spaceDetailViewModel.space.observe(this@SpaceDetailActivity, Observer {
            binding.space = it
        })

        spaceDetailViewModel.spaceMessages.observe(this@SpaceDetailActivity, Observer { list ->
            list?.let {
                binding.progressLayout.visibility = View.GONE
                binding.swipeContainer.isRefreshing = false

                if (it.isEmpty()) {
                    binding.noMessagesLabel.visibility = View.VISIBLE
                } else {
                    binding.noMessagesLabel.visibility = View.GONE
                }

                messageClientAdapter.messages.clear()
                messageClientAdapter.messages.addAll(it)
                messageClientAdapter.notifyDataSetChanged()
            }
        })

        spaceDetailViewModel.deleteMessage.observe(this@SpaceDetailActivity, Observer { model ->
            model?.let {
                val position = messageClientAdapter.messages.indexOf(it)
                messageClientAdapter.messages.removeAt(position)
                messageClientAdapter.notifyItemRemoved(position)
            }
        })

        spaceDetailViewModel.messageError.observe(this@SpaceDetailActivity, Observer { errorMessage ->
            errorMessage?.let {
                showErrorDialog(it)
            }
        })

        spaceDetailViewModel.markMessageAsReadStatus.observe(this@SpaceDetailActivity, Observer { model ->
            model?.let {
                showDialogWithMessage(this@SpaceDetailActivity, R.string.success, "Message with id ${it.messageId} marked as read")
            }
        })

        spaceDetailViewModel.getMeData.observe(this@SpaceDetailActivity, Observer { model ->
            model?.let {
                MessageActionBottomSheetFragment.selfPersonId = it.personId
            }
        })

        spaceDetailViewModel.messageEventLiveData.observe(this@SpaceDetailActivity, Observer { pair ->
            if(pair != null) {
                when (pair.first) {
                    WebexRepository.MessageEvent.Received -> {
                        Log.d(tag, "Message Received event fired!")
                        if(pair.second is Message) {
                            val message = pair.second as Message
                            if (message.getId() != spaceId) {
                                return@Observer
                            }
                            // For replies, find parent and add to replies list at bottom.
                            if(message.isReply()){
                                val parentMessagePosition = messageClientAdapter.getPositionById(message.getParentId()?: "")
                                // Ignore case when parent is not found, as parent might not be present in the list
                                if(parentMessagePosition != -1) {
                                    if(parentMessagePosition == messageClientAdapter.messages.size - 1 ){
                                        messageClientAdapter.messages.add(SpaceMessageModel.convertToSpaceMessageModel(message))
                                        messageClientAdapter.notifyItemInserted(messageClientAdapter.messages.size - 1)
                                    }else {
                                        var positionToInsert = parentMessagePosition + 1
                                        for(i in (parentMessagePosition + 1) until messageClientAdapter.messages.size - 1) {
                                            if (!messageClientAdapter.messages[i].isReply){
                                                positionToInsert = i;
                                                break;
                                            }
                                        }
                                        messageClientAdapter.messages.add(positionToInsert, SpaceMessageModel.convertToSpaceMessageModel(message))
                                        messageClientAdapter.notifyItemInserted(positionToInsert)
                                    }
                                }
                            }else {
                                messageClientAdapter.messages.add(0, SpaceMessageModel.convertToSpaceMessageModel(message))
                                messageClientAdapter.notifyItemInserted(messageClientAdapter.messages.size - 1)
                            }
                        }
                    }
                    WebexRepository.MessageEvent.Deleted -> {
                        if (pair.second is String?) {
                            Log.d(tag, "Message Deleted event fired!")
                            val position = messageClientAdapter.getPositionById(pair.second as String? ?: "")
                            if (!messageClientAdapter.messages.isNullOrEmpty() && position != -1) {
                                messageClientAdapter.messages.removeAt(position)
                                messageClientAdapter.notifyItemRemoved(position)
                            }
                        }
                    }
                    WebexRepository.MessageEvent.MessageThumbnailUpdated -> {
                        Log.d(tag, "Message ThumbnailUpdated event fired!")
                        val fileList: List<RemoteFile>? = pair.second as? List<RemoteFile>
                        if(!fileList.isNullOrEmpty()){
                            for( thumbnail in fileList){
                                Log.d(tag, "Message Updated thumbnail : ${thumbnail.getDisplayName()}")
                            }
                        }

                    }
                    WebexRepository.MessageEvent.Edited -> {
                        if (pair.second is Message) {
                            val message = pair.second as Message
                            val position = messageClientAdapter.getPositionById(message.getId() ?: "")
                            if (!messageClientAdapter.messages.isNullOrEmpty() && position != -1) {
                                messageClientAdapter.messages[position] = SpaceMessageModel.convertToSpaceMessageModel(message)
                                messageClientAdapter.notifyItemChanged(position)
                            }
                        }
                    }
                    WebexRepository.MessageEvent.Updated -> {
                        val messages = pair.second as List<Message>
                        for(message in messages) {
                            val position = messageClientAdapter.getPositionById(message.getId() ?: "")
                            if (!messageClientAdapter.messages.isNullOrEmpty() && position != -1) {
                                messageClientAdapter.messages[position] = SpaceMessageModel.convertToSpaceMessageModel(message)
                                messageClientAdapter.notifyItemChanged(position)
                            }
                        }
                    }
                }
            }
        })
    }

}


class MessageClientAdapter(private val messageActionBottomSheetFragment: MessageActionBottomSheetFragment, private val fragmentManager: FragmentManager) : RecyclerView.Adapter<MessageClientViewHolder>() {
    var messages: MutableList<SpaceMessageModel> = mutableListOf()

    fun getPositionById(messageId: String): Int {
        return messages.indexOfFirst { it.messageId == messageId }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageClientViewHolder {
        return MessageClientViewHolder(ListItemSpaceMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                messageActionBottomSheetFragment, fragmentManager)
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: MessageClientViewHolder, position: Int) {
        holder.bind(messages[position])
    }

}

class MessageClientViewHolder(private val binding: ListItemSpaceMessageBinding, private val messageActionBottomSheetFragment: MessageActionBottomSheetFragment, private val fragmentManager: FragmentManager) : RecyclerView.ViewHolder(binding.root) {
    var messageItem: SpaceMessageModel? = null
    val tag = "MessageClientViewHolder"

    init {
        binding.membershipContainer.setOnClickListener {
            messageItem?.let { message ->
                MessageDetailsDialogFragment.newInstance(message.messageId).show(fragmentManager, "MessageDetailsDialogFragment")
            }
        }
    }

    fun bind(message: SpaceMessageModel) {
        binding.message = message
        messageItem = message
        binding.membershipContainer.setOnLongClickListener { view ->
            messageActionBottomSheetFragment.message = message
            messageActionBottomSheetFragment.show(fragmentManager, MessageActionBottomSheetFragment.TAG)
            true
        }

        when {
            message.messageBody.getMarkdown() != null -> {
                binding.messageTextView.text =  Html.fromHtml(message.messageBody.getMarkdown(), Html.FROM_HTML_MODE_LEGACY)
            }
            message.messageBody.getHtml() != null -> {
                binding.messageTextView.text =  Html.fromHtml(message.messageBody.getHtml(), Html.FROM_HTML_MODE_LEGACY)
            }
            message.messageBody.getPlain() != null -> {
                binding.messageTextView.text = message.messageBody.getPlain()
            }
            else -> {
                binding.messageTextView.text = ""
            }
        }

        binding.executePendingBindings()
    }
}