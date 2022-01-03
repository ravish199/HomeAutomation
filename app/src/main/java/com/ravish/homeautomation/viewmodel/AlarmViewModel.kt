package com.ravish.homeautomation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ravish.homeautomation.db.AlarmDetailsDao
import com.ravish.homeautomation.db.AppDb
import com.ravish.homeautomation.model.AlarmDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application):AndroidViewModel(application) {

    val appContext = application
    var allAlarmDetailsLiveData: MutableLiveData<List<AlarmDetails>> = MutableLiveData()
    var alarmDetailsLiveData: MutableLiveData<AlarmDetails> = MutableLiveData()
    var alarmDetailsByDeviceLiveData: MutableLiveData<List<AlarmDetails>> = MutableLiveData()
    var alarmDetailsDao: AlarmDetailsDao? = null

    init {
       viewModelScope.launch {
           alarmDetailsDao = AppDb.getDatabase(application).alarmDetailsDao()
       }
    }

    fun getAllAlarmDetails() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                alarmDetailsDao?.getAllAlarmDetails().let {
                    allAlarmDetailsLiveData.postValue(it)
                }
            } catch (e:Exception) {
            }
        }
    }

    fun insertData(alarmDetails: AlarmDetails) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                alarmDetailsDao?.insert(alarmDetails)
            } catch (e:Exception) {
            }
        }
    }

    fun updateEnableStatus(id: Int, enable: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                alarmDetailsDao?.updateEnableStatus(id, enable)
            } catch (e:Exception) {
            }
        }
    }

    fun getAlarmDetailsById(id: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                alarmDetailsDao?.getAlarmDetailsById(id).let {
                    alarmDetailsLiveData.postValue(it)
                }
            } catch (e:Exception) {
            }
        }
    }

    fun getAlarmDetailsByDeviceName(deviceName: String) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                alarmDetailsDao?.getAlarmDetailsByDeviceName(deviceName).let {
                    alarmDetailsByDeviceLiveData.postValue(it)
                }
            } catch (e:Exception) {
            }
        }
    }

    fun deleteAlarmById(id: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                alarmDetailsDao?.deleteAlarmById(id)
            } catch (e:Exception) {
            }
        }
    }
}