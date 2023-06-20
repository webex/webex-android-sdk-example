package com.ciscowebex.androidsdk.kitchensink.person

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ciscowebex.androidsdk.kitchensink.databinding.BottomSheetPeopleOptionsBinding
import com.ciscowebex.androidsdk.utils.EmailAddress
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PeopleActionBottomSheetFragment(
    val postToPersonID: (String?, String?, PersonModel) -> Unit,
    val postToPersonEmail: (String?, EmailAddress?, PersonModel) -> Unit,
    val fetchPersonByID: (String) -> Unit,
    val updatePerson: (String, PersonModel) -> Unit,
    val deletePerson: (String) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetPeopleOptionsBinding
    lateinit var model: PersonModel
    lateinit var personId: String
    lateinit var email: EmailAddress

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return BottomSheetPeopleOptionsBinding.inflate(inflater, container, false).also { binding = it }.apply {

            postMessageByID.setOnClickListener {
                dismiss()
                postToPersonID(personId, null, model)
            }

            postMessageByEmail.setOnClickListener {
                dismiss()
                postToPersonEmail(null, email, model)
            }

            fetchPersonByID.setOnClickListener {
                dismiss()
                fetchPersonByID(personId)
            }

            updatePerson.setOnClickListener{
                dismiss()
                updatePerson(personId, model)
            }

            deletePerson.setOnClickListener{
                dismiss()
                deletePerson(personId)
            }

            cancel.setOnClickListener { dismiss() }
        }.root
    }

}