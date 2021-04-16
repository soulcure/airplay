package com.coocaa.smartscreen;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.skyworth.bleclient.BLEClient;
import com.skyworth.bleclient.BleDeviceInfo;
import com.skyworth.bleclient.BlePdu;
import com.skyworth.bleclient.BluetoothClientCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import swaiotos.channel.iot.ss.SSAdminChannel;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;


public class BleClientManager {
    private static final String TAG = "bleClient";
    private static final int HEART_BEAT_INTERVAL = 25; //心跳间隔25秒
    private static final int HANDLER_THREAD_INIT_CONFIG_START = 1;

    private static final boolean openBleRange = false;   //是否打开蓝牙围栏

    private BLEClient bleClient;
    private SSAdminChannel ssChannel;
    private InitDevicesCallBack initDevicesCallBack;

    private Context mContext;

    /**
     * pref文件名定义
     */
    public static final String SHARED_PREFERENCES = "device_connect";

    public static final String ALL_DEVICES = "all_devices";


    private ScheduledExecutorService heartBeatScheduled;


    private List<Device> mDeviceList;

    private List<ScanDevice> scanDeviceList = new ArrayList<>();


    private SessionCallBack sessionCallBack;
    private WifiCallBack wifiCallBack;

    private ProcessHandler mProcessHandler;  //子线程Handler
    private Handler mHandler;

    /**
     * callback start
     */
    private List<ScanCallBack> scanCallBacks = new ArrayList<>();

