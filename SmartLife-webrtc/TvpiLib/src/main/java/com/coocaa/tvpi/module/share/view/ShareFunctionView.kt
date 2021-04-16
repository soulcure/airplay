package com.coocaa.tvpi.module.share.view

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import com.coocaa.publib.utils.ToastUtils
import com.coocaa.smartscreen.connect.SSConnectManager
import com.coocaa.smartscreen.utils.CmdUtil
import com.coocaa.tvpi.module.connection.ConnectNetForDongleActivity
import com.coocaa.tvpi.module.connection.WifiConnectActivity
import com.coocaa.tvpi.module.mine.SmartScreenAboutActivity
import com.coocaa.tvpi.module.mine.lab.networktest.NetworkTestActivityW3
import com.coocaa.tvpi.module.mine.view.VerificationCodeDialog2
import com.coocaa.tvpi.util.TvpiClickUtil
import com.coocaa.tvpilib.R
import kotlinx.android.synthetic.main.layout_share_function.view.*
import swaiotos.channel.iot.ss.device.DeviceInfo
import swaiotos.channel.iot.ss.device.TVDeviceInfo

class ShareFunctionView(
        context: Context,
        attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_share_function, this, true)
        netTestLayout.setOnClickListener {
            context.startActivity(Intent(context, NetworkTestActivityW3::class.java))
        }
        var device = SSConnectManager.getInstance().historyDevice
        if (null != device && device.info is TVDeviceInfo
                && (device.info as TVDeviceInfo).blueSupport == 0) {//0支持蓝牙

            netChangeLayout.visibility = VISIBLE
            networkLine.visibility = VISIBLE

            netChangeLayout.setOnClickListener {
                if (!SSConnectManager.getInstance().isConnected) {
                    ToastUtils.getInstance().showGlobalLong("请先连接设备")
                } else {
                    changeWifi(context)
                }
            }
        } else {
            netChangeLayout.visibility = GONE
            networkLine.visibility = GONE
        }

        deviceSettingLayout.setOnClickListener(object : OnClickListener {
            override fun onClick(v: View?) {
                systemSetting(context)
            }

        })

        deviceAboutLayout.setOnClickListener {
            context.startActivity(Intent(context, SmartScreenAboutActivity::class.java))
        }
    }

    private fun systemSetting(context: Context){

        //发布会需求，不在同一wifi，不让点击
//                if (!SSConnectManager.getInstance().isSameWifi()) {
//                    WifiConnectTipActivity.start(context)
        if (!SSConnectManager.getInstance().isSameWifi()) {
            WifiConnectActivity.start(context)
            return
        }
        var device = SSConnectManager.getInstance().historyDevice
        if (!SSConnectManager.getInstance().isConnected || device == null) {
            ToastUtils.getInstance().showGlobalLong("请先连接设备")
        } else {
            if (TextUtils.isEmpty(device.merchantId)) {
                CmdUtil.startSettingApp();
                TvpiClickUtil.onClick(context, "np://com.coocaa.smart.donglevirtualinput/index?from=DeviceManagerActivity")
            } else {
                val verifyCodeDialog = VerificationCodeDialog2()
                verifyCodeDialog.setVerifyCodeListener {
                    CmdUtil.startSettingApp();
                    TvpiClickUtil.onClick(context, "np://com.coocaa.smart.donglevirtualinput/index?from=DeviceManagerActivity")
                }
                if (!verifyCodeDialog.isAdded && context is FragmentActivity) {
                    verifyCodeDialog.show(context.supportFragmentManager, "deviceSetting")
                }
            }
        }
    }

    private fun changeWifi(context: Context) {
        val device = SSConnectManager.getInstance().historyDevice
        if (device != null) {

            if (TextUtils.isEmpty(device.merchantId)) {
                val deviceInfo = device.info
                if (null != deviceInfo) {
                    if (deviceInfo.type() == DeviceInfo.TYPE.TV) {
                        val tvDeviceInfo = deviceInfo as TVDeviceInfo
                        ConnectNetForDongleActivity.start(context, tvDeviceInfo.MAC)
                    }
                }
            } else {
                val verifyCodeDialog = VerificationCodeDialog2()
                verifyCodeDialog.setVerifyCodeListener {
                    val deviceInfo = device.info
                    if (null != deviceInfo) {
                        if (deviceInfo.type() == DeviceInfo.TYPE.TV) {
                            val tvDeviceInfo = deviceInfo as TVDeviceInfo
                            ConnectNetForDongleActivity.start(context, tvDeviceInfo.MAC)
                        }
                    }
                }
                if (!verifyCodeDialog.isAdded && context is FragmentActivity) {
                    verifyCodeDialog.show(context.supportFragmentManager, "deviceSetting")
                }
            }
        }
    }

    fun setUpdateIconVisible(isVisibility: Boolean) {
        if (isVisibility) {
            system_update_img.visibility = VISIBLE
        } else {
            system_update_img.visibility = GONE
        }
    }
}