package com.coocaa.tvpi.module.remote

import android.R
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.util.ArrayMap
import android.util.Log
import android.util.TypedValue
import com.coocaa.smartscreen.businessstate.BusinessStatePhoneReport
import com.coocaa.smartscreen.utils.AndroidUtil
import com.coocaa.smartsdk.SmartApi
import com.coocaa.smartsdk.internal.SmartApiBinder
import com.coocaa.swaiotos.virtualinput.VirtualInputKeepAliveManager
import com.coocaa.swaiotos.virtualinput.action.GlobalAction
import com.coocaa.swaiotos.virtualinput.action.IAction
import com.coocaa.swaiotos.virtualinput.event.GlobalEvent
import com.coocaa.swaiotos.virtualinput.event.IEvent
import com.coocaa.swaiotos.virtualinput.iot.GlobalIOT
import com.coocaa.swaiotos.virtualinput.iot.IotImpl
import com.coocaa.swaiotos.virtualinput.state.FloatVIStateManager
import com.coocaa.swaiotos.virtualinput.statemachine.SceneControllerConfig
import com.coocaa.swaiotos.virtualinput.utils.SuperSpUtil
import com.coocaa.swaiotos.virtualinput.utils.VirtualInputUtils
import com.coocaa.tvpi.module.base.UnVirtualInputable
import com.coocaa.tvpi.module.base.VirtualInputable
import com.coocaa.tvpi.module.log.LogParams
import com.coocaa.tvpi.module.log.LogSubmit
import com.coocaa.tvpi.module.remote.floatui.VirtualInputFloatBinder
import com.coocaa.tvpi.module.remote.floatui.VirtualInputFloatView
import com.coocaa.tvpi.util.TvpiClickUtil
import com.umeng.analytics.MobclickAgent
import swaiotos.runtime.base.AppletActivity
import swaiotos.runtime.base.style.IControlBar
import swaiotos.runtime.base.style.IControlBarable
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList

/**
 * @Author: yuzhan
 */
object RemoteVirtualInputManager {
    val TAG = "Tvpi"
    private var instrumentation: TvpiInstrumentation? = null
    val inited = AtomicBoolean(false)
    val dialogMap: MutableMap<Activity, VirtualInputFloatBinder?> = ArrayMap()
    val translucentSet: MutableSet<Class<*>> = HashSet()
    val filterActivitys:ArrayList<String> = ArrayList();
    val filterPrefix:ArrayList<String> = ArrayList();

    fun init(application: Context?, isDefaultProcess: Boolean) {
        if (inited.get()) return
//        instrumentation = TvpiInstrumentation.init()
//        instrumentation?.setCallback(callback)
        TvpiInstrumentation.init(application)
        TvpiInstrumentation.setCallback(callback)
        AndroidUtil.setAppContext(application)
        GlobalIOT.iot = IotImpl(application)
//        VirtualInputServiceBinder.INSTANCE.create(application!!)
        SmartApiBinder.getInstance().init(application)
        SmartApiBinder.getInstance().isMobileRuntime = true;
        GlobalAction.action = action

//        //清除上一次保存的悬浮球位置
        if(isDefaultProcess) {
            SuperSpUtil.clear(application, VirtualInputFloatView.SAVE_KEY_X)
            SuperSpUtil.clear(application, VirtualInputFloatView.SAVE_KEY_Y)
            SceneControllerConfig.getInstance().init(application)
        }
        BusinessStatePhoneReport.getDefault().init(application)

        GlobalEvent.setEvent(object : IEvent {
            override fun onClick(appletId: String?, appletName: String?, btnName: String?) {
                val connectedDevice = SmartApi.getConnectDeviceInfo()
                val params = LogParams.newParams()
                        .append("ss_device_id", if (connectedDevice == null) "disconnected" else connectedDevice.lsid)
                        .append("ss_device_type", if (connectedDevice == null) "disconnected" else connectedDevice.zpRegisterType)
                        .append("applet_id", appletId)
                        .append("applet_name", appletName)
                        .append("btn_name", btnName)
                LogSubmit.event("remote_btn_clicked", params.params)
            }

            override fun onEvent(eventId: String?, extraParams: MutableMap<String, String>?) {
                val connectedDevice = SmartApi.getConnectDeviceInfo()
                val params = LogParams.newParams()
                        .append("ss_device_id", if (connectedDevice == null) "disconnected" else connectedDevice.lsid)
                        .append("ss_device_type", if (connectedDevice == null) "disconnected" else connectedDevice.zpRegisterType)
                        .append(extraParams)
                LogSubmit.event(eventId, params.params)
            }
        })

        if (application != null) {
            FloatVIStateManager.init(application)
        }
        if(isDefaultProcess) {
            VirtualInputKeepAliveManager.getInstance().start(application)
        }
        VirtualInputUtils.init(application)

        //过滤Activity，不显示虚拟遥控的入口
        filterPrefix.add("com.qiyukf");
    }

