package com.coocaa.tvpi.module.location

import java.io.Serializable

/**
 * @Author: yuzhan
 */
class LocationBean : Serializable{
    var longitude: Double = 0.0
    var latitude: Double = 0.0
    var accuracy: Float = 0f
    var provider: String = ""
    var address: String = ""

    var device_active_id: String = ""
    var open_id: String = ""

    override fun toString(): String {
        return "经度：$longitude, 纬度：$latitude, 精度: $accuracy, provider=$provider, 地址: $address"
    }
}