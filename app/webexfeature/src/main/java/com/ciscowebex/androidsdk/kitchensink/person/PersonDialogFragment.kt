package com.ciscowebex.androidsdk.kitchensink.person

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import org.koin.android.ext.android.inject

class PersonDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(personId: String) : PersonDialogFragment {
            val args = Bundle()
            args.putString(Constants.Bundle.PERSON_ID, personId)

            val fragment = PersonDialogFragment()
            fragment.arguments = args

            return fragment
        }
    }

    private val personViewModel : PersonViewModel by inject()
    private lateinit var personId : String
    
    private lateinit var progressLayout: RelativeLayout
    private lateinit var dialogOk: Button
    private lateinit var personIdTextView: TextView
    private lateinit var displayNameTextView: TextView
    private lateinit var nickNameTextView: TextView
    private lateinit var firstNameTextView: TextView
    private lateinit var lastNameTextView: TextView
    private lateinit var avatarTextView: TextView
    private lateinit var orgIdTextView: TextView
    private lateinit var createdTextView: TextView
    private lateinit var lastActivityTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var typeTextView: TextView
    private lateinit var emailTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        personId = arguments?.getString(Constants.Bundle.PERSON_ID) ?: ""

        val view = inflater.inflate(R.layout.fragment_dialog_person, container, false)
        
        // Initialize views
        progressLayout = view.findViewById(R.id.progressLayout)
        dialogOk = view.findViewById(R.id.dialogOk)
        personIdTextView = view.findViewById(R.id.personIdTextView)
        displayNameTextView = view.findViewById(R.id.displayNameTextView)
        nickNameTextView = view.findViewById(R.id.nickNameTextView)
        firstNameTextView = view.findViewById(R.id.firstNameTextView)
        lastNameTextView = view.findViewById(R.id.lastNameTextView)
        avatarTextView = view.findViewById(R.id.avatarTextView)
        orgIdTextView = view.findViewById(R.id.orgIdTextView)
        createdTextView = view.findViewById(R.id.createdTextView)
        lastActivityTextView = view.findViewById(R.id.lastActivityTextView)
        statusTextView = view.findViewById(R.id.statusTextView)
        typeTextView = view.findViewById(R.id.typeTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        
        progressLayout.visibility = View.VISIBLE

        personViewModel.person.observe(this, Observer { model ->
            if (model != null) {
                progressLayout.visibility = View.GONE
                // Manually set all TextView values
                personIdTextView.text = model.personId
                displayNameTextView.text = model.displayName
                nickNameTextView.text = model.nickName
                firstNameTextView.text = model.firstName
                lastNameTextView.text = model.lastName
                avatarTextView.text = model.avatar
                orgIdTextView.text = model.orgId
                createdTextView.text = model.createdString
                lastActivityTextView.text = model.lastActivity
                statusTextView.text = model.status
                typeTextView.text = model.type
                emailTextView.text = model.emailList
            } else {
                dismiss()
            }
        })

        dialogOk.setOnClickListener { dismiss() }
        
        return view
    }

    override fun onResume() {
        super.onResume()
        if(personId.isEmpty()) {
            personViewModel.getMe()
        } else {
            personViewModel.getPersonDetail(personId)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}