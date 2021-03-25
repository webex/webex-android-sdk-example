package com.ciscowebex.androidsdk.kitchensink.setup

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivitySetupBinding
import com.ciscowebex.androidsdk.phone.Phone

class SetupActivity: BaseActivity() {

    enum class CameraCap {
        Front,
        Back,
        Close
    }

    lateinit var binding: ActivitySetupBinding
    private var cameraCap: CameraCap = CameraCap.Close
    private lateinit var callCap: WebexRepository.CallCap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "SetupActivity"

        DataBindingUtil.setContentView<ActivitySetupBinding>(this, R.layout.activity_setup)
                .also { binding = it }
                .apply {
                    cameraCap = getDefaultCamera()

                    callCap = webexViewModel.callCapability

                    when (callCap) {
                        WebexRepository.CallCap.Audio_Only -> {
                            audioCallOnly.isChecked = true
                        }
                        WebexRepository.CallCap.Audio_Video -> {
                            audioVideoCall.isChecked = true
                        }
                    }

                    when (cameraCap) {
                        CameraCap.Front -> {
                            frontCamera.isChecked = true
                            setAndStartFrontCamera()
                        }
                        CameraCap.Back -> {
                            backCamera.isChecked = true
                            setAndStartBackCamera()
                        }
                        CameraCap.Close -> {
                            closePreview()
                        }
                    }

                    cameraRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                        when (checkedId) {
                            R.id.closePreview -> {
                                closePreview()
                            }
                            R.id.frontCamera -> {
                                setAndStartFrontCamera()
                            }
                            R.id.backCamera -> {
                                setAndStartBackCamera()
                            }
                        }
                    }

                    callCapabilityRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                        when (checkedId) {
                            R.id.audioCallOnly -> {
                                webexViewModel.callCapability = WebexRepository.CallCap.Audio_Only
                            }
                            R.id.audioVideoCall -> {
                                webexViewModel.callCapability = WebexRepository.CallCap.Audio_Video
                            }
                        }
                    }

                    enableBgStreamToggle.isChecked = webexViewModel.enableBgtoggle

                    enableBgStreamToggle.setOnCheckedChangeListener { _, checked ->
                        webexViewModel.enableBgtoggle = checked
                        webexViewModel.enableBackgroundStream(checked)
                    }
                }
    }

    private fun getDefaultCamera(): CameraCap {
        if (cameraCap == CameraCap.Close) {
            return cameraCap
        }

        return if (webexViewModel.getDefaultFacingMode() == Phone.FacingMode.USER) {
            CameraCap.Front
        } else {
            CameraCap.Back
        }
    }

    private fun closePreview() {
        stopPreview()
    }

    private fun setAndStartFrontCamera() {
        webexViewModel.setDefaultFacingMode(Phone.FacingMode.USER)
        cameraCap = CameraCap.Front
        startPreview()
    }

    private fun setAndStartBackCamera() {
        webexViewModel.setDefaultFacingMode(Phone.FacingMode.ENVIROMENT)
        cameraCap = CameraCap.Back
        startPreview()
    }

    private fun startPreview() {
        binding.preview.visibility = View.VISIBLE
        cameraCap = getDefaultCamera()
        webexViewModel.startPreview(binding.preview)
    }

    private fun stopPreview() {
        webexViewModel.stopPreview()
        binding.preview.visibility = View.GONE
    }

    override fun onDestroy() {
        webexViewModel.stopPreview()
        super.onDestroy()
    }
}