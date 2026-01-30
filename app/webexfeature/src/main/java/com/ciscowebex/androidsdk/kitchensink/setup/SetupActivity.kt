package com.ciscowebex.androidsdk.kitchensink.setup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.widget.SwitchCompat
import com.ciscowebex.androidsdk.internal.ResultImpl
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.KitchenSinkForegroundService
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.WebexRepository
import com.ciscowebex.androidsdk.kitchensink.utils.PermissionsHelper
import com.ciscowebex.androidsdk.kitchensink.utils.SharedPrefUtils
import com.ciscowebex.androidsdk.omniusenums.ReceivingSpeechEnhancementEnableResult
import com.ciscowebex.androidsdk.phone.Phone
import com.ciscowebex.androidsdk.phone.SpeechEnhancementResult
import org.koin.android.ext.android.inject

class SetupActivity: BaseActivity() {

    enum class CameraCap {
        Front,
        Back,
        Close
    }

    private var cameraCap: CameraCap = CameraCap.Close
    private lateinit var callCap: WebexRepository.CallCap
    private lateinit var streamMode: Phone.VideoStreamMode
    // permissionsHelper is inherited from BaseActivity

    // View references
    private lateinit var cameraOptions: TextView
    private lateinit var audioCallOnly: RadioButton
    private lateinit var audioVideoCall: RadioButton
    private lateinit var callCapabilityRadioGroup: RadioGroup
    private lateinit var enableBgStreamToggle: ToggleButton
    private lateinit var enableHWAccelToggle: ToggleButton
    private lateinit var enableAppBackgroundToggle: ToggleButton
    private lateinit var composited: RadioButton
    private lateinit var multiStream: RadioButton
    private lateinit var streamModeRadioGroup: RadioGroup
    private lateinit var enableBgConnectionToggle: ToggleButton
    private lateinit var enablePhonePermissionToggle: ToggleButton
    private lateinit var logLevelSpinner: Spinner
    private lateinit var bandwidthSpinner: Spinner
    private lateinit var switchConsoleLog: SwitchCompat
    private lateinit var multiStreamApproachNewToggle: ToggleButton
    private lateinit var enableLegacyNoiseRemovalToggle: ToggleButton
    private lateinit var enableSpeechEnhancementToggle: ToggleButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = "SetupActivity"

        setContentView(R.layout.activity_setup)

        // Initialize views
        cameraOptions = findViewById(R.id.cameraOptions)
        audioCallOnly = findViewById(R.id.audioCallOnly)
        audioVideoCall = findViewById(R.id.audioVideoCall)
        callCapabilityRadioGroup = findViewById(R.id.callCapabilityRadioGroup)
        enableBgStreamToggle = findViewById(R.id.enableBgStreamToggle)
        enableHWAccelToggle = findViewById(R.id.enableHWAccelToggle)
        enableAppBackgroundToggle = findViewById(R.id.enableAppBackgroundToggle)
        composited = findViewById(R.id.composited)
        multiStream = findViewById(R.id.multiStream)
        streamModeRadioGroup = findViewById(R.id.streamModeRadioGroup)
        enableBgConnectionToggle = findViewById(R.id.enableBgConnectionToggle)
        enablePhonePermissionToggle = findViewById(R.id.enablePhonePermissionToggle)
        logLevelSpinner = findViewById(R.id.logLevelSpinner)
        bandwidthSpinner = findViewById(R.id.bandwidth_spinner)
        switchConsoleLog = findViewById(R.id.switchConsoleLog)
        multiStreamApproachNewToggle = findViewById(R.id.multiStreamApproachNewToggle)
        enableLegacyNoiseRemovalToggle = findViewById(R.id.enableLegacyNoiseRemovalToggle)
        enableSpeechEnhancementToggle = findViewById(R.id.enableSpeechEnhancementToggle)

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

        streamMode = webexViewModel.streamMode

        when (streamMode) {
            Phone.VideoStreamMode.COMPOSITED -> {
                composited.isChecked = true
            }
            Phone.VideoStreamMode.AUXILIARY -> {
                multiStream.isChecked = true
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

        enableBgStreamToggle.isChecked = webexViewModel.enableBgStreamtoggle

        enableBgStreamToggle.setOnCheckedChangeListener { _, checked ->
            webexViewModel.enableBgStreamtoggle = checked
            webexViewModel.enableBackgroundStream(checked)
        }

        enableHWAccelToggle.isChecked = webexViewModel.enableHWAcceltoggle

        enableHWAccelToggle.setOnCheckedChangeListener { _, checked ->
            webexViewModel.enableHWAcceltoggle = checked
            webexViewModel.setHardwareAccelerationEnabled(checked)
        }

        enableAppBackgroundToggle.isChecked = SharedPrefUtils.isAppBackgroundRunningPreferred(this@SetupActivity)

        enableAppBackgroundToggle.setOnCheckedChangeListener { _, checked ->
            SharedPrefUtils.setAppBackgroundRunningPreferred(this@SetupActivity, checked)
            if(checked){
                KitchenSinkForegroundService.startForegroundService(this@SetupActivity)
            }else{
                KitchenSinkForegroundService.stopForegroundService(this@SetupActivity)
            }
        }

        streamModeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.composited -> {
                    webexViewModel.streamMode = Phone.VideoStreamMode.COMPOSITED
                }
                R.id.multiStream -> {
                    webexViewModel.streamMode = Phone.VideoStreamMode.AUXILIARY
                }
            }

            webexViewModel.setVideoStreamMode(webexViewModel.streamMode)
        }

