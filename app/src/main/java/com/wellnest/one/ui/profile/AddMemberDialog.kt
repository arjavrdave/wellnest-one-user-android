package com.wellnest.one.ui.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch
import com.wellnest.one.R
import com.wellnest.one.databinding.DialogAddMemberBinding
import com.wellnest.one.model.request.LinkMemberRequest
import com.wellnest.one.utils.isAlphaNumeric
import com.wellnest.one.utils.isNumeric

/**
 * Created by Hussain on 18/11/22.
 */
class AddMemberDialog(private val onSave: (LinkMemberRequest) -> Unit) : DialogFragment(),
    View.OnClickListener, TextWatcher {

    private lateinit var binding: DialogAddMemberBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DialogAddMemberBinding.inflate(inflater, container, false)

        binding.imgClose.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        binding.edAge.addTextChangedListener(this)
        binding.edName.addTextChangedListener(this)
        binding.edPhoneNumber.addTextChangedListener(this)

        binding.tgSex.onChangeListener = object : ToggleSwitch.OnChangeListener {
            override fun onToggleSwitchChanged(position: Int) {
                checkValid()
            }

        }

        return binding.root
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnSave -> {
                validateForm()
            }

            R.id.imgClose -> {
                dismiss()
            }
        }
    }

    private fun validateForm() {
        val name = binding.edName.text.toString()
        if (name.isBlank()) {
            Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (!name.isAlphaNumeric()) {
            Toast.makeText(requireContext(), "Invalid Name", Toast.LENGTH_SHORT).show()
            return
        }

        val nameComp = name.split(" ")
        var firstname = ""
        var lastname = ""
        if (nameComp.size == 1) {
            firstname = nameComp.first()
            lastname = " "
        }

        if (nameComp.size >= 2) {
            firstname = nameComp[0]
            lastname = nameComp.subList(1,nameComp.size).joinToString(" ")
        }

        val age = binding.edAge.text.toString()
        if (age.isBlank()) {
            Toast.makeText(requireContext(), "Age is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (!age.isNumeric()) {
            Toast.makeText(requireContext(), "Age is invalid", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = when (binding.tgSex.checkedPosition) {
            0 -> "Male"
            1 -> "Female"
            2 -> "Other"
            else -> ""
        }

        if (gender.isBlank()) {
            Toast.makeText(requireContext(), "Gender is required", Toast.LENGTH_SHORT).show()
            return
        }

        val phone = binding.edPhoneNumber.text.toString()

        val linkMemberRequest = LinkMemberRequest(
            age.toInt(),
            firstname,
            gender,
            lastname,
            phone
        )

        dismiss()

        onSave(linkMemberRequest)
    }

    private fun checkValid() {
        binding.btnSave.isEnabled =
            binding.edName.text.isNotBlank() && binding.edAge.text.isNotBlank() && binding.tgSex.checkedPosition != null
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun afterTextChanged(p0: Editable?) {
        checkValid()
    }


}