package com.coocaa.tvpi.module.service.api;

import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.connect.callback.ConnectCallback;
import com.coocaa.smartscreen.connect.callback.ConnectCallbackImpl;
import com.coocaa.smartscreen.data.channel.events.ConnectEvent;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.tvpi.module.log.ConnectDeviceEvent;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;

import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;
import swaiotos.channel.iot.ss.session.Session;

/**
 * @Author: yuzhan
 */
public class SmartDeviceConnectHelper {

    private String TAG = "SmartApi";

    private SmartDeviceConnectListener listener;
    private volatile String deviceName;
    private volatile boolean firstTimeConnectHistory = true;
    private volatile long firstTimeConnectHistoryStartTime;

    public interface SmartDeviceConnectListener {
        void onDeviceConnect(ISmartDeviceInfo deviceInfo);
        void onDeviceDisconnect();
        void loginState(int code, String info);
    }

    public SmartDeviceConnectHelper() {
//        Session session = SSConnectManager.getInstance().getConnectSession();
//        String lsid = session.getId();
//        Device device = SSConnectManager.getInstance().
        SSConnectManager.getInstance().addConnectCallback(callback);
        if(SSConnectManager.getInstance().getDevice() != null) {
            onDeviceConnectSuccess(SSConnectManager.getInstance().getDevice(), true, true);
        }
    }

    public void setListener(SmartDeviceConnectListener listener) {
        this.listener = listener;
    }

    public ISmartDeviceInfo getSmartDeviceInfo() {
        if(!SSConnectManager.getInstance().isConnected()) {
            Log.d(TAG, "getSmartDeviceInfo, not connected now.");
            return null;
        }
        return transInfo(SSConnectManager.getInstance().getDevice());
    }

    public boolean isConnected() {
        return SSConnectManager.getInstance().isConnected() && SSConnectManager.getInstance().getDevice() != null;
    }

    private synchronized void onConnectSuccess(ConnectEvent connectEvent, boolean autoConnect, boolean submitLog) {
        Log.d(TAG, "onDeviceConnectSuccess, connectEvent=" + connectEvent);
        onDeviceConnectSuccess(connectEvent.device, autoConnect, submitLog);
    }

    private synchronized void onDeviceConnectSuccess(Device device, boolean autoConnect, boolean submitLog) {
        if(listener != null) {
            listener.onDeviceConnect(transInfo(device));
        }
        if(submitLog) {
            LogSubmit.event("connect_smart_screen_result",
                    LogParams.newParams().append("ss_device_id", device == null ? "unknown" : device.getLsid())
                            .append("ss_device_type", device == null ? "unknown" : device.getZpRegisterType())
                            .append("connect_result", "success")
                            .append("auto_or_manual_connect", autoConnect ? "auto" : "manual")
                            .getParams());
        }
    }

    private ISmartDeviceInfo transInfo(Device device) {
        deviceName = getDeviceName(device);
        ISmartDeviceInfo smartDeviceInfo = new ISmartDeviceInfo();
        smartDeviceInfo.deviceName = deviceName;
        smartDeviceInfo.deviceId = getDeviceActiveId(device);
        smartDeviceInfo.deviceType = device != null ? device.getZpRegisterType() : "";
        smartDeviceInfo.zpRegisterType = device != null ? device.getZpRegisterType() : "";
        smartDeviceInfo.lsid = device != null ? device.getLsid() : "";
        smartDeviceInfo.spaceId = device != null ? device.getSpaceId() : "";
        smartDeviceInfo.source = getDeviceSource(device);

        //添加wifi名称和密码
        Session connectSession = SSConnectManager.getInstance().getTarget();
        if (connectSession != null) {
            Map<String, String> extras = connectSession.getExtras();
            if(extras != null) {
                smartDeviceInfo.ssid = extras.get("ssid");
                smartDeviceInfo.password = extras.get("password");
                String netType = extras.get("net");
                if(netType != null) {
                    smartDeviceInfo.netType = netType.toLowerCase();
                }
            }
        }

        return smartDeviceInfo;
    }

