package com.swaiotos.skymirror

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import com.swaiotos.skymirror.sdk.capture.MirManager
import com.swaiotos.skymirror.sdk.reverse.PlayerActivity
import com.swaiotos.skymirror.sdk.util.DLNACommonUtil
import com.swaiotos.skymirror.sdk.util.DeviceUtil
import kotlinx.android.synthetic.main.activity_main.*


//1.验证是否有系统权限
//              if(has)
//                  使用 DisplayManager 创建 VirtualDisplay 进行抓屏
//              else
//                  获取悬浮窗权限，再使用 MediaProjection 创建 VirtualDisplay 进行抓屏
//2.是否是iot-channel传输
//              if(isIotChannel)
//                  通过 handleIMMessage 获取 iot-channel 发送的IP，开始镜像
//              else（通过点击事件）
//                  需要在editText中输入正确IP，点击按钮将IP传入，开始镜像

class MainActivity : Activity()/* : SSChannelClient.SSChannelClientActivity()*/ {

    private var REQUEST_CODE_CHANNEL: Int = 1;
    private var REQUEST_CODE_CLICK: Int = 2;

    var mServerIp: String = null.toString()


    private var netSpeed: NetSpeed? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        netSpeed = NetSpeed(this)
        netSpeed?.startSpeedView()

        etInputCaptureIp.setText(DeviceUtil.getLocalIPAddress(this))
        //etInputCaptureIp.setText("172.20.130.169")
        //etInputCaptureIp.setText("192.168.50.202")
        //etInputCaptureIp.setText("192.168.137.115")
    }

    override fun onResume() {
        super.onResume()
        if (intent != null && intent.action != null) {
            if (intent.action == "action_startCapture") {
                val serverIp = intent.getStringExtra("serverIp")
                prepareScreen(serverIp, REQUEST_CODE_CHANNEL)
            }
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        netSpeed?.stopSpeedView()
    }


    /**
     * 开启发送镜像服务
     */
    fun startScreen(view: View) {
        prepareScreen(etInputCaptureIp.text.toString(), REQUEST_CODE_CLICK)
    }

    /**
     * 关闭发送镜像服务
     */
    fun stopScreen(view: View) {
        MirManager.instance().stopScreenCapture(this)
    }


    /**
     * 开始接收投屏数据（使用sdkUI）
     */
    fun startReverseScreen(view: View) {
        Log.e("colin", "colin start time01 --- pad start ReverseCaptureService by click")
        PlayerActivity.obtainPlayer(this, null);
    }


    /**
     * 关闭接收镜像服务
     */
    fun stopReverseScreen(view: View) {
        MirManager.instance().destroy();
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_CHANNEL -> {
                startScreen(resultCode, data)
                finish()//启动后关闭
            }

            REQUEST_CODE_CLICK -> {
                if (TextUtils.isEmpty(etInputCaptureIp.text)) {
                    Toast.makeText(this, "请输入IP", Toast.LENGTH_LONG).show()
                    return
                }
                startScreen(resultCode, data)
            }
        }

    }


    /**
     * 验证是否有系统权限
     */
    private fun hasPermission(): Boolean {
        return DLNACommonUtil.checkPermission(this)
    }

    /**
     * 开始镜像
     */
    private fun prepareScreen(ip: String, type: Int) {
        Log.e("colin", "colin start time01 --- tv start MirClientService by iot-channel")
        mServerIp = ip;

        if (hasPermission()) {
            MirManager.instance().startScreenCapture(this, ip)
            finish()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val projectionManager =
                getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            startActivityForResult(projectionManager.createScreenCaptureIntent(), type)
        }
    }


    private fun startScreen(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "权限被拒绝", Toast.LENGTH_LONG).show()
        } else {
            val dm = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(dm)
            MirManager.instance().startScreenCapture(
                this, mServerIp, dm.widthPixels, dm.heightPixels, resultCode, data
            )
        }

    }


}
