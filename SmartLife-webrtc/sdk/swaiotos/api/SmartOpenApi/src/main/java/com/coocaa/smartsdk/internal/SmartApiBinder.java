package com.coocaa.smartsdk.internal;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.coocaa.smartsdk.ILogService;
import com.coocaa.smartsdk.ISmartListener;
import com.coocaa.smartsdk.ISmartService;
import com.coocaa.smartsdk.IUserListener;
import com.coocaa.smartsdk.IUserService;
import com.coocaa.smartsdk.SmartApiListener;
import com.coocaa.smartsdk.SmartDefine;
import com.coocaa.smartsdk.UserChangeListener;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.smartsdk.object.IUserInfo;
import com.swaiotos.smart.openapi.ISmartBinder;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @Author: yuzhan
 */
public class SmartApiBinder {
    private Context appContext;
//    private ISmartService service;
    private ISmartBinder service;

    private ISmartService smartService;
    private IUserService userService;
    private ILogService logService;

    private Set<SmartApiListener> listenerSet = new HashSet<>();
    private SimpleSmartApiListenerWrapper wrapper;
    private Set<WeakReference<UserChangeListener>> userChangeListenerSet = new HashSet<>();

    private boolean isMobileRuntime = false;//区分手机、dongle端runtime

    private final static SmartApiBinder instance = new SmartApiBinder();
    private static volatile boolean inited = false;

    public final static SmartApiBinder getInstance() {
        return instance;
    }

    private SmartApiBinder() {

    }

    public void init(Context appContext) {
        if(inited) return ;

        inited = true;
        if(appContext instanceof Activity || appContext instanceof Service) {
            this.appContext = appContext.getApplicationContext();
        } else {
            this.appContext = appContext;
        }
        bindSmartService();
    }

    public void setMobileRuntime(boolean b) {
        isMobileRuntime = b;
    }

    public boolean isMobileRuntime() {
        return isMobileRuntime;
    }

    public void addListener(SmartApiListener listener) {
        listenerSet.add(listener);
    }

    public void removeListener(SmartApiListener listener) {
        listenerSet.remove(listener);
    }

    public void simpleCheckDeviceConnected(SimpleSmartApiListenerWrapper _wrapper) {
        wrapper = _wrapper;
        if(_wrapper == null || _wrapper.listener == null) {
            wrapper = null;
        }
    }

