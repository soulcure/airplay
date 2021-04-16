package com.coocaa.tvpi.module.remote.floatui

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.coocaa.publib.utils.DimensUtils
import com.coocaa.publib.utils.ToastUtils
import com.coocaa.smartsdk.SmartApi
import com.coocaa.swaiotos.virtualinput.utils.SuperSpUtil
import com.coocaa.tvpi.module.connection.WifiConnectActivity
import com.coocaa.tvpi.module.io.HomeIOThread
import com.coocaa.tvpi.util.TvpiClickUtil
import com.coocaa.tvpilib.R

/**
 * @Author: yuzhan
 */
class VirtualInputFloatView(context: Context): FrameLayout(context!!) {

    companion object {
        public lateinit var params: LayoutParams
        private var screenWidth:Int = 0
        private var screenHeight:Int = 0
        private var w:Int = 0
        private var h:Int = 0
        public var SAVE_KEY_X = "vi_float_pos_x"
        public var SAVE_KEY_Y = "vi_float_pos_y"

        private val TAG = "VIFloat"

        fun setScreenSize(context: Context, sw: Int?, sh: Int?) {
            screenWidth = sw?: 0
            screenHeight = sh?:0
            Log.d(TAG, "set screenWidth=$screenWidth, screenHeight=$screenHeight")

            w = DimensUtils.dp2Px(context, 70f)
            h = DimensUtils.dp2Px(context, 70f)
            params = LayoutParams(w, h)
            var sX = SuperSpUtil.getInt(context, SAVE_KEY_X, -1)
            var sY = SuperSpUtil.getInt(context, SAVE_KEY_Y, -1)
            Log.d(TAG, "get saved pos, sX=$sX, sY=$sY")
            params.leftMargin = if(sX >= 0) sX else screenWidth - w
            params.topMargin = if(sY >= 0) sY else screenHeight - h*2
        }
    }
    private var touchX = 0f
    private var touchY = 0f
    private var lastX = 0f
    private var lastY = 0f

    private var dotView: View? = null
    private var isMove = false
    private val moveThreshold = 5


    init {
        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        clipChildren = false
        if (screenWidth == 0 || screenHeight == 0) {
            screenWidth = context?.applicationContext!!.resources.displayMetrics.widthPixels
            screenHeight = context.applicationContext!!.resources.displayMetrics.heightPixels
            Log.d(TAG, "22 screenWidth=$screenWidth, screenHeight=$screenHeight")
        }

        dotView = View(context)
        dotView?.setBackgroundResource(R.drawable.vi_float_dot_dongle)
        dotView?.layoutParams = FrameLayout.LayoutParams(w, h)//params
        addView(dotView)
//        dotView!!.post {
//            Log.d(TAG, "dot pos, x=${params.leftMargin}, y=$params.topMargin")
//            dotView!!.animate().x(params.leftMargin.toFloat()).y(params.topMargin.toFloat()).setDuration(0).start()
//        }
        dotView?.isClickable = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            elevation = 1f
        }

