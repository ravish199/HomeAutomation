package com.ravish.homeautomation

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.ArraySet
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ravish.homeautomation.Utility.Companion.ACTION_CONNECTED_BTN_UPDATE
import com.ravish.homeautomation.Utility.Companion.ACTION_DEVICE_NOT_CONNECTED
import com.ravish.homeautomation.Utility.Companion.ACTION_MESSAGE
import com.ravish.homeautomation.Utility.Companion.ACTION_MESSAGE_READ
import com.ravish.homeautomation.Utility.Companion.ACTION_MESSAGE_WRITE
import com.ravish.homeautomation.Utility.Companion.ACTION_SOCKET_DISCONNECTED
import com.ravish.homeautomation.Utility.Companion.SYNCH
import com.ravish.homeautomation.Utility.Companion.updateUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class BLEConnection(val context: Context) {
    var bluetoothAdapter: BluetoothAdapter? = null
    var pairedDevicesList: MutableSet<BluetoothDevice>? = null
    private val TAG = "MY_APP_DEBUG_TAG"
    var uuid = ""
    var mmOutStream: OutputStream? = null
    var activeSocket: BluetoothSocket? = null

    init {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    fun searchPairedDevices() {
        pairedDevicesList = bluetoothAdapter?.bondedDevices
        val deviceslist = ArrayList<String>()
        var device: BluetoothDevice? = null
        pairedDevicesList?.forEach {
            deviceslist.add("${it.address}  ${it.name}   ${it.type}")
            if (it.name == "ESP32test") {
                device = it
                it.uuids?.let { a ->
                    if (a.isNotEmpty())
                        uuid = a[0].toString()
                }
            }
        }
        device?.let {
            val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
                it.createRfcommSocketToServiceRecord(UUID.fromString(uuid))
            }
            CoroutineScope(Dispatchers.Default).launch {
                startSocket(mmSocket)
            }
        }

    }

    private fun startSocket(socket: BluetoothSocket?) {
        bluetoothAdapter?.cancelDiscovery()
        socket?.use {
            Log.d("HomeAutomation", "Socket connecting...")
            try {
                it.connect()
                updateUI(true, context, ACTION_CONNECTED_BTN_UPDATE)
                Log.d("HomeAutomation", "Socket Connected")
            } catch (e: Exception) {
                Log.d("HomeAutomation", "Socket Connection failed")
            }
            manageMyConnectedSocket(it)
        }
    }

    private fun manageMyConnectedSocket(socket: BluetoothSocket) {
        val mmInStream: InputStream = socket.inputStream
        mmOutStream = socket.outputStream
        var numBytes: Int
        if (socket.isConnected) {
            activeSocket = socket
            writeDataToDevice(SYNCH.toString().toByteArray())
            while (true) {
                numBytes = try {
                    mmInStream.read()
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }
                Log.d(TAG, "Read message: $numBytes")
                updateUI(numBytes.toString(), context, ACTION_MESSAGE_READ)
            }

        } else {
            Log.d(TAG, "Socket Closed")
            updateUI(false, context, ACTION_DEVICE_NOT_CONNECTED)
        }
    }

     fun writeDataToDevice(bytes: ByteArray) {

        try {
            if(activeSocket != null) {
                activeSocket?.let {
                    if (it.isConnected) {
                        mmOutStream?.write(bytes)
                    } else {
                        Log.e(TAG, "Socket Disconnected")
                        updateUI(false, context, ACTION_SOCKET_DISCONNECTED)
                    }
                }
            } else {
                updateUI(false, context, ACTION_DEVICE_NOT_CONNECTED)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error occurred when sending data", e)
            val msg = "Couldn't send data to the other device"
            updateUI(msg, context, ACTION_MESSAGE)
            return
        }
    }

    fun cancelSocket(socket: BluetoothSocket?) {
        try {
            socket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the client socket", e)
        }
    }

}