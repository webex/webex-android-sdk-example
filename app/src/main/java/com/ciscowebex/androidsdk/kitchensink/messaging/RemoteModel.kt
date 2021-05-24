package com.ciscowebex.androidsdk.kitchensink.messaging

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import com.ciscowebex.androidsdk.message.RemoteFile
import com.ciscowebex.androidsdk.message.internal.RemoteFileImpl

class RemoteFile(private val model: RemoteModel): RemoteFile {

    private var thumbnail: RemoteFile.Thumbnail? = null
    class Thumbnail(private val width: Int, private val height: Int, private val mimeType: String, private val url: String): RemoteFile.Thumbnail {
        override fun getWidth(): Int {
            return width
        }

        override fun getHeight(): Int {
            return height
        }

        override fun getMimeType(): String? {
            return mimeType
        }

        override fun getUrl(): String? {
            return url
        }
    }

    init {
        thumbnail = RemoteFileImpl.ThumbnailImpl(model.thumbnailWidth?:0, model.thumbnailHeight?:0, model.mimeType?:"", model.thumbnailUrl?:"")
    }

    override fun getDisplayName(): String? {
        return model.displayName
    }

    override fun getSize(): Long {
        return model.size ?: 0
    }

    override fun getMimeType(): String? {
        return model.mimeType
    }

    override fun getThumbnail(): RemoteFile.Thumbnail? {
       return thumbnail
    }

    override fun getUrl(): String? {
        return model.url
    }

    override fun getConversationId(): String? {
        return model.conversationId
    }

    override fun getMessageId(): String? {
        return model.messageId
    }

    override fun getContentIndex(): Int? {
        return model.contentIndex
    }

}

@Parcelize
class RemoteModel(val displayName: String?,
                  val mimeType: String?,
                  val size: Long?,
                  val url: String?,
                  val conversationId: String?,
                  var messageId: String?,
                  var contentIndex: Int?,
                  var thumbnailWidth: Int?,
                  var thumbnailHeight: Int?,
                  var thumbnailMimeType: String?,
                  val thumbnailUrl: String?) : Parcelable {

    fun getRemoteFile(): RemoteFile {
        return RemoteFile(this)
    }
}