    private static class ScanDevice {
        String mac;
        long scanTime;
        String sid;

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (obj instanceof ScanDevice) {
                ScanDevice target = (ScanDevice) obj;
                return this.mac.equals(target.mac);
            }
            return false;
        }
    }


    public void addScanCallBack(ScanCallBack callback) {
        scanCallBacks.add(callback);
    }

    public void removeScanCallBack(ScanCallBack callback) {
        scanCallBacks.remove(callback);
    }


    private static BleClientManager instance;

    public interface InitDevicesCallBack {
        void onResult(List<Device> list);
    }


    public interface SessionCallBack {
        void onSession(Device<TVDeviceInfo> device);
    }


    public interface ScanCallBack {
        void onUpdateDevices(List<Device> list);

        void onStateChange(BluetoothClientCallback.DeviceState res);
    }

    public interface WifiCallBack {
        void onProgress(int errCode, int status, String sid, String bindCode);
    }


    private BluetoothClientCallback callback = new BluetoothClientCallback() {
        @Override
        public void onMessageShow(BlePdu blePdu) {
            try {
                String msg = new String(blePdu.body);
                Log.e("yao", "onMessageShow---" + msg);

                JSONObject jsonObject = new JSONObject(msg);
                int code = jsonObject.optInt("code");
                String proto = jsonObject.optString("proto");

                if (code == 0
                        && !TextUtils.isEmpty(proto)
                        && proto.equals("ConfigureWiFi")) {
                    int status = jsonObject.optInt("status");
                    int error = jsonObject.optInt("error");
                    String sid = jsonObject.optString("sid");
                    String bindCode = jsonObject.optString("bindCode");

                    if (wifiCallBack != null) {
                        wifiCallBack.onProgress(error, status, sid, bindCode);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                        if (status == 2  //配网成功
                                && !TextUtils.isEmpty(bindCode)) {
                            Log.e("yao", "onMessageShow---bindCode:" + bindCode);
                            bleClient.disConnect();
                            Log.d(TAG, "配网成功 断开蓝牙");
                        } /*else if (status == -1) {  //配网失败
                            bleClient.disConnect();
                            Log.d(TAG, "配网失败 断开蓝牙");
                        }*/

                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStateChange(DeviceState res) {
            Log.d(TAG, "onStateChange: 有设置回调 " + scanCallBacks.size());
            for (ScanCallBack scanCallBack : scanCallBacks) {
                scanCallBack.onStateChange(res);
            }
        }

        @Override
        public void onScanResult(String mac) {
            long curTime = System.currentTimeMillis();
            ScanDevice device = new ScanDevice();
            device.mac = mac;
            device.scanTime = curTime;
            Log.e(TAG, "onScanResult=mac" + mac);
            String sid = isTempBind(mac);

            if (!TextUtils.isEmpty(sid)) {
                device.sid = sid;
                Log.e(TAG, "onScanResult Temp Bind dongle=" + mac);
                if (scanDeviceList.contains(device)) {
                    int index = scanDeviceList.indexOf(device);
                    scanDeviceList.get(index).scanTime = curTime;
                } else {
                    scanDeviceList.add(device);
                }
            }
        }

        @Override
        public void onScanList(Collection<BleDeviceInfo> collection) {

        }
    };

    public static BleClientManager instance(Context context) {
        if (instance == null) {
            instance = new BleClientManager(context);
        }
        return instance;
    }


    private BleClientManager(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        initHandler();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bleClient = new BLEClient(context, callback, mProcessHandler);
        }
        //initTempDevice();
    }


    public void setSsChannel(SSAdminChannel channel, InitDevicesCallBack callBack) {
        if (channel != null) {
            ssChannel = channel;
            initDevicesCallBack = callBack;
            initDevicesOnlineStatus();
        }
    }

    //获取设备列表，强制刷新在线状态
    public synchronized void initDevicesOnlineStatus() {
        mProcessHandler.post(new Runnable() {
            @Override
            public void run() {
                List<Device> list = null;
                try {
                    list = ssChannel.getDeviceAdminManager().updateDeviceList();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (initDevicesCallBack != null) {
                    final List<Device> finalList = list;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            initDevicesCallBack.onResult(finalList);
                        }
                    });
                }
            }
        });
    }


    public void setConfigWifi(String info, WifiCallBack callBack) {
        wifiCallBack = callBack;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bleClient.sendMsg(info, BlePdu.TEMP_PROTO);
        }
    }


    public void reqSession(String info, SessionCallBack callBack) {
        sessionCallBack = callBack;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bleClient.sendMsg(info, BlePdu.TEMP_PROTO);
        }
    }


    public void startScan(String mac, ScanCallBack callBack) {
        scanCallBacks.add(callBack);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                bleClient.disConnect();
                bleClient.stopScan();
                bleClient.startScan(mac);

                if (openBleRange) {
                    startHeartBeat();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void startScan() {
        Log.d(TAG, "---startScan-");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && openBleRange) {
            try {
                bleClient.disConnect();
                bleClient.stopScan();
                bleClient.startScan();

                startHeartBeat();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isSupport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bleClient != null) {
                return bleClient.isSupport();
            }
        }
        return false;
    }


    public void stopScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bleClient != null) {
                bleClient.stopScan();
            }
            stopHeartBeat();
        }
    }


    public void setTargetMac(String mac) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bleClient != null) {
                bleClient.setTargetMac(mac);
            }
        }
    }


    public boolean isConnected(String mac) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bleClient != null) {
                return bleClient.isConnected(mac);
            }
        }
        return false;
    }


    public void disConnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bleClient.disConnect();
            stopHeartBeat();
        }
    }


    public void sendCmd(String msg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bleClient.sendMsg(msg, BlePdu.TEMP_CMD);
        }
    }


    /**
     * 判断临时设备是否在蓝牙围栏内
     *
     * @param sid
     * @return
     */
    public boolean isBleRange(String sid) {
        boolean res = false;
        if (openBleRange) {
            for (ScanDevice device : scanDeviceList) {
                if (!TextUtils.isEmpty(device.sid)
                        && !TextUtils.isEmpty(device.sid)
                        && sid.equals(device.sid)) {
                    res = true;
                    break;
                }
            }
        } else {
            res = true;
            /*try {
                String mySsid = connectedWifi(mContext);
                Session target = ssChannel.getSessionManager().getConnectedSession();
                if (target != null && target.getId().equals(sid)) {
                    String ssid = target.getExtra("ssid");
                    if (!TextUtils.isEmpty(ssid)
                            && !TextUtils.isEmpty(mySsid)
                            && ssid.equals(mySsid)) {
                        res = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                res = true;
            }*/
        }

        return res;
    }


    private String isTempBind(String mac) {
        String sid = null;
        if (mDeviceList == null) {
            try {
                mDeviceList = ssChannel.getDeviceAdminManager().updateDeviceList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mDeviceList != null) {
            for (Device<TVDeviceInfo> device : mDeviceList) {
                if (device.isTempDevice() && device.getInfo().MAC.equalsIgnoreCase(mac)) {
                    sid = device.getLsid();
                    break;
                }
            }
        }

        return sid;
    }


    private void scanWork(boolean needCallBack) {
        if (!openBleRange || scanDeviceList.size() == 0) {
            return;
        }

        final List<Device> deviceList = ssChannel.getDeviceAdminManager().updateDeviceList();

        Log.d(TAG, "ble scan scanWork");
        long curTime = System.currentTimeMillis();

        boolean isChange = false;
        for (Device<TVDeviceInfo> device : deviceList) {
            if (device.isTempDevice()) {
                TVDeviceInfo info = device.getInfo();
                for (ScanDevice item : scanDeviceList) {
                    if (info.MAC.equalsIgnoreCase(item.mac)) {
                        long time = curTime - item.scanTime;

                        int preStatus = device.getBleStatus();
                        if (time < HEART_BEAT_INTERVAL * 1000) {
                            if (preStatus == 0) {
                                Log.d(TAG, "temp device mac:" + info.MAC + "---上线");
                                device.setBleStatus(1); //0:下线 1:上线
                                isChange = true;
                            }
                            break;
                        } else {
                            if (preStatus == 1) {
                                Log.d(TAG, "temp device mac:" + info.MAC + "---下线");
                                device.setBleStatus(0);  //设置不在线
                                isChange = true;
                            }
                        }
                    }
                }

            }

        }

        if (needCallBack && isChange) {  //状态发送了改变 才回调
            Log.d(TAG, "scanWork will callback---");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (ScanCallBack scanCallBack : scanCallBacks) {
                        scanCallBack.onUpdateDevices(deviceList);
                    }
                }
            });
        }
    }


    /**
     * 开始心跳
     */
    private void startHeartBeat() {
        if (heartBeatScheduled == null) {
            Log.d(TAG, "ble Scan startHeartBeat");
            heartBeatScheduled = Executors.newScheduledThreadPool(2);
            heartBeatScheduled.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    heatBeat();
                }
            }, HEART_BEAT_INTERVAL, HEART_BEAT_INTERVAL, TimeUnit.SECONDS);
        }
    }

    /**
     * 停止心跳
     */
    private void stopHeartBeat() {
        if (heartBeatScheduled != null
                && !heartBeatScheduled.isShutdown()) {
            Log.d(TAG, "ble Scan stopHeartBeat");
            heartBeatScheduled.shutdown();
        }
    }

    /**
     * 心跳协议请求
     */
    private void heatBeat() {
        scanWork(true);
    }

    public List<Device> getDeviceList() {
        List<Device> deviceList = null;
        try {
            deviceList = ssChannel.getDeviceAdminManager().getDeviceOnlineStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (openBleRange) {
            for (Device item : deviceList) {
                String nSid = item.getLsid();
                int nStatus = item.getStatus();

                boolean isNew = true;
                for (int i = 0; i < deviceList.size(); i++) {
                    if (!openBleRange) {
                        deviceList.get(i).setBleStatus(1);
                    }

                    String oSid = deviceList.get(i).getLsid();
                    if (nSid.equals(oSid)) {
                        deviceList.get(i).setStatus(nStatus);
                        isNew = false;
                        break;
                    }
                }
                if (isNew) {
                    deviceList.add(item);
                }
            }

            scanWork(false);
        }

        return deviceList;

    }


    public List<Device> getTempDeviceList() {
        List<Device> tempDeviceList = new ArrayList<>();
        try {
            List<Device> deviceList = ssChannel.getDeviceAdminManager().getDeviceOnlineStatus();
            for (Device device : deviceList) {
                if (device.isTempDevice()) {
                    tempDeviceList.add(device);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempDeviceList;
    }


    public void removeDevice(String sid) {

    }

    public void addDevice(Device<TVDeviceInfo> device) {

    }


    /**
     * 线程初始化
     */
    private void initHandler() {
        if (mProcessHandler == null) {
            HandlerThread handlerThread = new HandlerThread(
                    "handler looper Thread");
            handlerThread.start();
            mProcessHandler = new ProcessHandler(handlerThread.getLooper());
        }
    }

    /**
     * 子线程handler,looper
     *
     * @author Administrator
     */
    private class ProcessHandler extends Handler {

        public ProcessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_THREAD_INIT_CONFIG_START:
                    break;
                default:
                    break;
            }

        }

    }


    /**
     * 是否已连接指定wifi
     */
    public static String connectedWifi(Context context) {
        String ssid = null;
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            switch (wifiInfo.getSupplicantState()) {
                case AUTHENTICATING:
                case ASSOCIATING:
                case ASSOCIATED:
                case FOUR_WAY_HANDSHAKE:
                case GROUP_HANDSHAKE:
                case COMPLETED:
                    ssid = wifiInfo.getSSID().replace("\"", "");
            }
        }

        return ssid;
    }

}
