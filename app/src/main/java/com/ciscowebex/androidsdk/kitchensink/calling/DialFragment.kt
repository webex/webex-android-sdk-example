package com.ciscowebex.androidsdk.kitchensink.calling

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.databinding.FragmentCallBinding
import com.ciscowebex.androidsdk.kitchensink.utils.extensions.hideKeyboard
import com.ciscowebex.androidsdk.kitchensink.utils.extensions.showKeyboard

class DialFragment : Fragment() {

    lateinit var binding: FragmentCallBinding
    private var isAddingCall = false
    private var switchToCucmOrWxcCallToggle = false
    private var moveMeeting = false

    companion object{
        private const val IS_ADDING_CALL = "isAddingCall"
        private const val CALLER_ID = "callerId"
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return FragmentCallBinding.inflate(inflater, container, false)
                .also { binding = it }
                .apply {
                    isAddingCall = arguments?.getBoolean(IS_ADDING_CALL) ?: false
                }
                .root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dialKeysList = listOf<View>(
                binding.tvNumber1,
                binding.tvNumber2,
                binding.tvNumber3,
                binding.tvNumber4,
                binding.tvNumber5,
                binding.tvNumber6,
                binding.tvNumber7,
                binding.tvNumber8,
                binding.tvNumber9,
                binding.tvNumberStar,
                binding.tvNumberHash
        )
        for (dialKey in dialKeysList) {
            dialKey.setOnClickListener { updateDialText(it) }
        }

        binding.ibStartCall.setOnClickListener {
            val dialText = binding.etDialInput.text.toString()
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

        binding.ibKeypadToggle.setOnClickListener {
            binding.dialButtonsContainer.visibility = View.GONE
            enableInput()
            binding.toggleButtonsContainer.showNext()
        }

        binding.switchCallType.setOnCheckedChangeListener {_, isChecked ->
            switchToCucmOrWxcCallToggle = isChecked
        }

        binding.moveMeetingSwitch.setOnCheckedChangeListener { _, isChecked ->
            moveMeeting = isChecked
        }

        binding.ibBackspace.setOnClickListener {
            var str = binding.etDialInput.text.toString()
            if (str.isNotEmpty()) {
                str = str.substring(0, str.length - 1)
                binding.etDialInput.setText(str)
                binding.etDialInput.setSelection(binding.etDialInput.text.length)
            }
        }

        binding.ibBackspace.setOnLongClickListener {
            binding.etDialInput.setText("")
            true
        }

        binding.llNumber0.setOnLongClickListener {
            binding.etDialInput.append(getString(R.string.number_plus))
            true
        }

        binding.llNumber0.setOnClickListener {
            binding.etDialInput.setText(binding.etDialInput.text.toString() + "0")
        }

        disableInput()

        binding.ibNumpadToggle.setOnClickListener {
            disableInput()
            binding.dialButtonsContainer.visibility = View.VISIBLE
            binding.toggleButtonsContainer.showNext()
        }
    }

    private fun enableInput() {
        binding.etDialInput.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        binding.etDialInput.setSelection(binding.etDialInput.text.length)
        binding.etDialInput.showKeyboard()
        binding.etDialInput.requestFocus()
    }

    private fun disableInput() {
        binding.etDialInput.inputType = InputType.TYPE_NULL
        context?.hideKeyboard(binding.etDialInput)
    }

    override fun onResume() {
        super.onResume()
        if (binding.ibNumpadToggle.visibility == View.VISIBLE) {
            binding.etDialInput.showKeyboard()
        }
    }

    override fun onPause() {
        super.onPause()
        context?.hideKeyboard(binding.etDialInput)
    }

    @SuppressLint("SetTextI18n")
    private fun updateDialText(view: View?) {
        val editText = binding.etDialInput
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