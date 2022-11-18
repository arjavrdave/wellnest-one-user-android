package com.wellnest.one.ui.recording.pair

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.ServiceConnection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rc.wellnestmodule.BluetoothLeService
import com.rc.wellnestmodule.interfaces.IBluetooth
import com.wellnest.one.R
import com.wellnest.one.databinding.RowBluetoothDeviceBinding

/**
 * Created by Hussain on 16/11/22.
 */
class BluetoothDeviceAdapter(var context: Context, var iBluetooth: IBluetooth, var bluetoothDevices: ArrayList<BluetoothDevice>): androidx.recyclerview.widget.RecyclerView.Adapter<BluetoothDeviceAdapter.RowBluetoothDeviceHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowBluetoothDeviceHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<RowBluetoothDeviceBinding>(layoutInflater, R.layout.row_bluetooth_device, parent, false)
        val userHolder = RowBluetoothDeviceHolder(binding)
        return userHolder
    }


    private var serviceConnection: ServiceConnection? = null

    override fun onBindViewHolder(holder: BluetoothDeviceAdapter.RowBluetoothDeviceHolder, position: Int) {
        val bluetoothDevice = bluetoothDevices.get(position)
        if (bluetoothDevice.name!=null && bluetoothDevice.name.isNotEmpty()){
            holder.bluetoothDeviceBinding.tvName.text = bluetoothDevice.name
        }
        else{
            holder.bluetoothDeviceBinding.tvName.setText(bluetoothDevice.address)
        }

        /*holder.bluetoothDeviceBinding.tvName.setOnClickListener {
            iBluetooth.connect(bluetoothDevice)
        }*/

        holder.bluetoothDeviceBinding.btnPair.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                iBluetooth.connect(bluetoothDevice)
            }
        })

    }

    override fun getItemCount(): Int = bluetoothDevices.size


    fun addBluetoothDevices(deviceList: ArrayList<BluetoothDevice>) {
        bluetoothDevices = deviceList
        notifyDataSetChanged()
    }

    fun connect(mBluetoothLeService: BluetoothLeService) {
        mBluetoothLeService.connect(bluetoothDevices.get(0).address)
    }

    class RowBluetoothDeviceHolder(binding: RowBluetoothDeviceBinding): RecyclerView.ViewHolder(binding.root) {
        var bluetoothDeviceBinding:RowBluetoothDeviceBinding = binding
    }
}