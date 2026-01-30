package com.ciscowebex.androidsdk.kitchensink.person

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.utils.EmailAddress
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PeopleActionBottomSheetFragment(
    val postToPersonID: (String?, String?, PersonModel) -> Unit,
    val postToPersonEmail: (String?, EmailAddress?, PersonModel) -> Unit,
    val fetchPersonByID: (String) -> Unit,
    val updatePerson: (String, PersonModel) -> Unit,
    val deletePerson: (String) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var postMessageByIDView: TextView
    private lateinit var postMessageByEmailView: TextView
    private lateinit var fetchPersonByIDView: TextView
    private lateinit var updatePersonView: TextView
    private lateinit var deletePersonView: TextView
    private lateinit var cancelView: TextView

    lateinit var model: PersonModel
    lateinit var personId: String
    lateinit var email: EmailAddress

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_people_options, container, false)

        postMessageByIDView = view.findViewById(R.id.postMessageByID)
        postMessageByEmailView = view.findViewById(R.id.postMessageByEmail)
        fetchPersonByIDView = view.findViewById(R.id.fetchPersonByID)
        updatePersonView = view.findViewById(R.id.updatePerson)
        deletePersonView = view.findViewById(R.id.deletePerson)
        cancelView = view.findViewById(R.id.cancel)

        postMessageByIDView.setOnClickListener {
            dismiss()
            postToPersonID(personId, null, model)
        }

        postMessageByEmailView.setOnClickListener {
            dismiss()
            postToPersonEmail(null, email, model)
        }

        fetchPersonByIDView.setOnClickListener {
            dismiss()
            fetchPersonByID(personId)
        }

        updatePersonView.setOnClickListener{
            dismiss()
            updatePerson(personId, model)
        }

        deletePersonView.setOnClickListener{
            dismiss()
            deletePerson(personId)
        }

        cancelView.setOnClickListener { dismiss() }

        return view
    }

}