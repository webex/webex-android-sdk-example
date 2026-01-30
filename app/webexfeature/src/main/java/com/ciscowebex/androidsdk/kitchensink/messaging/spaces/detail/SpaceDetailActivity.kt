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
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.widget.TextView
import android.widget.RelativeLayout
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

    private val spaceDetailViewModel: SpaceDetailViewModel by inject()
    private lateinit var spaceMessageRecyclerView: RecyclerView
    private lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var postMessageFAB: FloatingActionButton
    private lateinit var noMessagesLabel: TextView
    private lateinit var progressLayout: RelativeLayout
    private lateinit var spaceIdTextView: TextView
    private lateinit var spaceTitleTextView: TextView
    private lateinit var spaceDateCreatedTextView: TextView
    private lateinit var spaceLastActivityTextView: TextView
    private lateinit var spaceTeamIdTextView: TextView
    private val messageViewModel: MessageViewModel by inject()
    private lateinit var spaceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "SpaceDetailActivity"

        spaceId = intent.getStringExtra(Constants.Intent.SPACE_ID) ?: ""
        spaceDetailViewModel.spaceId = spaceId
        setContentView(R.layout.activity_space_detail)

        // Initialize views
        spaceMessageRecyclerView = findViewById(R.id.spaceMessageRecyclerView)
        swipeContainer = findViewById(R.id.swipeContainer)
        postMessageFAB = findViewById(R.id.postMessageFAB)
        noMessagesLabel = findViewById(R.id.noMessagesLabel)
        progressLayout = findViewById(R.id.progressLayout)
        spaceIdTextView = findViewById(R.id.spaceIdTextView)
        spaceTitleTextView = findViewById(R.id.spaceTitleTextView)
        spaceDateCreatedTextView = findViewById(R.id.spaceDateCreatedTextView)
        spaceLastActivityTextView = findViewById(R.id.spaceLastActivityTextView)
        spaceTeamIdTextView = findViewById(R.id.spaceTeamIdTextView)

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
        noMessagesLabel.visibility = View.GONE
        progressLayout.visibility = View.VISIBLE
        spaceDetailViewModel.getMessages()
    }

    private fun setUpObservers() {
        spaceDetailViewModel.space.observe(this@SpaceDetailActivity, Observer {
            it?.let { space ->
                spaceIdTextView.text = space.id
                spaceTitleTextView.text = space.title
                spaceDateCreatedTextView.text = space.createdDateTimeString
                spaceLastActivityTextView.text = space.lastActivityTimestampString
                spaceTeamIdTextView.text = space.teamId
            }
        })

        spaceDetailViewModel.spaceMessages.observe(this@SpaceDetailActivity, Observer { list ->
            list?.let {
                progressLayout.visibility = View.GONE
                swipeContainer.isRefreshing = false

                if (it.isEmpty()) {
                    noMessagesLabel.visibility = View.VISIBLE
                } else {
                    noMessagesLabel.visibility = View.GONE
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_space_message, parent, false)
        return MessageClientViewHolder(view, messageActionBottomSheetFragment, fragmentManager)
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: MessageClientViewHolder, position: Int) {
        holder.bind(messages[position])
    }

}

class MessageClientViewHolder(
    itemView: View,
    private val messageActionBottomSheetFragment: MessageActionBottomSheetFragment,
    private val fragmentManager: FragmentManager
) : RecyclerView.ViewHolder(itemView) {
    var messageItem: SpaceMessageModel? = null
    val tag = "MessageClientViewHolder"
    
    private val membershipContainer: ConstraintLayout = itemView.findViewById(R.id.membershipContainer)
    private val senderIdTextView: TextView = itemView.findViewById(R.id.senderIdTextView)
    private val sentDateTextView: TextView = itemView.findViewById(R.id.sentDateTextView)
    private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
    private val ivReply: ImageView = itemView.findViewById(R.id.iv_reply)

    init {
        membershipContainer.setOnClickListener {
            messageItem?.let { message ->
                MessageDetailsDialogFragment.newInstance(message.messageId).show(fragmentManager, "MessageDetailsDialogFragment")
            }
        }
    }

    fun bind(message: SpaceMessageModel) {
        messageItem = message
        senderIdTextView.text = message.personEmail ?: ""
        sentDateTextView.text = message.created?.toString() ?: ""
        ivReply.visibility = if (message.parentId != null) View.VISIBLE else View.GONE
        
        membershipContainer.setOnLongClickListener { view ->
            messageActionBottomSheetFragment.message = message
            messageActionBottomSheetFragment.show(fragmentManager, MessageActionBottomSheetFragment.TAG)
            true
        }

        when {
            message.messageBody.getMarkdown() != null -> {
                messageTextView.text = Html.fromHtml(message.messageBody.getMarkdown(), Html.FROM_HTML_MODE_LEGACY)
            }
            message.messageBody.getHtml() != null -> {
                messageTextView.text = Html.fromHtml(message.messageBody.getHtml(), Html.FROM_HTML_MODE_LEGACY)
            }
            message.messageBody.getPlain() != null -> {
                messageTextView.text = message.messageBody.getPlain()
            }
            else -> {
                messageTextView.text = ""
            }
        }
    }
}