package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetPhotoViewerBinding
import com.ciscowebex.androidsdk.phone.Call
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.graphics.BitmapFactory
import com.ciscowebex.androidsdk.kitchensink.R

class PhotoViewerBottomSheetFragment: BottomSheetDialogFragment() {
    companion object {
        val TAG = "PhotoViewerBottomSheetFragment"
    }

    private lateinit var binding: BottomSheetPhotoViewerBinding
    var imageData: ByteArray? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BottomSheetPhotoViewerBinding.inflate(inflater, container, false).also { binding = it }.apply {
            imageData?.let {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size);
                photoViewerImageView.setImageBitmap(bitmap)
            }
            cancel.setOnClickListener { dismiss() }
        }.root
    }
}