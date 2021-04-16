package swaiotos.channel.iot.ss.device;

import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import swaiotos.channel.iot.ss.SSChannelService;
import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.controller.ControllerServer;
import swaiotos.channel.iot.ss.controller.DeviceState;
import swaiotos.channel.iot.ss.controller.DeviceStateManager;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.ss.server.data.BindLsidData;
import swaiotos.channel.iot.ss.server.data.DeviceData;
import swaiotos.channel.iot.ss.server.data.DeviceStatusData;
import swaiotos.channel.iot.ss.server.data.FlushDeviceStatus;
import swaiotos.channel.iot.ss.server.data.JoinToLeaveData;
import swaiotos.channel.iot.ss.server.data.OnlineData;
import swaiotos.channel.iot.ss.server.http.SessionHttpService;
import swaiotos.channel.iot.ss.server.http.api.HttpApi;
import swaiotos.channel.iot.ss.server.http.api.HttpResult;
import swaiotos.channel.iot.ss.server.http.api.HttpSubscribe;
import swaiotos.channel.iot.ss.server.http.api.HttpThrowable;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.ss.server.utils.StringUtils;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.ThreadManager;

/**
 * @ClassName: SessionManager
 * @Author: lu
 * @CreateDate: 2020/3/27 4:24 PM
 * @Description:
 */
public class DeviceManagerServerImpl implements DeviceManagerServer {
    private static final String TAG = "iot-channel";
    private final Map<String, Device> bindDeviceMap = new ConcurrentHashMap<>();

    private final List<OnDeviceChangedListener> deviceChangedList = new CopyOnWriteArrayList<>();
    private final List<OnDeviceBindListener> deviceBindList = new CopyOnWriteArrayList<>();
    private final List<OnDeviceInfoUpdateListener> deviceDeviceInfoList = new CopyOnWriteArrayList<>();
    private final List<OnDevicesReflushListener> devicesReflushList = new CopyOnWriteArrayList<>();
    private SSContext mSSContext;
    private Device currentDevice = null;

    enum TYPE {
        ADD,
        REMOVE
    }

    public DeviceManagerServerImpl(SSContext ssContext) {
        mSSContext = ssContext;
        mSSContext.getController().addOnDeviceAliveChangeListener(mListener);
        mSSContext.getController().addOnDeviceBindStatusListener(bindListener);
        mSSContext.getController().getDeviceStateManager().addOnDeviceStateChangeListener(stateListener);
    }