        enableBgConnectionToggle.isChecked = webexViewModel.enableBgConnectiontoggle

        enableBgConnectionToggle.setOnCheckedChangeListener { _, checked ->
            webexViewModel.enableBgConnectiontoggle = checked
            webexViewModel.enableBackgroundConnection(checked)
        }

        enablePhonePermissionToggle.isChecked = webexViewModel.enablePhoneStatePermission

        enablePhonePermissionToggle.setOnCheckedChangeListener { _, checked ->
            webexViewModel.enablePhoneStatePermission = checked
            webexViewModel.enableAskingReadPhoneStatePermission(checked)
        }

        logLevelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                webexViewModel.logFilter = resources.getStringArray(R.array.logFilterArray)[position]
                webexViewModel.setLogLevel(webexViewModel.logFilter)
                Log.d(tag, "selected logLevel ${webexViewModel.logFilter}")
            }
        }

        logLevelSpinner.setSelection(resources.getStringArray(R.array.logFilterArray).indexOf(webexViewModel.logFilter))

        bandwidthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                webexViewModel.maxVideoBandwidth = resources.getStringArray(R.array.bw_options)[position] }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                webexViewModel.maxVideoBandwidth = WebexRepository.BandWidthOptions.BANDWIDTH_720P.name
            }

        }

        bandwidthSpinner.setSelection(resources.getStringArray(R.array.bw_options).indexOf(webexViewModel.maxVideoBandwidth))

        switchConsoleLog.setOnCheckedChangeListener { _ , checked ->
            webexViewModel.isConsoleLoggerEnabled = checked
            webexViewModel.enableConsoleLogger(webexViewModel.isConsoleLoggerEnabled)
            Log.d(tag, "enable console logger ${webexViewModel.isConsoleLoggerEnabled}")
        }
        switchConsoleLog.isChecked = webexViewModel.isConsoleLoggerEnabled

        cameraOptions.setOnClickListener {
            checkCameraPermissions()
        }

        multiStreamApproachNewToggle.isChecked = webexViewModel.multistreamNewApproach

        multiStreamApproachNewToggle.setOnCheckedChangeListener { _, checked ->
            webexViewModel.multistreamNewApproach = checked
        }

        enableLegacyNoiseRemovalToggle.setOnCheckedChangeListener { _, checked ->
            webexViewModel.useLegacyReceiverNoiseRemoval(checked)
            webexViewModel.enableLegacyNoiseRemoval = checked
        }

        enableLegacyNoiseRemovalToggle.isChecked = webexViewModel.enableLegacyNoiseRemoval

        enableSpeechEnhancementToggle.setOnCheckedChangeListener { _, checked ->
            webexViewModel.enableReceiverSpeechEnhancementByDefault(checked) {
                if (it.isSuccessful) {
                    Toast.makeText(this@SetupActivity, "Speech enhancement $checked", Toast.LENGTH_SHORT).show()
                }
                else {
                    showErrorDialog(it.error?.errorMessage ?: "Failed to enable speech enhancement")
                }
            }
        }

        enableSpeechEnhancementToggle.isChecked = webexViewModel.isReceiverSpeechEnhancementEnabledByDefault()
    }

    private fun checkCameraPermissions(): Boolean {
        if (!permissionsHelper.hasCameraPermission()) {
            Log.d(tag, "requesting read permission")
            requestPermissions(PermissionsHelper.permissionForCamera(), PermissionsHelper.PERMISSIONS_CAMERA_REQUEST)
            return true
        } else {
            startActivity(Intent(this@SetupActivity, SetupCameraActivity::class.java))
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PermissionsHelper.PERMISSIONS_CAMERA_REQUEST -> {
                if (PermissionsHelper.resultForCallingPermissions(permissions, grantResults)) {
                    Log.d(tag, "camera permission granted")
                    startActivity(Intent(this@SetupActivity, SetupCameraActivity::class.java))
                } else {
                    Toast.makeText(this, getString(R.string.camera_permission_error), Toast.LENGTH_LONG).show()
                }
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
}