        val onTouchListener = object : OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
//                        Log.d("Tvpi", "onTouch down... touchX=" + event.rawX + ", touchY=" + event.rawY)
                        isMove = false
                        touchX = event.rawX
                        touchY = event.rawY
                        lastX = params.leftMargin.toFloat()
                        lastY = params.topMargin.toFloat()
                        return false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val xOffset = (event.rawX - touchX).toInt()
                        val yOffset = (event.rawY - touchY).toInt()
//                    Log.d(TAG, "onTouch curX=" + event.rawX + ", curY=" + event.rawY + ", touchX=" + touchX + ", touchY=" + touchY + ", xOffset=" + xOffset + ", yOffset=" + yOffset)
                        if (Math.abs(xOffset) > moveThreshold || Math.abs(yOffset) > moveThreshold) {
                            isMove = true
                            updatePos(xOffset, yOffset)
                        } else {
                            isMove = false
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        Log.d(TAG, "touch up, isMove=$isMove")
                        if (isMove) {
                            onMoveFinish()
                        }
                    }
                }
                return false
            }
        }

        dotView?.setOnTouchListener(onTouchListener)

        val onClickListener = object : OnClickListener {
            override fun onClick(v: View?) {
                if (isMove) {
//                    Log.d(TAG, "dot onMove...")
                } else {
//                    Log.d(TAG, "dot onClick...")
                    startRemoteActivity()
                }
            }
        }
        dotView?.setOnClickListener(onClickListener)

    }

    //滑动停止后，让悬浮球靠边
    fun onMoveFinish() {
        val toLeft = params.leftMargin < (screenWidth/2)
        dotView!!.animate().cancel()
        val toX = if(toLeft) 0f else Math.max(screenWidth - params.leftMargin - w, screenWidth - w).toFloat()
        Log.d(TAG, "onMoveFinish, toLeft=$toLeft, toX=$toX")
        dotView!!.animate().x(toX).setDuration(300).start()
        params.leftMargin = toX.toInt()
        SuperSpUtil.putInt(context, SAVE_KEY_X, params.leftMargin)
        SuperSpUtil.putInt(context, SAVE_KEY_Y, params.topMargin)

        var sX = SuperSpUtil.getInt(context, SAVE_KEY_X, -1)
        var sY = SuperSpUtil.getInt(context, SAVE_KEY_Y, -1)
        Log.d(TAG, "save sX=$sX, sY=$sY")
    }

    fun onShow() {
        var sX = SuperSpUtil.getInt(context, SAVE_KEY_X, -1)
        var sY = SuperSpUtil.getInt(context, SAVE_KEY_Y, -1)
        Log.d(TAG, "get saved pos, sX=$sX, sY=$sY")
        params.leftMargin = if(sX >= 0) sX else screenWidth - w
        params.topMargin = if(sY >= 0) sY else screenHeight - h*2
        Log.d(TAG, "dot pos, x=${params.leftMargin}, y={$params.topMargin}")
        dotView!!.animate().x(params.leftMargin.toFloat()).y(params.topMargin.toFloat()).setDuration(0).start()
    }

    fun onHide() {

    }

    var x:Int = 0
    var y:Int = 0
    private fun isOutOfScreen(xOffset: Int, yOffset: Int): Boolean {
        if (lastX + xOffset < 0 || lastY + yOffset < 0) {
            return true
        } else if (lastX + xOffset + w > screenWidth || lastY + yOffset + h > screenHeight) {
            return true
        }
        return false
    }

    private fun updatePos(xOffset: Int, yOffset: Int) {
        if (lastX + xOffset < 0) {
            x = 0
        } else if (lastX + xOffset + w > screenWidth) {
            x = screenWidth - w
        } else {
            x = (lastX + xOffset).toInt()
        }
        if(lastY + yOffset < 0) {
            y = 0
        } else if(lastY + yOffset + h > screenHeight) {
            y = screenHeight - h
        } else {
            y = (lastY + yOffset).toInt()
        }
        params.leftMargin = x
        params.topMargin = y
//        dotView?.layoutParams = params
        Log.d(TAG, "update pos, x=$x, y=$y")
        dotView!!.x = x.toFloat()
        dotView!!.y = y.toFloat()
//        dotView!!.animate().cancel()
//        dotView!!.animate().x(x.toFloat()).y(y.toFloat()).setDuration(0).start()
    }

    private fun startRemoteActivity() {
        val isConnected = SmartApi.isDeviceConnect()
        val connectedDevice = SmartApi.getConnectDeviceInfo()
        Log.d("SmartApi", "isConnected=$isConnected, device=$connectedDevice")
        if (isConnected && connectedDevice != null) {
            HomeIOThread.execute {
                if (!SmartApi.isSameWifi()) {
                    WifiConnectActivity.start(context)
                } else {
                    TvpiClickUtil.onClick(context, "np://com.coocaa.smart.floatvirtualinput/index?from=floatui")
                }
            }
//            if(connectedDevice.isTempDevice) {
//                HomeIOThread.execute {
//                    if (!SmartApi.isSameWifi()) {
//                        WifiConnectTipActivity.start(context)
//                    } else {
//                        TvpiClickUtil.onClick(context, "np://com.coocaa.smart.floatvirtualinput/index?from=floatui")
//                    }
//                }
//            } else {
//                TvpiClickUtil.onClick(context, "np://com.coocaa.smart.virtualinput/index?from=floatui")
//            }
        } else {
            ToastUtils.getInstance().showGlobalShort("请先连接设备")
        }
    }
}