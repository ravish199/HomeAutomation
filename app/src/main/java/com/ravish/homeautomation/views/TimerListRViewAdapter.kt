package com.ravish.homeautomation.views

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ravish.homeautomation.AlarmListActionListener
import com.ravish.homeautomation.R
import com.ravish.homeautomation.Utility
import com.ravish.homeautomation.model.AlarmDetails
import kotlinx.android.synthetic.main.timer_list_layout.view.*
import android.content.DialogInterface
import android.view.ContextThemeWrapper


class TimerListRViewAdapter(
    val dataSet: List<AlarmDetails>,
    val alarmListActionListener: AlarmListActionListener
) :
    RecyclerView.Adapter<TimerListRViewAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val v = view
        @SuppressLint("SetTextI18n")
        fun bind(data: AlarmDetails) {
            v.startTimeText.text = "${data.startTime.hours}:${data.startTime.minutes}"
            v.statusImageView.isEnabled = data.startStatus
            v.enableSwitch.isChecked = data.enable
            v.container.isActivated = data.enable
            v.enableSwitch.setOnClickListener {
                v.container.isActivated = v.enableSwitch.isChecked
                alarmListActionListener.onAlarmEnabled(data.id, v.enableSwitch.isChecked)
            }
            v.container.setOnLongClickListener {
                AlertDialog.Builder(ContextThemeWrapper(it.context, R.style.AlertDialogStyle))
                    .setMessage(it.context.getString(R.string.do_you_want_to_delete))
                    .setCancelable(true)
                    .setPositiveButton(it.context.getString(R.string.yes)
                    ) { _, _ ->
                        alarmListActionListener.onDeleteAlarm(data.id)
                    }
                    .setNegativeButton(it.context.getString(R.string.no)){ view, _ ->
                        view.dismiss()
                    }
                    .show()
                return@setOnLongClickListener true
            }
            when (data.deviceName) {
                Utility.Companion.Devices.DEVICE1.name -> {
                    v.deviceImage.setImageResource(R.drawable.wifi_enabled)
                }
                Utility.Companion.Devices.DEVICE2.name -> {
                    v.deviceImage.setImageResource(R.drawable.extension_enabled)
                }
                Utility.Companion.Devices.DEVICE3.name -> {
                    v.deviceImage.setImageResource(R.drawable.speaker_enabled)
                }
                Utility.Companion.Devices.DEVICE4.name -> {
                    v.deviceImage.setImageResource(R.drawable.bulb_enabled)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.timer_list_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size
}