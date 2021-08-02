package com.ravish.homeautomation

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.ravish.homeautomation.Utility.Companion.ACTION_BT_BUTTON_UPDATE
import com.ravish.homeautomation.Utility.Companion.ACTION_CONNECTED_BTN_UPDATE
import com.ravish.homeautomation.Utility.Companion.ACTION_MESSAGE_READ
import com.ravish.homeautomation.Utility.Companion.DATA
import com.ravish.homeautomation.Utility.Companion.MSG_SWITCH_ON_OFF
import com.ravish.homeautomation.Utility.Companion.SYNCH
import com.ravish.homeautomation.Utility.Companion.TIMER_PREF
import com.ravish.homeautomation.Utility.Companion.sharedPreferences
import com.ravish.homeautomation.Utility.Companion.updateUI
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlin.math.abs


class MainActivity : AppCompatActivity(), View.OnClickListener {

    var bluetoothAdapter: BluetoothAdapter? = null
    private val enableBtRequest = 201
    private val TAG = "MainActivity"
    private val TIME_REQUEST_CODE = 111
    private lateinit var timerListRViewAdapter: TimerListRViewAdapter
    private lateinit var timerDataList: MutableList<TimerData>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences(TIMER_PREF, Context.MODE_PRIVATE)
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

        timerDataList = ArrayList()
        timerRView.layoutManager = LinearLayoutManager(baseContext)
        timerListRViewAdapter = TimerListRViewAdapter(timerDataList)
        timerRView.adapter = timerListRViewAdapter
        loadData()

        if (!isServiceRunning(BLEService::class.java)) {
            Intent(this, BLEService::class.java).also { intent ->
                startForegroundService(intent)
            }
        } else {
             updateUI(true, this, ACTION_CONNECTED_BTN_UPDATE)
            synchDevices()
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

    private fun loadData() {
        timerDataList.clear()
        timerDataList.addAll(Utility.fetchTimeData())
        timerListRViewAdapter.notifyDataSetChanged()
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
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TIME_REQUEST_CODE) {
            try {
                loadData()
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
}