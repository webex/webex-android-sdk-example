package com.ciscowebex.androidsdk.kitchensink.calling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetCameraOptionsBinding
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

    private lateinit var binding: BottomSheetCameraOptionsBinding
    var call: Call? = null
    lateinit var torchModeValue: Call.TorchMode
    lateinit var flashModeValue: Call.FlashMode

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BottomSheetCameraOptionsBinding.inflate(inflater, container, false).also { binding = it }.apply {

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
        }.root
    }
}