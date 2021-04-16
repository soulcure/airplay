package swaiotos.channel.iot.common.lsid;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import swaiotos.channel.iot.common.usecase.LSIDUseCase;
import swaiotos.channel.iot.common.usecase.RegisterUseCase;
import swaiotos.channel.iot.common.usecase.UpdateDeviceInfoUseCase;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.common.utils.FileAccessTokenUtils;
import swaiotos.channel.iot.common.utils.StringUtils;
import swaiotos.channel.iot.common.utils.TYPE;
import swaiotos.channel.iot.ss.device.PadDeviceInfo;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.ThreadManager;
import swaiotos.sal.SAL;
import swaiotos.sal.SalModule;
import swaiotos.sal.system.ISystem;

public abstract class LSIDManagerService extends Service {
    private LSID lsid;
    private static final String TAG = LSIDManagerService.class.getSimpleName();
    public static final String TV_NAME_CHANGED = "TVNAME_CHANGE";
    public static final String ACCOUNT_CHANGED = "com.tianci.user.account_changed";
    public static final String DONGLE_NAME_ACTION = "sky.action.start.app.msg";
    public static final String DEVICE_NAME_CHANGED_ACTION = "smart.life.change.device";
    private Stub mStub;
    private AtomicBoolean mAtomicBoolean = new AtomicBoolean(true);
//    private AtomicBoolean mRegisterAtomicBoolean = new AtomicBoolean(true);
    private TYPE mTYPE;
    private IRemoteServiceCallback iRemoteServiceCallback;
    private String mAccessToken;