    @Override
    public boolean addDevice(Device device) {
        if (null == device) {
            return false;
        }
        synchronized (bindDeviceMap) {
            String sid = device.getLsid();
            if (!TextUtils.isEmpty(sid)) {
                bindDeviceMap.put(sid, device);
                updateDeviceSP(TYPE.ADD, device);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeDevice(String lsid) {
        synchronized (bindDeviceMap) {
            if (bindDeviceMap.containsKey(lsid)) {
                Device tempDevice = bindDeviceMap.get(lsid);
                bindDeviceMap.remove(lsid);
                updateDeviceSP(TYPE.REMOVE, tempDevice);

                if (currentDevice != null && !TextUtils.isEmpty(currentDevice.getLsid())
                        && currentDevice.getLsid().equals(lsid)) {  //add by wyh
//                    mSSContext.getSessionManager().clearConnectedSession();
                    currentDevice = null;
                }

                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateDeviceState(DeviceState state) {
        try {
            String lsid = state.getLsid();
            synchronized (bindDeviceMap) {
                if (bindDeviceMap.containsKey(lsid)) {
                    Device d = bindDeviceMap.get(lsid);
                    d.setDeviceState(state);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateDeviceSP(TYPE type, Device device) {
        String json = ShareUtls.getInstance(SSChannelService.getContext()).getString(Constants.COOCAA_PREF_DEVICEs_LIST, "");
        if (!TextUtils.isEmpty(json))
            getSharedPreferences(json);

        switch (type) {
            case ADD:
                bindDeviceMap.put(device.getLsid(), device);

                Map<String, String> revertDeviceToString = new HashMap<>();
                for (String key : bindDeviceMap.keySet()) {
                    revertDeviceToString.put(key, bindDeviceMap.get(key).encode());
                }

                String res = JSONObject.toJSONString(revertDeviceToString);
                AndroidLog.androidLog("--res:" + res);
                ShareUtls.getInstance(SSChannelService.getContext()).putString(Constants.COOCAA_PREF_DEVICEs_LIST, res);
                break;
            case REMOVE:
                bindDeviceMap.remove(device.getLsid());
                Map<String, String> revertDeviceToString2 = new HashMap<>();
                for (String key : bindDeviceMap.keySet()) {
                    revertDeviceToString2.put(key, bindDeviceMap.get(key).encode());
                }
                String res2 = JSONObject.toJSONString(revertDeviceToString2);
                AndroidLog.androidLog("--res2:" + res2);
                ShareUtls.getInstance(SSChannelService.getContext()).putString(Constants.COOCAA_PREF_DEVICEs_LIST, res2);
                break;
        }
    }

    private void resetDeviceSP(Map<String, Device> map) {

        //设备数为空的时候，缓存指空
        if (map == null || map.size() == 0) {
            try {
                mSSContext.getSessionManager().clearConnectedSessionByUser();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ShareUtls.getInstance(SSChannelService.getContext()).putString(Constants.COOCAA_PREF_DEVICEs_LIST, "");
            return;
        }

        Log.d(TAG, "-----resetDeviceSP:" + map.size());
        Map<String, String> jsonMap = null;
        try {
            String json = ShareUtls.getInstance(SSChannelService.getContext()).getString(Constants.COOCAA_PREF_DEVICEs_LIST, "");
            if (!TextUtils.isEmpty(json)) {
                jsonMap = JSONObject.parseObject(json, Map.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean isExitConnectSidInDevices = false;
        Session connectSession = null;
        try {
            connectSession = mSSContext.getSessionManager().getConnectedSession();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> revertDeviceToString = new HashMap<>();
        for (String key : map.keySet()) {
            revertDeviceToString.put(key, map.get(key).encode());
            try {
                //接口获取列表与connectSession的sid进行对比，存在不处理  不存在clear connectSession
                if (connectSession != null && !TextUtils.isEmpty(connectSession.getId())
                        && connectSession.getId().equals(key)) {
                    isExitConnectSidInDevices = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //配置文件与接口获取列表对比,配置的设备不存在则unBind
          if (jsonMap != null && jsonMap.size() > 0) {
                boolean isExitInHttpDevices = false;
                String configKey = "";
                for (String jsonKey : jsonMap.keySet()) {
                    if (key.equals(jsonKey)) {
                        isExitInHttpDevices = true;
                    }
                    configKey = jsonKey;
                }
                if (!isExitInHttpDevices) {
                    bindListener.onDeviceUnBind(configKey);
                }
            }
        }

        if (!isExitConnectSidInDevices) {
            try {
                mSSContext.getSessionManager().clearConnectedSessionByUser();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String res = JSONObject.toJSONString(revertDeviceToString, SerializerFeature.WriteMapNullValue);
        Log.d(TAG, "DeviceManagerServerImpl  resetDeviceSP  res:" + res);
        ShareUtls.getInstance(SSChannelService.getContext()).putString(Constants.COOCAA_PREF_DEVICEs_LIST, res);
    }

    private void updateDeviceStatus(String sid, int status) {
        synchronized (bindDeviceMap) {
            if (bindDeviceMap.containsKey(sid)) {
                Log.d(TAG, "updateDeviceStatus  sid:" + sid + " status:" + status);
                Device d = bindDeviceMap.get(sid);
                d.setStatus(status);
                bindDeviceMap.put(sid, d);
            }
        }
    }

    @Override
    public List<Device> getDevices() throws Exception {
        if (bindDeviceMap.isEmpty()) {
            String json = ShareUtls.getInstance(SSChannelService.getContext()).getString(Constants.COOCAA_PREF_DEVICEs_LIST, "");
            Log.d(TAG, "DeviceManagerServerImpl  getDevices  json:" + json);
            if (TextUtils.isEmpty(json))
                return new ArrayList<>();
            getSharedPreferences(json);
        }

        synchronized (bindDeviceMap) {
            return new ArrayList<>(bindDeviceMap.values());
        }
    }

    private void getSharedPreferences(String json) {
        try {
            Map<String, String> map = JSONObject.parseObject(json, Map.class);
            if (map == null || map.size() <= 0)
                return;
            for (String key : map.keySet()) {
                Device device = new Device();
                device.parse(map.get(key));
                bindDeviceMap.put(key, device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Device> getDeviceOnlineStatus() throws Exception {
        if (bindDeviceMap.isEmpty()) {
            String json = ShareUtls.getInstance(SSChannelService.getContext()).getString(Constants.COOCAA_PREF_DEVICEs_LIST, "");
            Log.d(TAG, "DeviceManagerServerImpl  getDevices  json:" + json);
            if (TextUtils.isEmpty(json))
                return new ArrayList<>();
            getSharedPreferences(json);
        }

        mSSContext.post(new Runnable() {
            @Override
            public void run() {

                if (bindDeviceMap.size() > 0) {
                    List<String> list = new ArrayList<>(bindDeviceMap.keySet());
                    String[] sids = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        sids[i] = list.get(i);
                    }
                    FlushDeviceStatus flushDeviceStatus = new FlushDeviceStatus();
                    flushDeviceStatus.setScreen_ids(sids);
                    mSSContext.getServerInterface().queryFlushDeviceStatus(flushDeviceStatus, new HttpSubscribe<OnlineData>() {
                        @Override
                        public void onSuccess(OnlineData result) {
                            try {
                                Log.d(TAG, "result:" + result);
                                if (result != null && result.getCode() != null && !TextUtils.isEmpty(result.getCode())
                                        && result.getData() != null && result.getData().size() > 0) {
                                    List<DeviceStatusData> onlineData = result.getData();
                                    for (int i = 0; i < onlineData.size(); i++) {
                                        DeviceStatusData deviceStatusData = onlineData.get(i);
                                        for (String key : bindDeviceMap.keySet()) {
                                            if (!TextUtils.isEmpty(key) && deviceStatusData != null
                                                    && !TextUtils.isEmpty(deviceStatusData.getScreen_id())
                                                    && key.equals(deviceStatusData.getScreen_id())) {
                                                Log.d(TAG, " deviceStatusData.getOnline_status():" + deviceStatusData.getOnline_status());
                                                if (deviceStatusData.getOnline_status() == 1) {
                                                    //兼容 sse的丢失消息情况 在线
                                                    mListener.onDeviceOnline(key);
                                                } else {
                                                    //兼容 sse的丢失消息情况 离线
                                                    Session connectedSession = mSSContext.getSessionManager().getConnectedSession();
                                                    if (connectedSession != null && connectedSession.getId().equals(key)) {
                                                        mSSContext.getIMChannel().reOpenLocalClient(connectedSession);
                                                    }

                                                    mListener.onDeviceOffline(key);
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onError(HttpThrowable error) {

                        }
                    });
                }
            }
        });

        synchronized (bindDeviceMap) {
            return new ArrayList<>(bindDeviceMap.values());
        }
    }

    @Override
    public Device getCurrentDevice() throws Exception {
        return currentDevice;
    }

    @Override
    public Session getLocalSessionBySid(String sid) throws Exception {
        Device device = bindDeviceMap.get(sid);
        if (device != null) {
            DeviceState deviceState = device.getDeviceState();
            if (deviceState != null) {
                return deviceState.toSession();
            }
        }
        return null;
    }

    @Override
    public void startBind(final String accessToken, final String bindCode, final OnBindResultListener listener, final long time) throws Exception {
        Log.d(TAG, "accessToken:" + accessToken + " bindCode:" + bindCode + " time:" + time);
        String accessTokenChannel = accessToken;
        if (TextUtils.isEmpty(accessToken))
            accessTokenChannel = mSSContext.getSmartScreenManager().getLSIDManager().getLSIDInfo().accessToken;
        Log.d(TAG, "accessToken1:" + accessToken + " bindCode1:" + bindCode + " time1:" + time);
        if (StringUtils.isEmpty(bindCode) || StringUtils.isEmpty(accessTokenChannel)) {
            if (listener != null)
                listener.onFail(bindCode, Constants.COOCAA_TYPE_10, "bindcode or accesstoken is null,please check it!");
            return;
        }
        final AtomicBoolean atomicBoolean = new AtomicBoolean(true);
        Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
            @Override
            public void run() {

                if (atomicBoolean.get()) {
                    Log.d(TAG, "---------end time");
                    atomicBoolean.compareAndSet(true, false);
                    listener.onFail(bindCode, Constants.COOCAA_TYPE_11, "set time " + time + " but overtime!");
                }
            }
        }, time, TimeUnit.MILLISECONDS);

        submit(bindCode, listener, accessTokenChannel, atomicBoolean, time);
    }

    @Override
    public void unBindDevice(String accessToken, final String lsid, int type, final unBindResultListener listener) throws Exception {
        String accessTokenChannel = accessToken;
        if (TextUtils.isEmpty(accessToken)) {
            accessTokenChannel = mSSContext.getSmartScreenManager().getLSIDManager().getLSIDInfo().accessToken;
        }

        if (StringUtils.isEmpty(lsid) || StringUtils.isEmpty(accessTokenChannel)) {
            if (listener != null)
                listener.onFail(lsid, "-11", "lsid or accesstoken is null,please check it!");
            return;
        }
        mSSContext.getServerInterface().unBindDevices(accessTokenChannel, lsid, String.valueOf(type), new HttpSubscribe<HttpResult<String>>() {
            @Override
            public void onSuccess(HttpResult<String> result) {
                if (result != null && result.code.equals(Constants.COOCAA_SUCCESS)) {
                    try {
                        removeDevice(lsid);
                        //清除缓存
                        mSSContext.getSessionManager().clearHandlerConnectSession(lsid);

                        Session connectSession = mSSContext.getSessionManager().getConnectedSession();
                        if (connectSession != null && connectSession.getId() != null && connectSession.getId().equals(lsid))
                            mSSContext.getController().disconnect(connectSession);
                        else {
                            //如果没有连接session：解绑需要leaveRoom
                            ThreadManager.getInstance().ioThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mSSContext.getServerInterface().leaveRoom(mSSContext.getAccessToken(), "0");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    AndroidLog.androidLog("removeDevice success than callback unbind success!");
                    if (listener != null)
                        listener.onSuccess(lsid);
                } else {
                    if (listener != null) {
                        //出现错误码20005（sid不存在）的时候，返回成功
                        if (result != null && result.code.equals(Constants.COOCAA_TYPE_20005)) {
                            listener.onSuccess(lsid);
                        } else {
                            listener.onFail(lsid, "-12", "data error!!!");
                        }
                    }
                }
            }

            @Override
            public void onError(HttpThrowable error) {
                if (listener != null)
                    listener.onFail(lsid, String.valueOf(error.getErrCode()), error.getErrMsg());
                updateLsid(null, 0);
            }
        });
    }

    @Override
    public void startTempBindDirect(String accessToken, final String uniQueId, int type, final OnBindResultListener listener, final long time) throws Exception {
        AndroidLog.androidLog("startTempBindDirect accessToken:" + accessToken + " uniQueId:" + uniQueId + " type:" + type + " time:"+time);
        String accessTokenChannel = accessToken;
        if (TextUtils.isEmpty(accessToken))
            accessTokenChannel = mSSContext.getSmartScreenManager().getLSIDManager().getLSIDInfo().accessToken;
        if (TextUtils.isEmpty(uniQueId) || TextUtils.isEmpty(accessTokenChannel)) {
            if (listener != null)
                listener.onFail(uniQueId, Constants.COOCAA_TYPE_10, "spaceId or sid or accesstoken is null,please check it!");
            return;
        }
        final AtomicBoolean atomicBoolean = new AtomicBoolean(true);
        Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
            @Override
            public void run() {

                if (atomicBoolean.get()) {
                    AndroidLog.androidLog("startTempBindDirect---------end time");
                    atomicBoolean.compareAndSet(true, false);
                    listener.onFail(uniQueId, Constants.COOCAA_TYPE_11, "set time " + time + " but overtime!");
                }
            }
        }, time, TimeUnit.MILLISECONDS);

        tempSubmit(uniQueId, type,listener, accessTokenChannel, atomicBoolean, time);
    }

    @Override
    public void addOnDeviceChangedListener(OnDeviceChangedListener listener) throws RemoteException {
        synchronized (deviceChangedList) {
            if (!deviceChangedList.contains(listener)) {
                deviceChangedList.add(listener);
            }
        }
    }

    @Override
    public void removeOnDeviceChangedListener(OnDeviceChangedListener listener) throws RemoteException {
        synchronized (deviceChangedList) {
            deviceChangedList.remove(listener);
        }
    }

    @Override
    public void addDeviceBindListener(OnDeviceBindListener listener) throws RemoteException {
        synchronized (deviceBindList) {
            if (!deviceBindList.contains(listener)) {
                deviceBindList.add(listener);
            }
        }
    }

    @Override
    public void removeDeviceBindListener(OnDeviceBindListener listener) throws RemoteException {
        synchronized (deviceBindList) {
            deviceBindList.remove(listener);
        }
    }

    @Override
    public void addDeviceInfoUpdateListener(OnDeviceInfoUpdateListener listener) throws RemoteException {
        synchronized (deviceDeviceInfoList) {
            if (!deviceDeviceInfoList.contains(listener)) {
                deviceDeviceInfoList.add(listener);
            }
        }
    }

    @Override
    public void removeDeviceInfoUpdateListener(OnDeviceInfoUpdateListener listener) throws RemoteException {
        synchronized (deviceDeviceInfoList) {
            if (deviceDeviceInfoList.contains(listener)) {
                deviceDeviceInfoList.remove(listener);
            }
        }
    }

    @Override
    public void addDevicesReflushListener(OnDevicesReflushListener listener) throws RemoteException {
        synchronized (devicesReflushList) {
            if (!devicesReflushList.contains(listener)) {
                devicesReflushList.add(listener);
            }
        }
    }

    @Override
    public void removeDevicesReflushListener(OnDevicesReflushListener listener) throws RemoteException {
        synchronized (devicesReflushList) {
            if (devicesReflushList.contains(listener)) {
                devicesReflushList.remove(listener);
            }
        }
    }

    @Override
    public int join(String roomId) {

        HttpResult<JoinToLeaveData> data = null;
        try {
            data = mSSContext.getServerInterface().joinRoom(mSSContext.getAccessToken(), roomId, mSSContext.getSessionManager().getMySession().encode());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (data == null) {
            return -10;
        }
        return Integer.valueOf(data.code);

    }

    @Override
    public int leave(String userQuit) {
        HttpResult<JoinToLeaveData> data = mSSContext.getServerInterface().leaveRoom(mSSContext.getAccessToken(), userQuit);

        if (data == null) {
            return -10;
        }
        return Integer.valueOf(data.code);
    }

    @Override
    public void sseLoginSuccess() {
        synchronized (deviceDeviceInfoList) {
            for (int i = 0; i < deviceDeviceInfoList.size(); i++) {
                deviceDeviceInfoList.get(i).sseLoginSuccess();
            }
        }
    }

    @Override
    public void loginState(int state, String info) {
        synchronized (deviceDeviceInfoList) {
            for (int i = 0; i < deviceDeviceInfoList.size(); i++) {
                deviceDeviceInfoList.get(i).loginState(state, info);
            }
        }
    }

    @Override
    public void loginConnectingState(int state, String info) {
        synchronized (deviceDeviceInfoList) {
            for (int i = 0; i < deviceDeviceInfoList.size(); i++) {
                deviceDeviceInfoList.get(i).loginConnectingState(state, info);
            }
        }
    }

    @Override
    public void onDeviceInfoUpdateList() {
        updateLsid(new LsidListener() {
            @Override
            public void onUpdateEnd() {
                //发送service数据 add wyh
                mSSContext.post(new Runnable() {
                    @Override
                    public void run() {
                        mSSContext.getTransmitter().sendCoreData();
                    }
                });

                try {
                    synchronized (deviceBindList) {
                        for (int i = 0; i < deviceDeviceInfoList.size(); i++) {
                            deviceDeviceInfoList.get(i).onDeviceInfoUpdate(getDevices());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1);
    }

    private void submit(final String bindCode, final OnBindResultListener listener, String accessTokenChannel, final AtomicBoolean atomicBoolean, final long time) throws Exception {
        mSSContext.getServerInterface().submitStartBind(accessTokenChannel, bindCode, new HttpSubscribe<HttpResult<DeviceData>>() {
            @Override
            public void onSuccess(HttpResult<DeviceData> result) {
                commonBindResult(result, atomicBoolean, listener, bindCode, "submit Interface request result is null");

            }

            @Override
            public void onError(HttpThrowable error) {
                atomicBoolean.compareAndSet(true, false);
                if (listener != null)
                    listener.onFail(bindCode, "" + error.getErrCode(), error.getErrMsg());
            }
        });
    }


    private void tempSubmit(final String uniQueId, final int type, final OnBindResultListener listener, String accessTokenChannel, final AtomicBoolean atomicBoolean, final long time) throws Exception {
        mSSContext.getServerInterface().submitTempStartBindDirect(accessTokenChannel, uniQueId,type, new HttpSubscribe<HttpResult<DeviceData>>() {
            @Override
            public void onSuccess(HttpResult<DeviceData> result) {
                commonBindResult(result, atomicBoolean, listener, uniQueId, "tempSubmit Interface request result is null");

            }

            @Override
            public void onError(HttpThrowable error) {
                atomicBoolean.compareAndSet(true, false);
                if (listener != null)
                    listener.onFail(uniQueId, "" + error.getErrCode(), error.getErrMsg());
            }
        });
    }

    private void commonBindResult(HttpResult<DeviceData> result, AtomicBoolean atomicBoolean, OnBindResultListener listener, String uniQueId, String s) {
        if (result != null && result.code.equals(Constants.COOCAA_SUCCESS) && result.data != null) {
            if (!atomicBoolean.get())
                return;
            atomicBoolean.compareAndSet(true, false);
            try {
                DeviceData deviceData = result.data;
                DeviceInfo deviceInfo = null;
                DeviceState deviceState = null;
                int status = 1;

                try {
                    if (!StringUtils.isEmpty(deviceData.getDeviceInfo())) {
                        String clazzName = JSONObject.parseObject(deviceData.getDeviceInfo()).getString("clazzName");
                        try {
                            Class<?> clazz = (Class<?>) Class.forName(clazzName);
                            deviceInfo = (DeviceInfo) JSONObject.parseObject(deviceData.getDeviceInfo(), clazz);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (!StringUtils.isEmpty(deviceData.getAttributeJson()))
                        deviceState = DeviceState.parse(deviceData.getAttributeJson());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!StringUtils.isEmpty(deviceData.getStatus()))
                    status = Integer.parseInt(deviceData.getStatus());

                Device device = new Device(deviceData.getSid(), deviceInfo, deviceState,
                        status, deviceData.getIsTemp(), deviceData.getRoomId(), deviceData.getDeviceType(), deviceData.getMerchantName(),
                        deviceData.getMerchantIcon(), deviceData.getSpaceName(), deviceData.getMerchantId(), deviceData.getSpaceId(),
                        deviceData.getMerchantCoverPhoto(), deviceData.getLastConnectTime(),deviceData.getMerchantNameAlias());
                Log.d(TAG, " callback on success:" + deviceData.getDeviceInfo() + " --Attribute:" + deviceData.getAttributeJson());
                try {
                    addDevice(device);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                listener.onSuccess(uniQueId, device);
                updateLsid(null, 0);
            } catch (Exception e) {
                e.printStackTrace();
                if (listener != null)
                    listener.onFail(uniQueId, Constants.COOCAA_TYPE_12, "submit Interface request successful but on error occurs e:" + e.getMessage());
            }
        } else {
            if (result != null) {
                if (!atomicBoolean.get())
                    return;

                if (listener != null)
                    listener.onFail(uniQueId, result.code, result.msg);
                atomicBoolean.compareAndSet(true, false);
            } else {
                if (!atomicBoolean.get())
                    return;
                atomicBoolean.compareAndSet(true, false);
                if (listener != null)
                    listener.onFail(uniQueId, Constants.COOCAA_TYPE_13, s);
            }
        }
    }

    @Override
    public void updateLsid(final LsidListener lsidListener, final int callbackType) {
        Log.d(TAG, "updateLsid  :");
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String accessToken = mSSContext.getSmartScreenManager().getLSIDManager().getLSIDInfo().accessToken;
//                    String sid = ShareUtls.getInstance(SSChannelService.getContext()).getString(Constants.COOCAA_PREF_ACCESSTOKEN, "");
                    Log.d(TAG, "updateLsid  accessToken:" + accessToken);
                    if (TextUtils.isEmpty(accessToken)) {
                        return;
                    }
                    HttpResult<BindLsidData> data = HttpApi.getInstance().requestSync(SessionHttpService.SERVICE.getLsidList(accessToken, "3"), "query-bind-device", mSSContext.getLSID());
                    Log.d(TAG, "updateLsid  (data == null):" + (data == null));
                    if (data == null) {
                        return;
                    }
                    BindLsidData bd = data.data;
                    if (bd != null) {
                        synchronized (bindDeviceMap) {
                            bindDeviceMap.clear();
                            if (bd.deviceBoundToUserList != null && bd.deviceBoundToUserList.size() > 0) {
                                for (int i = 0; i < bd.deviceBoundToUserList.size(); i++) {
                                    BindLsidData.DeviceItem item = bd.deviceBoundToUserList.get(i);
                                    Log.d(TAG, "updateLsid deviceBoundToUserList item.zpLsid:" + item.zpLsid + " item.deviceName:" + item.deviceName);
                                    bindDeviceMap.put(item.zpLsid, data2Device(item));
                                }
                            }
                            if (bd.userBindDeviceList != null && bd.userBindDeviceList.size() > 0) {
                                for (int i = 0; i < bd.userBindDeviceList.size(); i++) {
                                    BindLsidData.DeviceItem item = bd.userBindDeviceList.get(i);
                                    Log.d(TAG, "updateLsid userBindDeviceList  item.zpLsid:" + item.zpLsid + " item.deviceName:" + item.deviceName);
                                    bindDeviceMap.put(item.zpLsid, data2Device(item));
                                }
                            }
                            resetDeviceSP(bindDeviceMap);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (lsidListener != null) {
                    lsidListener.onUpdateEnd();
                }

                if (callbackType == 1) {
                    devicesReflush();
                }

            }
        });
    }


    /**
     * 通知设备类别变更
     */
    private void devicesReflush() {
        AndroidLog.androidLog("devicesReflush---1-:" + System.currentTimeMillis());
        if (devicesReflushList != null && devicesReflushList.size() > 0) {
            synchronized (devicesReflushList) {
                for (int i = 0; i < devicesReflushList.size(); i++) {
                    try {
                        devicesReflushList.get(i).onDeviceReflushUpdate(getDevices());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        AndroidLog.androidLog("devicesReflush---2-:" + System.currentTimeMillis());
    }


    @Override
    public List<Device> updateDeviceList() {
        Log.d(TAG, "updateDeviceList:");

        String accessToken = mSSContext.getSmartScreenManager().getLSIDManager().getLSIDInfo().accessToken;
        Log.d(TAG, "updateLsid  accessToken:" + accessToken);
        if (TextUtils.isEmpty(accessToken)) {
            return new ArrayList<>();
        }
        HttpResult<BindLsidData> data = HttpApi.getInstance().requestSync(SessionHttpService.SERVICE.getLsidList(accessToken, "3"), "query-bind-device", mSSContext.getLSID());
        Log.d(TAG, "updateLsid  (data == null):" + (data == null));
        if (data == null) {
            return new ArrayList<>();
        }
        BindLsidData bd = data.data;
        if (bd != null) {
            synchronized (bindDeviceMap) {
                bindDeviceMap.clear();
                if (bd.deviceBoundToUserList != null && bd.deviceBoundToUserList.size() > 0) {
                    for (int i = 0; i < bd.deviceBoundToUserList.size(); i++) {
                        BindLsidData.DeviceItem item = bd.deviceBoundToUserList.get(i);
                        Log.d(TAG, "updateLsid deviceBoundToUserList item.zpLsid:" + item.zpLsid + " item.deviceName:" + item.deviceName);
                        bindDeviceMap.put(item.zpLsid, data2Device(item));
                    }


                }
                if (bd.userBindDeviceList != null && bd.userBindDeviceList.size() > 0) {
                    for (int i = 0; i < bd.userBindDeviceList.size(); i++) {
                        BindLsidData.DeviceItem item = bd.userBindDeviceList.get(i);
                        Log.d(TAG, "updateLsid userBindDeviceList  item.zpLsid:" + item.zpLsid + " item.deviceName:" + item.deviceName);
                        bindDeviceMap.put(item.zpLsid, data2Device(item));
                    }
                }
                resetDeviceSP(bindDeviceMap);
            }
        }
        Log.d(TAG, bindDeviceMap.values().toString());
        return new ArrayList<>(bindDeviceMap.values());
    }

    @Override
    public String getAccessToken() throws RemoteException {
        return mSSContext.getAccessToken();
    }

    private Device data2Device(BindLsidData.DeviceItem item) {
        try {
            String sid = item.zpLsid;
            DeviceState ds = null;
            if (!TextUtils.isEmpty(item.zp_attribute_json))
                ds = DeviceState.parse(item.zp_attribute_json);
            int status = Integer.parseInt(item.zp_status);
            String type = item.zpRegisterType.trim();
            DeviceInfo di = null;

            if (!TextUtils.isEmpty(item.zp_device_json)) {
                String clazzName = JSONObject.parseObject(item.zp_device_json).getString("clazzName");
                try {
                    Class<?> clazz = Class.forName(clazzName);
                    di = (DeviceInfo) JSONObject.parseObject(item.zp_device_json, clazz);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return new Device(sid, di, ds, status, item.isTemp, item.roomId, type,
                    item.merchantName, item.merchantIcon, item.spaceName, item.merchantId, item.spaceId,
                    item.merchantCoverPhoto,item.lastConnectTime,item.merchantNameAlias);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Device(item.zpLsid, null, null, Integer.valueOf(item.zp_status));
    }

    @Override
    public boolean validate(String source, String target) {
        boolean validate = bindDeviceMap.containsKey(target) || bindDeviceMap.containsKey(source);
        AndroidLog.androidLog("---validate:" + validate + " source:" + source + " target:" + target);
        return validate;
    }

    @Override
    public boolean updateCurrentDevice(Session s) {
        try {
            synchronized (bindDeviceMap) {
                String lsid = s.getId();
                if (bindDeviceMap.containsKey(lsid)) {
                    currentDevice = bindDeviceMap.get(lsid);
                    return true;
                } else {
                    updateLsid(null, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private ControllerServer.OnDeviceAliveChangeListener mListener = new ControllerServer.OnDeviceAliveChangeListener() {
        @Override
        public void onDeviceOnline(String lsid) {
            Log.d(TAG, "onDeviceOnline:" + lsid);
            updateDeviceStatus(lsid, 1);
            try {
                Device d = bindDeviceMap.get(lsid);
                if (d != null) {
                    deviceOnline(d);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onDeviceOffline(String lsid) {
            Log.d(TAG, "onDeviceOffline:" + lsid);
            updateDeviceStatus(lsid, 0);
            try {
                Device d = bindDeviceMap.get(lsid);
                if (d != null) {
                    deviceOffline(d);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private ControllerServer.OnDeviceBindStatusListener bindListener = new ControllerServer.OnDeviceBindStatusListener() {
        @Override
        public void onDeviceBind(final String lsid) {
            //已经在绑定流程中实现了添加动作，这里不需要了。
            Log.d(TAG, "onDeviceBind:" + lsid);
            updateLsid(new LsidListener() {
                @Override
                public void onUpdateEnd() {
                    synchronized (deviceBindList) {
                        for (OnDeviceBindListener listener : deviceBindList) {
                            listener.onDeviceBind(lsid);
                        }
                    }
                }
            }, 0);
        }

        @Override
        public void onDeviceUnBind(String lsid) {
            Log.d(TAG, "onDeviceUnBind:" + lsid);
            removeDevice(lsid);
            synchronized (deviceBindList) {
                for (int i = 0; i < deviceBindList.size(); i++) {  //防止遍历异常
                    deviceBindList.get(i).onDeviceUnBind(lsid);
                }
            }
        }
    };

    private DeviceStateManager.OnDeviceStateChangeListener stateListener = new DeviceStateManager.OnDeviceStateChangeListener() {
        @Override
        public void onDeviceStateUpdate(DeviceState state) {
            updateDeviceState(state);
        }
    };

    private void deviceOffline(Device d) {
        synchronized (deviceChangedList) {
            for (int i = 0; i < deviceChangedList.size(); i++) {  //防止遍历异常
                deviceChangedList.get(i).onDeviceOffLine(d);
            }
        }
    }

    private void deviceOnline(Device d) {
        synchronized (deviceChangedList) {
            for (int i = 0; i < deviceChangedList.size(); i++) {  //防止遍历异常
                deviceChangedList.get(i).onDeviceOnLine(d);
            }
        }
    }

    private void deviceUpdate(Device d) {
        synchronized (deviceChangedList) {
            for (int i = 0; i < deviceChangedList.size(); i++) {  //防止遍历异常
                deviceChangedList.get(i).onDeviceUpdate(d);
            }
        }
    }

}
