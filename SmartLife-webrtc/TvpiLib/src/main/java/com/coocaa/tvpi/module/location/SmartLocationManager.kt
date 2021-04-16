package com.coocaa.tvpi.module.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import com.coocaa.smartscreen.connect.SSConnectManager
import com.coocaa.smartscreen.repository.utils.SmartScreenKit
import com.coocaa.smartsdk.SmartApi
import com.coocaa.tvpi.module.log.PayloadEvent
import com.coocaa.tvpi.module.service.api.SmartDeviceConnectHelper
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.util.*

/**
 * @Author: yuzhan
 */
object SmartLocationManager {

    var locationManager: LocationManager = SmartScreenKit.getContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val geoCoder = Geocoder(SmartScreenKit.getContext(), Locale.CHINESE)
    var bestLocation: Location? = null
    var simpleBestLocation: LocationBean? = null

    val TAG = "SmartLocation";

    fun initAndSubmitLocation(activity: Activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        loopGetLocation()
    }

    fun getLastLocation() : Location? {
        return bestLocation
    }

    private fun loopGetLocation() {
        GlobalScope.launch(Dispatchers.Main) {
            Log.d(TAG, "initLocation in thread=" + Thread.currentThread().name)
            withContext(Dispatchers.IO) {
                getBestLocation()
            }
            submit()
            withContext(Dispatchers.IO) {
                delay(10000)
                Log.d(TAG, "after delayed in thread=" + Thread.currentThread().name)
                GlobalScope.launch(Dispatchers.Main) {
                    //注册network监听
                    removeListener()
                }
            }
        }
    }

    private fun submit() {
        if(bestLocation != null) {
            PayloadEvent.submit("smartscreen.location",
                    "connect_device",
                    simpleBestLocation)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getBestLocation() {
        Log.d(TAG, "getBestLocation in thread=" + Thread.currentThread().name)
        val allProviders = locationManager.allProviders
        var tempBestLocation: Location? = null

        for (provider in allProviders) {
            Log.d(TAG, "try to get location from : $provider")
            //从GPS获取最新的定位信息
            val location = locationManager.getLastKnownLocation(provider)
            if (location != null) {
                printLocation(location, provider) //将最新的定位信息传递给创建的locationUpdates()方法中
                if (tempBestLocation == null || location.accuracy < tempBestLocation.accuracy) {
                    //新的范围更小
                    tempBestLocation = location
                }
            } else {
                Log.d(TAG, "to get location from : $provider return null.")
            }
        }
        if(bestLocation != null && tempBestLocation != null) {
            if(tempBestLocation.accuracy < bestLocation!!.accuracy)
                updateLocation(tempBestLocation)
        } else {
            updateLocation(tempBestLocation)
        }
        if(bestLocation == null) {
            GlobalScope.launch(Dispatchers.Main) {
                //注册network监听
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1f, networkLocationListener)
            }
        }
    }

    private var networkLocationListener = object :  LocationListener{
        override fun onLocationChanged(location: Location) {
            Log.d(TAG, "onLocationChanged from : ${location?.provider}")
            if (location != null) {
                printLocation(location, LocationManager.NETWORK_PROVIDER) //将最新的定位信息传递给创建的locationUpdates()方法中
                if(bestLocation != null && location.accuracy < bestLocation?.accuracy!!) {
                    bestLocation = location
                }
                removeListener()
                updateLocation(location)
                submit()
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {
            Log.d(TAG, "onStatusChanged from$provider, status=$status")
        }

        override fun onProviderEnabled(provider: String) {
        }

        override fun onProviderDisabled(provider: String) {
            locationManager.removeUpdates(this)
        }
    }

    private fun removeListener() {
        try {
            locationManager.removeUpdates(networkLocationListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateLocation(location: Location?) {
        if(location == null) return
        bestLocation = location
        if(simpleBestLocation == null) {
            simpleBestLocation = LocationBean()
        }
        simpleBestLocation?.apply {
            longitude = bestLocation!!.longitude
            latitude = bestLocation!!.altitude
            accuracy = bestLocation!!.accuracy
            provider = location.provider
            address = getLocationAddress(bestLocation!!)

            open_id = SmartApi.getUserInfo()?.open_id ?: ""
            device_active_id = SmartDeviceConnectHelper.getDeviceActiveId(SSConnectManager.getInstance().device)
        }
    }

    private fun getLocationAddress(location: Location): String {
        var addr = ""
        try {
            val addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
            for(a in addresses) {
                Log.i(TAG, "addr: $a")
            }
            val address: Address = addresses[0]
            // Address[addressLines=[0:"广东省深圳市南山区沙河西路3061号"],feature=沙河西路3061号,admin=广东省,sub-admin=桃源街道,locality=深圳市,thoroughfare=沙河西路,
            // postalCode=null,countryCode=CN,countryName=中国,hasLatitude=true,latitude=22.575007,hasLongitude=true,longitude=113.949803,phone=null,url=null,extras=Bundle[mParcelledData.dataSize=92]]
            val maxLine: Int = address.maxAddressLineIndex
            addr = address.getAddressLine(0)
//            if (maxLine >= 2) {
//                address.getAddressLine(1) + address.getAddressLine(2)
//            } else {
//                address.getAddressLine(0)
//            }
            Log.i(TAG, "result addr: $addr")
        } catch (e: Exception) {
            Log.i(TAG, "get addr fail : $e")
            addr = ""
        }
        return addr
    }

    private fun printLocation(location: Location?, provider: String) {
        if(location == null) return

        val sb = StringBuilder("best-location : ")
        sb.apply {
            append(provider)
                    .append(" --> 经度：")
                    .append(location.longitude)
                    .append("，纬度：")
                    .append(location.latitude)
                    .append("，精度：")
                    .append(location.accuracy)
        }
        Log.d(TAG, sb.toString())
    }
}