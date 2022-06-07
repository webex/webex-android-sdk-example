package com.ciscowebex.androidsdk.kitchensink.setup

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetVirtualBackgroundItemAddBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetVirtualBackgroundItemBlurBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetVirtualBackgroundItemImageBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetVirtualBackgroundItemNoneBinding
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetVirtualBackgroundsBinding
import com.ciscowebex.androidsdk.kitchensink.utils.FileUtils
import com.ciscowebex.androidsdk.kitchensink.utils.PermissionsHelper
import com.ciscowebex.androidsdk.phone.Phone
import com.ciscowebex.androidsdk.phone.VirtualBackground
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import java.io.File


class BackgroundOptionsBottomSheetFragment(
    val onBackgroundChanged: (VirtualBackground) -> Unit,
    val onBackgroundRemoved: (VirtualBackground) -> Unit,
    val onNewBackgroundAdded: (File) -> Unit,
    val onBottomSheetDimissed: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetVirtualBackgroundsBinding
    val backgrounds: MutableList<VirtualBackground> = arrayListOf()
    private val PICKFILE_REQUEST_CODE = 10011
    private val permissionsHelper: PermissionsHelper by inject()
    var adapter: BackgroundAdapter? = null
    private val TAG: String = "BackgroundOptionsBottomSheetFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return BottomSheetVirtualBackgroundsBinding.inflate(inflater, container, false).also { binding = it }.apply {
            adapter = BackgroundAdapter(dialog, backgrounds, onBackgroundChanged,
                onBackgroundRemoved, {
                val checkingPermission = checkReadStoragePermissions()
                if (!checkingPermission) {
                    openFileExplorer()
                }
            })
            virtualBackgrounds.adapter  =  adapter
        }.root
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onBottomSheetDimissed()
    }

    class BackgroundAdapter(
        val dialog: Dialog?,
        val virtualBackgrounds: MutableList<VirtualBackground>,
        val onBackgroundChanged: (VirtualBackground) -> Unit,
        val onBackgroundRemoved: (VirtualBackground) -> Unit,
        val onAddButtonClicked: () -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
        private val itemTypeNone = 0
        private val itemTypeBlur  = 1
        private val itemTypeImage = 2
        private val itemTypeAdd   = 3

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when(viewType) {
                itemTypeNone -> {
                    val view = BottomSheetVirtualBackgroundItemNoneBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                    ItemTypeNoneViewHolder(view, dialog, onBackgroundChanged)
                }
                itemTypeBlur -> {
                    val view = BottomSheetVirtualBackgroundItemBlurBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                    ItemTypeBlurViewHolder(view, dialog, onBackgroundChanged)
                }
                itemTypeImage -> {
                    val view = BottomSheetVirtualBackgroundItemImageBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                    ItemTypeImageViewHolder(view, dialog, onBackgroundChanged, onBackgroundRemoved)
                }
                itemTypeAdd -> {
                    val view = BottomSheetVirtualBackgroundItemAddBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                    ItemTypeAddViewHolder(view, dialog, onAddButtonClicked)
                }
                else -> {
                    val view  = BottomSheetVirtualBackgroundItemNoneBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                    ItemTypeNoneViewHolder(view, dialog, onBackgroundChanged)
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when(virtualBackgrounds[position].type) {
                Phone.VirtualBackgroundType.NONE -> itemTypeNone
                Phone.VirtualBackgroundType.BLUR -> itemTypeBlur
                Phone.VirtualBackgroundType.CUSTOM -> itemTypeImage
                else -> itemTypeAdd
            }
        }

        override fun getItemCount(): Int {
            return virtualBackgrounds.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                itemTypeAdd -> (holder as ItemTypeAddViewHolder).bind()
                itemTypeNone -> (holder as ItemTypeNoneViewHolder).bind()
                itemTypeImage -> (holder as ItemTypeImageViewHolder).bind()
                itemTypeBlur -> (holder as ItemTypeBlurViewHolder).bind()
            }
        }

        inner class ItemTypeNoneViewHolder(
            val binding: BottomSheetVirtualBackgroundItemNoneBinding,
            dialog: Dialog?,
            val onBackgroundChanged: (VirtualBackground) -> Unit
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind() {
                val item =  virtualBackgrounds[adapterPosition]
                if (item.isActive) {
                    binding.tvNone.foreground =
                        ContextCompat.getDrawable(binding.tvNone.context, R.drawable.border)
                } else {
                    binding.tvNone.foreground = null
                }

                binding.tvNone.setOnClickListener {
                    binding.tvNone.foreground = ContextCompat.getDrawable(binding.tvNone.context, R.drawable.border)
                    onBackgroundChanged(item)
                    dialog?.cancel()
                }
            }
        }
        inner class ItemTypeBlurViewHolder(
            val binding: BottomSheetVirtualBackgroundItemBlurBinding,
            dialog: Dialog?,
            val onBackgroundChanged: (VirtualBackground) -> Unit
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind() {
                val item =  virtualBackgrounds[adapterPosition]
                if (item.isActive) {
                    binding.tvBlur.foreground =
                        ContextCompat.getDrawable(binding.tvBlur.context, R.drawable.border)
                } else {
                    binding.tvBlur.foreground = null
                }

                binding.tvBlur.setOnClickListener {
                    binding.tvBlur.foreground = ContextCompat.getDrawable(binding.tvBlur.context, R.drawable.border)
                    onBackgroundChanged(item)
                    dialog?.cancel()
                }
            }
        }
        inner class ItemTypeImageViewHolder(
            val binding: BottomSheetVirtualBackgroundItemImageBinding,
            dialog: Dialog?,
            val onBackgroundChanged: (VirtualBackground) -> Unit,
            val onBackgroundRemoved: (VirtualBackground) -> Unit
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind() {
                val item =  virtualBackgrounds[adapterPosition]
                if (item.isActive) {
                    binding.bgImg.foreground =
                        ContextCompat.getDrawable(binding.bgImg.context, R.drawable.border)
                } else {
                    binding.bgImg.foreground = null
                }

                binding.imgDelete.setOnClickListener {
                    onBackgroundRemoved(item)
                }

                binding.bgImg.setOnClickListener {
                    binding.bgImg.foreground = ContextCompat.getDrawable(binding.bgImg.context, R.drawable.border)
                    onBackgroundChanged(item)
                    dialog?.cancel()
                }

                val byteArray = virtualBackgrounds[adapterPosition].thumbnail?.thumbnail
                if (byteArray != null) {
                    val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    Log.d("TAG", "bitmap: ${bmp?.byteCount}")
                    binding.bgImg.setImageBitmap(bmp)
                }
            }
        }
        inner class ItemTypeAddViewHolder(
            val binding: BottomSheetVirtualBackgroundItemAddBinding,
            dialog: Dialog?,
            val onAddButtonClicked: () -> Unit
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind() {
                binding.tvAdd.setOnClickListener {
                    onAddButtonClicked()
                }
            }
        }
    }

    private fun openFileExplorer() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, PICKFILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        handleActivityResult(requestCode, resultCode, data)
    }

    private fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let { intent ->
                intent.clipData?.let { data ->
                    for (index in 0 until data.itemCount) {
                        val uri = data.getItemAt(index).uri
                        addUriToList(uri)
                    }
                } ?: run {
                    intent.data?.let { uri ->
                        addUriToList(uri)
                    }
                }
            }
        }
    }

    private fun addUriToList(uri: Uri) {
        val filePath = FileUtils.getPath(requireContext(), uri)
        val file = File(filePath)
        Log.d(tag, "PICKFILE_REQUEST_CODE filePath: $filePath")
        Log.d(TAG, "PICKFILE_REQUEST_CODE file Exist: ${file.exists()}")

        onNewBackgroundAdded(file)
    }

    private fun checkReadStoragePermissions(): Boolean {
        if (!permissionsHelper.hasReadStoragePermission()) {
            Log.d(TAG, "requesting read permission")
            requestPermissions(
                PermissionsHelper.permissionForStorage(),
                PermissionsHelper.PERMISSIONS_STORAGE_REQUEST
            )
            return true
        } else {
            Log.d(TAG, "read permission granted")
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PermissionsHelper.PERMISSIONS_STORAGE_REQUEST -> {
                if (PermissionsHelper.resultForCallingPermissions(permissions, grantResults)) {
                    Log.d(TAG, "read permission granted")
                    openFileExplorer()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.post_message_attach_permission_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

}