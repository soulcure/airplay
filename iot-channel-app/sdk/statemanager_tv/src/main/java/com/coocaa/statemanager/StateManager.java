package com.coocaa.statemanager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.statemanager.businessstate.BusinessStateTvReport;
import com.coocaa.statemanager.common.bean.AppState;
import com.coocaa.statemanager.common.bean.CmdData;
import com.coocaa.statemanager.common.utils.StateUtils;
import com.coocaa.statemanager.common.utils.SystemInfoUtil;
import com.coocaa.statemanager.data.AppResolver;
import com.coocaa.statemanager.data.CastAppResolve;
import com.coocaa.statemanager.data.SmartScreenState;
import com.coocaa.statemanager.model.StateModel;
import com.coocaa.statemanager.view.countdown.TimeOutCallBack;
import com.coocaa.statemanager.view.manager.ViewManagerImpl;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import swaiotos.channel.iot.IOTChannel;
import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.client.event.QueryConnectRoomDeviceEvent;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.ss.session.SessionManager;
import swaiotos.channel.iot.utils.EmptyUtils;
import swaiotos.channel.iot.utils.NetUtils;
import swaiotos.channel.iot.utils.ThreadManager;
import swaiotos.sal.SAL;
import swaiotos.sal.SalModule;
import swaiotos.sal.platform.IDeviceInfo;

public class StateManager {
    private static final String TAG = "state";
    public static StateManager INSTANCE = new StateManager();
    private Context mContext = null;
    private SSChannel mSSChannel = null;
    private PackageManager manager;
    private StateModel mStateModel;
    private AppState tmpState = null;
    private boolean isReady = false;  //有时推送过程中，有回到第三方应用的情况
    private String cur_code = "";  //当前传屏码
    private String cur_screenQR = "https://s.skysrt.com/IvEFz2";  //当前传屏二维码
    private String cur_mobile = "";  //当前推送的用户
    private String mOwner;//业务拥有者
    private Map<String, String> sessionMap = new HashMap<>();  //sid 与 mobile的映射
    private boolean isSceenCodeShow = false;
    private boolean needShowGlobalSceenCode = false;
    private boolean isShowBigQrCodeWindow = false;
    private int connectedDevices = 1;
    private Timer deviceTimer = null;
    private Timer netTimer = null;
    private static final long NO_BUSINESS_DISCONNECT_NET_TIME = 270 * 1000;//4分30秒

