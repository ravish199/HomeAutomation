package com.ravish.homeautomation

interface AlarmListActionListener {
        fun onAlarmEnabled(id:Int, status: Boolean)
       fun onDeleteAlarm(id:Int)
    }