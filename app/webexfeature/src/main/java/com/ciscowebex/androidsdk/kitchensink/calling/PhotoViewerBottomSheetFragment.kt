package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.ciscowebex.androidsdk.kitchensink.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.graphics.BitmapFactory

class PhotoViewerBottomSheetFragment: BottomSheetDialogFragment() {
    companion object {
        val TAG = "PhotoViewerBottomSheetFragment"
    }

    private lateinit var photoViewerImageView: ImageView
    private lateinit var cancel: TextView
    var imageData: ByteArray? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_photo_viewer, container, false)

        photoViewerImageView = view.findViewById(R.id.photoViewerImageView)
        cancel = view.findViewById(R.id.cancel)

        imageData?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size);
            photoViewerImageView.setImageBitmap(bitmap)
        }
        cancel.setOnClickListener { dismiss() }

        return view
    }
}