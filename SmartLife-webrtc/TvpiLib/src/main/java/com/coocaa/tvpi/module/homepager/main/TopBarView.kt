package com.coocaa.tvpi.module.homepager.main

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import com.coocaa.smartscreen.constant.SmartConstans
import com.coocaa.tvpi.event.NetworkEvent
import com.coocaa.tvpi.module.connection.ScanActivity2
import com.coocaa.tvpi.module.login.LoginActivity
import com.coocaa.tvpi.module.login.UserInfoCenter
import com.coocaa.tvpi.util.NetworkUtil
import com.coocaa.tvpi.util.WifiUtil
import com.coocaa.tvpi.view.webview.SimpleWebViewActivity
import com.coocaa.tvpilib.R
import kotlinx.android.synthetic.main.layout_mainpager_toolbar.view.*
import kotlinx.android.synthetic.main.popup_connect_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TopBarView(
        context: Context,
        attrs: AttributeSet
) : FrameLayout(context, attrs) {
    private var popupWindow: PopupWindow? = null
    private var isDark: Boolean = false

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_mainpager_toolbar, this, true)
        setNetworkInfo()
        tvEnv.visibility = if (SmartConstans.isTestServer()) View.VISIBLE else View.GONE
        ivMore.setOnClickListener {
            showPopUpWindow(ivMore)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d("ToolbarView", "onAttachedToWindow: ")
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d("ToolbarView", "onDetachedFromWindow: ")
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }


    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        Log.d("ToolbarView", "onWindowFocusChanged: $hasWindowFocus")
        setNetworkInfo()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(networkEvent: NetworkEvent) {
        Log.d("NetworkEvent", "onEvent: $networkEvent")
        setNetworkInfo()
    }

    private fun setNetworkInfo() = when {
        NetworkUtil.isWifiConnected(context) -> {
            if (isDark) {
                ivNetworkType.setBackgroundResource(R.drawable.icon_home_network_type_wifi_dark)
            } else {
                ivNetworkType.setBackgroundResource(R.drawable.icon_home_network_type_wifi_light)
            }

            val connectWifiSSID = WifiUtil.getConnectWifiSsid(context)
            if (TextUtils.isEmpty(connectWifiSSID) || "<unknown ssid>" == connectWifiSSID) {
                tvNetworkName.text = "无线网络"
            } else {
                tvNetworkName.text = connectWifiSSID
            }
        }
        NetworkUtil.isConnected(context) -> {
            if (isDark) {
                ivNetworkType.setBackgroundResource(R.drawable.icon_home_network_type_4g_dark)
            } else {
                ivNetworkType.setBackgroundResource(R.drawable.icon_home_network_type_4g_light)
            }
            tvNetworkName.text = "移动网络"
        }
        else -> {
            if (isDark) {
                ivNetworkType.setBackgroundResource(R.drawable.icon_home_network_type_no_dark)
            } else {
                ivNetworkType.setBackgroundResource(R.drawable.icon_home_network_type_no_light)
            }
            tvNetworkName.text = "无网络"
        }
    }


    private fun showPopUpWindow(ivMore: ImageView) {
        val popupRootView = LayoutInflater.from(context).inflate(R.layout.popup_connect_layout, null)

        if (popupWindow == null) {
            popupWindow = PopupWindow(popupRootView)
        }

        popupWindow?.apply {
            animationStyle = R.style.popupAnimation
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            isFocusable = true
            isTouchable = true
            isOutsideTouchable = true
            showAsDropDown(ivMore)
        }

        popupRootView.scan_connect_layout.setOnClickListener {
            ScanActivity2.start(context)
            popupWindow?.dismiss()
        }

        popupRootView.code_connect_layout.setOnClickListener {
            ScanActivity2.start(context, 2, false)
            popupWindow?.dismiss()
        }

        popupRootView.history_connect_layout.setOnClickListener {
            ScanActivity2.start(context, 1, true)
            popupWindow?.dismiss()
        }

        popupRootView.novice_guide.setOnClickListener {
            SimpleWebViewActivity.startAsApplet(context, "https://webapp.skysrt.com/swaiot/novice-guide/index.html")
            popupWindow?.dismiss()
        }
    }

    fun setThemeDark(isDark: Boolean) {
        this.isDark = isDark
        if (isDark) {
            tvNetworkName.setTextColor(Color.parseColor("#99FFFFFF"))
            tvEnv.setTextColor(Color.parseColor("#99FFFFFF"))
            ivMore.setImageResource(R.drawable.icon_home_more_dark)
        } else {
            tvNetworkName.setTextColor(Color.parseColor("#66000000"))
            tvEnv.setTextColor(Color.parseColor("#66000000"))
            ivMore.setImageResource(R.drawable.icon_home_more_light)
        }
        setNetworkInfo()
    }

    fun isDark(): Boolean {
        return isDark
    }

}