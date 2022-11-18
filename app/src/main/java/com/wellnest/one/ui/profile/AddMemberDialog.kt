package com.wellnest.one.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.wellnest.one.R
import com.wellnest.one.databinding.DialogAddMemberBinding

/**
 * Created by Hussain on 18/11/22.
 */
class AddMemberDialog : DialogFragment(), View.OnClickListener {

    private lateinit var binding : DialogAddMemberBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL,R.style.AppTheme_FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DialogAddMemberBinding.inflate(inflater,container,false)

        binding.imgClose.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)

        return binding.root
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnSave -> {

            }

            R.id.imgClose -> {
                dismiss()
            }
        }
    }



}