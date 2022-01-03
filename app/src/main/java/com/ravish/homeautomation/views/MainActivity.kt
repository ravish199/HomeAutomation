package com.ravish.homeautomation.views

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ravish.homeautomation.AlarmListActionListener
import com.ravish.homeautomation.R
import com.ravish.homeautomation.Utility
import com.ravish.homeautomation.Utility.Companion.ACTION_BT_BUTTON_UPDATE
import com.ravish.homeautomation.Utility.Companion.ACTION_CONNECTED_BTN_UPDATE
import com.ravish.homeautomation.Utility.Companion.ACTION_DEVICE_CONNECTED
import com.ravish.homeautomation.Utility.Companion.ACTION_DEVICE_NOT_CONNECTED
import com.ravish.homeautomation.Utility.Companion.ACTION_MESSAGE_READ
import com.ravish.homeautomation.Utility.Companion.ACTION_SOCKET_DISCONNECTED
import com.ravish.homeautomation.Utility.Companion.DATA
import com.ravish.homeautomation.Utility.Companion.MSG_SWITCH_ON_OFF
import com.ravish.homeautomation.Utility.Companion.SYNCH
import com.ravish.homeautomation.Utility.Companion.allAlarmList
import com.ravish.homeautomation.Utility.Companion.updateUI
import com.ravish.homeautomation.model.AlarmDetails
import com.ravish.homeautomation.services.BLEService
import com.ravish.homeautomation.viewmodel.AlarmViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), View.OnClickListener, AlarmListActionListener {

    var bluetoothAdapter: BluetoothAdapter? = null
    private val enableBtRequest = 201
    private val TAG = "MainActivity"
    private val TIME_REQUEST_CODE = 111
    private lateinit var timerListRViewAdapter: TimerListRViewAdapter
    private lateinit var alarmDetailsList: MutableList<AlarmDetails>

    var timer: Timer? = null
    private var isDeviceConnected = false

    lateinit var alarmViewModel: AlarmViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        alarmViewModel = ViewModelProvider(this).get(AlarmViewModel::class.java)
        connectionIcon.isEnabled = false
        bleIcon.isEnabled = false
        bulb1.setOnClickListener(this)
        bulb2.setOnClickListener(this)
        bulb3.setOnClickListener(this)
        bulb4.setOnClickListener(this)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_BT_BUTTON_UPDATE)
        intentFilter.addAction(ACTION_CONNECTED_BTN_UPDATE)
        intentFilter.addAction(ACTION_MESSAGE_READ)
        intentFilter.addAction(ACTION_SOCKET_DISCONNECTED)
        intentFilter.addAction(ACTION_DEVICE_NOT_CONNECTED)
        intentFilter.addAction(ACTION_DEVICE_CONNECTED)
        registerReceiver(
            uIUpdateReceiver, intentFilter
        )

        setTimeBtn.setOnClickListener {
            val timeIntent = Intent(this, AddTimerActivity::class.java)
            startActivityForResult(timeIntent, TIME_REQUEST_CODE)
        }

        setTimeBtn.setOnClickListener {
            addTimer()
        }

        alarmDetailsList = ArrayList()
        timerRView.layoutManager = LinearLayoutManager(baseContext)
        timerListRViewAdapter = TimerListRViewAdapter(alarmDetailsList, this)
        timerRView.adapter = timerListRViewAdapter
        alarmViewModel.getAllAlarmDetails()

        if (!isServiceRunning(BLEService::class.java)) {
            Intent(this, BLEService::class.java).also { intent ->
                startForegroundService(intent)
            }
        } else {
           updateUI(true, this, ACTION_CONNECTED_BTN_UPDATE)
            //synchDevices()
        }


        alarmViewModel.allAlarmDetailsLiveData.observe(this) { it ->
            allAlarmList = it.filter { it.enable }
            alarmDetailsList.clear()
            alarmDetailsList.addAll(it)
            timerListRViewAdapter.notifyDataSetChanged()
        }

    }


    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        manager.getRunningServices(Int.MAX_VALUE).forEach {
            if (serviceClass.name == it.service.className) {
                return true
            }
        }
        return false
    }

    private fun addTimer() {
        val addTimeIntent = Intent(this, AddTimerActivity::class.java)
        startActivityForResult(addTimeIntent, TIME_REQUEST_CODE)
    }



    private fun updateDevicesUi(deviceId: Int) {
        val isActivate = deviceId < 5
        when (if (isActivate) deviceId else 256 - deviceId) {
            1 -> bulb1.isActivated = isActivate
            2 -> bulb2.isActivated = isActivate
            3 -> bulb3.isActivated = isActivate
            4 -> bulb4.isActivated = isActivate
        }
        Log.d(TAG, "updateDevicesUi $deviceId")
    }

    private val uIUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_BT_BUTTON_UPDATE -> {
                    updateBtOnView(intent.getBooleanExtra(DATA, false))
                }
                ACTION_CONNECTED_BTN_UPDATE -> {
                    activateBtn(intent.getBooleanExtra(DATA, false))
                }
                ACTION_MESSAGE_READ -> {
                    updateDevicesUi(intent.getStringExtra(DATA)?.toInt()!!)
                    Log.d(TAG, "ACTION_MESSAGE_READ")
                }
                ACTION_SOCKET_DISCONNECTED -> {
                    activateBtn(false)
                    stopService(Intent(context, BLEService::class.java))
                    onRestart()
                    Log.d(TAG, "ACTION_SOCKET_DISCONNECTED")
                }
                ACTION_DEVICE_NOT_CONNECTED -> {
                    updateDeviceConnectionStatus(Utility.Companion.DeviceConnectinStatus.DISCONNECTED)
                    Log.d(TAG, "ACTION_DEVICE_NOT_CONNECTED")
                }
                ACTION_DEVICE_CONNECTED -> {
                   updateDeviceConnectionStatus(Utility.Companion.DeviceConnectinStatus.CONNECTED)
                    Log.d(TAG, "ACTION_DEVICE_CONNECTED")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableBlueTooth()

    }

    @SuppressLint("MissingPermission")
    private fun enableBlueTooth() {
        bluetoothAdapter?.let {
            if (!it.isEnabled) {
                val bluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(bluetoothIntent, enableBtRequest)
            } else {
               synchDevices()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TIME_REQUEST_CODE) {
            try {
                alarmViewModel.getAllAlarmDetails()
            } catch (e: Exception) {
            }
        }
    }

    private fun updateBtOnView(result: Boolean) {
        bleIcon.isEnabled = result
    }

    private fun updateAppBar(isConnected: Boolean) {
        toolbar.setBackgroundColor(
            if (isConnected) resources.getColor(R.color.connectColor) else resources.getColor(
                R.color.disconnectedColor
            )
        )
    }

    private fun activateBtn(isActivate: Boolean) {
        isDeviceConnected = isActivate
        if (isActivate) {
            progressBar.visibility = View.GONE
            shutter.visibility = View.GONE
            container.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.VISIBLE
            shutter.visibility = View.VISIBLE
            container.visibility = View.GONE
        }
        bleIcon.isEnabled = isActivate
        connectionIcon.isEnabled = isActivate
        updateAppBar(isActivate)
    }

    private fun updateDeviceConnectionStatus(deviceConnectinStatus: Utility.Companion.DeviceConnectinStatus) {
        when(deviceConnectinStatus) {
            Utility.Companion.DeviceConnectinStatus.CONNECTING -> {
                connectionStatus.visibility = View.GONE
                startConnectionProgress()
            }
            Utility.Companion.DeviceConnectinStatus.DISCONNECTED -> {
                connectionStatus.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                stopConnectionProgress()
            }
            else -> {
                connectionStatus.visibility = View.GONE
                progressBar.visibility = View.GONE
                stopConnectionProgress()
            }
        }
    }

    private fun startConnectionProgress() {
        timer = Timer()
        var c = 1
        connectionProgress.visibility = View.VISIBLE
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    connectionProgress.progress = connectionProgress.progress + c
                    if (connectionProgress.progress == 100) {
                        c = -1
                    } else if (connectionProgress.progress == 0) {
                        c = 1
                    }
                }
            }
        }, 100, 100)
    }

    private fun stopConnectionProgress() {
        timer?.cancel()
        timer = null
        connectionProgress.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        var m = 0
        when (v?.id) {
            R.id.bulb1 -> m = 1
            R.id.bulb2 -> m = 2
            R.id.bulb3 -> m = 3
            R.id.bulb4 -> m = 4
        }
        if (v?.isActivated!!) {
            m = -m
        }
        sendSignal(m)
    }

    private fun synchDevices() {
           // updateDeviceConnectionStatus(Utility.Companion.DeviceConnectinStatus.CONNECTING)
            Intent().putExtra(DATA, SYNCH.toString()).also {
                it.action = MSG_SWITCH_ON_OFF
                sendBroadcast(it)
            }
    }

    private fun sendSignal(deviceId: Int) {
        Intent().putExtra(DATA, deviceId.toString()).also {
            it.action = MSG_SWITCH_ON_OFF
            sendBroadcast(it)
        }
    }

    override fun onAlarmEnabled(id: Int, status: Boolean) {
        alarmViewModel.updateEnableStatus(id, status)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDeleteAlarm(id: Int) {
        alarmViewModel.deleteAlarmById(id)
        alarmViewModel.getAllAlarmDetails()
        timerListRViewAdapter.notifyDataSetChanged()
    }
}