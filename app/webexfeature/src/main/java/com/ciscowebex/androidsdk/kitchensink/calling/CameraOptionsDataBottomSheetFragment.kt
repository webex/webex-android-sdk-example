package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.ciscowebex.androidsdk.kitchensink.R
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

    private lateinit var propertyTextView: TextView
    private lateinit var property2TextView: TextView
    private lateinit var property2RelLayout: RelativeLayout
    private lateinit var ok: TextView
    private lateinit var propertyEditText: EditText
    private lateinit var property2EditText: EditText
    private lateinit var cancel: TextView

    var call: Call? = null
    var propertyText: String? = null
    var propertyText2: String? = null
    var doMakeProperty2RelLayoutVisible: Boolean = false
    var type: OptionType = OptionType.NONE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_data_camera_options, container, false)

        propertyTextView = view.findViewById(R.id.propertyTextView)
        property2TextView = view.findViewById(R.id.property2TextView)
        property2RelLayout = view.findViewById(R.id.property2RelLayout)
        ok = view.findViewById(R.id.ok)
        propertyEditText = view.findViewById(R.id.propertyEditText)
        property2EditText = view.findViewById(R.id.property2EditText)
        cancel = view.findViewById(R.id.cancel)

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
                else -> {}
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

        return view
    }
}