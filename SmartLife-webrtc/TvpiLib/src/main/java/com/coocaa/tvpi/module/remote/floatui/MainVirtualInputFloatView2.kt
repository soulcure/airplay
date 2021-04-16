package com.coocaa.tvpi.module.remote.floatui

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import com.coocaa.smartscreen.utils.NetworkUtils
import com.coocaa.smartsdk.SmartApi
import com.coocaa.swaiotos.virtualinput.state.FloatVIStateManager
import com.coocaa.tvpi.module.io.HomeUIThread
import com.coocaa.tvpilib.R

/**
 * @Author: yuzhan
 */
class MainVirtualInputFloatView2(context: Context, bottom: Int): VirtualInputFloatView2(context, bottom) {

    var isDeviceConnect: Boolean = false
    var notConnectView: ImageView
    var curUIConnect: Boolean = true
    var inited: Boolean = false

    init {
        TAG = "MainFloatVI"
        Log.d(TAG, "main float view init")
        notConnectView = ImageView(context)
        notConnectView.setImageResource(R.drawable.vi_float_status_not_connect)
        notConnectView.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        notConnectView.isClickable = true
        notConnectView.setOnClickListener { startRemoteActivity() }

        isDeviceConnect = SmartApi.isDeviceConnect()
        Log.d(TAG, "main float view init isDeviceConnect=$isDeviceConnect")

        containerView.removeAllViews()

        refreshUIOnDeviceChanged(isDeviceConnect)
    }

    override fun onDeviceConnect() {
        refreshUIOnDeviceChanged(true)
        super.onDeviceConnect()
    }

    override fun onDeviceDisconnect() {
        refreshUIOnDeviceChanged(false)
        super.onDeviceDisconnect()
    }

    override fun onStateInit() {
        Log.d(TAG, "onStateInit")
        inited = true
        refreshUIOnDeviceChanged(SmartApi.isDeviceConnect())
        super.onStateInit()
    }

    override fun onShow() {
        super.onShow()
        isDeviceConnect = SmartApi.isDeviceConnect()
        Log.d(TAG, "main float view onShow isDeviceConnect=$isDeviceConnect")
        refreshUIOnDeviceChanged(isDeviceConnect)
    }

    private fun refreshUIOnDeviceChanged(isConnect: Boolean) {
        val hasHistory = FloatVIStateManager.hasHistoryDevice()
        val showConnect = isConnect || FloatVIStateManager.hasHistoryDevice()//已连接，或者有历史设备，都显示连接状态
        Log.d(TAG, "refreshUIOnDeviceChanged, hasHistory=$hasHistory, isConnect=$isConnect, showConnect=$showConnect, cur=$curUIConnect")

        curUIConnect = showConnect
        HomeUIThread.removeTask(refreshRunnable)
        if(!inited) {
            HomeUIThread.execute(5000, delayInitRunnable)
            return
        } else {
            HomeUIThread.removeTask(delayInitRunnable)
        }

        var delay: Long = 50
        if(!curUIConnect && !inited) {
            delay = 5000 //初始回调前，拿到的历史记录不准确，延迟显示
        }
        Log.d(TAG, "refresh, delay=$delay")
        HomeUIThread.execute(delay, refreshRunnable)
    }

    private val delayInitRunnable = object: Runnable {
        override fun run() {
            if(!inited) {//timeout
                inited = true
                refreshUIOnDeviceChanged(SmartApi.isDeviceConnect())
            }
        }

    }

    private val refreshRunnable = object : Runnable {
        override fun run() {
            Log.d(TAG, "real refresh, connect=$curUIConnect")
            if(curUIConnect) {
                containerView.removeView(notConnectView)
                if(dotView.parent == null) {
                    containerView.addView(dotView)
                }
            } else {
                //扫一扫挪到首页和发现条中间
                containerView.setBackgroundColor(Color.TRANSPARENT)
                containerView.removeView(dotView)
                if(notConnectView.parent == null) {
                    containerView.addView(notConnectView)
                }
            }
        }

    }

    override fun startConnectDevice() {
        if(!NetworkUtils.isConnected(mContext)) {
            SmartApi.startConnectDevice()
        }else {
            FloatVIStateManager.startConnectDevice()
        }
    }
}