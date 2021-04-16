package com.coocaa.tvpi.module.service.api;


import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.connect.service.SSMsgDispatcher;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.data.account.TpTokenInfo;
import com.coocaa.smartscreen.data.device.BindCodeMsg;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.callback.RepositoryCallback;
import com.coocaa.smartscreen.repository.service.BindCodeRepository;
import com.coocaa.smartscreen.repository.service.LoginRepository;
import com.coocaa.smartscreen.utils.StartUtils;
import com.coocaa.smartsdk.ILogService;
import com.coocaa.smartsdk.ISmartListener;
import com.coocaa.smartsdk.ISmartService;
import com.coocaa.smartsdk.IUserListener;
import com.coocaa.smartsdk.IUserService;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.SmartDefine;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.smartsdk.object.IUserInfo;
import com.coocaa.tvpi.event.UserLoginEvent;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.module.log.PayloadEvent;
import com.coocaa.tvpi.module.login.LoginActivity;
import com.coocaa.tvpi.module.login.LoginHelper;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.onlineservice.OnlineServiceHelp;
import com.coocaa.tvpi.module.openapi.StartAppStore;
import com.coocaa.tvpi.util.FastClick;
import com.swaiotos.smart.openapi.ISmartBinder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import swaiotos.channel.iot.ss.channel.im.IMMessage;

import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_BOTH;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_LOCAL;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_NOTHING;

/**
 * @Author: yuzhan
 */
public class SmartApiService extends Service {

    private Set<ISmartListener> listenerSet = Collections.synchronizedSet(new HashSet<>());
    private Set<IUserListener> userListenerSet = Collections.synchronizedSet(new HashSet<>());
    private SmartDeviceConnectHelper helper;
    private FastClick fastClick = new FastClick();
    String TAG = "SmartApi";


