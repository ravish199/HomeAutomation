package com.ravish.homeautomation.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "AlarmDetails")
class AlarmDetails(
    @PrimaryKey(autoGenerate = true) val id:Int,
    val deviceName:String,
    val startTime:java.util.Date,
    @TypeConverters(DateConverter::class)
    val startStatus:Boolean,
    val enable:Boolean
)