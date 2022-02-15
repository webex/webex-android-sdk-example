package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetDataCameraOptionsBinding
import com.ciscowebex.androidsdk.phone.Call
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CameraOptionsDataBottomSheetFragment(val zoomfactorValueSetListener: (Float) -> Unit,
                                           val cameraFocusValueSetClickListener: (Float, Float) -> Unit,
                                           val cameraCustomExposureValueSetClickListener: (Double, Float) -> Unit,
                                           val cameraAutoExposureValueSetClickListener: (Float) -> Unit): BottomSheetDialogFragment() {
    enum class OptionType {
        NONE,
        ZOOM_FACTOR,
        CAMERA_FOCUS_POINT,
        CUSTOM_EXPOSURE,
        AUTO_EXPOSURE
    }

    companion object {
        val TAG = "CameraOptionsDataBottomSheetFragment"
    }

    private lateinit var binding: BottomSheetDataCameraOptionsBinding
    var call: Call? = null
    var propertyText: String? = null
    var propertyText2: String? = null
    var doMakeProperty2RelLayoutVisible: Boolean = false
    var type: OptionType = OptionType.NONE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BottomSheetDataCameraOptionsBinding.inflate(inflater, container, false).also { binding = it }.apply {

            propertyTextView.text = propertyText
            property2TextView.text = propertyText2

            if (doMakeProperty2RelLayoutVisible) {
                property2RelLayout.visibility = View.VISIBLE
            } else {
                property2RelLayout.visibility = View.GONE
            }

            ok.setOnClickListener {
                when (type) {
                    OptionType.ZOOM_FACTOR -> {
                        if (!propertyEditText.text.isNullOrEmpty()) {
                            zoomfactorValueSetListener(propertyEditText.text.toString().toFloat())
                        }
                    }
                    OptionType.CAMERA_FOCUS_POINT -> {
                        if (!propertyEditText.text.isNullOrEmpty() && !property2EditText.text.isNullOrEmpty()) {
                            cameraFocusValueSetClickListener(propertyEditText.text.toString().toFloat(), property2EditText.text.toString().toFloat())
                        }
                    }
                    OptionType.CUSTOM_EXPOSURE -> {
                        if (!propertyEditText.text.isNullOrEmpty() && !property2EditText.text.isNullOrEmpty()) {
                            cameraCustomExposureValueSetClickListener(propertyEditText.text.toString().toDouble(), property2EditText.text.toString().toFloat())
                        }
                    }
                    OptionType.AUTO_EXPOSURE -> {
                        if (!propertyEditText.text.isNullOrEmpty()) {
                            cameraAutoExposureValueSetClickListener(propertyEditText.text.toString().toFloat())
                        }
                    }
                }
                propertyEditText.text.clear()
                property2EditText.text.clear()
                dismiss()
            }
            cancel.setOnClickListener {
                propertyEditText.text.clear()
                property2EditText.text.clear()
                dismiss()
            }
        }.root
    }
}