package swaiotos.channel.iot.ss.device;

import android.os.RemoteException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.utils.ThreadManager;

public class DeviceManagerImpl implements DeviceManagerClient {

    private IDeviceManagerService mService;
    private final Map<OnDeviceChangedListener, IBaseOnDeviceChangedListener> mOnDeviceChangedListeners = new LinkedHashMap<>();
    private final Map<OnDeviceBindListener, IDeviceRelationListener> mOnDeviceBindListeners = new LinkedHashMap<>();
    private final Map<OnDeviceInfoUpdateListener, IBaseDeviceInfoUpdateListener> mOnDeviceInfoUpdateListeners = new LinkedHashMap<>();
    private final Map<OnDevicesReflushListener, IBaseDevicesReflushListener> mOnDevicesReflushListeners = new LinkedHashMap<>();

    public DeviceManagerImpl() {
    }

    @Override
    public void setService(IDeviceManagerService service) {
        mService = service;
    }

    @Override
    public List<Device> getDevices() throws Exception {
        return mService.getDevices();
    }

    @Override
    public List<Device> getDeviceOnlineStatus() throws Exception {
        return mService.getDeviceOnlineStatus();
    }

    @Override
    public Device getCurrentDevice() throws Exception {
        return mService.getCurrentDevice();
    }

    @Override
    public Session getLocalSessionBySid(String sid) throws Exception {
        return null;
    }

    @Override
    public void addOnDeviceChangedListener(final OnDeviceChangedListener listener) throws RemoteException {
        synchronized (mOnDeviceChangedListeners) {
            if (!mOnDeviceChangedListeners.containsKey(listener)) {
                IBaseOnDeviceChangedListener l = new IBaseOnDeviceChangedListener.Stub() {
                    @Override
                    public void onDeviceOffLine(Device device) throws RemoteException {
                        listener.onDeviceOffLine(device);
                    }

                    @Override
                    public void onDeviceOnLine(Device device) throws RemoteException {
                        listener.onDeviceOnLine(device);
                    }

                    @Override
                    public void onDeviceUpdate(Device device) throws RemoteException {
                        listener.onDeviceUpdate(device);
                    }
                };
                mOnDeviceChangedListeners.put(listener, l);
                mService.addOnDeviceChangedListener(l);
            }
        }
    }

    @Override
    public void removeOnDeviceChangedListener(OnDeviceChangedListener listener) throws RemoteException {
        synchronized (mOnDeviceChangedListeners) {
            IBaseOnDeviceChangedListener l = mOnDeviceChangedListeners.get(listener);
            if (l != null) {
                mService.removeOnDeviceChangedListener(l);
                mOnDeviceChangedListeners.remove(listener);
            }
        }
    }

    @Override
    public void addDeviceBindListener(final OnDeviceBindListener listener) throws RemoteException {
        synchronized (mOnDeviceBindListeners) {
            if (!mOnDeviceBindListeners.containsKey(listener)) {
                final IDeviceRelationListener l = new IDeviceRelationListener.Stub() {
                    @Override
                    public void onDeviceBind(String lsid) throws RemoteException {
                        listener.onDeviceBind(lsid);
                    }

                    @Override
                    public void onDeviceUnbind(String lsid) throws RemoteException {
                        listener.onDeviceUnBind(lsid);
                    }
                };
                mOnDeviceBindListeners.put(listener, l);
                mService.addDeviceBindListener(l);
            }
        }
    }

    @Override
    public void removeDeviceBindListener(final OnDeviceBindListener listener) throws RemoteException {
        synchronized (mOnDeviceBindListeners) {
            IDeviceRelationListener l = mOnDeviceBindListeners.get(listener);
            if (l != null) {
                mService.removeDeviceBindListener(l);
                mOnDeviceBindListeners.remove(listener);
            }
        }
    }

    @Override
    public void addDeviceInfoUpdateListener(final OnDeviceInfoUpdateListener listener) throws RemoteException {
        synchronized (mOnDeviceInfoUpdateListeners) {
            if (!mOnDeviceInfoUpdateListeners.containsKey(listener)) {
                final IBaseDeviceInfoUpdateListener l = new IBaseDeviceInfoUpdateListener.Stub() {

                    @Override
                    public void onDeviceInfoUpdate(List<Device> devices) throws RemoteException {
                        listener.onDeviceInfoUpdate(devices);
                    }

                    @Override
                    public void sseLoginSuccess() throws RemoteException {
                        listener.sseLoginSuccess();
                    }

                    @Override
                    public void loginState(int code, String info) throws RemoteException {
                        listener.loginState(code,info);
                    }

                    @Override
                    public void loginConnectingState(int code, String info) throws RemoteException {
                        listener.loginConnectingState(code,info);
                    }
                };
                mOnDeviceInfoUpdateListeners.put(listener, l);
                mService.addDeviceInfoUpdateListener(l);
            }
        }
    }

    @Override
    public void removeDeviceInfoUpdateListener(OnDeviceInfoUpdateListener listener) throws RemoteException {
        synchronized (mOnDeviceInfoUpdateListeners) {
            IBaseDeviceInfoUpdateListener l = mOnDeviceInfoUpdateListeners.get(listener);
            if (l != null) {
                mService.removeDeviceInfoUpdateListener(l);
                mOnDeviceInfoUpdateListeners.remove(listener);
            }
        }
    }

    @Override
    public void addDevicesReflushListener(final OnDevicesReflushListener listener) throws RemoteException  {
        synchronized (mOnDevicesReflushListeners) {
            if (!mOnDevicesReflushListeners.containsKey(listener)) {
                final IBaseDevicesReflushListener l = new IBaseDevicesReflushListener.Stub() {

                    @Override
                    public void onDeviceReflushUpdate(List<Device> devices) {
                        listener.onDeviceReflushUpdate(devices);
                    }
                };
                mOnDevicesReflushListeners.put(listener, l);
                mService.addDevicesReflushListener(l);
            }
        }
    }

    @Override
    public void removeDevicesReflushListener(OnDevicesReflushListener listener) throws RemoteException {
        synchronized (mOnDevicesReflushListeners) {
            IBaseDevicesReflushListener l = mOnDevicesReflushListeners.get(listener);
            if (l != null) {
                mService.removeDevicesReflushListener(l);
                mOnDevicesReflushListeners.remove(listener);
            }
        }
    }

    @Override
    public List<Device> updateDeviceList() {
        try {
            return mService.updateDeviceList();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getAccessToken() throws RemoteException {
        return mService.getAccessToken();
    }

    @Override
    public void close() {
        mOnDeviceChangedListeners.clear();
        mOnDeviceBindListeners.clear();
        mOnDeviceInfoUpdateListeners.clear();
        mOnDevicesReflushListeners.clear();
    }

    @Override
    public void updateDeviceList(final DeviceListCallBack callBack) {
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                List<Device> list = updateDeviceList();
                if (callBack != null) {
                    callBack.onDevices(list);
                }
            }
        });
    }
}
