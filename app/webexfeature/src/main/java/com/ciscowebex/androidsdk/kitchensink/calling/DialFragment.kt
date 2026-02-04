package com.ciscowebex.androidsdk.kitchensink.calling

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewSwitcher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.ciscowebex.androidsdk.kitchensink.BaseActivity
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.utils.extensions.hideKeyboard
import com.ciscowebex.androidsdk.kitchensink.utils.extensions.showKeyboard
import android.Manifest
import com.google.android.material.switchmaterial.SwitchMaterial

class DialFragment : Fragment() {

    // View references
    private lateinit var tvNumber1: TextView
    private lateinit var tvNumber2: TextView
    private lateinit var tvNumber3: TextView
    private lateinit var tvNumber4: TextView
    private lateinit var tvNumber5: TextView
    private lateinit var tvNumber6: TextView
    private lateinit var tvNumber7: TextView
    private lateinit var tvNumber8: TextView
    private lateinit var tvNumber9: TextView
    private lateinit var tvNumberStar: TextView
    private lateinit var tvNumberHash: TextView
    private lateinit var ibStartCall: ImageButton
    private lateinit var etDialInput: EditText
    private lateinit var ibKeypadToggle: ImageButton
    private lateinit var dialButtonsContainer: ViewGroup
    private lateinit var toggleButtonsContainer: ViewSwitcher
    private lateinit var switchCallType: SwitchMaterial
    private lateinit var moveMeetingSwitch: SwitchMaterial
    private lateinit var ibBackspace: ImageButton
    private lateinit var llNumber0: LinearLayout
    private lateinit var ibNumpadToggle: ImageButton
    private var isAddingCall = false
    private var switchToCucmOrWxcCallToggle = false
    private var moveMeeting = false

