package com.coocaa.swaiotos.virtualinput.state

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import com.coocaa.smartscreen.businessstate.`object`.BusinessState
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean
import com.coocaa.smartscreen.data.channel.AppInfo
import com.coocaa.smartscreen.data.channel.events.ProgressEvent
import com.coocaa.smartsdk.SmartApi
import com.coocaa.smartsdk.SmartApiListenerImpl
import com.coocaa.smartsdk.`object`.ISmartDeviceInfo
import com.coocaa.swaiotos.virtualinput.IVirtualInputState
import com.coocaa.swaiotos.virtualinput.IVirtualInputStateListener
import com.coocaa.swaiotos.virtualinput.event.RequestAppInfoEvent
import com.coocaa.tvpi.module.io.HomeUIThread
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import swaiotos.channel.iot.ss.channel.im.IMMessage

/**
 * @Author: yuzhan
 */
object FloatVIStateManager {

    private val TAG = "FloatVI2"
    private var state: BusinessState? = null
    private var stateJson: String? = null
    private var normalDataList: MutableList<SceneConfigBean> = ArrayList<SceneConfigBean>()

    private val listenerSet = HashSet<FloatVIStateChangeListener>()
    private var service: IVirtualInputState? = null
    private var isLoading: Boolean = false
    private lateinit var mContext: Context

    fun addListener(lis: FloatVIStateChangeListener) {
        listenerSet.add(lis)
    }

    fun removeListener(lis: FloatVIStateChangeListener) {
        listenerSet.remove(lis)
    }

    fun init(context: Context) {
        mContext = context
        SmartApi.addListener(connectCallback)
        bind(context)
    }

    private fun bind(context: Context) {
        Log.d(TAG, "start bind VI float service.")
        val intent = Intent()
        intent.setPackage(context.packageName)
        intent.setAction("swaiotos.service.virtualinput.float")
        context.bindService(intent, conn, Context.BIND_AUTO_CREATE)
    }

