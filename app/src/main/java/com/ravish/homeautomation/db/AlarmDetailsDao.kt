package com.ravish.homeautomation.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ravish.homeautomation.model.AlarmDetails

@Dao
interface AlarmDetailsDao {
    @Query("Select * from AlarmDetails where id = :id")
    fun getAlarmDetailsById(id: Int): AlarmDetails

    @Query("Select * from AlarmDetails")
    fun getAllAlarmDetails(): List<AlarmDetails>


    @Query("Select * from AlarmDetails where deviceName = :deviceName")
    fun getAlarmDetailsByDeviceName(deviceName: String): List<AlarmDetails>

    @Query("delete from AlarmDetails where id = :id")
    fun deleteAlarmById(id: Int)

    @Insert
    fun insert(alerDetails: AlarmDetails)

    @Query("Update AlarmDetails set enable = :enable where id = :id")
    fun updateEnableStatus(id: Int, enable: Boolean)
}
