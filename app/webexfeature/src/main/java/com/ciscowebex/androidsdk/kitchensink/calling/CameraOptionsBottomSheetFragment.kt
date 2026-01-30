package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.phone.Call
import com.ciscowebex.androidsdk.phone.Phone
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CameraOptionsBottomSheetFragment(val zoomFactorClickListener: (Call?) -> Unit,
                                       val torchModeClickListener: (Call?) -> Unit,
                                       val flashModeClickListener: (Call?) -> Unit,
                                       val cameraFocusClickListener: (Call?) -> Unit,
                                       val cameraCustomExposureClickListener: (Call?) -> Unit,
                                       val cameraAutoExposureClickListener: (Call?) -> Unit,
                                       val takePhotoClickListener: (Call?) -> Unit): BottomSheetDialogFragment() {
    companion object {
        val TAG = "CameraOptionsBottomSheet"
    }

    private lateinit var zoomFactor: TextView
    private lateinit var torchMode: TextView
    private lateinit var flashMode: TextView
    private lateinit var cameraFocus: TextView
    private lateinit var cameraCustomExposure: TextView
    private lateinit var cameraAutoExposure: TextView
    private lateinit var takePhoto: TextView
    private lateinit var cancel: TextView

    var call: Call? = null
    lateinit var torchModeValue: Call.TorchMode
    lateinit var flashModeValue: Call.FlashMode

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_camera_options, container, false)

        zoomFactor = view.findViewById(R.id.zoomFactor)
        torchMode = view.findViewById(R.id.torchMode)
        flashMode = view.findViewById(R.id.flashMode)
        cameraFocus = view.findViewById(R.id.cameraFocus)
        cameraCustomExposure = view.findViewById(R.id.cameraCustomExposure)
        cameraAutoExposure = view.findViewById(R.id.cameraAutoExposure)
        takePhoto = view.findViewById(R.id.takePhoto)
        cancel = view.findViewById(R.id.cancel)

        zoomFactor.setOnClickListener {
            dismiss()
            zoomFactorClickListener(call)
        }

        var torchModeText = getString(R.string.torch_mode)

        torchModeText += when (torchModeValue) {
            Call.TorchMode.OFF -> {
                " - " + getString(R.string.mode_off)
            }
            Call.TorchMode.ON -> {
                " - " + getString(R.string.mode_on)
            }
            Call.TorchMode.AUTO -> {
                " - " + getString(R.string.mode_auto)
            }
            else -> {
                " - " + getString(R.string.mode_unknown)
            }
        }
        torchMode.text = torchModeText
        torchMode.setOnClickListener {
            dismiss()
            torchModeClickListener(call)
        }

        var flashModeText = getString(R.string.flash_mode)

        flashModeText += when (flashModeValue) {
            Call.FlashMode.OFF -> {
                " - " + getString(R.string.mode_off)
            }
            Call.FlashMode.ON -> {
                " - " + getString(R.string.mode_on)
            }
            Call.FlashMode.AUTO -> {
                " - " + getString(R.string.mode_auto)
            }
            else -> {
                " - " + getString(R.string.mode_unknown)
            }
        }
        flashMode.text = flashModeText
        flashMode.setOnClickListener {
            dismiss()
            flashModeClickListener(call)
        }

        cameraFocus.setOnClickListener {
            dismiss()
            cameraFocusClickListener(call)
        }

        cameraCustomExposure.setOnClickListener {
            dismiss()
            cameraCustomExposureClickListener(call)
        }

        cameraAutoExposure.setOnClickListener {
            dismiss()
            cameraAutoExposureClickListener(call)
        }

        takePhoto.setOnClickListener {
            dismiss()
            takePhotoClickListener(call)
        }

        cancel.setOnClickListener { dismiss() }

        return view
    }
}