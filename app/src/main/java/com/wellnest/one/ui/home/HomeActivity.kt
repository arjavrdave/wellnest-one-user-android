package com.wellnest.one.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rc.wellnestmodule.BluetoothLeService
import com.wellnest.one.R
import com.wellnest.one.data.local.user_pref.PreferenceManager
import com.wellnest.one.databinding.ActivityHomeBinding
import com.wellnest.one.model.response.GetRecordingResponse
import com.wellnest.one.ui.BaseActivity
import com.wellnest.one.ui.feedback.ECGFeedbackActivity
import com.wellnest.one.ui.profile.ProfileViewModel
import com.wellnest.one.ui.profile.UserProfileActivity
import com.wellnest.one.ui.recording.pair.PairDeviceActivity
import com.wellnest.one.ui.recording.pair.SymptomsActivity
import com.wellnest.one.utils.KeyboardHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HomeActivity : BaseActivity(), View.OnClickListener {


    private lateinit var binding: ActivityHomeBinding
    private var listOfRecordings = mutableListOf<GetRecordingResponse>()
    private var bluetoothLeService: BluetoothLeService? = null

    @Inject
    lateinit var preferenceManager: PreferenceManager
    private lateinit var homeAdapter: HomeAdapter
    private val recordingViewModel: HomeViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    private var isLoading: Boolean = false
    private var isSearch: Boolean = false
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        binding.imgSettings.setOnClickListener(this)
        binding.btnRecording.setOnClickListener(this)
        binding.imgSearch.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        binding.imgClearTv.setOnClickListener(this)

        setupRecyclerView()
        profileViewModel.getProfile()
        recordingViewModel.getReadTokenForUser()
        recordingViewModel.getRecordings()
        binding.swRecordings.setOnRefreshListener {
            recordingViewModel.getRecordings()
        }
        setupObservers()
    }

    private fun setupRecyclerView() {
        homeAdapter = HomeAdapter(this@HomeActivity)
        layoutManager = LinearLayoutManager(this)
        binding.rvRecordings.layoutManager = layoutManager
        binding.rvRecordings.adapter = homeAdapter

        binding.rvRecordings.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isLoading) {
                    if (layoutManager.findLastCompletelyVisibleItemPosition() == listOfRecordings.size - 1) {
                        if (!isSearch) {
                            loadData()
                        }
                        isLoading = true
                    }
                }
            }
        })

        homeAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                layoutManager.scrollToPositionWithOffset(positionStart, 0)
            }
        })

        homeAdapter.setListener(object : HomeAdapter.ListItemClickListener {
            override fun onItemClick(id: Int, position: Int) {
                val feedbackIntent = Intent(this@HomeActivity, ECGFeedbackActivity::class.java)
                feedbackIntent.putExtra("id", id)
                feedbackIntent.putExtra("status", position)
                startActivity(feedbackIntent)
            }
        })
    }

    private fun setupObservers() {
        recordingViewModel.recordings.observe(this) {
            binding.llNoRecording.visibility = View.GONE
            if (isLoading) {
                isLoading = false
                if (isSearch) {
                    listOfRecordings.clear()
                }
                val newMovies = ArrayList<GetRecordingResponse>()
                newMovies.addAll(it)
                updateDataList(newMovies)
            } else {
                listOfRecordings.clear()
                listOfRecordings.addAll(it)
                homeAdapter.addNewRecordings(listOfRecordings)
                if (listOfRecordings.isEmpty()) {
                    if (isSearch) {
                        binding.llNoRecording.visibility = View.VISIBLE
                        binding.imgRecord.setBackgroundResource(R.drawable.image_search_noresult)
                        binding.tvLabel.text = resources.getString(R.string.no_results)
                    } else {
                        binding.llNoRecording.visibility = View.VISIBLE
                        binding.imgRecord.setBackgroundResource(R.drawable.ic_recordings_error)
                        binding.tvLabel.text = resources.getString(R.string.no_recordings)
                    }
                } else {
                    binding.llNewRecordingMsg.visibility = View.GONE
                    binding.llNoRecording.visibility = View.GONE
                }
            }
            binding.swRecordings.isRefreshing = false
        }
        recordingViewModel.readTokenUser.observe(this) {
            homeAdapter.setSasToken(it.sasToken)
        }
        recordingViewModel.errorMsg.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        profileViewModel.profileData.observe(this) {
            preferenceManager.saveUser(it)
        }
    }

    override fun onClick(view: View?) {

        when (view?.id) {
            R.id.imgSettings -> {
                startActivity(Intent(this, UserProfileActivity::class.java))
            }
            R.id.imgSearch -> {
                searchRecordings()
            }
            R.id.imgBack -> {
                goBackToListMode()
            }
            R.id.btnRecording -> {
                if (bluetoothLeService?.isConnected() == true) {
                    val symptoms = Intent(this, SymptomsActivity::class.java)
                    startActivity(symptoms)
                } else {
                    val pairDeviceIntent = Intent(this, PairDeviceActivity::class.java)
                    pairDeviceIntent.putExtra("isHomePage", true)
                    pairDeviceIntent.putExtra("prepareRecording", true)
                    startActivity(pairDeviceIntent)
                }
            }

            R.id.imgClearTv -> binding.edtSearch.text.clear()
        }
    }

    private fun searchRecordings() {
        isSearch = true
        binding.swRecordings.isEnabled = false
        binding.toolSearch.visibility = View.VISIBLE
        binding.toolMain.visibility = View.GONE
        binding.btnRecording.visibility = View.GONE
        binding.llNewRecordingMsg.visibility = View.GONE
        binding.edtSearch.requestFocus()
        KeyboardHelper.showKeyboard(this@HomeActivity, binding.edtSearch)
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.length >= 3) {
                    recordingViewModel.getRecordings(patientName = s.toString())
                }
                if (s.isEmpty()) {
                    recordingViewModel.getRecordings()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun goBackToListMode() {
        KeyboardHelper.hideKeyboard(this)
        isSearch = false
        recordingViewModel.getRecordings()
        binding.swRecordings.isEnabled = true
        binding.edtSearch.text.clear()
        binding.toolMain.visibility = View.VISIBLE
        binding.toolSearch.visibility = View.GONE
        binding.btnRecording.visibility = View.VISIBLE
        binding.llNewRecordingMsg.visibility = View.VISIBLE
    }

    fun loadData() {
        val skip = listOfRecordings.size
        val take = skip + 10
        recordingViewModel.getRecordings(null, take, skip)
    }

    private fun updateDataList(newList: List<GetRecordingResponse>) {
        val tempList = listOfRecordings.toMutableList()
        tempList.addAll(newList)
        homeAdapter.addNewRecordings(tempList)
        listOfRecordings = tempList
    }
}