package com.ravish.homeautomation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.text.format.Time
import androidx.annotation.RequiresApi


class Utility {

    companion object {
      const val ACTION_BT_BUTTON_UPDATE = "ACTION_BT_BUTTON_UPDATE"
        const val ACTION_CONNECTED_BTN_UPDATE = "ACTION_CONNECTED_BTN_UPDATE"
        const val DATA = "DATA"
        const val ENABLE_BLE = "ENABLE_BLE"
        const val BLE_CONNECTED = "BLE_CONNECTED"
        const val UI_UPDATE = "UI_UPDATE"
        const val ACTION_MESSAGE_READ = "MESSAGE_READ"
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

       var devices = arrayOf("Device_1", "Device_2", "Device_3", "Device_4")
       var maxDevice = devices.size

      fun initReadPrefKey(deviceId: String) {
        START_TIME_KEY = START_TIME + deviceId
        END_TIME_KEY = END_TIME + deviceId
        START_STATUS_KEY = START_STATUS + deviceId
        END_STATUS_KEY = END_STATUS + deviceId
        ENABLE_KEY = DEVICE_ENABLE + deviceId
      }

      fun savePreferences(key: String, value: String) {
        sharedPreferences?.edit()?.also {
          it.putString(key, value)
          it.apply()
        }
      }

      fun getPreference(key: String): String {
        return sharedPreferences?.getString(key, "")!!
      }

      fun deletePreference(key: String) {
        sharedPreferences?.edit()?.also {
          it.remove(key)
          it.apply()
        }
      }

      fun fetchTimeData(): ArrayList<TimerData> {
        val list = ArrayList<TimerData>()
        for(i in 0 until maxDevice) {
          initReadPrefKey(devices[i])
          if(getPreference(START_TIME_KEY).isNotEmpty()) {
            val timerData = TimerData(
              devices[i],
              getPreference(START_TIME_KEY),
              getPreference(END_TIME_KEY),
              getPreference(START_STATUS_KEY).toBoolean(),
              getPreference(END_STATUS_KEY).toBoolean(),
              getPreference(ENABLE_KEY).toBoolean()
            )
            list.add(timerData)
          }
        }
        return list
      }



      @RequiresApi(Build.VERSION_CODES.O)
      fun createNotificationChannel(context: Context, channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
          channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
          context.sendBroadcast(it)
        }
      }
    }

}