package com.coocaa.smartscreen.utils;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.device.ConnectRecord;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.RequiresApi;
import swaiotos.channel.iot.ss.device.Device;

/**
 * @ClassName DeviceListUtil
 * @Description 管理设备列表
 * @User wuhaiyuan
 * @Date 2020/12/25
 * @Version TODO (write something)
 */
public class DeviceListManager {

    private static final String TAG = DeviceListManager.class.getSimpleName();

    private static final String DEVICE_LIST_RECORD = "DEVICE_LIST_RECORD";

    private Context mContext;

    private static DeviceListManager mInstance;

    private Gson mGson;

//    private List<ConnectRecord> mConnectRecordList;

    private DeviceListManager() {}

    private void DeviceListManager() {

    }

    public static synchronized DeviceListManager getInstance() {
        if (null == mInstance) {
            mInstance = new DeviceListManager();
        }
        return mInstance;
    }

    public void init(Context context) {
        if (null == mContext) {
            mContext = context;
            mGson = new Gson();
        }
    }

    public List<Device> getSortedDevices(List<Device> dataList) {
        if(dataList == null || dataList.isEmpty()) {//NullPointer保护
            return null;
        }

        //线上数据是准确的了，暂时不用本地保存的
        /*List<ConnectRecord> connectRecordList = getRecord();
        if (null != connectRecordList && connectRecordList.size() > 0
                && null != dataList && dataList.size() > 0) {

            Iterator<ConnectRecord> it = connectRecordList.iterator();
            while (it.hasNext()) {
                ConnectRecord connectRecord = it.next();
                boolean hasUpdateDevice = false;

                for (Device device :
                        dataList) {

                    if (connectRecord.lsid.equals(device.getLsid())) {
                        device.setLastConnectTime(connectRecord.connectTime);
                        hasUpdateDevice = true;
                    }

                }

                if (!hasUpdateDevice) {
                    Log.d(TAG, "getSortedDevices: 没有该设备，移除记录");
                    it.remove();
                }
            }

        }*/

        Device historyDevice = SSConnectManager.getInstance().getHistoryDevice();
        try {
            for (int i = 0; i < dataList.size(); i++) {
                Device device = dataList.get(i);
                if (historyDevice.equals(device)) {
                    //为设备列表里的相同设备重新赋值连接时间
                    device.setLastConnectTime(historyDevice.getLastConnectTime());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(dataList, new Comparator<Device>() {
            @Override
            public int compare(Device o1, Device o2) {
                if (o1.getLastConnectTime() > o2.getLastConnectTime()) {
                    return -1;
                } else if (o1.getLastConnectTime() < o2.getLastConnectTime()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        return dataList;
    }

    public List<ConnectRecord> getRecord() {
        String jsonStr = SpUtil.getString(mContext, DEVICE_LIST_RECORD);
        Log.d(TAG, "getRecord: " + jsonStr);
        if (TextUtils.isEmpty(jsonStr)) {
            return null;
        } else {
            try {
                return mGson.fromJson(jsonStr, new TypeToken<List<ConnectRecord>>() {}.getType());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public void saveRecord(String lsid) {
        Log.d(TAG, "saveRecord: " + lsid);
        String jsonStr = SpUtil.getString(mContext, DEVICE_LIST_RECORD);
        Log.d(TAG, "saveRecord init jsonStr: " + jsonStr);
        if (TextUtils.isEmpty(jsonStr)) {
            Log.d(TAG, "saveRecord: string为空，init lsit");
            initList(lsid);
        } else {
            List<ConnectRecord> connectRecordList = null;

            try {
                connectRecordList = mGson.fromJson(jsonStr, new TypeToken<List<ConnectRecord>>() {}.getType());
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (null != connectRecordList && connectRecordList.size() > 0) {
                //检查是否有重复数据
                for (int i = 0; i < connectRecordList.size(); i++) {
                    if (lsid.equals(connectRecordList.get(i).lsid)) {
                        Log.d(TAG, "saveRecord: 移除相同lsid数据");
                        connectRecordList.remove(i);
                        break;
                    }
                }
                //添加新的记录
                connectRecordList.add(new ConnectRecord(lsid, System.currentTimeMillis()));
                //排序
                Collections.sort(connectRecordList);
                //保存
                SpUtil.putString(mContext, DEVICE_LIST_RECORD, mGson.toJson(connectRecordList));
            } else {
                Log.d(TAG, "saveRecord: list为空，init list");
                initList(lsid);
            }
        }
    }

    private void initList(String lsid) {
        ConnectRecord data = new ConnectRecord(lsid, System.currentTimeMillis());
        List<ConnectRecord> connectRecordList = new ArrayList<>();
        connectRecordList.add(data);
        SpUtil.putString(mContext, DEVICE_LIST_RECORD, mGson.toJson(connectRecordList));
    }

}