    public void startConnectDevice() {
        if(service != null) {
            try {
                smartService.startConnectDevice();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindSmartService();
        }
    }

    public ISmartDeviceInfo getConnectDeviceInfo() {
        if(smartService != null) {
            try {
                return smartService.getConnectDeviceInfo();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindSmartService();
        }
        return null;
    }

    public boolean isDeviceConnect() {
        if(smartService != null) {
            try {
                return smartService.isDeviceConnect();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindSmartService();
        }
        return false;
    }

    public boolean hasDevice() {
        if(smartService != null) {
            try {
                return smartService.hasDevice();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindSmartService();
        }
        return false;
    }

    public IUserInfo getUserInfo() {
        if(userService != null) {
            try {
                return userService.getUserInfo();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindSmartService();
        }
        return null;
    }

    public IUserInfo getUserInfo(UserChangeListener listener, boolean showLoginIfLogout) {
        if(listener != null) {
            boolean needAdd = true;
            for(WeakReference<UserChangeListener> listenerRef : userChangeListenerSet) {
                if(listenerRef != null && listener == listenerRef.get()) {
                    needAdd = false;
                    break;
                }
            }
            if(needAdd) {
                userChangeListenerSet.add(new WeakReference<>(listener));
            }
        }
        IUserInfo userInfo = getUserInfo();
        if(userInfo == null && showLoginIfLogout) {
            showLoginUser();
        }
        return userInfo;
    }

    public void showLoginUser() {
        if(userService != null) {
            try {
                userService.showLoginUser();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindSmartService();
        }
    }

    public void removeUserChangeListener(UserChangeListener listener) {
        if(listener != null) {
            Iterator<WeakReference<UserChangeListener>> iterator = userChangeListenerSet.iterator();
            while(iterator.hasNext()) {
                WeakReference<UserChangeListener> listenerRef = iterator.next();
                if(listenerRef != null && listener == listenerRef.get()) {
                    iterator.remove();
                }
            }
        }
    }

    public void updateAccessToken(String token) {
        if(userService != null) {
            try {
                userService.updateAccessToken(token);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindSmartService();
        }
    }

    public void startConnectSameWifi(String networkForceKey) {
        if(smartService != null) {
            try {
                smartService.startConnectSameWifi(networkForceKey);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindSmartService();
        }
    }

    public void submitLog(String name, Map<String, String> params) {
        if(logService != null) {
            try {
                logService.submitLog(name, params);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindSmartService();
        }
    }

    public void submitLogWithTag(String tag, String name, Map<String, String> params) {
        if(logService != null) {
            try {
                logService.submitLogWithTag(tag, name, params);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindSmartService();
        }
    }

    public void startWxMP(String id, String path) {
        if(checkBind()) {
            try {
                smartService.startWxMP(id, path);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void startAppStore(String pkg) {
        if(checkBind()) {
            try {
                smartService.startAppStore(pkg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void requestBindCode(String requestId) {
        if(checkBind()) {
            try {
                smartService.requestBindCode(requestId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkBind() {
        if(smartService == null) {
            bindSmartService();
            return false;
        }
        return true;
    }

    public boolean isSameWifi() {
        if(smartService != null) {
            try {
                return smartService.isSameWifi();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindSmartService();
        }
        return false;
    }

    public void setMsgDispatchEnable(String clientId, boolean enable) {
        if(smartService != null) {
            try {
                smartService.setMsgDispatchEnable(clientId, enable);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            bindSmartService();
        }
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ISmartBinder.Stub.asInterface(binder);
            try {
                smartService = ISmartService.Stub.asInterface(service.getBinder(SmartDefine.TYPE_SMART_SERVICE));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                userService = IUserService.Stub.asInterface(service.getBinder(SmartDefine.TYPE_USER_SERVICE));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                logService = ILogService.Stub.asInterface(service.getBinder(SmartDefine.TYPE_LOG_SERVICE));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if(smartService != null) {
                try {
                    if(smartService.getConnectDeviceInfo() != null) {
                        innerOnDeviceConnect(smartService.getConnectDeviceInfo());
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                try {
                    smartService.addListener(smartServiceListener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            if(userService != null) {
                try {
                    userService.addObserveUserInfo(userListenerStub);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            try {
                binder.linkToDeath(deathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("SmartApi", "SmartApiBinder onServiceDisconnected.");
            service = null;
            smartService = null;
            userService = null;
        }
    };

    private IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d("SmartApi", "SmartApiBinder died, try to re bind.");
            if(service != null) {
                service.asBinder().unlinkToDeath(this, 0);
                service = null;
            }
            bindSmartService();
        }
    };

    private void innerOnDeviceConnect(ISmartDeviceInfo deviceInfo) {
        for(SmartApiListener listener : listenerSet) {
            try {
                listener.onDeviceConnect(deviceInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(wrapper != null) {
            try {
                wrapper.listener.onDeviceConnectStatusChanged(true, wrapper.args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void innerOnDeviceDisconnect() {
        for(SmartApiListener listener : listenerSet) {
            try {
                listener.onDeviceDisconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(wrapper != null) {
            try {
                wrapper.listener.onDeviceConnectStatusChanged(false, wrapper.args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ISmartListener.Stub smartServiceListener = new ISmartListener.Stub() {
        @Override
        public void onDeviceConnect(ISmartDeviceInfo deviceInfo) throws RemoteException {
            innerOnDeviceConnect(deviceInfo);
        }

        @Override
        public void onDeviceDisconnect() throws RemoteException {
            innerOnDeviceDisconnect();
        }

        @Override
        public void loginState(int code, String info) throws RemoteException {
            for(SmartApiListener listener : listenerSet) {
                try {
                    listener.loginState(code, info);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onDispatchMessage(String clientId, String msgJson) throws RemoteException {
            for(SmartApiListener listener : listenerSet) {
                try {
                    listener.onDispatchMessage(clientId, msgJson);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onBindCodeResult(String requestId, String bindCode) throws RemoteException {
            for(SmartApiListener listener : listenerSet) {
                try {
                    listener.onBindCodeResult(requestId, bindCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private IUserListener.Stub userListenerStub = new IUserListener.Stub() {
        @Override
        public void onUserChanged(boolean login, IUserInfo userInfo) throws RemoteException {
            Iterator<WeakReference<UserChangeListener>> iterator = userChangeListenerSet.iterator();
            while(iterator.hasNext()) {
                WeakReference<UserChangeListener> listenerRef = iterator.next();
                if(listenerRef != null && listenerRef.get() != null) {
                    Log.d("SmartVI", "****** call back to : " + listenerRef.get() + ", isLogin=" + login);
                    listenerRef.get().onUserChanged(login, userInfo);
                }
            }
        }
    };

    private void bindSmartService() {
        if(appContext == null) {
            return ;
        }
        Intent intent = new Intent();
        intent.setPackage(appContext.getPackageName());
        intent.setAction("coocaa.intent.action.SmartApiService");
        appContext.bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }
}
