package com.ravish.homeautomation.views

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.ravish.homeautomation.R
import com.ravish.homeautomation.model.TimerTime
import com.ravish.homeautomation.Utility
import com.ravish.homeautomation.model.AlarmDetails
import com.ravish.homeautomation.viewmodel.AlarmViewModel
import kotlinx.android.synthetic.main.activity_add_timer.*
import kotlinx.android.synthetic.main.toolbar_layout2.*
import java.util.*

class AddTimerActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {


    private val calendar = Calendar.getInstance()
    private val hour = calendar.get(Calendar.HOUR_OF_DAY)
    private val minute = calendar.get(Calendar.MINUTE)
    var startTime:Date? = null
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

    lateinit var alarmViewModel: AlarmViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_timer)
        alarmViewModel = ViewModelProvider(this).get(AlarmViewModel::class.java)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Set Timer"

        device1.setOnCheckedChangeListener(this)
        device2.setOnCheckedChangeListener(this)
        device3.setOnCheckedChangeListener(this)
        device4.setOnCheckedChangeListener(this)

        Utility.devices.forEach {
            deviceList.add(it)
        }
        backBtn.setOnClickListener {
            onBackPressed()
        }

        startTimeBtn.setOnClickListener {
            selectTime(TimeSelection.START)
        }

        saveBtn.setOnClickListener {
            if(deviceName.isNotEmpty() && startTime != null) {
                alarmViewModel.insertData(
                    AlarmDetails(
                        0,
                        deviceName,
                        startTime!!,
                        startStatus,
                        enable
                    )
                )
                onBackPressed()
            } else {
                Snackbar.make(container, "Please select a Device and Time", Snackbar.LENGTH_SHORT)
                    .setTextColor(getColor(R.color.white))
                    .show()
            }
          //  enableInputUi(false)
        }

        editBtn.setOnClickListener {
            enableInputUi(true)
        }

        deleteBtn.setOnClickListener {
            onBackPressed()
        }

        startStatusSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            startStatus = isChecked
        }
        try {
        } catch (e: Exception) {
        }

    }

    private fun enableInputUi(isEnable: Boolean) {
        startTimeBtn.isEnabled = isEnable
        startStatusSwitch.isEnabled = isEnable
        saveBtn.visibility = if (isEnable) View.VISIBLE else View.GONE
        editBtn.visibility = if (isEnable) View.GONE else View.VISIBLE
        deleteBtn.visibility = if (isEnable) View.GONE else View.VISIBLE
    }




    @SuppressLint("SetTextI18n")
    private fun selectTime(timeselection: TimeSelection) {
        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
          startTime = Date()
            when (timeselection) {
                TimeSelection.START -> {
                    startTime?.hours = hourOfDay
                    startTime?.minutes = minute
                    startLable.text = "${startTime?.hours} : ${startTime?.minutes}"
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
        if (isChecked) {
            deviceName = when (buttonView?.id!!) {
                R.id.device1 -> Utility.Companion.Devices.DEVICE1.name
                R.id.device2 -> Utility.Companion.Devices.DEVICE2.name
                R.id.device3 -> Utility.Companion.Devices.DEVICE3.name
                R.id.device4 -> Utility.Companion.Devices.DEVICE4.name
                else -> {
                  Utility.Companion.Devices.DEVICE4.toString()
                }
            }
        }
    }
}