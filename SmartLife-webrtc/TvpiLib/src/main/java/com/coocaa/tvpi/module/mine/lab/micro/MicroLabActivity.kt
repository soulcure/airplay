package com.coocaa.tvpi.module.mine.lab.micro

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.coocaa.publib.base.BaseAppletActivity
import com.coocaa.publib.utils.ToastUtils
import com.coocaa.smartscreen.connect.SSConnectManager
import com.coocaa.tvpi.util.permission.PermissionListener
import com.coocaa.tvpi.util.permission.PermissionsUtil
import com.coocaa.tvpilib.R
import com.swaiot.webrtcc.sound.WebRTCSoundManager


import kotlinx.android.synthetic.main.activity_micro_lab.*
import swaiotos.runtime.h5.H5ChannelInstance

class MicroLabActivity : BaseAppletActivity() {

    private val STATE_NORMAL = 0 //未镜像

    private val STATE_CONNECTING = 1 //镜像连接中

    private val STATE_MIRRORING = 2 //正在镜像

    private var mirrorState = STATE_NORMAL

    private val result = WebRTCSoundManager.WebRtcResult { i: Int, s: String? ->
        if (i == 0) {  //成功
            mirrorState = STATE_MIRRORING
            ToastUtils.getInstance().showGlobalLong("正在录音")
            Log.d(TAG, ": STATE_MIRRORING")
        } else if (i == 1) { //正在连接
            mirrorState = STATE_CONNECTING
            Log.d(TAG, ": STATE_CONNECTING")
        } else {  //失败
            mirrorState = STATE_NORMAL
            Log.d(TAG, ": STATE_NORMAL")
        }
    }

    private val sender = WebRTCSoundManager.SenderImpl { content: String? -> H5ChannelInstance.getSingleton().sendWebRTCVoice(content) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_micro_lab)
        TAG = "MicroLabActivity"
        initListener()
        PermissionsUtil.getInstance().requestPermission(this, object : PermissionListener {
            override fun permissionGranted(permission: Array<out String>?) {

            }

            override fun permissionDenied(permission: Array<String>) {
                ToastUtils.getInstance().showGlobalLong("将无法使用扩音器")
            }
        }, Manifest.permission.RECORD_AUDIO)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        btn_start_record.setOnClickListener(View.OnClickListener { v ->
            if(btn_start_record.text.toString().equals("点击说话")){
                startRecord()
            } else {
                stopRecord()
            }

        })


    }


    private fun startRecord() {
        WebRTCSoundManager.instance().init(this, object : WebRTCSoundManager.InitListener {
            override fun success() {
                ToastUtils.getInstance().showGlobalLong("麦克风启动")
                WebRTCSoundManager.instance().setSender(sender)
                WebRTCSoundManager.instance().setResult(result)
                WebRTCSoundManager.instance().start()  //开始麦克风
                btn_start_record.setText("点击停止")
            }

            override fun fail() {
                Log.d(TAG, "fail: ")
            }
        })
    }

    private fun stopRecord() {
        ToastUtils.getInstance().showGlobalLong("麦克风停止")
        WebRTCSoundManager.instance().stop()
        WebRTCSoundManager.instance().destroy()
        btn_start_record.setText("点击说话")
    }

}