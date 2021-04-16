package swaiotos.channel.iot.ss.controller;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.client.ClientManager;
import swaiotos.channel.iot.ss.manager.lsid.LSIDManager;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.ThreadManager;

/**
 * @ClassName: DeviceStateManagerImpl
 * @Author: lu
 * @CreateDate: 2020/4/22 8:43 PM
 * @Description:
 */
public class DeviceStateManagerImpl implements DeviceStateManager, ClientManager.OnClientChangeListener {
    private static final String TAG = "DSMImpl";
    private List<OnDeviceStateChangeListener> mOnDeviceStateChangeListeners = new ArrayList<>();
    private List<OnDeviceStateChangeListener> mOnMyDeviceStateChangeListeners = new ArrayList<>();
    private SSContext mSSContext;
    private DeviceState mDeviceState;

    private Map<String, Long> mDeviceStates = new HashMap<>();

    DeviceStateManagerImpl(SSContext ssContext) {
        mSSContext = ssContext;
    }

    @Override
    public void open() {
        mDeviceState = new DeviceState(mSSContext.getLSID());
        mSSContext.getSmartScreenManager().getLSIDManager().addCallback(mLSIDCallback);
        mSSContext.getClientManager().addOnClientChangeListener(this);
    }

    private LSIDManager.Callback mLSIDCallback = new LSIDManager.Callback() {
        @Override
        public void onLSIDUpdate() {
            Log.d(TAG, "onLSIDUpdate update DeviceState lsid!");
            mDeviceState.setLsid(mSSContext.getLSID());
            dispatchOnMyDeviceStateChange();
        }
    };

    @Override
    public void close() {
        mSSContext.getSmartScreenManager().getLSIDManager().removeCallback(mLSIDCallback);
        mSSContext.getClientManager().removeOnClientChangeListener(this);
    }

    @Override
    public void updateConnective(String connective, String value, boolean notify) {
        AndroidLog.androidLog("DeviceStateManagerImpl update address connective:" + connective + " value:" + value);
        if (mDeviceState != null && mDeviceState.putConnectiveInfo(connective, value) && notify) {
            dispatchOnMyDeviceStateChange();
        }
    }

    @Override
    public void addOnDeviceStateChangeListener(OnDeviceStateChangeListener listener) {
        synchronized (mOnDeviceStateChangeListeners) {
            if (!mOnDeviceStateChangeListeners.contains(listener)) {
                mOnDeviceStateChangeListeners.add(listener);
            }
        }
    }

    @Override
    public void removeOnDeviceStateChangeListener(OnDeviceStateChangeListener listener) {
        synchronized (mOnDeviceStateChangeListeners) {
            mOnDeviceStateChangeListeners.remove(listener);
        }
    }

    @Override
    public void addMyDeviceOnDeviceStateChangeListener(OnDeviceStateChangeListener listener) {
        synchronized (mOnMyDeviceStateChangeListeners) {
            if (!mOnMyDeviceStateChangeListeners.contains(listener)) {
                mOnMyDeviceStateChangeListeners.add(listener);
            }
        }
    }

    @Override
    public void removeMyDeviceOnDeviceStateChangeListener(OnDeviceStateChangeListener listener) {
        synchronized (mOnMyDeviceStateChangeListeners) {
            mOnMyDeviceStateChangeListeners.remove(listener);
        }
    }

    private void dispatchDeviceStateChanged(final DeviceState state) {
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                synchronized (mOnDeviceStateChangeListeners) {
                    for (OnDeviceStateChangeListener listener : mOnDeviceStateChangeListeners) {
                        try {
                            listener.onDeviceStateUpdate(state);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void updateDeviceState(long timestamp, DeviceState state) {
        synchronized (mDeviceStates) {
            String lsid = state.getLsid();
            Long t = mDeviceStates.get(lsid);
            if (t == null) {
                mDeviceStates.put(lsid, timestamp);
                dispatchDeviceStateChanged(state);
            } else if (t < timestamp) {
                mDeviceStates.put(lsid, timestamp);
                dispatchDeviceStateChanged(state);
            }
        }
    }

    @Override
    public void reflushDeviceStateOfSid() {
        try {
            Log.d(TAG, "onLSIDUpdate update DeviceState lsid!");
            mDeviceState.setLsid(mSSContext.getLSID());
            mDeviceState.putConnectiveInfo(SSChannel.IM_CLOUD, mSSContext.getLSID());
            dispatchOnMyDeviceStateChange();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClientChange(String clientID, Integer version) {
        boolean r;
        if (version == VERSION_REMOVE) {
            r = mDeviceState.removeClientInfo(clientID);
        } else {
            r = mDeviceState.putClientInfo(clientID, version.toString());
        }
        if (r) {
            dispatchOnMyDeviceStateChange();
        }
    }

    private Runnable mDispatchOnMyDeviceStateChangeRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mOnMyDeviceStateChangeListeners) {
                for (OnDeviceStateChangeListener listener : mOnMyDeviceStateChangeListeners) {
                    try {
                        listener.onDeviceStateUpdate(mDeviceState);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };
    
    private void dispatchOnMyDeviceStateChange() {
//        mSSContext.removeCallbacks(mDispatchOnMyDeviceStateChangeRunnable);
        ThreadManager.getInstance().ioThread(mDispatchOnMyDeviceStateChangeRunnable, 0);
    }
}
