package com.coocaa.swaiotos.virtualinput.state

import com.coocaa.smartscreen.businessstate.`object`.BusinessState

/**
 * @Author: yuzhan
 */
interface FloatVIStateChangeListener {
    fun onStateInit()
    fun onStateChanged(businessState: BusinessState?)
    fun onDeviceConnectChanged(isConnect: Boolean)
    fun onProgressLoading(json: String)
    fun onProgressResult(json: String)
}