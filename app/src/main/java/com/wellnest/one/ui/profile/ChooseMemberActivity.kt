package com.wellnest.one.ui.profile

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.wellnest.one.R
import com.wellnest.one.data.local.user_pref.PreferenceManager
import com.wellnest.one.databinding.ActivityChooseMemberBinding
import com.wellnest.one.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Created by Hussain on 18/11/22.
 */
@AndroidEntryPoint
class ChooseMemberActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding : ActivityChooseMemberBinding


    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_choose_member)

        val user = preferenceManager.getUser()

        binding.self.tvUserName.text = user?.getFullName()

        binding.llAddMember.setOnClickListener(this)


    }

    override fun onClick(view : View) {
        when (view.id) {

            R.id.llAddMember -> {
                showAddMemberDialog()
            }

        }
    }

    private fun showAddMemberDialog() {
        val addMemberDialog = AddMemberDialog()
        addMemberDialog.show(supportFragmentManager,null)
    }

}