package com.coocaa.tvpi.module.remote.floatui

import android.app.Service
import android.content.Intent
import android.os.DeadObjectException
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.JSON
import com.coocaa.smartscreen.businessstate.BusinessStatePhoneReport
import com.coocaa.smartscreen.businessstate.IBusinessStateListener
import com.coocaa.smartscreen.businessstate.`object`.BusinessState
import com.coocaa.smartscreen.connect.SSConnectManager
import com.coocaa.smartscreen.connect.service.MsgAppInfoEventObserver
import com.coocaa.smartscreen.connect.service.MsgProgressEventObserver
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean
import com.coocaa.smartscreen.data.channel.AppInfo
import com.coocaa.smartscreen.data.channel.CmdData
import com.coocaa.smartscreen.data.channel.events.ProgressEvent
import com.coocaa.swaiotos.virtualinput.IVirtualInputState
import com.coocaa.swaiotos.virtualinput.IVirtualInputStateListener
import com.coocaa.swaiotos.virtualinput.statemachine.SceneControllerConfig
import com.coocaa.tvpi.module.connection.ScanActivity2
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * @Author: yuzhan
 */
class VirtualInputFloatService: Service() {

    private val TAG = "FloatVI2"
    private var state: BusinessState? = null
    private var stateJson: String? = null
    private var normalDataList: MutableList<SceneConfigBean> = ArrayList<SceneConfigBean>()
    private var listenerSet = HashSet<IVirtualInputStateListener>()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "VirtualInputFloatService onCreate")
        BusinessStatePhoneReport.getDefault().getBusinessState()
        sendAppState()
        BusinessStatePhoneReport.getDefault().addListener(businessStateListener)

        val list = SceneControllerConfig.getInstance().sceneConfigList
        Log.d(TAG, "sceneConfigList=$list")
        if(list != null && list.isNotEmpty()) {
            normalDataList.addAll(list)
        }
        MsgProgressEventObserver.setObserver(object : MsgProgressEventObserver.IProgressEventObserver {
            override fun onProgressLoading(event: ProgressEvent?) {
                val iter = listenerSet.iterator()
                while (iter.hasNext()) {
                    val listener = iter.next()
                    try {
                        listener.onProgressLoading(JSON.toJSONString(event))
                    } catch (e: DeadObjectException) {
                        Log.d(TAG, "remove dead listener$listener")
                        iter.remove()
                        e.printStackTrace()
                    } catch (e: Exception) {

                    }
                }
            }

            override fun onProgressResult(event: ProgressEvent?) {
                val iter = listenerSet.iterator()
                while (iter.hasNext()) {
                    val listener = iter.next()
                    try {
                        listener.onProgressResult(JSON.toJSONString(event))
                    } catch (e: DeadObjectException) {
                        Log.d(TAG, "remove dead listener$listener")
                        iter.remove()
                        e.printStackTrace()
                    } catch (e: Exception) {

                    }
                }
            }
        })

        MsgAppInfoEventObserver.setObserver(object : MsgAppInfoEventObserver.IAppInfoEventObserver {
            override fun onAppInfoLoaded(appInfo: MutableList<AppInfo>?) {
                Log.d(TAG, "onAppInfoLoaded....$appInfo")
                val iter = listenerSet.iterator()
                while (iter.hasNext()) {
                    val listener = iter.next()
                    try {
                        listener.onAppInfoLoaded(appInfo)
                    } catch (e: DeadObjectException) {
                        Log.d(TAG, "remove dead listener$listener")
                        iter.remove()
                        e.printStackTrace()
                    } catch (e: Exception) {

                    }
                }
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? {
        return stub
    }

    private val businessStateListener = object: IBusinessStateListener {
        override fun onUdpateBusinessState(businessState: BusinessState?) {
            var json: String? = null
            if(businessState != null) {
                json = BusinessState.encode(businessState)
            }
            Log.d(TAG, "service onUdpateBusinessState : $json")
            if(isBadOldVersionState(businessState)) {
                Log.d(TAG, "isBadOldVersionState, ignore it.")
                return
            }
            stateJson = json
            state = businessState
            val iter = listenerSet.iterator()
            while(iter.hasNext()) {
                val listener = iter.next()
                try {
                    listener.onStateChanged(stateJson)
                } catch (e: DeadObjectException) {
                    Log.d(TAG, "remove dead listener$listener")
                    iter.remove()
                    e.printStackTrace()
                } catch (e: Exception) {

                }
            }
        }
    }

    private val stub = object: IVirtualInputState.Stub() {
        override fun startConnectDevice() {
            Log.d(TAG, "startConnectDevice")
            ScanActivity2.start(this@VirtualInputFloatService)
        }

        override fun hasHistoryDevice(): Boolean {
            return SSConnectManager.getInstance().getHistoryDevice() != null
        }

        override fun refreshCurState() {
            Log.d(TAG, "refreshCurState")
            BusinessStatePhoneReport.getDefault().getBusinessState()
            sendAppState()
        }

        override fun addListener(lis: IVirtualInputStateListener?) {
            Log.d(TAG, "addListener=$lis")
            if(lis != null) {
                listenerSet.add(lis)
                try {
                    lis.onStateChanged(stateJson)
                } catch (e: Exception) {

                }
            }
        }

        override fun removeListener(lis: IVirtualInputStateListener?) {
            Log.d(TAG, "removeListener=$lis")
            if(lis != null) {
                listenerSet.remove(lis)
            }
        }

        override fun getConfigList(): MutableList<SceneConfigBean> {
            return normalDataList
        }

        override fun getCurState(): String {
            return stateJson?:""
        }
    }

    //丢弃老版本dongle，app类型，非dongle首页的消息
    private fun isBadOldVersionState(state: BusinessState?): Boolean {
        Log.d(TAG, "id=${state?.id}, type=${state?.type}")
        if (TextUtils.isEmpty(state?.id)) {
            if (!TextUtils.isEmpty(state?.type)) {
                //老版本
                val isApp = "APP" == state?.type?.toUpperCase(Locale.US)
                var isHome = false
                if(isApp) {
                    isHome = state?.values?.contains("com.coocaa.dongle.launcher")?: false
                }
                Log.d(TAG, "isAppState=$isApp, isHomeState=$isHome")
                return isApp && !isHome
            }
        }
        return false
    }


    //请求appstate
    fun sendAppState() {
        val data = CmdData("getAppState", CmdData.CMD_TYPE.STATE.toString(), "")
        val cmd = data.toJson()
        SSConnectManager.getInstance().sendTextMessage(cmd, SSConnectManager.TARGET_APPSTATE)
    }
}