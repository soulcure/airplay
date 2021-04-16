package com.coocaa.tvpi.module.homepager.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.coocaa.publib.base.GlideApp
import com.coocaa.smartscreen.connect.SSConnectManager
import com.coocaa.tvpi.module.connection.ScanActivity2
import com.coocaa.tvpi.module.share.ShareCodeActivity
import com.coocaa.tvpilib.R
import kotlinx.android.synthetic.main.layout_mainpager_connect_status.view.*
import swaiotos.channel.iot.ss.device.Device

class ConnectStatusView(
        context: Context,
        attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private companion object {
        val TAG: String = ConnectStatusView::class.java.simpleName
    }

    private var status = UIConnectStatus.NOT_CONNECTED

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_mainpager_connect_status, this, true)
        notConnectLayout.visibility = View.VISIBLE
        connectInfoLayout.visibility = View.GONE
        setOnClickListener {
            if (status == UIConnectStatus.NOT_CONNECTED) {
                ScanActivity2.start(context)
            } else {
                context.startActivity(Intent(context, ShareCodeActivity::class.java))
            }
        }

        connectScanLayout.setOnClickListener {
            ScanActivity2.start(context)
        }
    }

    fun setUIConnectStatus(status: UIConnectStatus) {
        if (isDestroy(context as Activity?)) {
            Log.d(TAG, "setUIConnectStatus: activity is destroy return function")
            return
        }
        Log.d(TAG, "setUIStatus: $status")
        val deviceName = SSConnectManager.getInstance().getDeviceName(SSConnectManager.getInstance().getHistoryDevice())
        Log.d(TAG, "setUIStatus:deviceName $deviceName")
        when (status) {
            UIConnectStatus.NOT_CONNECTED -> {
                notConnectLayout.visibility = View.VISIBLE
                connectInfoLayout.visibility = View.GONE
            }
            UIConnectStatus.CONNECTING -> {
                notConnectLayout.visibility = View.GONE
                connectInfoLayout.visibility = View.VISIBLE
                tvDeviceName.text = deviceName
                progressBar.visibility = View.VISIBLE
                ivDeviceIcon.visibility = View.GONE
                ivConnectError.visibility = View.GONE
            }
            UIConnectStatus.CONNECTED -> {
                notConnectLayout.visibility = View.GONE
                connectInfoLayout.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                tvDeviceName.text = deviceName
                ivConnectError.visibility = View.GONE
                ivDeviceIcon.visibility = View.VISIBLE
                GlideApp.with(this)
                        .load(SSConnectManager.getInstance().historyDevice?.merchantIcon)
                        .error(R.drawable.icon_connect_device_logo)
                        .into(ivDeviceIcon)
            }
            UIConnectStatus.CONNECT_NOT_SAME_WIFI -> {
                notConnectLayout.visibility = View.GONE
                connectInfoLayout.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                tvDeviceName.text = deviceName
                ivConnectError.visibility = View.VISIBLE
                ivDeviceIcon.visibility = View.VISIBLE
                GlideApp.with(this)
                        .load(SSConnectManager.getInstance().historyDevice?.merchantIcon)
                        .error(R.drawable.icon_connect_device_logo)
                        .into(ivDeviceIcon)
            }
            UIConnectStatus.CONNECT_ERROR -> {
                //兼容逻辑,首页onResume执行updateStatusByDevices（）会产生错误回调中断loading
                if (this.status != UIConnectStatus.CONNECTING) {
                    notConnectLayout.visibility = View.GONE
                    connectInfoLayout.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    tvDeviceName.text = deviceName
                    ivConnectError.visibility = View.VISIBLE
                    ivDeviceIcon.visibility = View.VISIBLE
                    GlideApp.with(this)
                            .load(SSConnectManager.getInstance().historyDevice?.merchantIcon)
                            .error(R.drawable.icon_connect_device_logo)
                            .into(ivDeviceIcon)
                } else {
                    Log.d(TAG, "setUIConnectStatus: connecting not response error status")
                }
            }
        }
        //执行完保存状态
        this.status = status
    }

    fun setDeviceName(device: Device<*>) {
        val deviceName = SSConnectManager.getInstance().getDeviceName(device)
        tvDeviceName.text = deviceName
    }

    fun isDestroy(mActivity: Activity?): Boolean {
        return if (mActivity == null || mActivity.isFinishing() || Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && mActivity.isDestroyed()) {
            true
        } else {
            false
        }
    }
}