package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.BuildConfig
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityFileViewerBinding
import com.ciscowebex.androidsdk.kitchensink.messaging.MessagingRepository
import com.ciscowebex.androidsdk.kitchensink.messaging.RemoteModel
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.kitchensink.utils.FileUtils.getFile
import com.ciscowebex.androidsdk.kitchensink.utils.FileUtils.getThumbnailFile
import kotlinx.android.synthetic.main.activity_file_viewer.*
import org.koin.android.ext.android.inject
import java.io.File
import java.util.Locale


class FileViewerActivity : BaseActivity() {

    private var remoteModel: RemoteModel? = null
    private lateinit var binding: ActivityFileViewerBinding
    private val messageViewModel: MessageViewModel by inject()

    companion object {
        fun getIntent(context: Context, remoteFile: RemoteModel): Intent {
            val intent = Intent(context, FileViewerActivity::class.java)
            intent.putExtra(Constants.Bundle.REMOTE_FILE, remoteFile)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "FileViewerActivity"

        remoteModel = intent.getParcelableExtra(Constants.Bundle.REMOTE_FILE)

        DataBindingUtil.setContentView<ActivityFileViewerBinding>(this, R.layout.activity_file_viewer).also {
            binding = it
            remoteModel?.let { _remoteModel ->
                val text =  "${_remoteModel.getRemoteFile().getSize()} " + resources.getString(R.string.total_bytes)
                totalBytesLabel.text = text
                messageViewModel.downloadThumbnail(_remoteModel.getRemoteFile(), getThumbnailFile(applicationContext))
                setUpObservers()
            }
        }.apply {
            btnDownload.setOnClickListener {
                hideThumbnailView()
                progressBar.visibility = View.VISIBLE
                remoteModel?.let { _remoteModel ->
                    messageViewModel.downloadFile(_remoteModel.getRemoteFile(), getFile(applicationContext))
                }
            }
        }
    }

    private fun setUpObservers() {
        messageViewModel.error.observe(this, Observer { error ->
            Toast.makeText(this, "Unable to get thumbnail, error: $error", Toast.LENGTH_LONG).show()
        })

        messageViewModel.thumbnailUri.observe(this, Observer { uri ->
            uri?.let {
                Log.d(tag, "thumbnail uri: $it")
                progressBar.visibility = View.GONE
                imgThumbnail.setImageURI(it)
            }
        })

        messageViewModel.downloadFileCompletionLiveData.observe(this, Observer {
            it?.let { _pair ->
                downloadComplete(_pair)
            }
        })

        messageViewModel.downloadFileProgressLiveData.observe(this, Observer {
            it?.let { bytes ->
                val text =  "$bytes " + resources.getString(R.string.bytes_downloaded)
                progressLabel.text = text
            }
        })
    }

    private fun downloadComplete(_pair: Pair<MessagingRepository.FileDownloadEvent, String>) {
        runOnUiThread {
            progressBar.visibility = View.GONE
            when (_pair.first) {
                MessagingRepository.FileDownloadEvent.DOWNLOAD_COMPLETE -> {
                    Log.d(tag, "file downloaded at ${_pair.second}")
                    showDownloadedFile(_pair.second)
                }
                MessagingRepository.FileDownloadEvent.DOWNLOAD_FAILED -> {
                    val errorMsg = "file download failed  :${_pair.second}"
                    Log.d(tag, errorMsg)
                    Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDownloadedFile(fileUrl: String?) {
        fileUrl?.let {
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(File(fileUrl).extension.toLowerCase(Locale.US))
            Log.d(tag, "mimetype: $mimeType")
            if (mimeType != null) {
                finish()
                displayPdf(fileUrl, mimeType)
            }
        }
    }

    private fun hideThumbnailView() {
        imgThumbnail.visibility = View.GONE
        btnDownload.visibility = View.GONE
    }

    private fun getFileUri(context: Context, fileName: String): Uri? {
        val file = File(fileName)
        return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
    }

    private fun displayPdf(fileName: String, mimeType: String) {
        val uri = getFileUri(this, fileName)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, mimeType)

        // FLAG_GRANT_READ_URI_PERMISSION is needed on API 24+ so the activity opening the file can read it
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
        if (intent.resolveActivity(packageManager) == null) {
            // Show an error
        } else {
            startActivity(intent)
        }
    }
}