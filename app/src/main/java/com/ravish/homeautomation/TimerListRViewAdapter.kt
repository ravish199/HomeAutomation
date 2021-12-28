package com.ravish.homeautomation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.timer_list_layout.view.*

class TimerListRViewAdapter(val dataSet: List<TimerData>) :
        RecyclerView.Adapter<TimerListRViewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val v = view
        fun bind(data: TimerData, position: Int) {
            v.startTimeText.text = data.startTime
            v.startTimeSwitch.isEnabled = data.startStatus
            v.enableSwitch.isChecked = data.enable
            v.container.isActivated =  data.enable
            v.enableSwitch.setOnClickListener {
                v.container.isActivated =  v.enableSwitch.isChecked
                Utility.initReadPrefKey(Utility.devices[position])
                Utility.updateEnableStatus(v.enableSwitch.isChecked)
            }
            val deviceId = data.deviceName.split("_")[1].toInt()
            when(deviceId) {
                1 -> {
                    v.deviceImage.text = "1"
                }
                2 -> {
                    v.deviceImage.text = "2"
                }
                3 -> {
                    v.deviceImage.text = "3"
                }
                4 -> {
                    v.deviceImage.text = "4"
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
        viewHolder.bind(dataSet[position], position)
    }

    override fun getItemCount() = dataSet.size

}