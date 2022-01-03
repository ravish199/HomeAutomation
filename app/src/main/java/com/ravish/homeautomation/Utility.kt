package com.ravish.homeautomation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.text.format.Time
import androidx.annotation.RequiresApi
import com.ravish.homeautomation.db.AlarmDetailsDao
import com.ravish.homeautomation.model.AlarmDetails
import android.media.AudioAttributes


class Utility {

    companion object {
        const val ACTION_BT_BUTTON_UPDATE = "ACTION_BT_BUTTON_UPDATE"
        const val ACTION_CONNECTED_BTN_UPDATE = "ACTION_CONNECTED_BTN_UPDATE"
        const val DATA = "DATA"
        const val ENABLE_BLE = "ENABLE_BLE"
        const val BLE_CONNECTED = "BLE_CONNECTED"
        const val UI_UPDATE = "UI_UPDATE"
        const val ACTION_MESSAGE_READ = "MESSAGE_READ"
        const val ACTION_SOCKET_DISCONNECTED = "SOCKET_DISCONNECTED"
        const val ACTION_DEVICE_NOT_CONNECTED = "DEVICE_NOT_CONNECTED"
        const val ACTION_DEVICE_CONNECTED = "DEVICE_CONNECTED"
        const val ACTION_MESSAGE_WRITE = "MESSAGE_WRITE"
        const val SYNCH = 99
        const val ACTION_MESSAGE = "ACTION_MESSAGE"
        const val MSG_SWITCH_ON_OFF = "MSG_SWITCH_ON_OFF"

        var sharedPreferences: SharedPreferences? = null
        var TIMER_PREF = "TIMER_PREF"

        const val DEVICE_ID = "DEVICE_ID"
        const val START_TIME = "START_TIME"
        const val END_TIME = "END_TIME"
        const val START_STATUS = "START_STATUS"
        const val END_STATUS = "END_STATUS"
        const val REPEAT_TIME = "REPEAT_TIME"
        const val DEVICE_NAME = "DEVICE_NAME"
        const val DEVICE_ENABLE = "DEVICE_ENABLE"

        var START_TIME_KEY = ""
        var END_TIME_KEY = ""
        var START_STATUS_KEY = ""
        var END_STATUS_KEY = ""
        var ENABLE_KEY = ""

        enum class Devices {
            DEVICE1, DEVICE2, DEVICE3, DEVICE4
        }

        var devices = arrayOf("Device_1", "Device_2", "Device_3", "Device_4")
        var maxDevice = devices.size
/*
      fun initReadPrefKey(deviceId: String) {
        START_TIME_KEY = START_TIME + deviceId
        START_STATUS_KEY = START_STATUS + deviceId
        ENABLE_KEY = DEVICE_ENABLE + deviceId
      }

      fun savePreferences(key: String, value: String) {
        sharedPreferences?.edit()?.also {
          it.putString(key, value)
          it.apply()
        }
      }

      fun saveData(startTime: TimerTime, startStatus: Boolean, enable: Boolean) {
        savePreferences(START_TIME_KEY, "${startTime.hour} : ${startTime.minute}")
        savePreferences(START_STATUS_KEY, startStatus.toString())
        savePreferences(ENABLE_KEY, enable.toString())
      }*/

        /*     fun updateEnableStatus(enable: Boolean) {
               savePreferences(ENABLE_KEY, enable.toString())
             }*/

        enum class DeviceConnectinStatus {
            CONNECTING, CONNECTED, DISCONNECTED
        }


        @RequiresApi(Build.VERSION_CODES.O)
        fun createNotificationChannel(
            context: Context,
            channelId: String,
            channelName: String
        ): String {
            val chan = NotificationChannel(
                channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val soundUri =
                Uri.parse(context.assets.open("notification_tone.mp3").bufferedReader().readText())

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            chan.setSound(soundUri, audioAttributes);
            val service =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
            return channelId
        }


        fun updateUI(isEnable: Boolean, context: Context, action: String) {
            Intent().putExtra(Utility.DATA, isEnable).also {
                it.action = action
                context.sendBroadcast(it)
            }
        }

        fun updateUI(data: String, context: Context, action: String) {
            Intent().putExtra(Utility.DATA, data).also {
                it.action = action
                context.sendOrderedBroadcast(it, null)
            }
        }

        var allAlarmList: List<AlarmDetails>? = null
    }


}