    @Override
    public void onCreate() {
        super.onCreate();
        helper =  new SmartDeviceConnectHelper();
        helper.setListener(listener);
        EventBus.getDefault().register(this);
        Log.d(TAG, "SmartApiService onCreate ##");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SmartApiService onStartCommand ##");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "SmartApiService onDestroy ##");
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "SmartApiService onBind ##");
        return smartApiBinder;
    }

    private SmartDeviceConnectHelper.SmartDeviceConnectListener listener = new SmartDeviceConnectHelper.SmartDeviceConnectListener() {
        @Override
        public void onDeviceConnect(ISmartDeviceInfo deviceInfo) {
            Log.d(TAG, "onDeviceConnect : " + deviceInfo);
            Iterator<ISmartListener> iter = listenerSet.iterator();
            while(iter.hasNext()) {
                ISmartListener listener = iter.next();
                try {
                    listener.onDeviceConnect(deviceInfo);
                } catch(DeadObjectException e) {
                    iter.remove();
                } catch (RemoteException e) {
//                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onDeviceDisconnect() {
            Log.d(TAG, "onDeviceDisconnect");
            Iterator<ISmartListener> iter = listenerSet.iterator();
            while(iter.hasNext()) {
                ISmartListener listener = iter.next();
                try {
                    listener.onDeviceDisconnect();
                } catch(DeadObjectException e) {
                    iter.remove();
                } catch (RemoteException e) {
//                    e.printStackTrace();
                }
            }
        }

        @Override
        public void loginState(int code, String info) {
            Iterator<ISmartListener> iter = listenerSet.iterator();
            while(iter.hasNext()) {
                ISmartListener listener = iter.next();
                try {
                    listener.loginState(code, info);
                } catch(DeadObjectException e) {
                    iter.remove();
                } catch (RemoteException e) {
//                    e.printStackTrace();
                }
            }
        }
    };

    private ISmartBinder.Stub smartApiBinder = new ISmartBinder.Stub() {
        @Override
        public IBinder getBinder(int type) throws RemoteException {
            IBinder iBinder = null;
            switch (type) {
                case SmartDefine.TYPE_USER_SERVICE:
                    iBinder = userServiceStub;
                    break;
                case SmartDefine.TYPE_SMART_SERVICE:
                    iBinder = smartServiceStub;
                    break;
                case SmartDefine.TYPE_LOG_SERVICE:
                    iBinder = logServiceStub;
                    break;

            }
            Log.d(TAG, "getBinder, type=" + type + ", ret=" + iBinder);
            return iBinder;
        }
    };

    private ISmartService.Stub smartServiceStub = new ISmartService.Stub() {
        @Override
        public void addListener(ISmartListener listener) throws RemoteException {
            if(listener != null)
                listenerSet.add(listener);
        }

        @Override
        public void removeListener(ISmartListener listener) throws RemoteException {
            if(listener != null)
                listenerSet.remove(listener);
        }

        @Override
        public boolean isDeviceConnect() throws RemoteException {
            return helper.isConnected();
        }

        @Override
        public ISmartDeviceInfo getConnectDeviceInfo() throws RemoteException {
            return helper.getSmartDeviceInfo();
        }

        @Override
        public void startConnectDevice() throws RemoteException {
            Log.d(TAG, "call startConnectDevice");
            StartUtils.startActivity(SmartApiService.this, "np://com.coocaa.smart.devicelist/index?from=api");
        }

        @Override
        public void startConnectSameWifi(String networkForceKey) throws RemoteException {
            boolean isFastClick = fastClick.isFaskClick();
            Log.d(TAG, "call startConnectSameWifi, networkForceKey=" + networkForceKey + ", fastClick=" + isFastClick);
            if(!isFastClick) {
                try {
                    Intent intent = new Intent("com.coocaa.smart.connectwifi");
                    intent.putExtra("from", "api");
                    intent.putExtra("networkForceType", networkForceKey);
                    intent.setPackage(getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    Log.d(TAG, "call start connect wifi activity");
                    SmartApiService.this.startActivity(intent);
                } catch (Exception e) {
                    Log.d(TAG, "call startConnectSameWifi, fail=" + e.toString());
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "fast click");
            }
        }

        @Override
        public boolean isSameWifi() throws RemoteException {
            int connectState = SSConnectManager.getInstance().getConnectState();
            final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
            Log.d(TAG, "pushToTv: connectState" + connectState);
            Log.d(TAG, "pushToTv: deviceInfo" + deviceInfo);
            //未连接
            if(connectState == CONNECT_NOTHING || deviceInfo == null){
                Log.d(TAG, "call isSameWifi, CONNECT_NOTHING");
                return false;
            }
            //本地连接不通
            if(!(connectState == CONNECT_LOCAL || connectState == CONNECT_BOTH)){
                Log.d(TAG, "call isSameWifi, !CONNECT_LOCAL or !CONNECT_BOTH");
                return false;
            }
            Log.d(TAG, "call isSameWifi, ret=" + true);
            return true;
        }

        @Override
        public void setMsgDispatchEnable(String clientId, boolean enable) throws RemoteException {
            Log.d(TAG, "setMsgDispatchEnable : clientId=" + clientId + ", enable=" + enable);
            if(enable) {
                SSMsgDispatcher.register(clientId, msgDispatcherReceiver);
            } else {
                SSMsgDispatcher.unRegister(clientId);
            }
        }

        @Override
        public void startWxMP(String id, String path) throws RemoteException {
            Log.d(TAG, "startWxMp, id=" + id + ", path=" + path);
            Intent intent = new Intent();
            intent.setData(Uri.parse("np://com.coocaa.smart.lab.jump_mp/index?from=openapi"));
            if(id != null)
                intent.putExtra("id", id);
            if(path != null)
                intent.putExtra("path", path);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void startAppStore(String pkg) throws RemoteException {
            Log.d(TAG, "startAppStore, pkg=" + pkg);
            StartAppStore.startAppStore(SmartApiService.this, pkg);
        }

        @Override
        public boolean hasDevice() throws RemoteException {
            boolean isConnected = SSConnectManager.getInstance().isConnected();
            boolean isHistoryDeviceValid = SSConnectManager.getInstance().isHistoryDeviceValid();
            Log.d(TAG, "call hasDevice, isConnected=" + isConnected + ", isHistoryDeviceValid=" + isHistoryDeviceValid);
            return isConnected || isHistoryDeviceValid;
        }

        @Override
        public void requestBindCode(final String requestId) throws RemoteException {
            String accessToken = UserInfoCenter.getInstance().getAccessToken();
            ISmartDeviceInfo deviceInfo = helper.getSmartDeviceInfo();
            if(deviceInfo == null || TextUtils.isEmpty(accessToken)) {
                Log.d(TAG, "requestBindCode, but token or connect device is null.");
                return ;
            }
            Log.d(TAG, "requestBindCode, token=" + accessToken + ", info=" + deviceInfo.deviceId + ", spaceId=" + deviceInfo.spaceId);
            Repository.get(BindCodeRepository.class)
                    .getBindCode(accessToken, deviceInfo.deviceId, deviceInfo.spaceId)
                    .setCallback(new RepositoryCallback.Default<BindCodeMsg>() {
                        @Override
                        public void onSuccess(BindCodeMsg success) {
                            Log.d(TAG, "requestBindCode success : " + success);
                            onBindCodeLoaded(requestId, success == null ? "" : success.getBindCode());
                        }

                        @Override
                        public void onFailed(Throwable e) {
                            Log.d(TAG, "requestBindCode onFailed : " + e.toString());
                            onBindCodeLoaded(requestId, "");
                            e.printStackTrace();
                        }
                    });
        }
    };

    private void onBindCodeLoaded(String requestId, String bindCode) {
        Iterator<ISmartListener> iter = listenerSet.iterator();
        while(iter.hasNext()) {
            ISmartListener listener = iter.next();
            try {
                listener.onBindCodeResult(requestId, bindCode);
            } catch(DeadObjectException e) {
                iter.remove();
            } catch (RemoteException e) {

            }
        }
    }

    private SSMsgDispatcher.IMsgReceiver msgDispatcherReceiver = new SSMsgDispatcher.IMsgReceiver() {
        @Override
        public void onReceive(String clientId, IMMessage message) {
            Iterator<ISmartListener> iter = listenerSet.iterator();
            String msgString = JSON.toJSONString(message);
            while(iter.hasNext()) {
                ISmartListener listener = iter.next();
                try {
                    listener.onDispatchMessage(clientId, msgString);
                } catch(DeadObjectException e) {
                    iter.remove();
                } catch (RemoteException e) {
//                    e.printStackTrace();
                }
            }
        }
    };

    IUserInfo userInfo = null;
    private IUserService.Stub userServiceStub = new IUserService.Stub() {
        @Override
        public IUserInfo getUserInfo() throws RemoteException {
            IUserInfo userInfo = compositeUserInfo();
//            Log.d(TAG, "getUserInfo : " + userInfo);
            return userInfo;
        }

        @Override
        public void addObserveUserInfo(IUserListener listener) throws RemoteException {
            if(listener != null) {
                userListenerSet.add(listener);
            }
        }

        @Override
        public void removeUserObserver(IUserListener listener) throws RemoteException {
            if(listener != null) {
                userListenerSet.remove(listener);
            }
        }

        @Override
        public void showLoginUser() throws RemoteException {
            Log.d(TAG, "showLoginUser : isLogin=" + (Repository.get(LoginRepository.class).queryCoocaaUserInfo() != null));
            if (Repository.get(LoginRepository.class).queryCoocaaUserInfo() == null) {
                startLogin();
            }
        }

        @Override
        public void updateAccessToken(String token) throws RemoteException {
            CoocaaUserInfo coocaaUserInfo = Repository.get(LoginRepository.class).queryCoocaaUserInfo();
            Log.d(TAG, "updateAccessToken : " + token + ", curToken=" + (coocaaUserInfo == null ? null : coocaaUserInfo.access_token));
            LoginHelper.loginByAccessToken(token);
        }
    };

    private ILogService.Stub logServiceStub = new ILogService.Stub() {
        @Override
        public void submitLog(String name, Map params) throws RemoteException {
            Log.d(TAG, "submitLog : " + name);
            LogSubmit.event(name, params);
        }

        @Override
        public void submitLogWithTag(String tag, String name, Map params) throws RemoteException {
            Log.d(TAG, "submitLogWithTag : " + tag + " : " + name);
            LogSubmit.event(name, params);
            PayloadEvent.submit(tag, name, params);
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserLoginEvent userLoginEvent) {
        Log.d(TAG, "onEvent: "+userLoginEvent);
        if(userLoginEvent != null) {
            Log.d(TAG, "on user changed, isLogin=" + userLoginEvent.isLogin);
            if(!userLoginEvent.isLogin) {
                for(IUserListener listener : userListenerSet) {
                    try {
                        listener.onUserChanged(false, null);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                for(IUserListener listener : userListenerSet) {
                    try {
                        listener.onUserChanged(true, compositeUserInfo());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                OnlineServiceHelp.getInstance().logout();//账号退出，在线客服退出
            }
        }
    }

    private IUserInfo compositeUserInfo() {
        CoocaaUserInfo coocaaUserInfo = Repository.get(LoginRepository.class).queryCoocaaUserInfo();
        if(coocaaUserInfo == null) return null;
        if(userInfo == null) {
            userInfo = new IUserInfo();
        }
        userInfo.avatar = coocaaUserInfo.avatar;
        userInfo.mobile = coocaaUserInfo.mobile;
        userInfo.nickName = coocaaUserInfo.nick_name;
        userInfo.open_id = coocaaUserInfo.open_id;
        userInfo.accessToken = coocaaUserInfo.access_token;

        TpTokenInfo tpTokenInfo = Repository.get(LoginRepository.class).queryTpTokenInfo();
        if(tpTokenInfo != null) {
            userInfo.tp_token = tpTokenInfo.tp_token;
        }

        if(TextUtils.isEmpty(userInfo.accessToken)) {
            userInfo.accessToken = UserInfoCenter.getInstance().getAccessToken();
        }
        return userInfo;
    }

    private void startLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
