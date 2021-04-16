package com.coocaa.tvpi.module.remote.floatui

import android.app.Activity
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.alibaba.fastjson.JSON
import com.coocaa.smartscreen.businessstate.`object`.BusinessState
import com.coocaa.smartscreen.data.channel.events.ProgressEvent
import com.coocaa.swaiotos.virtualinput.state.FloatVIStateChangeListener
import com.coocaa.swaiotos.virtualinput.state.FloatVIStateManager
import com.coocaa.tvpi.event.StartPushEvent
import com.coocaa.tvpi.module.base.Navigatgorable
import com.coocaa.tvpi.module.io.HomeUIThread
import com.coocaa.tvpi.module.live.LiveActivity
import com.coocaa.tvpi.module.local.album2.PreviewActivityW7
import com.coocaa.tvpi.module.local.document.page.DocumentPlayerActivity
import com.coocaa.tvpilib.R
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import swaiotos.channel.iot.ss.channel.im.IMMessage

/**
 * @Author: yuzhan
 */
class VirtualInputFloatBinder(activity: Activity) : FloatVIStateChangeListener {
    private var activity: Activity? = null
    private var view: VirtualInputFloatView2? = null
    private var params: FrameLayout.LayoutParams? = null
    private var isAdded = false
    private var decorView: ViewGroup? = null
    private var contentView: ViewGroup? = null
    private var bottom: Int = 0
    private var pushInAnimation: Animation? = null

    private val TAG = "FloatVI2"

    companion object {
        private var dm: DisplayMetrics? = null
    }

    init {
        this.activity = activity
        Log.d(TAG, "init activity=$activity")
        if (dm == null) {
            dm = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(dm)
            VirtualInputFloatView.setScreenSize(activity, dm?.widthPixels, dm?.heightPixels)
        }
        if (activity is Navigatgorable) {
            this.bottom = (activity as Navigatgorable).navigatorHeight()
            if (this.bottom < 0) {
                this.bottom = 0
            }
        }
        addView()
    }

    private fun addView() {
        try {
//            val isMainActivity = activity!!::class.java.name.contains("com.coocaa.tvpi.MainActivity")
//            Log.d(TAG, "name = ${activity!!::class.java.name}, isMainActivity=$isMainActivity")
//            view = if (isMainActivity) MainVirtualInputFloatView2(activity!!, bottom) else VirtualInputFloatView2(activity!!, bottom)
            view = VirtualInputFloatView2(activity!!, bottom)
            val rootView = activity?.window?.decorView?.rootView
            Log.d(TAG, "rootView=$rootView")
            decorView = activity?.window?.decorView as ViewGroup
            Log.d(TAG, "decorView=$decorView")

            params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initContentView() {
        if (contentView == null) {
            val content = decorView?.findViewById<View>(android.R.id.content)
            if (content != null) {
                contentView = content as ViewGroup
            }
            Log.d(TAG, "contentView=$contentView")
        }
    }

    fun hide() {
        if (!isAdded) return
//        decorView?.removeView(view)
        contentView?.removeView(view)
        view?.onHide()
        isAdded = false
        FloatVIStateManager.removeListener(this)
    }

    fun show() {
        if (isAdded) return
        registeEventBus()
        initContentView()
//        decorView?.addView(view, params)
        contentView?.addView(view, params)
        view?.onShow()
        if (contentView != null) {
            isAdded = true
        }
        view?.onStateChanged(FloatVIStateManager.getCurState())
        FloatVIStateManager.addListener(this)
        if (!isAdded) {
            ensureContentView()
        }
    }

    fun showWithAnim() {
        if (isAdded) return
        initContentView()
//        decorView?.addView(view, params)
        contentView?.addView(view, params)
        view?.onShow()
        if (contentView != null) {
            isAdded = true
        }
        view?.onStateChanged(FloatVIStateManager.getCurState())
        FloatVIStateManager.addListener(this)
        if (!isAdded) {
            ensureContentView()
        }

        view?.apply {
            if (pushInAnimation == null) {
                pushInAnimation = AnimationUtils.loadAnimation(activity, R.anim.push_bottom_in)
            }
            clearAnimation()
            view!!.startAnimation(pushInAnimation)
        }
    }

    private fun ensureContentView() {
        Log.d(TAG, "ensureContentView")
        decorView?.post(Runnable {
            show()
        })
    }

    fun destroy() {
        hide()
        EventBus.getDefault().unregister(this)//解绑
    }

    override fun onStateChanged(state: BusinessState?) {
        Log.d(TAG, "receive state : $state")
        // 当id为null时，表示是旧版业务数据,需要根据type去解析配置文件
        HomeUIThread.execute(Runnable {
            view?.onStateChanged(state)
        })
    }

    override fun onDeviceConnectChanged(isConnect: Boolean) {
        if (isConnect) {
            view?.onDeviceConnect()
        } else {
            view?.onDeviceDisconnect()
        }
    }

    override fun onStateInit() {
        Log.d(TAG, "receive onStateInit")
        HomeUIThread.execute(Runnable {
            view?.onStateInit()
        })
    }

    override fun onProgressLoading(json: String) {
        if (TextUtils.isEmpty(json)) return
        try {
            val event = JSON.parseObject(json, ProgressEvent::class.java)
            HomeUIThread.execute({
                if (event.type == IMMessage.TYPE.PROGRESS) {
                    view?.refreshLoadingUI(event.progress)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onProgressResult(json: String) {
        if (TextUtils.isEmpty(json)) return
        try {
            val event = JSON.parseObject(json, ProgressEvent::class.java)
            if (event.type == IMMessage.TYPE.RESULT) {
                HomeUIThread.execute({
                    Log.d(TAG, "onProgressResult: " + event.result)
                    view?.showLoadingResultUI(event.result)
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun registeEventBus() {
        if (activity is PreviewActivityW7
                || activity is DocumentPlayerActivity
                || activity is LiveActivity) {
            if (!EventBus.getDefault().isRegistered(this)) {
                //注册，重复注册会导致崩溃
                EventBus.getDefault().register(this)
            }
        }
    }

    //接收消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: StartPushEvent) {
        if (event != null) {
            Log.d(TAG, "onEvent: StartPushEvent...")
            if (FloatVIStateManager.getIsLoading() == false) {
                view?.startLoading(event.pushPageType)
            }
        }
    }
}