    public void init(final Context c) {
        mContext = c;
        initConfig(mContext);
        initReceiver(mContext);
        mStateModel = new StateModel(mContext);
        NetUtils.NetworkReceiver.register(mContext, mNetworkReceiver);
        AppResolver.initScreenApps(c);
        BusinessStateTvReport.getDefault().init(mContext);
        IOTChannel.mananger.open(mContext, "swaiotos.channel.iot", new IOTChannel.OpenCallback() {
            @Override
            public void onConntected(SSChannel ssChannel) {
                Log.d(TAG, " onConntected success ");
                mSSChannel = ssChannel;
                try {
                    mSSChannel.getSessionManager().addServerSessionOnUpdateListener(new SessionManager.OnSessionUpdateListener() {
                        @Override
                        public void onSessionConnect(final Session session) {
                            Log.d(TAG, " onConnect ~~~~~~~~~~~~~ ");
                            ThreadManager.getInstance().ioThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateConnectDevicesZero(false);
                                    mStateModel.getConfigInfo();
                                }
                            });
                        }

                        @Override
                        public void onSessionUpdate(Session session) {

                        }

                        @Override
                        public void onSessionDisconnect(final Session session) {
                            Log.d(TAG, " onDisconnect ~~~~~~~~~~~~~ ");
                        }
                    });
                    mSSChannel.getSessionManager().addRoomDevicesOnUpdateListener(new SessionManager.OnRoomDevicesUpdateListener() {
                        @Override
                        public void onRoomDevicesUpdate(final int count) {
                            Log.d(TAG, " onRoomDevicesUpdate ---------- count:" + count);
                            connectedDevices = count;
                            if (count == 1) {
                                try {
                                    //添加一个校验dongle连接数判断为0，出错的补丁
                                    List<Session> list = mSSChannel.getSessionManager().getServerSessions();
                                    int size = list.size();
                                    Log.e(TAG, "dongle ServerSessions size=" + size);
                                    if (size > 0) {
                                        Log.e(TAG, "dongle ServerSessions size=" + size + " do not goto home");
                                        return;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                updateConnectDevicesZero(true);
                            } else {
                                updateConnectDevicesZero(false);
                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mStateModel.getConfigInfo();
            }

            @Override
            public void onError(String s) {

            }
        });
        startInitQR(mContext);
    }

    private void initConfig(Context mContext) {
        try {
            //系统配置prop可以决定是否显示全局二维码
            String iotQR = SystemProperties.get("third.iot.screenqr");
            Log.d(TAG, "initConfig iotQR:" + iotQR);
            if (!TextUtils.isEmpty(iotQR) && iotQR.equals("1")) {
                needShowGlobalSceenCode = true;
                return;
            }
            IDeviceInfo deviceInfo = SAL.getModule(mContext, SalModule.DEVICE_INFO);
            String cMode = deviceInfo.getModel();
            Log.d(TAG, "initConfig cMode:" + cMode);
            if ("HDD500".equals(cMode)) {
                needShowGlobalSceenCode = true;
            } else {
                needShowGlobalSceenCode = false;
            }
        } catch (Exception e) {
        }
    }

    private void startInitQR(Context context) {
        try {
            ComponentName cn = AppResolver.getTopComponet(context);
            if (cn == null) {
                return;
            }
            Log.d(TAG, "startInitQR  pkg:" + cn.getPackageName() + "  class:" + cn.getClassName());
            updateScreenQR(cn.getPackageName(), cn.getClassName());
        } catch (Exception e) {
        }
    }

    public Context getContext(){
        return mContext;
    }

    private void initReceiver(Context c) {
        IntentFilter installFilter = new IntentFilter();
        installFilter.addAction("sky.action.start.app.msg");
        c.registerReceiver(activiytReceiver, installFilter);


//需求变更，注释掉显示共享二维码的开关
//        IntentFilter qrFilter = new IntentFilter();
//        qrFilter.addAction("swaiotos.channel.iot.action.qrshow");
//        c.registerReceiver(qrReceiver, qrFilter);
    }

    private BroadcastReceiver activiytReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, " activiytReceiver 11 action:" + intent.getAction());
            final String pkg = intent.getExtras().getString("pkgName");
            String className = intent.getExtras().getString("actName");
            if (TextUtils.isEmpty(className)) {
                className = SystemProperties.get("sky.current.actname");  //TV上通过这个来获取classname
            }
            receiveApp(pkg, className);
        }
    };

    private BroadcastReceiver qrReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final boolean isShow = intent.getExtras().getBoolean("show", false);
            Log.d(TAG, " qrReceiver  isShow:" + isShow);
            ThreadManager.getInstance().uiThread(new Runnable() {
                @Override
                public void run() {
                    if (isShow && needShowGlobalSceenCode) {
                        isSceenCodeShow = true;
                        ViewManagerImpl.getSingleton().showQrCodeGlobalWindow(mContext, cur_code, cur_screenQR);
                    } else {
                        isSceenCodeShow = false;
                        ViewManagerImpl.getSingleton().dismissQrCodeGlobalWindow();
                    }
                }
            });
        }
    };

    private NetUtils.NetworkReceiver mNetworkReceiver = new NetUtils.NetworkReceiver() {
        @Override
        public void onConnected() {
            Log.d(TAG, "mNetworkReceiver onConnected ");
            if (netTimer != null) {
                netTimer.cancel();
            }
            ThreadManager.getInstance().uiThread(new Runnable() {
                @Override
                public void run() {
                    ViewManagerImpl.getSingleton().cancelInterDisconnectDialog();
                }
            });
        }

        @Override
        public void onDisconnected() {
            if (!needShowGlobalSceenCode|| !SystemInfoUtil.isDangle())//非dongle设备，不弹
                return;
            long delayTime = 0;
            AppResolver.AppBean appBean = AppResolver.getScreenAppConfig(mContext);
            if(EmptyUtils.isEmpty(appBean)){
                delayTime = NO_BUSINESS_DISCONNECT_NET_TIME;
            }else{
                delayTime = appBean.disNetworExitTime;
            }
            Log.d(TAG, "mNetworkReceiver onDisconnected delayTime:" + delayTime);
            if (netTimer != null) {
                netTimer.cancel();
                netTimer = null;
            }
            netTimer = new Timer();
            netTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ThreadManager.getInstance().uiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (null == tmpState)
                                return;
                            if (tmpState != null && AppResolver.homePkg.equals(tmpState.getPkgName())&&!tmpState.getClassName().equals("com.tianci.ThirdPlayer.ThirdDetailInfoActivity")) {
                                Log.d(TAG, " onDisconnected 当前在主页！！！");
                                return;
                            }
                            boolean isConnect = NetUtils.isConnected(mContext);
                            Log.d(TAG, " onDisconnected isConnect:" + isConnect);
                            if (isConnect)
                                return;
                            ViewManagerImpl.getSingleton().showInterDisconnectDialog(mContext, new TimeOutCallBack() {
                                @Override
                                public void onFinish() {
                                    AppResolver.comebackHome();
                                }
                            });
                        }
                    });
                }
            }, delayTime);
        }
    };

    public void receiveApp(final String pkg, final String className) {
        if (manager == null) {
            manager = mContext.getPackageManager();
        }
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                String clientID = StateUtils.getServiceMetaData(manager, pkg);
                Log.d(TAG, " onReceive pkg:" + pkg + "  classname:" + className + " clientID:" + clientID);
                tmpState = new AppState();
                tmpState.setPkgName(pkg);
                tmpState.setClassName(className);
                tmpState.setClientID(clientID);

                if (needShowGlobalSceenCode) {
                    updateScreenQR(pkg, className);
                }

                if (!(!TextUtils.isEmpty(pkg) && pkg.equals("com.yozo.office.education")))
                    CastAppResolve.Resolver.endCast(mContext, pkg, className, false);
                Log.d(TAG, " onReceive isReady:" + isReady);
                if (AppResolver.isDongleCastBusiness(pkg,className)) {
                    if (isReady||pkg.equals("com.tianci.de")) {//当爱投屏业务启动的时候，杀进程
                        isReady = false;
                        SmartScreenState.Instance.updataState(pkg, className, SmartScreenState.SCREEN_TYPE.ONSTARTING);
                        AppResolver.killBackProcess(mContext, pkg, mSSChannel);
                        // 开始启动应用了
                        ThreadManager.getInstance().uiThread(new Runnable() {
                            @Override
                            public void run() {
                                ViewManagerImpl.getSingleton().showUserGlobalWindow(mContext, mOwner);
                            }
                        });
                        if (!(!TextUtils.isEmpty(pkg) && pkg.equals("com.yozo.office.education")))
                            CastAppResolve.Resolver.startCast(CastAppResolve.Resolver.getCastData());
                    } else {
                        SmartScreenState.Instance.updataState(pkg, className, SmartScreenState.SCREEN_TYPE.IDLE);
                        //永中office应用直接返回，显示UserGlobalWindow
                        if (EmptyUtils.isNotEmpty(pkg)&&pkg.equals("com.yozo.office.education")){
                           return;
                        }
                        ThreadManager.getInstance().uiThread(new Runnable() {
                            @Override
                            public void run() {
                                ViewManagerImpl.getSingleton().dismissUserGlobalWindow();
                            }
                        });
                    }
                } else {
                    SmartScreenState.Instance.setScreenType(SmartScreenState.SCREEN_TYPE.ONEXIST);
                    SmartScreenState.Instance.updataState(pkg, className, SmartScreenState.SCREEN_TYPE.IDLE);
                    ThreadManager.getInstance().uiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewManagerImpl.getSingleton().dismissUserGlobalWindow();
                        }
                    });
                }
            }
        });

        //返回launcher页，保证关隐藏掉连屏码
        if (AppResolver.homePkg.equals(pkg) || "com.coocaa.dongle.launcher".equals(pkg)) {
            ViewManagerImpl.getSingleton().dismissQrCodeGlobalWindow();
        }

        //防止上一个业务的退出，导致遥控器混乱
        ThreadManager.getInstance().uiThread(new Runnable() {
            @Override
            public void run() {
                BusinessStateTvReport.getDefault().getDangleTvBusinessState();
                EventBus.getDefault().post(new QueryConnectRoomDeviceEvent());
            }
        },3000);

    }


    public void startClient(String clientID, String pkg, String msg) {
        try {
            IMMessage message = null;
            try {
                message = IMMessage.Builder.decode(msg);
            } catch (Exception e) {
            }
            if (message == null) {
                return;
            }
            String pkgName = getStartPkg(pkg, message);
            IMMessage.TYPE type = message.getType();
            String mobile = message.getExtra("mobile");
            String uid = message.getExtra("open_id");
            mOwner = message.getExtra("owner");
            String castType = message.getExtra("log_castType");
            String contentURI = message.getExtra("log_appScreenURI");
            String sourceClient = message.getClientSource();
            String sID = message.getSource().getId();
            String content = message.getContent();
            String response = message.getExtra("response");
            Log.d(TAG, "startClient  pkgName:" + pkgName + "  clientid:" + clientID + " type:" + type + " mobile:" + mobile + " sID:" + sID + " sourceClient:" + sourceClient + " owner:"+ mOwner);
            cur_mobile = mobile;
            sessionMap.put(sID, cur_mobile);
            String tips = message.getExtra("showtips");
            if (!TextUtils.isEmpty(tips) && Boolean.valueOf(tips) && !"ss-clientID-UniversalMediaPlayer".equals(clientID)) {
                ThreadManager.getInstance().uiThread(new Runnable() {
                    @Override
                    public void run() {
                        ViewManagerImpl.getSingleton().showLoadingDialog(mContext, cur_code, mOwner);
                    }
                });
            }

            Map<String, AppResolver.AppBean> map = AppResolver.getAppBean(pkgName);
            if (AppResolver.isDongleCastBusiness(pkgName,type.name())){
                isReady = true;
                if(EmptyUtils.isNotEmpty(mOwner)){
                    BusinessStateTvReport.getDefault().setOwner(mOwner);
                    BusinessStateTvReport.getDefault().setExtra(message.getExtra());
                }
                String curPkg = SmartScreenState.Instance.getPkgName();
                String curclassName = SmartScreenState.Instance.getClassName();
                Log.d(TAG, "startClient  curPkg:" + curPkg + "  curclassName:" + curclassName);
                if (pkgName.equals(curPkg)) {
                    try {
                        String startClassName = map.get(type.name()).className;
                        Log.d(TAG, "startClient  startClassName:" + startClassName);
                        if (curclassName.equals(startClassName)) {
                            CastAppResolve.Resolver.endCast(mContext, pkgName, startClassName, true);
                            SmartScreenState.Instance.updataState(pkgName, startClassName, SmartScreenState.SCREEN_TYPE.ONSTARTING);
                            ThreadManager.getInstance().uiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ViewManagerImpl.getSingleton().showUserGlobalWindow(mContext, mOwner);
                                }
                            });
                            CastAppResolve.Resolver.updateCastData(uid, pkgName, startClassName, TextUtils.isEmpty(castType) ? map.get(type.name()).mediaType.name() : castType, content, response, contentURI, sourceClient);
                            CastAppResolve.Resolver.startCast(CastAppResolve.Resolver.getCastData());
                        } else {
                            SmartScreenState.Instance.updataState(pkgName, startClassName, SmartScreenState.SCREEN_TYPE.ONREADY);
                            CastAppResolve.Resolver.updateCastData(uid, pkgName, startClassName, TextUtils.isEmpty(castType) ? map.get(type.name()).mediaType.name() : castType, content, response, contentURI, sourceClient);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        SmartScreenState.Instance.updataState(pkgName, curclassName, SmartScreenState.SCREEN_TYPE.ONSTARTING);
                        ThreadManager.getInstance().uiThread(new Runnable() {
                            @Override
                            public void run() {
                                ViewManagerImpl.getSingleton().showUserGlobalWindow(mContext, mOwner);
                            }
                        });
                    }
                } else {
                    try {
                        CastAppResolve.Resolver.updateCastData(uid, pkgName, map.get(type.name()).className, TextUtils.isEmpty(castType) ? map.get(type.name()).mediaType.name() : castType, content, response, contentURI, sourceClient);
                        SmartScreenState.Instance.updataState(pkgName, map.get(type.name()).className, SmartScreenState.SCREEN_TYPE.ONREADY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //如果是回到主页命令
                try {
                    if ("swaiotos.channel.iot".equals(pkgName)) {
                        CmdData data = new Gson().fromJson(message.getContent(), CmdData.class);
                        if (data.type.equals("KEY_EVENT") && data.cmd.equals("3")) {
                            if (tmpState != null && tmpState.getPkgName().equals(AppResolver.homePkg)) {
                                return;
                            }
                            Log.d(TAG, "startClient  comeback exit cap");
                            ThreadManager.getInstance().uiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ViewManagerImpl.getSingleton().dismissQrCodeGlobalWindow();
                                    ViewManagerImpl.getSingleton().dismissUserGlobalWindow();
                                    ViewManagerImpl.getSingleton().showUserFinishDialog(mContext, mOwner);
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                }
            }

            if ("com.coocaa.danma".equals(pkgName) && (IMMessage.TYPE.TEXT == type)) {
                CastAppResolve.Resolver.submitDanma(mContext, uid, pkgName, "", type.name(), content, response, sourceClient);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getStartPkg(String pkgName, final IMMessage message) {
        switch (pkgName) {
            case "swaiotos.channel.iot":
                try {
                    CmdData data = new Gson().fromJson(message.getContent(), CmdData.class);
                    String cmd = data.cmd;
                    if (cmd.equals("LIVE_VIDEO")) {
                        return "com.fengmizhibo.live";
                    }
                } catch (Exception e) {
                }
                break;
            case "com.tianci.movieplatform":
                try {
                    CmdData data = new Gson().fromJson(message.getContent(), CmdData.class);
                    String cmd = data.cmd;
                    if (cmd.equals("GET_SOURCE")) {
                        return "com.tianci.movieplatform:GET_SOURCE";
                    }
                } catch (Exception e) {
                }
                break;
        }
        return pkgName;
    }

    private boolean isDisconnecting = false;

    private void updateConnectDevicesZero(boolean flag) {
        Log.d(TAG, "updateConnectDevicesZero  flag:" + flag);
        if (flag) {
            if (isDisconnecting||SystemInfoUtil.isFamilyClient(mContext)) {//如果是C端用户，不弹此弹框)
                return;
            }
            long delayTime = 0;
            AppResolver.AppBean appBean = AppResolver.getScreenAppConfig(mContext);
            if(EmptyUtils.isEmpty(appBean)||!appBean.isAutoExitNoDevice){
                return;
            }
            delayTime = appBean.noDeviceExitTime;
            Log.d(TAG, "noDevice connect delayTime:" + delayTime);
            if (deviceTimer != null) {
                deviceTimer.cancel();
                deviceTimer = null;
            }
            isDisconnecting = true;
            deviceTimer = new Timer();
            deviceTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    ThreadManager.getInstance().uiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d(TAG, " updateConnectDevicesZero connectedDevices:" + connectedDevices);
                                isDisconnecting = false;
                                if (connectedDevices == 1) {
                                    ViewManagerImpl.getSingleton().cancelDisconnectDialog();
                                    if (!needShowGlobalSceenCode)
                                        return;
                                    ViewManagerImpl.getSingleton().showNoDeviceDialog(mContext, new TimeOutCallBack() {
                                        @Override
                                        public void onFinish() {
                                            Log.i(TAG, "onFinish: showNoDeviceDialog");
                                            AppResolver.comebackHome();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                            }
                        }
                    });
                }
            }, delayTime);
        } else {
            isDisconnecting = false;
            if (deviceTimer != null)
                deviceTimer.cancel();
            ThreadManager.getInstance().uiThread(new Runnable() {
                @Override
                public void run() {
                    ViewManagerImpl.getSingleton().cancelDeviceDialog();
                }
            });
        }
    }

    private void updateScreenQR(String pkg, final String className) {
        if (TextUtils.isEmpty(AppResolver.homePkg)) {
            AppResolver.homePkg = SystemProperties.get("persist.service.homepage.pkg");
        }
        Log.d(TAG, "updateScreenQR  AppResolver.homePkg:" + AppResolver.homePkg);
        if (AppResolver.homePkg.equals(pkg) || "com.coocaa.dongle.launcher".equals(pkg)) {
            ThreadManager.getInstance().uiThread(new Runnable() {
                @Override
                public void run() {
                    ViewManagerImpl.getSingleton().dismissQrCodeGlobalWindow();
                    ViewManagerImpl.getSingleton().dismissUserGlobalWindow();
                    ViewManagerImpl.getSingleton().cancelInterDisconnectDialog();
                    ViewManagerImpl.getSingleton().cancelDeviceDialog();
                    ViewManagerImpl.getSingleton().cancelDisconnectDialog();
                    ViewManagerImpl.getSingleton().dismissBigQrCodeGlobalWindow();
                }
            });
            isSceenCodeShow = false;
        } else if (("com.tianci.system".equals(pkg) && ("com.tianci.system.fullbt.rc.RcFullBtOneKeyActivitySS").equals(className))
        ) {
            ThreadManager.getInstance().uiThread(new Runnable() {
                @Override
                public void run() {
                    ViewManagerImpl.getSingleton().dismissQrCodeGlobalWindow();
                    ViewManagerImpl.getSingleton().dismissUserGlobalWindow();
                }
            });
            isSceenCodeShow = false;
        } else {
            isSceenCodeShow = false;
            if (!needShowGlobalSceenCode)
                return;
            ThreadManager.getInstance().uiThread(new Runnable() {
                @Override
                public void run() {
                    isSceenCodeShow = true;
                    Log.d(TAG, "updateScreenQR  cur_code:" + cur_code);
                    ViewManagerImpl.getSingleton().showQrCodeGlobalWindow(mContext, cur_code, cur_screenQR);
                }
            });
        }
    }

    public void updateScreenCode(String bind_code, String screenQR) {
        if (bind_code != null) {    //可以为""
            cur_code = bind_code;
        }
        if (!TextUtils.isEmpty(screenQR)) {
            cur_screenQR = screenQR;
        }
        sendQRReceiver(bind_code, screenQR);
        Log.d(TAG, " updateScreenCode cur_code:" + cur_code + " cur_screenQR:" + cur_screenQR);
        if (TextUtils.isEmpty(AppResolver.homePkg)) {
            AppResolver.homePkg = SystemProperties.get("persist.service.homepage.pkg");
        }
        if (TextUtils.isEmpty(AppResolver.homePkg)) {    //主页初始化异常
            Log.d(TAG, " homePkg 初始化异常！！！");
            return;
        }
        Log.d(TAG, " updateScreenCode AppResolver.homePkg:" + AppResolver.homePkg + " (tmpState == null):" + (tmpState == null));
        if (tmpState == null) {
            return;
        }
        if (tmpState != null && AppResolver.homePkg.equals(tmpState.getPkgName())) {
            Log.d(TAG, " 当前在主页！！！");
            return;
        }
//        if (tmpState != null && "com.swaiotos.universalmediaplayer.document.page.DocumentPlayerActivity".equals(tmpState.getClassName())) {
//            Log.d(TAG, " 当前在ppt！！！");
//            return;
//        }
        ThreadManager.getInstance().uiThread(new Runnable() {
            @Override
            public void run() {
                if (needShowGlobalSceenCode && isSceenCodeShow) {
                    ViewManagerImpl.getSingleton().showQrCodeGlobalWindow(mContext, cur_code, cur_screenQR);
                }
            }
        });
    }

    /**
     * 展示大的共享二维码弹框
     */
    public void showBigQrCodeWindow() {
        Log.d("BusinessState", "showBigQrCodeWindow");
        try {
            if (!isShowBigQrCodeWindow) {
                ThreadManager.getInstance().uiThread(new Runnable() {
                    @Override
                    public void run() {
                        ViewManagerImpl.getSingleton().showBigQrCodeGlobalWindow(mContext, cur_code, cur_screenQR);
                        isShowBigQrCodeWindow = true;
                    }
                });
            } else {
                ThreadManager.getInstance().uiThread(new Runnable() {
                    @Override
                    public void run() {
                        ViewManagerImpl.getSingleton().reStartShowBigQrCodeGlobalWindow(mContext,cur_code,cur_screenQR);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置展示大二维码状态标志
     *
     * @param isShow
     */
    public void setShowBigQrCodeWindowStatus(boolean isShow) {
        isShowBigQrCodeWindow = isShow;
    }

    private void sendQRReceiver(String bind_code, String screenQR) {
        try {
            if (TextUtils.isEmpty(screenQR)) {
                screenQR = "https://s.skysrt.com/IvEFz2?";
            }
            Intent i = new Intent();
            i.setPackage("swaiotos.runtime.h5.app");
            i.setAction("swaiotos.channel.iot.action.qrupdate");
            i.putExtra("bind_code", bind_code);
            i.putExtra("screenQR", screenQR);
            mContext.sendBroadcast(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getBindCode() {
        return cur_code;
    }

    public String getCurQR() {
        return cur_screenQR;
    }

}
