package com.ravish.homeautomation

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
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
import java.util.*

class AddTimerActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {


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

        device1.setOnCheckedChangeListener(this)
        device2.setOnCheckedChangeListener(this)
        device3.setOnCheckedChangeListener(this)
        device4.setOnCheckedChangeListener(this)

        Utility.devices.forEach {
            deviceList.add(it)
        }

        startTimeBtn.setOnClickListener {
            selectTime(TimeSelection.START)
        }

        saveBtn.setOnClickListener {
            Utility.saveData(startTime, startStatus, enable)
            enableInputUi(false)
        }

        editBtn.setOnClickListener {
            enableInputUi(true)
        }

        deleteBtn.setOnClickListener {
            deletePreference(START_TIME + deviceName)
            deletePreference(START_STATUS + deviceName)
            onBackPressed()
        }

        startStatusSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            startStatus = isChecked
        }
        try {
            loadData()
        } catch (e: Exception) {
        }

    }

    private fun enableInputUi(isEnable: Boolean) {
        startTimeBtn.isEnabled = isEnable
        startStatusSwitch.isEnabled = isEnable
        saveBtn.visibility = if(isEnable) View.VISIBLE else View.GONE
        editBtn.visibility =  if(isEnable) View.GONE else View.VISIBLE
        deleteBtn.visibility = if(isEnable) View.GONE else View.VISIBLE
    }


    private fun loadData() {
        getPreference(START_TIME_KEY).split(":").also {
            startTime = TimerTime(it[0].toInt(), it[1].toInt())
            startTimeBtn.text = startTime.toString()
        }

        getPreference(START_STATUS_KEY).also {
            startStatus = it.toBoolean()
            startStatusSwitch.isChecked = startStatus
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

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(isChecked) {
            var position = 0
            when (buttonView?.id!!) {
                R.id.device1 -> position = 0
                R.id.device2 -> position = 1
                R.id.device3 -> position = 2
                R.id.device4 -> position = 3
            }
            deviceName = Utility.devices[position]
            initReadPrefKey(deviceName)
            selectedDevicePosition = position
        }
    }
}