    private val callingPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val allGranted = grants.values.all { it }
            val webexVM = (activity as? BaseActivity)?.webexViewModel
            if (allGranted) {
                webexVM?.retryPendingDialIfAny()
                webexVM?.retryPendingAnswerIfAny()
            } else {
                Toast.makeText(requireContext(), getString(R.string.permission_error), Toast.LENGTH_LONG).show()
            }
        }

    companion object{
        private const val IS_ADDING_CALL = "isAddingCall"
        private const val CALLER_ID = "callerId"
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        isAddingCall = arguments?.getBoolean(IS_ADDING_CALL) ?: false
        return inflater.inflate(R.layout.fragment_call, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        tvNumber1 = view.findViewById(R.id.tv_number_1)
        tvNumber2 = view.findViewById(R.id.tv_number_2)
        tvNumber3 = view.findViewById(R.id.tv_number_3)
        tvNumber4 = view.findViewById(R.id.tv_number_4)
        tvNumber5 = view.findViewById(R.id.tv_number_5)
        tvNumber6 = view.findViewById(R.id.tv_number_6)
        tvNumber7 = view.findViewById(R.id.tv_number_7)
        tvNumber8 = view.findViewById(R.id.tv_number_8)
        tvNumber9 = view.findViewById(R.id.tv_number_9)
        tvNumberStar = view.findViewById(R.id.tv_number_star)
        tvNumberHash = view.findViewById(R.id.tv_number_hash)
        ibStartCall = view.findViewById(R.id.ib_startCall)
        etDialInput = view.findViewById(R.id.et_dial_input)
        ibKeypadToggle = view.findViewById(R.id.ib_keypad_toggle)
        dialButtonsContainer = view.findViewById(R.id.dial_buttons_container)
        toggleButtonsContainer = view.findViewById(R.id.toggle_buttons_container)
        switchCallType = view.findViewById(R.id.switch_callType)
        moveMeetingSwitch = view.findViewById(R.id.moveMeetingSwitch)
        ibBackspace = view.findViewById(R.id.ib_backspace)
        llNumber0 = view.findViewById(R.id.ll_number_0)
        ibNumpadToggle = view.findViewById(R.id.ib_numpad_toggle)
        
        // Fragment-local fallback observer to launch permission prompts
        (activity as? BaseActivity)?.webexViewModel?.callingLiveData?.observe(viewLifecycleOwner) { live ->
            val missing = live?.missingPermissions
            if (!missing.isNullOrEmpty()) {
                val normalized = normalizePermissionsForApi(missing.toSet()).toTypedArray()
                callingPermissionLauncher.launch(normalized)
            }
        }
        val dialKeysList = listOf<View>(
                tvNumber1,
                tvNumber2,
                tvNumber3,
                tvNumber4,
                tvNumber5,
                tvNumber6,
                tvNumber7,
                tvNumber8,
                tvNumber9,
                tvNumberStar,
                tvNumberHash
        )
        for (dialKey in dialKeysList) {
            dialKey.setOnClickListener { updateDialText(it) }
        }

        ibStartCall.setOnClickListener {
            val dialText = etDialInput.text.toString()
            if(isAddingCall){
                val intent = Intent()
                intent.putExtra(CALLER_ID, dialText)
                intent.putExtra("switchToUcOrWebexCalling", switchToCucmOrWxcCallToggle)
                activity?.setResult(Activity.RESULT_OK, intent)
                activity?.finish()
            }else{
                startActivity(context?.let { ctx -> CallActivity.getOutgoingIntent(ctx, dialText, switchToCucmOrWxcCallToggle, moveMeeting)})
            }
        }

        ibKeypadToggle.setOnClickListener {
            dialButtonsContainer.visibility = View.GONE
            enableInput()
            toggleButtonsContainer.showNext()
        }

        switchCallType.setOnCheckedChangeListener {_, isChecked ->
            switchToCucmOrWxcCallToggle = isChecked
        }

        moveMeetingSwitch.setOnCheckedChangeListener { _, isChecked ->
            moveMeeting = isChecked
        }

        ibBackspace.setOnClickListener {
            var str = etDialInput.text.toString()
            if (str.isNotEmpty()) {
                str = str.substring(0, str.length - 1)
                etDialInput.setText(str)
                etDialInput.setSelection(etDialInput.text.length)
            }
        }

        ibBackspace.setOnLongClickListener {
            etDialInput.setText("")
            true
        }

        llNumber0.setOnLongClickListener {
            etDialInput.append(getString(R.string.number_plus))
            true
        }

        llNumber0.setOnClickListener {
            etDialInput.setText(etDialInput.text.toString() + "0")
        }

        disableInput()

        ibNumpadToggle.setOnClickListener {
            disableInput()
            dialButtonsContainer.visibility = View.VISIBLE
            toggleButtonsContainer.showNext()
        }
    }

    private fun normalizePermissionsForApi(perms: Set<String>): Set<String> {
        if (Build.VERSION.SDK_INT >= 31) {
            val mapped = perms.map {
                if (it == Manifest.permission.BLUETOOTH) Manifest.permission.BLUETOOTH_CONNECT else it
            }
            return mapped.toSet()
        }
        return perms
    }

    private fun enableInput() {
        etDialInput.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        etDialInput.setSelection(etDialInput.text.length)
        etDialInput.showKeyboard()
        etDialInput.requestFocus()
    }

    private fun disableInput() {
        etDialInput.inputType = InputType.TYPE_NULL
        context?.hideKeyboard(etDialInput)
    }

    override fun onResume() {
        super.onResume()
        if (ibNumpadToggle.visibility == View.VISIBLE) {
            etDialInput.showKeyboard()
        }
    }

    override fun onPause() {
        super.onPause()
        context?.hideKeyboard(etDialInput)
    }

    @SuppressLint("SetTextI18n")
    private fun updateDialText(view: View?) {
        val editText = etDialInput
        when (view?.id) {
            R.id.tv_number_1 -> {
                editText.setText(editText.text.toString() + "1")
            }
            R.id.tv_number_2 -> {
                editText.setText(editText.text.toString() + "2")
            }
            R.id.tv_number_3 -> {
                editText.setText(editText.text.toString() + "3")
            }
            R.id.tv_number_4 -> {
                editText.setText(editText.text.toString() + "4")
            }
            R.id.tv_number_5 -> {
                editText.setText(editText.text.toString() + "5")
            }
            R.id.tv_number_6 -> {
                editText.setText(editText.text.toString() + "6")
            }
            R.id.tv_number_7 -> {
                editText.setText(editText.text.toString() + "7")
            }
            R.id.tv_number_8 -> {
                editText.setText(editText.text.toString() + "8")
            }
            R.id.tv_number_9 -> {
                editText.setText(editText.text.toString() + "9")
            }
            R.id.tv_number_star -> {
                editText.setText(editText.text.toString() + "*")
            }
            R.id.tv_number_hash -> {
                editText.setText(editText.text.toString() + "#")
            }
        }
    }
}