    private val conn = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected")
            service = null
        }

        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Log.d(TAG, "onServiceConnected")
            service = IVirtualInputState.Stub.asInterface(binder)
            try {
                stateJson = service?.curState
                Log.d(TAG, "stateJson=$stateJson")
                state = if(TextUtils.isEmpty(stateJson) ) null else BusinessState.decode(stateJson)
                Log.d(TAG, "init state=$state")

                val list = service?.configList
                Log.d(TAG, "init list=$list")
                if(list != null && !list.isEmpty()) {
                    normalDataList.addAll(list)
                }

                service?.addListener(serviceListener)
                for(lis in listenerSet) {
                    try {
                        lis.onStateInit()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                binder?.linkToDeath(deathRecipient, 0)
            } catch (e: Exception) {
                Log.d(TAG, "init err=$e")
                e.printStackTrace()
            }
        }

    }

    private val deathRecipient = object: IBinder.DeathRecipient {
        override fun binderDied() {
            Log.d(TAG, "VIFloatService died, try to re bind.")
            if (service != null) {
                service!!.asBinder().unlinkToDeath(this, 0)
                service = null
            }
            bind(mContext)
        }

    }

    private val serviceListener = object : IVirtualInputStateListener.Stub() {
        override fun onStateChanged(businessStateJson: String?) {
            Log.d(TAG, "onReceive : onStateChanged.$businessStateJson")
            state = if(TextUtils.isEmpty(businessStateJson)) null else BusinessState.decode(businessStateJson)
            for(lis in listenerSet) {
                try {
                    lis.onStateChanged(state)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun onProgressLoading(json: String?) {
            isLoading = true
            HomeUIThread.removeTask(overtimeRunnable)
            HomeUIThread.execute(11000, overtimeRunnable)
            for(lis in listenerSet) {
                try {
                    lis.onProgressLoading(json?:"")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun onProgressResult(json: String?) {
            isLoading = false
            HomeUIThread.removeTask(overtimeRunnable)
            for(lis in listenerSet) {
                try {
                    lis.onProgressResult(json?:"")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun onAppInfoLoaded(appInfos: MutableList<AppInfo>?) {
            val event = RequestAppInfoEvent()
            event.appInfoList = appInfos
            EventBus.getDefault().post(event)
        }
    }

    fun getIsLoading():Boolean {
        return isLoading
    }

    //超时需要赋值为false
    fun setIsLoading(result: Boolean) {
        isLoading = result
    }

    fun getCurState() : BusinessState? {
//        BusinessStatePhoneReport.getDefault().getBusinessState()
//        SSConnectManager.getInstance().sendAppState()
        if(service != null) {
            checkState()
//            service?.refreshCurState()
        } else {
            bind(mContext)
        }
        return state
    }

    fun checkState() {
        val newStateJson = service?.curState
        if(!TextUtils.equals(newStateJson, stateJson)) {
            Log.d(TAG, "checkState changed, newState=$newStateJson")
            state = if(TextUtils.isEmpty(newStateJson)) null else BusinessState.decode(newStateJson)
        }
    }

    fun freshState(){
        service?.refreshCurState()
    }


    fun getSceneConfigBean(idOrType: String) : SceneConfigBean? {
        Log.d(TAG, "idOrType=$idOrType")
        for(bean in normalDataList) {
            if (idOrType.toUpperCase() == bean.id.toUpperCase()) {
                return bean
            }
        }
        return null
    }

    fun startConnectDevice() {
        if(service != null) {
            try {
                service!!.startConnectDevice()
            } catch (e: Exception) {
                e.printStackTrace()
                SmartApi.startConnectDevice()
            }
        } else {
            bind(mContext)
            SmartApi.startConnectDevice()
        }
    }

    fun hasHistoryDevice(): Boolean {
        if(service == null) {
            bind(mContext)
            return false
        }
        try {
            return service?.hasHistoryDevice()?: false
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun onProgressEvent(event: ProgressEvent) {

    }

//    private val businessStateListener = object: IBusinessStateListener {
//        override fun onUdpateBusinessState(businessState: BusinessState?) {
//            Log.d(TAG, "onUdpateBusinessState : ${BusinessState.encode(businessState)}")
//            if(isBadOldVersionState(businessState)) {
//                Log.d(TAG, "isBadOldVersionState, ignore it.")
//                return
//            }
//            state = businessState
//
//            for(lis in listenerSet) {
//                try {
//                    lis.onStateChanged(businessState)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }
//    }

//    //丢弃老版本dongle，app类型，非dongle首页的消息
//    private fun isBadOldVersionState(state: BusinessState?): Boolean {
//        Log.d(TAG, "id=${state?.id}, type=${state?.type}")
//        if (TextUtils.isEmpty(state?.id)) {
//            if (!TextUtils.isEmpty(state?.type)) {
//                //老版本
//                val isApp = "APP" == state?.type?.toUpperCase(Locale.US)
//                var isHome = false
//                if(isApp) {
//                    isHome = state?.values?.contains("com.coocaa.dongle.launcher")?: false
//                }
//                Log.d(TAG, "isAppState=$isApp, isHomeState=$isHome")
//                return isApp && !isHome
//            }
//        }
//        return false
//    }

    private fun onDeviceConnectChanged(isConnect: Boolean) {
        for(lis in listenerSet) {
            try {
                lis.onDeviceConnectChanged(isConnect)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val connectCallback = object : SmartApiListenerImpl() {

        override fun onDeviceConnect(deviceInfo: ISmartDeviceInfo?) {
            Log.d(TAG,"receive onConnect")
            freshState()
            HomeUIThread.execute(Runnable {
                onDeviceConnectChanged(true)
            })
        }

        override fun onDeviceDisconnect() {
            Log.d(TAG,"chen receive onDisconnect")
            state = null
            HomeUIThread.execute(Runnable {
                onDeviceConnectChanged(false)
            })
        }
    }

    private val overtimeRunnable = object : Runnable {
        override fun run() {
            isLoading = false
            var progressEvent = ProgressEvent()
            progressEvent.type = IMMessage.TYPE.RESULT
            progressEvent.result = false
            progressEvent.progress = -1

            for(lis in listenerSet) {
                try {
                    lis.onProgressResult(Gson().toJson(progressEvent))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}