    private synchronized void onDeviceDisconnect() {
        Log.d(TAG, "onDeviceDisconnect");
        deviceName = "";
        if(listener != null) {
            listener.onDeviceDisconnect();
        }
        LogSubmit.event("smart_screen_disconnected",
                LogParams.newParams().append("ss_device_id", "disconnected")
                        .append("ss_device_type", "disconnected")
                        .append("connect_result", "fail")
                        .getParams());
    }

    private ConnectCallback callback = new ConnectCallbackImpl() {
        @Override
        public void onSuccess(ConnectEvent connectEvent) {
            onConnectSuccess(connectEvent, false, true);
        }

        @Override
        public void onFailure(ConnectEvent connectEvent) {
            LogSubmit.event("connect_smart_screen_result",
                    LogParams.newParams().append("ss_device_id", "disconnected")
                            .append("ss_device_type", "disconnected")
                            .append("connect_result", "fail")
                            .getParams());
            LogSubmit.event("connect_smart_screen_fail_msg",
                    LogParams.newParams().append("ss_device_id", "disconnected")
                            .append("ss_device_type", "disconnected")
                            .append("msg", connectEvent.msg)
                            .getParams());
        }

        @Override
        public void onCheckConnect(ConnectEvent connectEvent) {
//            onConnectSuccess(connectEvent, true, false);
        }

        @Override
        public void onHistorySuccess(ConnectEvent connectEvent) {
            if(firstTimeConnectHistory) {
                firstTimeConnectHistory = false;
                Log.d(TAG, "onHistorySuccess ...");
                ConnectDeviceEvent.submit("第一次自动连接历史设备", true, SystemClock.uptimeMillis() - firstTimeConnectHistoryStartTime);
            }
            onConnectSuccess(connectEvent, true, true);
        }

        @Override
        public void onHistoryFailure(ConnectEvent connectEvent) {
            super.onHistoryFailure(connectEvent);
            if(firstTimeConnectHistory) {
                firstTimeConnectHistory = false;
                Log.d(TAG, "onHistoryFailure ...");
                ConnectDeviceEvent.submit("第一次自动连接历史设备", false, SystemClock.uptimeMillis() - firstTimeConnectHistoryStartTime);
            }
        }

        @Override
        public void onHistoryConnecting() {
            if(firstTimeConnectHistory) {
                Log.d(TAG, "onHistoryConnecting ...");
                firstTimeConnectHistoryStartTime = SystemClock.uptimeMillis();
            }
        }

        @Override
        public void onSessionDisconnect(Session session) {
            onDeviceDisconnect();
        }

        @Override
        public void onDeviceReflushUpdate(List<Device> devices) {
            Log.d(TAG, "onDeviceReflushUpdate");
            super.onDeviceReflushUpdate(devices);
            if(!SSConnectManager.getInstance().isConnected()) {
                onDeviceDisconnect();
            }
        }

        @Override
        public void loginState(int code, String info) {
            if(listener != null) {
                listener.loginState(code, info);
            }
        }
    };

    public static String getDeviceName(Device device) {
        return SSConnectManager.getInstance().getDeviceName(device);
    }

    public static String getDeviceActiveId(Device device) {
        if (null == device) {
            return "";
        }
        DeviceInfo deviceInfo = device.getInfo();
        if (null != deviceInfo) {
            switch (deviceInfo.type()) {
                case TV:
                    TVDeviceInfo tvDeviceInfo = (TVDeviceInfo) deviceInfo;
                    return tvDeviceInfo.activeId;
            }
        }
        return "";
    }

    public static String getDeviceSource(Device device) {
        if (null == device) {
            return "";
        }
        DeviceInfo deviceInfo = device.getInfo();
        if (null != deviceInfo) {
            switch (deviceInfo.type()) {
                case TV:
                    TVDeviceInfo tvDeviceInfo = (TVDeviceInfo) deviceInfo;
                    return tvDeviceInfo.mMovieSource;
            }
        }
        return "";
    }
}
