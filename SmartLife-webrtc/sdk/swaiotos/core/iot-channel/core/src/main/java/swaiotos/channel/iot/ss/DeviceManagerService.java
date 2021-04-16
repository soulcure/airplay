package swaiotos.channel.iot.ss;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import java.util.List;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceManager;
import swaiotos.channel.iot.ss.device.IBaseDeviceInfoUpdateListener;
import swaiotos.channel.iot.ss.device.IBaseDevicesReflushListener;
import swaiotos.channel.iot.ss.device.IBaseOnDeviceChangedListener;
import swaiotos.channel.iot.ss.device.IDeviceManagerService;
import swaiotos.channel.iot.ss.device.IDeviceRelationListener;
import swaiotos.channel.iot.utils.AndroidLog;


public class DeviceManagerService extends IDeviceManagerService.Stub implements DeviceManager.OnDeviceChangedListener,
        DeviceManager.OnDeviceBindListener,DeviceManager.OnDeviceInfoUpdateListener,DeviceManager.OnDevicesReflushListener {

    private SSContext mSSContext;
    private RemoteCallbackList<IBaseOnDeviceChangedListener> mRemoteOnDeviceChangedListener = new RemoteCallbackList<>();
    private RemoteCallbackList<IDeviceRelationListener> mRemoteOnDeviceBindListener = new RemoteCallbackList<>();
    private RemoteCallbackList<IBaseDeviceInfoUpdateListener> mRemoteIDeviceInfoUpdateListener = new RemoteCallbackList<>();
    private RemoteCallbackList<IBaseDevicesReflushListener> mRemoteIDevicesReflushListener = new RemoteCallbackList<>();

    public DeviceManagerService(SSContext ssContext) {
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
    public List<Device> getDevices() throws RemoteException {
        try {
            return mSSContext.getDeviceManager().getDevices();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Device> getDeviceOnlineStatus() throws RemoteException {
        try {
            return mSSContext.getDeviceManager().getDeviceOnlineStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Device getCurrentDevice() throws RemoteException {
        try {
            return mSSContext.getDeviceManager().getCurrentDevice();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addOnDeviceChangedListener(IBaseOnDeviceChangedListener listener) throws RemoteException {
        mRemoteOnDeviceChangedListener.register(listener);
    }

    @Override
    public void removeOnDeviceChangedListener(IBaseOnDeviceChangedListener listener) throws RemoteException {
        mRemoteOnDeviceChangedListener.unregister(listener);
    }

    @Override
    public void addDeviceBindListener(IDeviceRelationListener listener) throws RemoteException {
        mRemoteOnDeviceBindListener.register(listener);
    }

    @Override
    public void removeDeviceBindListener(IDeviceRelationListener listener) throws RemoteException {
        mRemoteOnDeviceBindListener.unregister(listener);
    }

    @Override
    public void addDeviceInfoUpdateListener(IBaseDeviceInfoUpdateListener listener) throws RemoteException {
        mRemoteIDeviceInfoUpdateListener.register(listener);

        try {
            AndroidLog.androidLog("----addDeviceInfoUpdateListener-");
            if (mSSContext.getSessionManager() != null && mSSContext.getSessionManager().isConnectSSE()) {
                AndroidLog.androidLog("----addDeviceInfoUpdateListener-callback");
                //处理获取设备列表时序问题
                listener.sseLoginSuccess();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void removeDeviceInfoUpdateListener(IBaseDeviceInfoUpdateListener listener) throws RemoteException {
        mRemoteIDeviceInfoUpdateListener.unregister(listener);
    }

    @Override
    public void addDevicesReflushListener(IBaseDevicesReflushListener listener) throws RemoteException {
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
    public void removeDevicesReflushListener(IBaseDevicesReflushListener listener) throws RemoteException {
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
    public void onDeviceOffLine(Device device) {
        int n = mRemoteOnDeviceChangedListener.beginBroadcast();
        for (int i = 0; i < n; i++) {
            IBaseOnDeviceChangedListener listener = mRemoteOnDeviceChangedListener.getBroadcastItem(i);
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
            IBaseOnDeviceChangedListener listener = mRemoteOnDeviceChangedListener.getBroadcastItem(i);
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
            IBaseOnDeviceChangedListener listener = mRemoteOnDeviceChangedListener.getBroadcastItem(i);
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
            IDeviceRelationListener listener = mRemoteOnDeviceBindListener.getBroadcastItem(i);
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
            IDeviceRelationListener listener = mRemoteOnDeviceBindListener.getBroadcastItem(i);
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
            IBaseDeviceInfoUpdateListener listener = mRemoteIDeviceInfoUpdateListener.getBroadcastItem(i);
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
            IBaseDeviceInfoUpdateListener listener = mRemoteIDeviceInfoUpdateListener.getBroadcastItem(i);
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
            IBaseDeviceInfoUpdateListener listener = mRemoteIDeviceInfoUpdateListener.getBroadcastItem(i);
            if (listener != null) {
                try {
                    listener.loginState(state,info);
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
            IBaseDeviceInfoUpdateListener listener = mRemoteIDeviceInfoUpdateListener.getBroadcastItem(i);
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
            IBaseDevicesReflushListener listener = mRemoteIDevicesReflushListener.getBroadcastItem(i);
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
