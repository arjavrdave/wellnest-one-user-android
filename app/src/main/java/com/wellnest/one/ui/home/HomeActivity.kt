package com.wellnest.one.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.wellnest.one.ui.profile.UserProfileActivity
import com.wellnest.one.ui.recording.RecordingViewModel
import com.wellnest.one.ui.recording.pair.PairDeviceActivity
import com.wellnest.one.ui.recording.pair.SymptomsActivity
import com.wellnest.one.utils.KeyboardHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Hussain on 08/11/22.
 */
@AndroidEntryPoint
class HomeActivity : BaseActivity(), View.OnClickListener, TextWatcher {

    private var listOfRecordings = listOf<GetRecordingResponse>()
    private val TAG = "HomeActivity"

    private lateinit var binding: ActivityHomeBinding

    private var bluetoothLeService: BluetoothLeService? = null

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private lateinit var recordingAdapter: RecordingAdapter

    private val recordingViewModel: RecordingViewModel by viewModels()

    private var mTotalRecordings = 30
    private var mSkip = 0

    private var mSearchMode = false

    private var loading = true
    private var pastVisiblesItems = 0
    private var visibleItemCount: Int = 0
    private var totalItemCount: Int = 0

    private var LIST_MODE: String = "listMode"
    private var SEARCH_MODE: String = "searchMode"
    private var CURRENT_MODE = ""

    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        binding.imgSettings.setOnClickListener(this)
        binding.btnRecording.setOnClickListener(this)
        binding.imgSearch.setOnClickListener(this)
        binding.imgBack.setOnClickListener(this)
        binding.imgClearTv.setOnClickListener(this)

        layoutManager = LinearLayoutManager(this)

        recordingAdapter = RecordingAdapter(this)


        recordingAdapter.addNewRecordings(mutableListOf())

        binding.rvRecordings.adapter = recordingAdapter
        binding.rvRecordings.layoutManager = layoutManager

        setInfiniteScrolling()

        setupObservers()

        recordingViewModel.getReadTokenForUser()

        recordingViewModel.getRecordings()

        binding.swRecordings.setOnRefreshListener {
            recordingViewModel.getRecordings()
        }

        val token = preferenceManager.getFcmToken()
        Log.i(TAG,"$token")

    }

    private fun setupObservers() {
        recordingViewModel.recordings.observe(this) {

            if (it.isEmpty() && recordingAdapter.itemCount == 0) {
                binding.imgSearch.visibility = View.INVISIBLE
                binding.groupError.visibility = View.VISIBLE
                binding.llNoRecording.visibility = View.VISIBLE

                if (CURRENT_MODE == LIST_MODE)
                    binding.llNewRecordingMsg.visibility = View.VISIBLE

                recordingAdapter.setSearchedRecordings(mutableListOf())
            } else {
                binding.groupError.visibility = View.GONE
                binding.llNoRecording.visibility = View.GONE
                binding.llNewRecordingMsg.visibility = View.GONE

                if (binding.swRecordings.isRefreshing && !mSearchMode) {
                    loading = true
                    binding.swRecordings.isRefreshing = false
                    recordingAdapter.addNewRecordings(it.toMutableList())
                    mTotalRecordings = 60
                    mSkip = 30
                } else if (!mSearchMode) {
                    listOfRecordings = it
                    loading = true
                    recordingAdapter.addECGRecordings(it.toMutableList())
                    mTotalRecordings += 30
                    mSkip += 30
                } else {
                    recordingAdapter.setSearchedRecordings(it.toMutableList())
                }
            }
            binding.swRecordings.isRefreshing = false
        }

        recordingViewModel.readTokenUser.observe(this) {
            recordingAdapter.setSasToken(it.sasToken)
        }

        recordingViewModel.errorMsg.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }


    private fun searchRecordings() {
        mSearchMode = true
        binding.swRecordings.isEnabled = false

        binding.edtSearch.addTextChangedListener(this)
        binding.llNewRecordingMsg.visibility = View.GONE


        if (listOfRecordings.isEmpty()) {
            binding.groupError.visibility = View.VISIBLE
        } else {
            binding.groupError.visibility = View.GONE
        }

        toggleNoResultView(SEARCH_MODE)

        visibility(View.GONE, View.VISIBLE, View.GONE)


        binding.edtSearch.requestFocus()
        KeyboardHelper.showKeyboard(this)
    }

    private fun toggleNoResultView(type: String) {

        when (type) {
            LIST_MODE -> {
                binding.imgNoResult.setBackgroundResource(R.drawable.ic_recordings_error)
                binding.tvNoResults.text = "No Recordings"
                CURRENT_MODE = LIST_MODE
            }
            SEARCH_MODE -> {
                binding.imgNoResult.setBackgroundResource(R.drawable.image_search_noresult)
                binding.tvNoResults.text = "No Results"
                CURRENT_MODE = SEARCH_MODE
            }
        }
    }

    fun visibility(home: Int, search: Int, recording: Int) {
        binding.groupHome.visibility = home
        binding.groupSearch.visibility = search
        binding.btnRecording.visibility = recording
    }

    private fun goBackToListMode() {
        mSearchMode = false
        KeyboardHelper.hideKeyboard(this)
        binding.swRecordings.isEnabled = true
        binding.edtSearch.text.clear()
        binding.edtSearch.removeTextChangedListener(this)


        if (listOfRecordings.isEmpty()) {
            binding.groupError.visibility = View.VISIBLE
            binding.llNewRecordingMsg.visibility = View.VISIBLE
        }

        if (listOfRecordings.isNotEmpty())
            recordingAdapter.addNewRecordings(listOfRecordings.toMutableList())


        toggleNoResultView(LIST_MODE)

        CoroutineScope(Dispatchers.Main).launch {
            delay(500)
            visibility(View.VISIBLE, View.GONE, View.VISIBLE)
        }
    }


    override fun onResume() {
        super.onResume()
        if (bluetoothLeService != null) {
            setBluetoothState()
        } else {
            val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
            bindService(
                gattServiceIntent,
                mServiceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    private fun setInfiniteScrolling() {

        binding.rvRecordings.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.childCount
                    totalItemCount = layoutManager.itemCount
                    pastVisiblesItems = layoutManager.findFirstVisibleItemPosition()

                    if (loading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            loading = false;
                            recordingViewModel.getRecordings(null, mTotalRecordings, mSkip)
                        }
                    }
                }
            }
        })
    }

    private fun setBluetoothState() {
//        if (!checkBluetoothPermissions()) return

//        val bluetoothDevice = preferenceManager.getBluetoothDevice()
//
//        if (bluetoothLeService?.isConnected() == false) {
//            val eventProp = JSONObject()
//            eventProp.put("ecgDeviceId", bluetoothDevice?.deviceId)
//
//            //eventProp.put("patientId", patient.id)
//            Amplitude.getInstance().logEvent("Start Pairing (New Recording)", eventProp)
//
//            isBluetoothDeviceConnected = false
//            return
//        } else {
//            isBluetoothDeviceConnected = true
//            isUsbDeviceConnected = false
//            return
//        }
    }

    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            bluetoothLeService = (service as BluetoothLeService.LocalBinder).service
            if (!bluetoothLeService!!.initialize()) {
                //  finish()
            }
            setBluetoothState()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothLeService = null
            //WellNestLoader.dismissLoader()
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

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        if (p0?.length!! > 3)
            recordingViewModel.getRecordings(patientName = p0.toString())
    }

    override fun afterTextChanged(p0: Editable?) {
    }
}