    public abstract TYPE getLSIDType();
    private BroadcastReceiver mAccountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "acount change action：" + action);
            if (action != null) {
                if (action.equals(ACCOUNT_CHANGED) || action.equals(DONGLE_NAME_ACTION) || action.equals(DEVICE_NAME_CHANGED_ACTION)) {
                    //处理多线程锁死情况，启动线程来执行设备更新
                    checkDeviceInfo();
                } else if (action.equals(TV_NAME_CHANGED)) {
                    String tvName = intent.getStringExtra("tvname");

                    Log.d(TAG, "tvName changed：" + tvName);
                    String accessToken = ShareUtls.getInstance(getApplicationContext()).getString(Constants.COOCAA_PREF_ACCESSTOKEN, "");
                    String deviceInfo = ShareUtls.getInstance(getApplicationContext()).getString(Constants.COOCAA_PREF_DEVICEINFO, "");

                    if (!TextUtils.isEmpty(accessToken) && !TextUtils.isEmpty(deviceInfo)) {
                        TVDeviceInfo tvDeviceInfo = JSONObject.parseObject(deviceInfo, TVDeviceInfo.class);
                        tvDeviceInfo.mNickName = tvName;
                        tvDeviceInfo.deviceName = tvName;
                        String newDeviceInfo = JSONObject.toJSONString(tvDeviceInfo);
                        Log.d(TAG, "UpdateDevice  DeviceInfo：" + newDeviceInfo);
                        UpdateDeviceInfoUseCase.getInstance(getApplicationContext()).run(
                                new UpdateDeviceInfoUseCase.RequestValues(accessToken, newDeviceInfo),
                                new UpdateDeviceInfoUseCase.UpdateDeviceInfoCallBackListener() {
                                    @Override
                                    public void onError(String errorType, String msg) {
                                        Log.d(TAG, "errorType:" + errorType + " msg:" + msg);
                                    }

                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "UpdateDevice  DeviceInfo onSuccess");
                                    }
                                });
                    }

                } //TV_NAME_CHANGED
            }
        }
    };

    private void checkDeviceInfo() {
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                String accessToken = ShareUtls.getInstance(getApplicationContext()).getString(Constants.COOCAA_PREF_ACCESSTOKEN,"");
                String deviceInfo = ShareUtls.getInstance(getApplicationContext()).getString(Constants.COOCAA_PREF_DEVICEINFO,"");
                if (!TextUtils.isEmpty(accessToken) && !TextUtils.isEmpty(deviceInfo))
                    checkDeviceInfo(deviceInfo,accessToken);
            }
        });
    }


    private class Stub extends ILSIDManagerService.Stub {
        @Override
        public LSID getLSID() throws RemoteException {
            if (mAtomicBoolean != null)
                mAtomicBoolean.compareAndSet(false,true);

            if (TextUtils.isEmpty(mAccessToken)) {
                try {
                    if (new File(getApplicationContext().getFilesDir(),Constants.COOCAA_FILE_ACCESSTOKEN_NAME).exists()) {
                        //首先读取文件中的accessToken
                        mAccessToken = FileAccessTokenUtils.getDataFromFile(getApplicationContext(),Constants.COOCAA_FILE_ACCESSTOKEN_NAME);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //为空情况读取SharedPreferences
                if (TextUtils.isEmpty(mAccessToken)) {
                    mAccessToken = ShareUtls.getInstance(getApplicationContext()).getString(Constants.COOCAA_PREF_ACCESSTOKEN,"");
                }
            }
            //accessToken为空,走注册流程
            if (TextUtils.isEmpty(mAccessToken)) {
                RegisterUseCase.getInstance(getApplicationContext()).run(
                        new RegisterUseCase.RequestValues(mTYPE),
                        new RegisterUseCase.RegisterCallBackListener() {
                            @Override
                            public void onError(String errType,String msg) {
                                mAtomicBoolean.compareAndSet(true,false);

                                lsid = new LSID("","");
                                Log.d(TAG,"Register/login query error msg:"+msg);
                            }

                            @Override
                            public void onSuccess(final String token) {
                                ShareUtls.getInstance(getApplicationContext()).putString(Constants.COOCAA_PREF_ACCESSTOKEN,token);
                                FileAccessTokenUtils.saveDataToFile(getApplicationContext(),Constants.COOCAA_FILE_ACCESSTOKEN_NAME,token);

                                Log.d(TAG," token1:" + ShareUtls.getInstance(getApplicationContext()).getString(Constants.COOCAA_PREF_ACCESSTOKEN,""));
                                mAccessToken = token;
                                queryUser(token,mAtomicBoolean);
                            }
                        }
                );
            } else {
                //获取用户信息
                queryUser(mAccessToken,mAtomicBoolean);
            }

            while (mAtomicBoolean.get()) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG," token2:" + ShareUtls.getInstance(getApplicationContext()).getString(Constants.COOCAA_PREF_ACCESSTOKEN,"") + " "+ShareUtls.getInstance(getApplicationContext()));
            Log.d(TAG,lsid.lsid+":---:"+lsid.token);
            return lsid;
        }

        @Override
        public void reset() throws RemoteException {
            if (iRemoteServiceCallback != null) {
                iRemoteServiceCallback.openSSe();
            }
        }

        @Override
        public void registerCallback(IRemoteServiceCallback cb) throws RemoteException {
            iRemoteServiceCallback = cb;
        }

        @Override
        public void unregisterCallback(IRemoteServiceCallback cb) throws RemoteException {
            iRemoteServiceCallback = null;
        }

    }

    private void queryUser(final String token,final AtomicBoolean atomicBoolean) {
        //获取sid
        LSIDUseCase.getInstance(getApplicationContext()).run(new LSIDUseCase.RequestValues(token,mTYPE), new LSIDUseCase.QuerylSIDCallBackListener() {
            @Override
            public void onError(String errorType,String msg) {
                lsid = new LSID("","");

                if (!TextUtils.isEmpty(errorType) && errorType.equals("1000")) {
                    AndroidLog.androidLog("==========re-register======");
                    //token失效:1000 重新注册
                    ShareUtls.getInstance(getApplicationContext()).putString(Constants.COOCAA_PREF_ACCESSTOKEN,"");
                    FileAccessTokenUtils.saveDataToFile(getApplicationContext(),Constants.COOCAA_FILE_ACCESSTOKEN_NAME,"");
                    ShareUtls.getInstance(getApplicationContext()).putString("sp_accessToken", "");
                    ShareUtls.getInstance(getApplicationContext()).putString("sp_sid", "");
                    mAccessToken = null;
                }
                Log.d(TAG,"LSID query error msg:"+msg);
                atomicBoolean.compareAndSet(true,false);
            }

            @Override
            public void onSuccess(String sid,String deviceInfo) {
                lsid = new LSID(sid,token);
                checkDeviceInfo(deviceInfo,token);
                Log.d(TAG,"-----------------------sid:"+sid + " token:"+token);
                //存储设备信息
                ShareUtls.getInstance(getApplicationContext()).putString(Constants.COOCAA_PREF_DEVICEINFO,deviceInfo);
                atomicBoolean.compareAndSet(true,false);
            }

            @Override
            public void onSuccess(String sid, String deviceInfo, String tempCode,String roomId) {
                lsid = new LSID(sid,token,tempCode,roomId);
                checkDeviceInfo(deviceInfo,token);
                Log.d(TAG,"-----------------------sid:"+sid + " token:"+token + " tempCode:"+tempCode + " roomId:"+roomId);
                //存储设备信息
                ShareUtls.getInstance(getApplicationContext()).putString(Constants.COOCAA_PREF_DEVICEINFO,deviceInfo);
                atomicBoolean.compareAndSet(true,false);
            }
        });
    }

    private void checkDeviceInfo(String deviceInfo,String token) {
        //判断设备信息变更
        boolean isSameDeviceInfo = false;
        Log.d(TAG,"deviceInfo1:"+deviceInfo);
        try {
            if (mTYPE == TYPE.TV) {
                TVDeviceInfo tvDeviceInfo = TvDeviceInfoManager.getInstance(getApplicationContext()).getDeviceInfo();
                String currentDeviceInfo = JSONObject.toJSONString(tvDeviceInfo);
                if (StringUtils.isEmpty(deviceInfo) || StringUtils.isEmpty(currentDeviceInfo) || !deviceInfo.equals(currentDeviceInfo)) {
                    isSameDeviceInfo = true;
                    deviceInfo = currentDeviceInfo;
                }
            } else if (mTYPE == TYPE.PAD) {
                PadDeviceInfo padDeviceInfo = PadDeviceInfoManager.getInstance(getApplicationContext()).getDeviceInfo();
                String currentDeviceInfo = JSONObject.toJSONString(padDeviceInfo);
                if (StringUtils.isEmpty(deviceInfo) || StringUtils.isEmpty(currentDeviceInfo) || !deviceInfo.equals(currentDeviceInfo)) {
                    isSameDeviceInfo = true;
                    deviceInfo = currentDeviceInfo;
                }
            } else {
                //...
            }
            Log.d(TAG,"deviceInfo2:"+deviceInfo);
            if (isSameDeviceInfo) {
                //存储设备信息
                ShareUtls.getInstance(getApplicationContext()).putString(Constants.COOCAA_PREF_DEVICEINFO,deviceInfo);

                UpdateDeviceInfoUseCase.getInstance(getApplicationContext()).run(new UpdateDeviceInfoUseCase.RequestValues(token, deviceInfo), new UpdateDeviceInfoUseCase.UpdateDeviceInfoCallBackListener() {
                    @Override
                    public void onError(String errorType, String msg) {
                        Log.d(TAG,"errorType:"+errorType + " msg:"+msg);
                    }

                    @Override
                    public void onSuccess() {
                        Log.d(TAG,"onSuccess");
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTYPE = getLSIDType();
        mStub = new Stub();
        registerAccountReceiver();

        try {
            ISystem iSystem = SAL.getModule(getApplicationContext(), SalModule.SYSTEM);
            iSystem.setDeviceNameListener(new ISystem.SystemDeviceNameListener() {
                @Override
                public void onDeviceNameChanged(String s) {
                    checkDeviceInfo();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerAccountReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACCOUNT_CHANGED);
        intentFilter.addAction(DONGLE_NAME_ACTION);
        intentFilter.addAction(TV_NAME_CHANGED);
        intentFilter.addAction(DEVICE_NAME_CHANGED_ACTION);
        registerReceiver(mAccountReceiver, intentFilter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mStub;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterAccountReceiver();
    }

    private void unRegisterAccountReceiver() {
        try {
            unregisterReceiver(mAccountReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
