package swaiotos.channel.iot.ss;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceAdminManager;
import swaiotos.channel.iot.ss.device.IBaseDeviceInfoUpdateListener;
import swaiotos.channel.iot.ss.device.IBindResult;
import swaiotos.channel.iot.ss.device.IDeviceAdminManagerService;
import swaiotos.channel.iot.ss.device.IDeviceBindListener;
import swaiotos.channel.iot.ss.device.IDeviceInfoUpdateListener;
import swaiotos.channel.iot.ss.device.IDevicesReflushListener;
import swaiotos.channel.iot.ss.device.IOnDeviceChangedListener;
import swaiotos.channel.iot.ss.device.IUnBindResult;
import swaiotos.channel.iot.utils.AndroidLog;

/**
 * @ClassName: DeviceAdminManagerService
 * @Author: lu
 * @CreateDate: 2020/4/16 3:56 PM
 * @Description:
 */
public class DeviceAdminManagerService extends IDeviceAdminManagerService.Stub implements DeviceAdminManager.OnDeviceChangedListener,
        DeviceAdminManager.OnDeviceBindListener, DeviceAdminManager.OnDeviceInfoUpdateListener, DeviceAdminManager.OnDevicesReflushListener {
    private RemoteCallbackList<IOnDeviceChangedListener> mRemoteOnDeviceChangedListener = new RemoteCallbackList<>();
    private RemoteCallbackList<IDeviceBindListener> mRemoteOnDeviceBindListener = new RemoteCallbackList<>();
    private RemoteCallbackList<IDeviceInfoUpdateListener> mRemoteIDeviceInfoUpdateListener = new RemoteCallbackList<>();
    private RemoteCallbackList<IDevicesReflushListener> mRemoteIDevicesReflushListener = new RemoteCallbackList<>();

    private SSContext mSSContext;

    public DeviceAdminManagerService(SSContext ssContext) {
        mSSContext = ssContext;
        try {
            //设备状态变化监听
            mSSContext.getDeviceManager().addOnDeviceChangedListener(this);
            //设备绑定解绑变化监听
            mSSContext.getDeviceManager().addDeviceBindListener(this);
            //设备deviceInfo变化监听
            mSSContext.getDeviceManager().addDeviceInfoUpdateListener(this);
            //设备请求网络监听
            mSSContext.getDeviceManager().addDevicesReflushListener(this);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startBind(String accessToken, String bindCode, final IBindResult result, long time) throws RemoteException {
        try {
            String start = "spring server startBind start bindCode=" + bindCode;
            Log.d("logfile", start);
//            LogFile.inStance().toFile(start);

            mSSContext.getDeviceManager().startBind(accessToken, bindCode, new DeviceAdminManager.OnBindResultListener() {
                @Override
                public void onSuccess(String bindCode, Device device) {
                    try {
                        result.onSuccess(bindCode, device);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    String end = "spring server startBind onSuccess bindCode=" + bindCode;
                    Log.d("logfile", end);
//                    LogFile.inStance().toFile(end);
                }

                @Override
                public void onFail(String bindCode, String errorType, String msg) {
                    try {
                        result.onFail(bindCode, errorType, msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    String end = "spring server startBind onFail bindCode=" + bindCode;
                    Log.e("logfile", end);
//                    LogFile.inStance().toFile(end);
                }
            }, time);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void addOnDeviceChangedListener(IOnDeviceChangedListener listener) throws RemoteException {
        mRemoteOnDeviceChangedListener.register(listener);
    }

    @Override
    public void removeOnDeviceChangedListener(IOnDeviceChangedListener listener) throws RemoteException {
        mRemoteOnDeviceChangedListener.unregister(listener);
    }

    @Override
    public void addDeviceBindListener(IDeviceBindListener listener) throws RemoteException {
        mRemoteOnDeviceBindListener.register(listener);
    }

    @Override
    public void removeDeviceBindListener(IDeviceBindListener listener) throws RemoteException {
        mRemoteOnDeviceBindListener.unregister(listener);
    }

    @Override
    public void addDeviceInfoUpdateListener(IDeviceInfoUpdateListener listener) throws RemoteException {
        mRemoteIDeviceInfoUpdateListener.register(listener);

        try {
            AndroidLog.androidLog("----addDevicesRefreshListener-");
            if (mSSContext.getSessionManager() != null && mSSContext.getSessionManager().isConnectSSE()) {
                AndroidLog.androidLog("----addDevicesRefreshListener-callback");
                //处理获取设备列表时序问题
                listener.sseLoginSuccess();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeDeviceInfoUpdateListener(IDeviceInfoUpdateListener listener) throws RemoteException {
        mRemoteIDeviceInfoUpdateListener.unregister(listener);
    }

    @Override
    public void addDevicesReflushListener(IDevicesReflushListener listener) throws RemoteException {
        mRemoteIDevicesReflushListener.register(listener);

        try {
            AndroidLog.androidLog("----addDevicesRefreshListener-");
            //处理获取设备列表时序问题
            listener.onDeviceReflushUpdate(mSSContext.getDeviceManager().getDevices());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeDevicesReflushListener(IDevicesReflushListener listener) throws RemoteException {
        mRemoteIDevicesReflushListener.unregister(listener);
    }

    @Override
    public List<Device> updateDeviceList() throws RemoteException {
        return mSSContext.getDeviceManager().updateDeviceList();
    }

    @Override
    public String getAccessToken() throws RemoteException {
        return mSSContext.getAccessToken();
    }

    @Override
    public void startTempBindDirect(String accessToken, String uniQueId, int type, final IBindResult result, long time) throws RemoteException {
        try {
            String start = "spring server startBind start uniQueId=" + uniQueId;
            Log.d("logfile", start);
//            LogFile.inStance().toFile(start);

            mSSContext.getDeviceManager().startTempBindDirect(accessToken, uniQueId, type,new DeviceAdminManager.OnBindResultListener() {
                @Override
                public void onSuccess(String uniQueId, Device device) {
                    try {
                        result.onSuccess(uniQueId, device);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    String end = "spring server startTempBindDirect onSuccess uniQueId=" + uniQueId;
                    Log.d("logfile", end);
//                    LogFile.inStance().toFile(end);
                }

                @Override
                public void onFail(String uniQueId, String errorType, String msg) {
                    try {
                        result.onFail(uniQueId, errorType, msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    String end = "spring server startTempBindDirect onFail uniQueId=" + uniQueId;
                    Log.e("logfile", end);
//                    LogFile.inStance().toFile(end);
                }
            }, time);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void unBindDevice(String accessToken, String lsid, int type, final IUnBindResult result) throws RemoteException {
        try {
            mSSContext.getDeviceManager().unBindDevice(accessToken, lsid, type, new DeviceAdminManager.unBindResultListener() {
                @Override
                public void onSuccess(String lsid) {
                    try {
                        result.onSuccess(lsid);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFail(String bindCode, String errorType, String msg) {
                    try {
                        result.onFail(bindCode, errorType, msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void onDeviceOffLine(Device device) {
        int n = mRemoteOnDeviceChangedListener.beginBroadcast();
        for (int i = 0; i < n; i++) {
            IOnDeviceChangedListener listener = mRemoteOnDeviceChangedListener.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.onDeviceOffLine(device);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mRemoteOnDeviceChangedListener.finishBroadcast();
    }

    @Override
    public void onDeviceOnLine(Device device) {
        int n = mRemoteOnDeviceChangedListener.beginBroadcast();
        for (int i = 0; i < n; i++) {
            IOnDeviceChangedListener listener = mRemoteOnDeviceChangedListener.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.onDeviceOnLine(device);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mRemoteOnDeviceChangedListener.finishBroadcast();
    }

    @Override
    public void onDeviceUpdate(Device device) {
        int n = mRemoteOnDeviceChangedListener.beginBroadcast();
        for (int i = 0; i < n; i++) {
            IOnDeviceChangedListener listener = mRemoteOnDeviceChangedListener.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.onDeviceUpdate(device);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mRemoteOnDeviceChangedListener.finishBroadcast();
    }

    @Override
    public void onDeviceBind(String lsid) {
        int n = mRemoteOnDeviceBindListener.beginBroadcast();
        for (int i = 0; i < n; i++) {
            IDeviceBindListener listener = mRemoteOnDeviceBindListener.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.onDeviceBind(lsid);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mRemoteOnDeviceBindListener.finishBroadcast();
    }

    @Override
    public void onDeviceUnBind(String lsid) {
        int n = mRemoteOnDeviceBindListener.beginBroadcast();
        for (int i = 0; i < n; i++) {
            IDeviceBindListener listener = mRemoteOnDeviceBindListener.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.onDeviceUnbind(lsid);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mRemoteOnDeviceBindListener.finishBroadcast();
    }

    @Override
    public void onDeviceInfoUpdate(List<Device> devices) {
        int n = mRemoteIDeviceInfoUpdateListener.beginBroadcast();
        for (int i = 0; i < n; i++) {
            IDeviceInfoUpdateListener listener = mRemoteIDeviceInfoUpdateListener.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.onDeviceInfoUpdate(devices);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mRemoteIDeviceInfoUpdateListener.finishBroadcast();
    }

    @Override
    public void sseLoginSuccess() {
        int n = mRemoteIDeviceInfoUpdateListener.beginBroadcast();
        for (int i = 0; i < n; i++) {
            IDeviceInfoUpdateListener listener = mRemoteIDeviceInfoUpdateListener.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.sseLoginSuccess();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mRemoteIDeviceInfoUpdateListener.finishBroadcast();
    }

    @Override
    public void loginState(int state, String info) {
        int n = mRemoteIDeviceInfoUpdateListener.beginBroadcast();
        for (int i = 0; i < n; i++) {
            IDeviceInfoUpdateListener listener = mRemoteIDeviceInfoUpdateListener.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.loginState(state, info);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mRemoteIDeviceInfoUpdateListener.finishBroadcast();
    }

    @Override
    public void loginConnectingState(int code, String info) {
        int n = mRemoteIDeviceInfoUpdateListener.beginBroadcast();
        for (int i = 0; i < n; i++) {
            IDeviceInfoUpdateListener listener = mRemoteIDeviceInfoUpdateListener.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.loginConnectingState(code,info);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mRemoteIDeviceInfoUpdateListener.finishBroadcast();
    }

    @Override
    public void onDeviceReflushUpdate(List<Device> devices) {
        int n = mRemoteIDevicesReflushListener.beginBroadcast();
        for (int i = 0; i < n; i++) {
            IDevicesReflushListener listener = mRemoteIDevicesReflushListener.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.onDeviceReflushUpdate(devices);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mRemoteIDevicesReflushListener.finishBroadcast();
    }

}
