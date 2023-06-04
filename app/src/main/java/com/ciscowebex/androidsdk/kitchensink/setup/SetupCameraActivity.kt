package com.ciscowebex.androidsdk.kitchensink.setup

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.databinding.ActivityCameraConfigBinding
import com.ciscowebex.androidsdk.kitchensink.utils.extensions.toast
import com.ciscowebex.androidsdk.message.LocalFile
import com.ciscowebex.androidsdk.phone.AdvancedSetting
import com.ciscowebex.androidsdk.phone.Phone
import com.ciscowebex.androidsdk.phone.VirtualBackground
import com.ciscowebex.androidsdk.utils.internal.MimeUtils
import java.io.File

class SetupCameraActivity: BaseActivity() {

    enum class CameraCap {
        Front,
        Back,
        Close
    }

    lateinit var binding: ActivityCameraConfigBinding
    private var cameraCap: CameraCap = CameraCap.Front
    private var enablePhotoCapture:Boolean = true
    private lateinit var callCap: WebexRepository.CallCap
    var bottomSheetFragment: BackgroundOptionsBottomSheetFragment? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "SetupActivity"

        DataBindingUtil.setContentView<ActivityCameraConfigBinding>(this, R.layout.activity_camera_config)
                .also { binding = it }
                .apply {
                    cameraCap = getDefaultCamera()
                    enablePhotoCapture = getEnablePhotoCaptureSetting() == true
                    callCap = webexViewModel.callCapability

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

                    if(enablePhotoCapture) {
                        isPhotoCaptureSupportedTrue.isChecked = true
                    }
                    else {
                        isPhotoCaptureSupportedFalse.isChecked = true
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

                    changeBgButton.setOnClickListener {
                        webexViewModel.fetchVirtualBackgrounds()
                    }

                    isPhotoCaptureSupportedButton.setOnCheckedChangeListener { _, checkedId ->
                        when(checkedId) {
                            R.id.isPhotoCaptureSupportedTrue -> {
                                enablePhotoCaptureSetting(true)
                            }
                            R.id.isPhotoCaptureSupportedFalse -> {
                                enablePhotoCaptureSetting(false)
                            }
                        }
                    }

                    val limit = webexViewModel.getMaxVirtualBackgrounds()
                    textLimit.setText(limit.toString())
                    setLimitButton.setOnClickListener {
                        val text = textLimit.text.toString()
                        if (text.isNotEmpty() && isNumber(text)) {
                            webexViewModel.setMaxVirtualBackgrounds(text.toInt())
                        } else {
                            toast(getString(R.string.invalid_number_error))
                        }
                    }

                    webexViewModel.virtualBgError.observe(this@SetupCameraActivity, Observer {
                        toast(it)
                    })

                    webexViewModel.virtualBackground.observe(this@SetupCameraActivity, Observer {
                        Log.d(tag, "virtualBackgrounds size: ${it.size}")
                        val emptyBackground = VirtualBackground()

                        if (bottomSheetFragment == null) {
                            bottomSheetFragment =
                                BackgroundOptionsBottomSheetFragment({ virtualBackground ->
                                    if (!webexViewModel.isVirtualBackgroundSupported()) {
                                        Log.d(tag, getString(R.string.virtual_bg_not_supported))
                                        Toast.makeText(
                                            applicationContext,
                                            getString(R.string.virtual_bg_not_supported),
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        return@BackgroundOptionsBottomSheetFragment
                                    }

                                    webexViewModel.applyVirtualBackground(
                                        virtualBackground,
                                        Phone.VirtualBackgroundMode.PREVIEW)
                                },
                                    { virtualBackground ->
                                        webexViewModel.removeVirtualBackground(virtualBackground)
                                    },
                                    { file ->
                                        if (!webexViewModel.isVirtualBackgroundSupported()) {
                                            Log.d(tag, getString(R.string.virtual_bg_not_supported))
                                            Toast.makeText(
                                                applicationContext,
                                                getString(R.string.virtual_bg_not_supported),
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                            return@BackgroundOptionsBottomSheetFragment
                                        }

                                        val localFile = processAttachmentFile(file)
                                        webexViewModel.addVirtualBackground(localFile)
                                    },
                                    {
                                        bottomSheetFragment = null
                                    })

                            bottomSheetFragment?.show(
                                supportFragmentManager,
                                BackgroundOptionsBottomSheetFragment::class.java.name
                            )
                        }

                        bottomSheetFragment?.backgrounds?.clear()
                        bottomSheetFragment?.backgrounds?.addAll(it)
                        bottomSheetFragment?.backgrounds?.add(emptyBackground)
                        bottomSheetFragment?.adapter?.notifyDataSetChanged()
                    })
                }
    }

    private fun processAttachmentFile(file: File): LocalFile {
        var thumbnail: LocalFile.Thumbnail? = null
        if (MimeUtils.getContentTypeByFilename(file.name) == MimeUtils.ContentType.IMAGE) {
            thumbnail = LocalFile.Thumbnail(file, null, resources.getInteger(R.integer.virtual_bg_thumbnail_width), resources.getInteger(R.integer.virtual_bg_thumbnail_height))
        }

        return LocalFile(file, null, thumbnail, null)
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

    private fun enablePhotoCaptureSetting(value: Boolean) {
        webexViewModel.enablePhotoCaptureSetting(value)
    }

    private fun getEnablePhotoCaptureSetting(): Boolean? {
        return webexViewModel.getEnablePhotoCaptureSetting()
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

    private fun isNumber(s: String?): Boolean {
        return if (s.isNullOrEmpty()) false else s.all { Character.isDigit(it) }
    }
}