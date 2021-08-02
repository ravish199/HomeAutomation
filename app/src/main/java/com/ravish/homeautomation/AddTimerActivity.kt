package com.ravish.homeautomation

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.ravish.homeautomation.Utility.Companion.DEVICE_NAME
import com.ravish.homeautomation.Utility.Companion.ENABLE_KEY
import com.ravish.homeautomation.Utility.Companion.END_STATUS
import com.ravish.homeautomation.Utility.Companion.END_STATUS_KEY
import com.ravish.homeautomation.Utility.Companion.END_TIME
import com.ravish.homeautomation.Utility.Companion.END_TIME_KEY
import com.ravish.homeautomation.Utility.Companion.START_STATUS
import com.ravish.homeautomation.Utility.Companion.START_STATUS_KEY
import com.ravish.homeautomation.Utility.Companion.START_TIME
import com.ravish.homeautomation.Utility.Companion.START_TIME_KEY
import com.ravish.homeautomation.Utility.Companion.deletePreference
import com.ravish.homeautomation.Utility.Companion.getPreference
import com.ravish.homeautomation.Utility.Companion.initReadPrefKey
import com.ravish.homeautomation.Utility.Companion.savePreferences
import kotlinx.android.synthetic.main.activity_add_timer.*
import kotlinx.android.synthetic.main.activity_add_timer.deviceNameText
import kotlinx.android.synthetic.main.timer_list_layout.*
import java.lang.Exception
import java.util.*

class AddTimerActivity : AppCompatActivity() {

    private val calendar = Calendar.getInstance()
    private val hour = calendar.get(Calendar.HOUR_OF_DAY)
    private val minute = calendar.get(Calendar.MINUTE)
    var startTime = TimerTime(0, 0)
    var endTime = TimerTime(0, 0)
    var startStatus = false
    var endStatus = false
    var deviceName = ""
    var enable = true
    private val deviceList = ArrayList<String>()
    private var selectedDevicePosition = 0
    private val TIME_REQUEST_CODE = 111

    enum class TimeSelection {
        START, END
    }


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_timer)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Set Timer"

        Utility.devices.forEach {
            deviceList.add(it)
        }
        spinner.adapter = ArrayAdapter(this, R.layout.device_spinner_layout, deviceList)
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                deviceName = Utility.devices[position]
                deviceNameText.text = deviceName
                initReadPrefKey(deviceName)
                selectedDevicePosition = position
            }
        }

        startTimeBtn.setOnClickListener {
            selectTime(TimeSelection.START)
        }

        endTimeBtn.setOnClickListener {
            selectTime(TimeSelection.END)
        }

        saveBtn.setOnClickListener {
            savePreferences(START_TIME_KEY, "${startTime.hour} : ${startTime.minute}")
            savePreferences(END_TIME_KEY, "${endTime.hour} : ${endTime.minute}")
            savePreferences(START_STATUS_KEY, startStatus.toString())
            savePreferences(END_STATUS_KEY, endStatus.toString())
            savePreferences(ENABLE_KEY, enable.toString())
            enableInputUi(false)
        }

        editBtn.setOnClickListener {
            enableInputUi(true)
        }

        deleteBtn.setOnClickListener {
            deletePreference(START_TIME + deviceName)
            deletePreference(END_TIME + deviceName)
            deletePreference(START_STATUS + deviceName)
            deletePreference(END_STATUS + deviceName)
            onBackPressed()
        }

        startStatusSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            startStatus = isChecked
        }

        endStatusSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            endStatus = isChecked
        }


        try {
            loadData()
        } catch (e: Exception) {
        }

    }

    private fun enableInputUi(isEnable: Boolean) {
        startTimeBtn.isEnabled = isEnable
        endTimeBtn.isEnabled = isEnable
        startStatusSwitch.isEnabled = isEnable
        endStatusSwitch.isEnabled = isEnable
        saveBtn.visibility = if(isEnable) View.VISIBLE else View.GONE
        editBtn.visibility =  if(isEnable) View.GONE else View.VISIBLE
        deleteBtn.visibility = if(isEnable) View.GONE else View.VISIBLE
    }


    private fun loadData() {
        getPreference(START_TIME_KEY).split(":").also {
            startTime = TimerTime(it[0].toInt(), it[1].toInt())
            startTimeBtn.text = startTime.toString()
        }
        getPreference(END_TIME_KEY).split(":").also {
            endTime = TimerTime(it[0].toInt(), it[1].toInt())
            endTimeBtn.text = endTime.toString()
        }
        getPreference(START_STATUS_KEY).also {
            startStatus = it.toBoolean()
            startStatusSwitch.isChecked = startStatus
        }
        getPreference(END_STATUS_KEY).also {
            endStatus = it.toBoolean()
            endStatusSwitch.isChecked = endStatus
        }
    }

    @SuppressLint("SetTextI18n")
    private fun selectTime(timeselection: TimeSelection) {
        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            val t = TimerTime(hourOfDay, minute)
            when (timeselection) {
                TimeSelection.START -> {
                    startTime = t
                    startTimeBtn.text = "${startTime.hour} : ${startTime.minute}"
                }
                TimeSelection.END -> {
                    endTime = t
                    endTimeBtn.text = "${endTime.hour} : ${endTime.minute}"
                }
            }
        }, hour, minute, true).also {
            it.setTitle("Select Time")
            it.show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}