package com.ravish.homeautomation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.timer_list_layout.view.*

class TimerListRViewAdapter(private val dataSet: List<TimerData>) :
        RecyclerView.Adapter<TimerListRViewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val v = view
        fun bind(data: TimerData) {
            v.startTimeText.text = data.startTime
            v.endTimeText.text = data.endTime
            v.startTimeSwitch.isEnabled = data.startStatus
            v.endTimeSwitch.isEnabled = data.endStatus
            val deviceId = data.deviceName.split("_")[1].toInt()
            when(deviceId) {
                1 -> {
                    v.deviceImage.setImageResource(R.drawable.ic_one_enable)
                }
                2 -> {
                    v.deviceImage.setImageResource(R.drawable.ic_two_enable)
                }
                3 -> {
                    v.deviceImage.setImageResource(R.drawable.ic_three_enable)
                }
                4 -> {
                    v.deviceImage.setImageResource(R.drawable.ic_four_enable)
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