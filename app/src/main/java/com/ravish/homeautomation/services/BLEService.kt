package com.ravish.homeautomation.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_TIME_TICK
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Messenger
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.ravish.homeautomation.views.MainActivity
import com.ravish.homeautomation.R
import com.ravish.homeautomation.Utility
import com.ravish.homeautomation.Utility.Companion.ACTION_BT_BUTTON_UPDATE
import com.ravish.homeautomation.Utility.Companion.ACTION_MESSAGE_READ
import com.ravish.homeautomation.Utility.Companion.DATA
import com.ravish.homeautomation.Utility.Companion.MSG_SWITCH_ON_OFF
import com.ravish.homeautomation.Utility.Companion.allAlarmList
import com.ravish.homeautomation.Utility.Companion.createNotificationChannel
import com.ravish.homeautomation.Utility.Companion.updateUI
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.lang.Math.abs
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


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
    var notificationManager: NotificationManager? = null

    enum class SOCKETSTATE {
        CONNECTED, CLOSED
    }

    var mState: SOCKETSTATE = SOCKETSTATE.CLOSED
    private lateinit var bleConnection: BLEConnection
    private var counter = 0
    private var maxCounter = 1
    private var timer: Timer? = null
    private val timerDelay = 10 * 1000L
    private var notification: Notification? = null
    private var stateIntentFilter: IntentFilter? = null
    private var incomingIntentFilter: IntentFilter? = null
    private var timeTickIntentFilter: IntentFilter? = null
    private var deviceStatusReadIntentFilter: IntentFilter? = null
    private var remoteViews: RemoteViews? = null
    private var notificationId = createID()
    private var deviceStatusMap: HashMap<Int, Int>? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        deviceStatusMap = hashMapOf( 1 to 1, 2  to 2, 3 to 3, 4 to 4)
        val stateIntentFilter = IntentFilter().also {
            it.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        val incomingIntentFilter = IntentFilter().also {
            it.addAction(MSG_SWITCH_ON_OFF)
        }
        timeTickIntentFilter = IntentFilter().also {
            it.addAction(ACTION_TIME_TICK)
        }
        val notificationClickIntentFilter = IntentFilter().also {
            it.addAction(Utility.Companion.Devices.DEVICE1.name)
            it.addAction(Utility.Companion.Devices.DEVICE2.name)
            it.addAction(Utility.Companion.Devices.DEVICE3.name)
            it.addAction(Utility.Companion.Devices.DEVICE4.name)
        }

        deviceStatusReadIntentFilter = IntentFilter().also {
            it.addAction(Utility.ACTION_MESSAGE_READ)
        }

        bleConnection = BLEConnection(applicationContext)
        registerReceiver(bleStateReciever, stateIntentFilter)
        registerReceiver(incomingReciever, incomingIntentFilter)
        registerReceiver(notificationClickReciever, notificationClickIntentFilter)
    }

    @SuppressLint("RemoteViewLayout")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
           registerReceiver(timeChangeReciever, timeTickIntentFilter)
          registerReceiver(deviceSynchReciever, deviceStatusReadIntentFilter)
           bleConnection.searchPairedDevices()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(
                    applicationContext,
                    "Home_Automation",
                    "Home Automation Service"
                )
            } else {
                "ChannelID"
            }
        val pendingIntent =
            Intent(this.baseContext, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(applicationContext, 0, notificationIntent, 0)
            }
        remoteViews = RemoteViews(packageName, R.layout.custom_notification_layout)
        remoteViews?.setOnClickPendingIntent(R.id.bulb1, PendingIntent.getBroadcast(applicationContext, 0, Intent().setAction(
            Utility.Companion.Devices.DEVICE1.name), 0))
        remoteViews?.setOnClickPendingIntent(R.id.bulb2, PendingIntent.getBroadcast(applicationContext, 0, Intent().setAction(
            Utility.Companion.Devices.DEVICE2.name), 0))
        remoteViews?.setOnClickPendingIntent(R.id.bulb3, PendingIntent.getBroadcast(applicationContext, 0, Intent().setAction(
            Utility.Companion.Devices.DEVICE3.name), 0))
        remoteViews?.setOnClickPendingIntent(R.id.bulb4, PendingIntent.getBroadcast(applicationContext, 0, Intent().setAction(
            Utility.Companion.Devices.DEVICE4.name), 0))

        notification = Notification.Builder(baseContext, channelId)
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.mipmap.app_icon)
            .setContentIntent(pendingIntent)
            .setCustomContentView(remoteViews)
            .setTicker(getText(R.string.ticker_text))
            .setChannelId(channelId)
            .build()



        startForeground(notificationId, notification)
        Log.d(TAG, "onStartCommand")
        return START_STICKY
    }

    private fun getTimerDeviceId(
        deviceName: String,
        startTime: Date,
        startStatus: Boolean
    ): Int {
        var deviceId = 0
        var check = checkTimeMatch(startTime)
        if (check) {
            deviceId = when (deviceName) {
                Utility.Companion.Devices.DEVICE1.name -> 1
                Utility.Companion.Devices.DEVICE2.name -> 2
                Utility.Companion.Devices.DEVICE3.name -> 3
                Utility.Companion.Devices.DEVICE4.name -> 4
                else -> {
                    4
                }
            }
            if (!startStatus) deviceId = -deviceId
        } else {
        }
        return deviceId
    }

    private fun checkTimeMatch(startTime: Date): Boolean {
        val date = Calendar.getInstance().time
        var result = false
        if (startTime.hours == date.hours) {
            if (startTime.minutes == date.minutes) {
                result = true
            }
        } else {
            result = false
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

    private val timeChangeReciever = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action.equals(ACTION_TIME_TICK)) {
                allAlarmList?.forEach {
                    CoroutineScope(Dispatchers.IO).launch {
                        val deviceId = getTimerDeviceId(it.deviceName, it.startTime, it.startStatus)
                        if (deviceId != 0) {
                            bleConnection.writeDataToDevice((-deviceId).toString().toByteArray()!!)
                            Log.d(TAG, "Timer Signal sent")
                        } else {
                            Log.d(TAG, "Time Not matching")
                        }
                    }
                }
            }
        }
    }

    private val notificationClickReciever = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
           val deviceId = when(intent?.action) {
                Utility.Companion.Devices.DEVICE1.name -> {
                    deviceStatusMap?.get(1)
                }
                Utility.Companion.Devices.DEVICE2.name -> {
                    deviceStatusMap?.get(2)
                }
                Utility.Companion.Devices.DEVICE3.name -> {
                    deviceStatusMap?.get(3)
                }
                Utility.Companion.Devices.DEVICE4.name ->{
                    deviceStatusMap?.get(4)
                }
               else -> deviceStatusMap?.get(4)
            }
            bleConnection.writeDataToDevice(deviceId.toString().toByteArray()!!)
        }
    }

    private val deviceSynchReciever = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_MESSAGE_READ) {
                val id = intent.getStringExtra(DATA)?.toInt()!!
                val isActivate = id < 5
                val deviceId = if (isActivate) id else 256 - id
                when (deviceId) {
                    1 -> remoteViews?.setImageViewResource(
                        R.id.bulb1,
                        if (!isActivate) R.drawable.wifi_enabled else R.drawable.wifi_disabled
                    )
                    2 -> remoteViews?.setImageViewResource(
                        R.id.bulb2,
                        if (!isActivate) R.drawable.extension_enabled else R.drawable.extension_disabled
                    )
                    3 -> remoteViews?.setImageViewResource(
                        R.id.bulb3,
                        if (!isActivate) R.drawable.speaker_enabled else R.drawable.speaker_disabled
                    )
                    4 -> remoteViews?.setImageViewResource(
                        R.id.bulb4,
                        if (!isActivate) R.drawable.bulb_enabled else R.drawable.bulb_disabled
                    )
                }
                if(deviceId in 1..4) {
                    deviceStatusMap?.put(deviceId, if(isActivate) -deviceId else deviceId)
                }
                notificationManager?.notify(notificationId,notification)
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
            unregisterReceiver(timeChangeReciever)
            unregisterReceiver(notificationClickReciever)
        } catch (e: Exception) {
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

    private fun createID(): Int {
        val now = Date()
        return SimpleDateFormat("ddHHmmss", Locale.US).format(now).toInt()
    }
}