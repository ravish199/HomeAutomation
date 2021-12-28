package com.ravish.homeautomation

import android.app.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.*
import android.text.format.Time
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.ravish.homeautomation.Utility.Companion.ACTION_BT_BUTTON_UPDATE
import com.ravish.homeautomation.Utility.Companion.DATA
import com.ravish.homeautomation.Utility.Companion.MSG_SWITCH_ON_OFF
import com.ravish.homeautomation.Utility.Companion.createNotificationChannel
import com.ravish.homeautomation.Utility.Companion.fetchTimeData
import com.ravish.homeautomation.Utility.Companion.updateUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.lang.Exception
import java.util.*

class BLEService : Service() {
    private lateinit var massenger: Messenger
    var bluetoothAdapter: BluetoothAdapter? = null
    var pairedDevicesList: MutableSet<BluetoothDevice>? = null
    var devicesList: MutableSet<BluetoothDevice>? = null
    private var deviceConnected = false
    private val TAG = "BLEService"

    private var enableBtRequest = 99
    var uuid = ""
    private var selectedItem: View? = null
    lateinit var bleconnectionHandler: Handler
    var mmOutStream: OutputStream? = null
    private var message = ""
    private val CHANNEL_ID = "101"

    enum class SOCKETSTATE {
        CONNECTED, CLOSED
    }

    var mState: SOCKETSTATE = SOCKETSTATE.CLOSED
    private lateinit var bleConnection: BLEConnection
    private var counter = 0
    private var maxCounter = 1
    private var timer: Timer? = null
    private val timerDelay = 10 * 1000L

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        bleConnection = BLEConnection(applicationContext)
        val stateIntentFilter = IntentFilter().also {
            it.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }

        val incomingIntentFilter = IntentFilter().also {
            it.addAction(MSG_SWITCH_ON_OFF)
        }
        registerReceiver(bleStateReciever, stateIntentFilter)
        registerReceiver(incomingReciever, incomingIntentFilter)

        bleConnection.searchPairedDevices()
        val pendingIntent: PendingIntent =
            Intent(applicationContext, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(applicationContext, 0, notificationIntent, 0)
            }

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(applicationContext, "Home_Automation", "Home Automation Service")
            } else {
                "ChannelID"
            }
        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.drawable.automation)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.ticker_text))
            .build()

        startForeground(199, notification)

        Log.d(TAG, "onStartCommand")
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val timerList = fetchTimeData()
                timerList.forEach {
                    CoroutineScope(Dispatchers.IO).launch {
                        val deviceId = getTimerDeviceId(it)
                        if (deviceId != 0) {
                            bleConnection.writeDataToDevice(deviceId.toString().toByteArray()!!)
                            Log.d(TAG, "Timer Signal sent")
                        } else {
                            Log.d(TAG, "Time Not matching")
                        }
                        delay(1000)
                    }
                }
                Log.d(TAG, "Counter In")
            }

        }, timerDelay, timerDelay)
        return START_STICKY
    }




    private fun getTimerDeviceId(timerData: TimerData):Int {
        var deviceId = 0
        var check = checkTimeMatch(timerData.startTime.split(":"))
        if(check) {
            deviceId = timerData.deviceName.split("_")[1].toInt()
            if(!timerData.startStatus) deviceId = -deviceId
        } else {
        }
        return deviceId
    }

    private fun checkTimeMatch(t : List<String>): Boolean {
        val date = Calendar.getInstance().time
        var result = false
        if(t[0].trim().toInt() == date.hours) {
            if(t[1].trim().toInt()== date.minutes) {
              result = true
            }
        } else {
            result  = false
        }
        return result
    }

    private val bleStateReciever = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                val state =
                    intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_ON -> {
                        Log.d(TAG, "Bluethooth State ON")
                        updateUI(true, context!!, ACTION_BT_BUTTON_UPDATE)
                        bleConnection.searchPairedDevices()
                    }

                    BluetoothAdapter.STATE_OFF -> {
                        Log.d(TAG, "Bluethooth State OFF")
                        updateUI(false, context!!, ACTION_BT_BUTTON_UPDATE)
                    }

                }
            }
        }
    }

    private val incomingReciever = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action?.equals(MSG_SWITCH_ON_OFF)!!) {
                bleConnection.writeDataToDevice(intent.getStringExtra(DATA)?.toByteArray()!!)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(bleStateReciever)
            unregisterReceiver(incomingReciever)
        }catch (e: Exception) {
        }
        timer?.cancel()
        timer = null
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
    }

    private fun refreshTimeData() {

    }

}