package com.coocaa.tvpi.module.mine.lab

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.coocaa.publib.base.BaseAppletActivity
import com.coocaa.smartscreen.constant.SmartConstans
import com.coocaa.smartsdk.SmartApi
import com.google.gson.Gson
import com.umeng.commonsdk.statistics.common.DeviceConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @Author: yuzhan
 */
class ShowDeviceInfoActivity : BaseAppletActivity() {

    lateinit var layout: LinearLayout
    lateinit var textView: TextView
    lateinit var verView: TextView
    lateinit var spaceIdView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layout = LinearLayout(this)
        layout.apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            gravity = Gravity.CENTER
            setContentView(layout)
        }
        textView = TextView(this)
        textView.apply {
            setTextColor(Color.BLACK)
            textSize = 18F
        }
        layout.addView(textView)

        verView = TextView(this)
        verView.apply {
            setTextColor(Color.BLACK)
            textSize = 18F
        }
        layout.addView(verView)

        spaceIdView = TextView(this)
        spaceIdView.apply {
            setTextColor(Color.BLACK)
            textSize = 18F
        }
        layout.addView(spaceIdView)

        lifecycleScope.launch {
            val info = loadInfo()
            textView.setText(info)

            val ver = SmartConstans.getBuildInfo().buildDate + ", " + SmartConstans.getBuildInfo().buildChannel
            verView.setText(ver)

            val deviceInfo = SmartApi.getConnectDeviceInfo()
            deviceInfo?.apply {
                spaceIdView.setText("\nSpaceId : ${deviceInfo.spaceId}")
            }
        }
    }

    private suspend fun loadInfo() = withContext(Dispatchers.IO) {
        val infoMap = hashMapOf<String, String>()
        try {
            infoMap.put("device_id", DeviceConfig.getDeviceIdForGeneral(this@ShowDeviceInfoActivity.application))
            infoMap.put("mac", DeviceConfig.getMac(this@ShowDeviceInfoActivity.application))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d("TvpiDevice", "device_info=$infoMap")
        Gson().toJson(infoMap)
    }
}