    private val action = object: IAction {
        override fun startActivity(context: Context?, uri: String?): Boolean {
            return TvpiClickUtil.onClick(context, uri)
        }
    }

    private val callback: IInstrumentation = object : IInstrumentation {
        override fun onActivityCreate(activity: Activity) {
            if (isActivitySupportVirtualInput(activity)) {
                if (!activityHasRemoteFloatingView(activity)) {
                    addFloatViewToActivity(activity)
                }
                if(activity is IControlBarable) {
                    (activity as IControlBarable).setIControlBar(object : IControlBar {
                        override fun setControlBarVisible(b: Boolean) {
                            Log.d("ControlBar", "setControlBarVisible:$b")
                            if(b) {
                                showFloatViewToActivity(activity)
                            } else {
                                hideFloatViewToActivity(activity)
                            }
                            if (activityHasRemoteFloatingView(activity)) {
                                (activity as IControlBarable).onControlBarVisibleChanged(b)
                            }
                        }
                    })
                }
            } else {
                translucentSet.add(activity.javaClass)
            }
        }

        override fun onActivityResume(activity: Activity) {
            showFloatViewToActivity(activity)
            if(activity is AppletActivity) {
                MobclickAgent.onResume(activity)
            }
        }

        override fun onActivityPause(activity: Activity) {
            if (activity.isFinishing) removeFloatViewToActivity(activity) else hideFloatViewToActivity(activity)
            if(activity is AppletActivity) {
                MobclickAgent.onPause(activity)
            }
        }

        override fun onActivityStop(activity: Activity) {
            if (activity.isFinishing) removeFloatViewToActivity(activity) else hideFloatViewToActivity(activity)
        }

        override fun onActivityDestroy(activity: Activity) {
            removeFloatViewToActivity(activity)
            if(activity is IControlBarable) {
                (activity as IControlBarable).setIControlBar(null)
            }
        }
    }

    private fun activityHasRemoteFloatingView(activity: Activity): Boolean {
        return dialogMap[activity] != null
    }

    private fun addFloatViewToActivity(activity: Activity?) {
        Log.d(TAG, "addFloatViewToActivity : $activity")
        activity?.let {
            val binder = VirtualInputFloatBinder(activity)
            dialogMap[activity] = binder
        }
    }

    private fun removeFloatViewToActivity(activity: Activity?) {
        dialogMap[activity]?.hide()
        dialogMap.remove(activity)
    }

     fun showFloatViewToActivity(activity: Activity?) {
        dialogMap[activity]?.show()
    }

    fun showFloatViewWithAnimToActivity(activity: Activity?) {
        dialogMap[activity]?.showWithAnim()
    }

     fun hideFloatViewToActivity(activity: Activity?) {
        dialogMap[activity]?.hide()
    }

    private fun isActivitySupportVirtualInput(activity: Activity?): Boolean {
        for(prefix in filterPrefix) {
            if(activity?.javaClass?.name?.startsWith(prefix)!!)
                return false
        }
        if(filterActivitys.contains(activity?.javaClass?.name)) {
            return false
        }
        if(activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE ||
                activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
            //横屏不显示控制器
            return false
        }
        return (activity is VirtualInputable) || (!isActivityFloat(activity) && activity !is UnVirtualInputable)
    }

    private fun isActivityFloat(activity: Activity?): Boolean {
        val typedValue = TypedValue()
        activity?.theme?.obtainStyledAttributes(intArrayOf(R.attr.windowIsTranslucent, R.attr.windowIsTranslucent))?.getValue(0, typedValue)
        if (typedValue.type == TypedValue.TYPE_INT_BOOLEAN) {
            if (typedValue.data != 0) {
                Log.d(TAG, "activity is translucent : " + activity?.javaClass!!.name)
                return true
            }
        }
        activity?.theme?.obtainStyledAttributes(intArrayOf(R.attr.windowIsFloating))?.getValue(0, typedValue)
        if (typedValue.type == TypedValue.TYPE_INT_BOOLEAN) {
            if (typedValue.data != 0) {
                Log.d(TAG, "activity is floating : " + activity?.javaClass!!.name)
                return true
            }
        }
        return false
    }
}