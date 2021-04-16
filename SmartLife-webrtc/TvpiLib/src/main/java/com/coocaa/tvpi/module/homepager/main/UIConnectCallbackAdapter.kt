package com.coocaa.tvpi.module.homepager.main

import android.util.Log
import com.coocaa.smartscreen.connect.SSConnectManager
import com.coocaa.smartscreen.connect.callback.ConnectCallbackImpl
import com.coocaa.smartscreen.data.channel.events.ConnectEvent
import com.coocaa.smartscreen.data.channel.events.UnbindEvent
import com.coocaa.tvpi.module.log.LogParams
import com.coocaa.tvpi.module.log.LogSubmit
import swaiotos.channel.iot.ss.device.Device
import swaiotos.channel.iot.ss.device.DeviceInfo
import swaiotos.channel.iot.ss.session.Session
import java.text.DecimalFormat

/**
 * 目的：UI更新并不想关心这么多回调逻辑及其他的统计逻辑同时方便多个界面复用所以在这做个适配
 */
class UIConnectCallbackAdapter(
        private var uiConnectCallback: UIConnectCallback
) : ConnectCallbackImpl() {
    companion object {
        val TAG = UIConnectCallbackAdapter::class.simpleName
    }

    private var connectTime: Long = 0

    override fun onConnecting() {
        Log.d(TAG, "onConnecting")
//        uiConnectCallback.onConnectStatusChange(false, UIConnectStatus.CONNECTING)
    }

    override fun onSuccess(connectEvent: ConnectEvent?) {
        Log.d(TAG, "onSuccess: $connectEvent")
        handleConnectEvent(true, connectEvent)
    }

    override fun onFailure(connectEvent: ConnectEvent?) {
        Log.d(TAG, "onFailure: $connectEvent")
//        ToastUtils.getInstance().showGlobalShort(connectEvent?.msg)
//        uiConnectCallback.onConnectStatusChange(status = UIConnectStatus.CONNECT_ERROR)
        updateDeviceInfoUI();
    }

    override fun onHistoryConnecting() {
        initConnectStartTime()
    }

    override fun onHistorySuccess(connectEvent: ConnectEvent?) {
        Log.d(TAG, "onHistorySuccess: $connectEvent")
        submitTotalAutoConnectTime()
        handleConnectEvent(false, connectEvent)
    }

    override fun onHistoryFailure(connectEvent: ConnectEvent?) {
        submitTotalAutoConnectTime()
    }

    override fun onCheckConnect(connectEvent: ConnectEvent?) {
        Log.d(TAG, "onCheckConnect: $connectEvent")
        handleConnectEvent(false, connectEvent)
    }

    override fun onDeviceSelected(connectEvent: ConnectEvent?) {
        Log.d(TAG, "onDeviceSelected: $connectEvent")
    }

    override fun onUnbind(unbindEvent: UnbindEvent?) {
        Log.d(TAG, "onUnbind: $unbindEvent")
        updateDeviceInfoUI();
    }

    override fun onSessionConnect(session: Session?) {
        Log.d(TAG, "onSessionConnect: $session")
        updateDeviceInfoUI()
    }

    override fun onSessionDisconnect(session: Session?) {
        Log.d(TAG, "onSessionDisconnect: $session")
        updateDeviceInfoUI()
        uiConnectCallback.onDeviceTypeChange(null)
    }

    override fun onDeviceReflushUpdate(devices: MutableList<Device<DeviceInfo>>?) {
        Log.d(TAG, "onDeviceReflushUpdate: $devices")
        updateStatusByDevices(devices)
    }

    override fun onDeviceOffLine(device: Device<*>?) {
        Log.d(TAG, "onDeviceOffLine: $device")
        updateDeviceInfoUI()
    }

    override fun onDeviceOnLine(device: Device<*>?) {
        Log.d(TAG, "onDeviceOnLine: $device")
    }

    override fun loginState(code: Int, info: String?) {//code 0 未连接; 1sse 连接； 2 本地连接； 3 都连接
        updateDeviceInfoUI()
    }


    private fun handleConnectEvent(showDialog: Boolean = false, event: ConnectEvent?) {
        Log.d(TAG, "handleConnectEvent: $event")
        if (event != null && event.isConnected && event.device != null) {
            if (SSConnectManager.getInstance().isSameWifi) {
                uiConnectCallback.onConnectStatusChange(showDialog, UIConnectStatus.CONNECTED)
            } else if (SSConnectManager.getInstance().isConnected) {
                uiConnectCallback.onConnectStatusChange(showDialog, UIConnectStatus.CONNECT_NOT_SAME_WIFI)
            }
            uiConnectCallback.onDeviceTypeChange(event.device?.zpRegisterType)
        } else {
            uiConnectCallback.onDeviceTypeChange(null)
        }
    }

    private fun updateStatusByDevices(devices: List<Device<*>>?) {
        Log.d(TAG, "updateStatusByDevices $devices")
        if (!SSConnectManager.getInstance().isConnectedChannel) {
            //确保服务连上，能正确获取设备列表才去更新
            Log.d(TAG, "updateStatusByDevices: service not bind, return !!!")
            return
        }
        updateDeviceInfoUI()
    }

    public fun updateDeviceInfoUI() {
        Log.d(TAG, "updateDeviceInfoUI: ")
        var status: UIConnectStatus
        if (SSConnectManager.getInstance().historyDevice == null) {
            Log.d(TAG, "updateDeviceInfoUI: historyDevice == null")
            status = UIConnectStatus.NOT_CONNECTED
        } else if (SSConnectManager.getInstance().isConnecting) {
            Log.d(TAG, "updateDeviceInfoUI: isConnecting")
            status = UIConnectStatus.CONNECTING
        } else{
            when (SSConnectManager.getInstance().connectState) {
                SSConnectManager.CONNECT_BOTH -> {
                    Log.d(TAG, "updateDeviceInfoUI: CONNECT_BOTH")
                    status = UIConnectStatus.CONNECTED
                }
                SSConnectManager.CONNECT_LOCAL -> {
                    Log.d(TAG, "updateDeviceInfoUI: CONNECT_LOCAL")
                    status = UIConnectStatus.CONNECTED
                }
                SSConnectManager.CONNECT_SSE -> {
                    Log.d(TAG, "updateDeviceInfoUI: CONNECT_SSE")
                    status = UIConnectStatus.CONNECT_NOT_SAME_WIFI
                }
                SSConnectManager.CONNECT_NOTHING -> {
                    status = if (SSConnectManager.getInstance().isHistoryDeviceValid) {
                        Log.d(TAG, "updateDeviceInfoUI: CONNECT_ERROR")
                        UIConnectStatus.CONNECT_ERROR
                    } else {
                        Log.d(TAG, "updateDeviceInfoUI: NOT_CONNECTED")
                        UIConnectStatus.NOT_CONNECTED
                    }
                }
                else -> {
                    Log.d(TAG, "updateDeviceInfoUI: match nothing status")
                    status = UIConnectStatus.NOT_CONNECTED
                }
            }
        }
        uiConnectCallback.onConnectStatusChange(false, status);
    }

    private fun initConnectStartTime() {
        connectTime = System.currentTimeMillis()
    }

    private fun submitTotalAutoConnectTime() {
        try {
            val decimalFormat = DecimalFormat("0.0")
            var durationLong = System.currentTimeMillis() - connectTime
            if (durationLong > 10 * 1000) {
                durationLong = 10 * 1000.toLong()
            }
            val duration = decimalFormat.format(durationLong.toFloat() / 1000.toDouble())
            val params = LogParams.newParams()
            params.append("duration", duration)
            LogSubmit.event("connect_device_auto_load_time", params.params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface UIConnectCallback {
        /**
         * 连接状态发生改变
         * @param showDialog 是否是来自循环onCheckConnect的回调
         * @param status 最新连接状态
         */
        fun onConnectStatusChange(showDialog: Boolean = false, status: UIConnectStatus)

        /**
         * 连接设备类型发生改变
         * @param deviceType? null未连接否则返回对应的连接设备类型
         */
        fun onDeviceTypeChange(deviceType: String?)

        /**
         * 连接设备信息发生改变
         * @param deviceType? null未连接否则返回对应的连接设备类型
         */
        fun onDeviceChange(device: Device<*>)
    }

    open class SimpleUIConnectCallback : UIConnectCallback{

        override fun onConnectStatusChange(showDialog: Boolean, status: UIConnectStatus) {
        }

        override fun onDeviceTypeChange(deviceType: String?) {
        }

        override fun onDeviceChange(device: Device<*>) {
        }

    }
}