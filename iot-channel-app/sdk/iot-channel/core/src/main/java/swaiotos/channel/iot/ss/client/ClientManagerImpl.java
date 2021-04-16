package swaiotos.channel.iot.ss.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.ss.ISSChannelService;
import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.client.event.StartClientEvent;
import swaiotos.channel.iot.ss.client.model.ApkItem;
import swaiotos.channel.iot.ss.client.model.ClientIDHandleModel;
import swaiotos.channel.iot.ss.client.model.CmdData;
import swaiotos.channel.iot.ss.client.model.IClientIDHandleModel;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;
import swaiotos.channel.iot.ss.server.data.ApkInfo;
import swaiotos.channel.iot.ss.server.data.AppItem;
import swaiotos.channel.iot.ss.server.http.api.AppStoreResult;
import swaiotos.channel.iot.ss.server.http.api.HttpResult;
import swaiotos.channel.iot.ss.server.http.api.HttpSubscribe;
import swaiotos.channel.iot.ss.server.http.api.HttpThrowable;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.ui.DialogActivity;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.ApkUtils;
import swaiotos.channel.iot.utils.ToastUtils;
import swaiotos.channel.iot.utils.ipc.ParcelableBinder;
import swaiotos.channel.iot.webrtc.DataChannelClient;
import swaiotos.channel.iot.webrtc.Peer;
import swaiotos.channel.iot.webrtc.config.Constant;
import swaiotos.channel.iot.webrtc.entity.Model;
import swaiotos.channel.iot.webrtc.entity.SSEEvent;
import swaiotos.sal.SAL;
import swaiotos.sal.SalModule;
import swaiotos.sal.platform.ISystemInfo;
import swaiotos.sal.system.ISystem;

/**
 * @ClassName: ClientManagerImpl
 * @Author: lu
 * @CreateDate: 2020/4/8 11:33 AM
 * @Description:
 */
public class ClientManagerImpl implements ClientManager, ClientManager.OnClientChangeListener {
    private static final String TAG = "ClientManager";
    private static final String APPSTORE_CLIENT_SERVER = "ss-clientID-appstore_12345";
    private static final String IOT_CHANNEL_CLIENT_SERVER = "ss-iotclientID-9527";
    private static final String LOCALMEDIA_CLIENT_SERVER = "ss-clientID-UniversalMediaPlayer";
    public static final String DEVICE_NAME_CHANGED_ACTION = "smart.life.change.device";

    public static class Client {
        public static final int TYPE_ACTIVITY = 0;
        public static final int TYPE_SERVICE = 1;
        public final int type;
        public ComponentName cn;
        public final int version;

        public Client(int type, ComponentName cn, int version) {
            this.type = type;
            this.cn = cn;
            this.version = version;
        }

        @Override
        public String toString() {
            return "Client{" +
                    "type=" + type +
                    ", cn=" + cn +
                    ", version=" + version +
                    '}';
        }
    }

    private Context mContext;
    private SSContext ssContext;
    private ISSChannelService.Stub mBinder;
    private final List<OnClientChangeListener> mClientChangeListeners = new ArrayList<>();
    private IClientIDHandleModel mClientIDHandleModel;

    private List<ApkItem> mSendList;
    private Map<String, Long> mDialogMap;

    public ClientManagerImpl(Context context, SSContext ssContext) {
        mContext = context;
        this.ssContext = ssContext;
        mSendList = new ArrayList<>();
        mDialogMap = new HashMap<>();
        mClientIDHandleModel = new ClientIDHandleModel(mContext, ssContext);
    }

    @Override
    public void init(ISSChannelService.Stub binder, Intent filter) {
        mBinder = binder;
        mClientIDHandleModel.findClients(filter);
        mClientIDHandleModel.addOnClientChangeListener(this);
    }

    @Override
    public int getClientVersion(String clientID) {
        Map<String, Client> clients = Clients.getInstance().getClients();
        Client client = clients.get(clientID);
        if (client != null) {
            return client.version;
        }
        return -1;
    }


    /**
     * 是否开启版本拉平功能 true开始, false 关闭
     */
    private static final boolean autoUpdate = true;


    @Override
    public boolean start(String clientID, final IMMessage message) {
        try {
            //mSSContext.getSessionManager().getServerSessions(); //size=0 的规避机制
            Session target = message.getSource();
            if (!TextUtils.isEmpty(target.getExtra(SSChannel.STREAM_LOCAL))
                    && ssContext.getDeviceInfo() instanceof TVDeviceInfo) {
                Log.d("yao", "reConnectSession = " + target.toString());
                ssContext.getController().reConnectSession(target, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (clientID.equals(LOCALMEDIA_CLIENT_SERVER)) {
            checkAIStandby();
        }

        if (clientID.equals(Peer.SOURCE_CLIENT)
                || clientID.equals(DataChannelClient.SOURCE_CLIENT)) {
            String content = message.getContent();
            SSEEvent event = new SSEEvent();
            Model model = new Model(content, false);
            event.setModel(model);
            event.setImMessage(message);

            if (content.contains(Constant.OFFER)) {
                event.setMsgType(Constant.OFFER);
            } else if (content.contains(Constant.ANSWER)) {
                event.setMsgType(Constant.ANSWER);
            } else if (content.contains(Constant.CANDIDATE)) {
                event.setMsgType(Constant.CANDIDATE);
            }
            EventBus.getDefault().post(event);
            return true;
        }

        //截获应用圈的消息，转到iot
        if (!TextUtils.isEmpty(clientID) && clientID.equals(APPSTORE_CLIENT_SERVER)) {
            CmdData cmdData;
            Log.e("client", "start  message.getType():" + message.getType());
            switch (message.getType()) {
                case IMAGE:
                case AUDIO:
                case VIDEO:
                case DOC:
                    clientID = LOCALMEDIA_CLIENT_SERVER;
                    message.setClientTarget(LOCALMEDIA_CLIENT_SERVER);
//                    client.cn = new ComponentName("swaiotos.channel.iot","swaiotos.channel.iot.tv.iothandle.IotClientService");
                    break;
                case TEXT:
                    try {
                        cmdData = JSONObject.parseObject(message.getContent(), CmdData.class);
                        CmdData.CMD_TYPE type = CmdData.CMD_TYPE.valueOf(cmdData.type);
                        Log.e("client", "start  type:" + type.name());
                        switch (type) {
                            case KEY_EVENT:
//                        case SCREEN_SHOT:
                            case START_APP:
                                clientID = IOT_CHANNEL_CLIENT_SERVER;
                                message.setClientTarget(IOT_CHANNEL_CLIENT_SERVER);
                                break;
                            case LOCAL_MEDIA:
                                clientID = LOCALMEDIA_CLIENT_SERVER;
                                message.setClientTarget(LOCALMEDIA_CLIENT_SERVER);
//                            client.cn = new ComponentName("swaiotos.channel.iot","swaiotos.channel.iot.tv.iothandle.IotClientService");
                                break;
                            default:
                                clientID = APPSTORE_CLIENT_SERVER;
                                message.setClientTarget(APPSTORE_CLIENT_SERVER);
//                                client.cn = new ComponentName("com.tianci.appstore","com.coocaa.x.serivce.lite.iotchannel.MyClientService");
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    clientID = APPSTORE_CLIENT_SERVER;
                    message.setClientTarget(APPSTORE_CLIENT_SERVER);
//                    client.cn = new ComponentName("com.tianci.appstore","com.coocaa.x.serivce.lite.iotchannel.MyClientService");
            }
        } else if (!TextUtils.isEmpty(clientID) && clientID.equals(IOT_CHANNEL_CLIENT_SERVER)) {
            if (message.getType() == IMMessage.TYPE.TEXT) {
                try {
                    final CmdData cmdData = JSONObject.parseObject(message.getContent(), CmdData.class);
                    CmdData.CMD_TYPE type = CmdData.CMD_TYPE.valueOf(cmdData.type);
                    if (type == CmdData.CMD_TYPE.DEVICE_INFO) {
                        if (!TextUtils.isEmpty(cmdData.param)) {
                            SAL.getModule(mContext, SAL.SYSTEM).setDeviceNameListener(new ISystem.SystemDeviceNameListener() {
                                @Override
                                public void onDeviceNameChanged(String s) {
                                    AndroidLog.androidLog("----onDeviceNameChanged:" + s);
                                    mContext.sendBroadcast(new Intent(DEVICE_NAME_CHANGED_ACTION));
                                }
                            });
                            SAL.getModule(mContext, SAL.SYSTEM).setDeviceName(cmdData.param);
                        }
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        Map<String, Client> clients = Clients.getInstance().getClients();
        Client client = clients.get(clientID);

        startClientEvent(mContext, clientID, client, message);

        Log.e("yao1", "start message---clientID：" + clientID);
        Log.e("yao1", "start message---" + message.toString());

        if (message.getType() == IMMessage.TYPE.DIALOG) {
            Log.e("yao1", "message DIALOG");
            //show dialog
            VersionCheck vc = new VersionCheck(ssContext);
            String msg = "电视目前还不支持该操作，需要更新相关服务，确定下载安装吗?";
            String registerType = message.getExtra("registerType");
            if (!TextUtils.isEmpty(registerType) && registerType.equals("dongle")) {
                msg = "共享屏目前还不支持该操作，需要更新相关服务，确定下载安装吗?";
            }
            DialogActivity.showDialog(mContext, msg, message, vc);
            //return true;  //dialog 继续派发
        } else if (message.getType() == IMMessage.TYPE.CANCEL) {
            Log.e("yao1", "message.getType():" + message.getType());
            final String clientId = message.getContent(); //消息接收方 clientID
            for (int i = 0; i < mSendList.size(); i++) {
                if (mSendList.get(i).getClientId().equals(clientId)) {
                    mSendList.remove(i);
                    return true;
                }
            }
        } else if (message.getType() == IMMessage.TYPE.CONFIRM) {
            Log.e("yao1", "message.getType():" + message.getType());
            try {
                final String clientId = message.getContent(); //消息接收方 clientID
                int index = -1;
                for (int i = 0; i < mSendList.size(); i++) {
                    if (mSendList.get(i).getClientId().equals(clientId)) {
                        index = i;
                        break;
                    }
                }
                if (index < 0) {
                    return false;
                }

                final ApkItem item = mSendList.get(index);
                final String packageName = item.getPackageName();
                final int versionCode = item.getVersionCode();

                Log.e("yao1", "appStore packageName---" + packageName);
                Log.e("yao1", "appStore versionCode---" + versionCode);

                HttpSubscribe<AppStoreResult<AppItem>> subscribe = new HttpSubscribe<AppStoreResult<AppItem>>() {
                    @Override
                    public void onSuccess(AppStoreResult<AppItem> result) {
                        int updateVersionCode = result.data.getVersioncode();
                        if (updateVersionCode >= versionCode) {
                            Intent intent = new Intent("coocaa.intent.action.SMART_DETAIL");
                            intent.putExtra("pkg", packageName); //下载应用的包名
                            intent.putExtra("from", "core"); //从哪里启动的简易详情页

                            PackageManager packageManager = mContext.getPackageManager();
                            Intent eIntent = packageManager.getLaunchIntentForPackage(packageName);
                            if (eIntent != null && hasMainAct(packageName)) {
                                eIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("eIntent", eIntent); // 安装成功后，启动的intent
                            }

                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                            ssContext.postDelay(new Runnable() {
                                @Override
                                public void run() {
                                    mSendList.remove(item);
                                }
                            }, 60 * 1000);
                        }
                    }

                    @Override
                    public void onError(HttpThrowable error) {
                        Log.e("yao", "checkAppStore error---" + error.getErrMsg());
                    }
                };
                VersionCheck.checkAppStore(packageName, subscribe, ssContext.getLSID());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else if (message.getType() == IMMessage.TYPE.PROTO) {
            int version;
            if (client != null) {
                version = client.version;
            } else {
                version = -1;
            }

            IMMessage msg = IMMessage.Builder.sendClientProto(message, version);
            try {
                ssContext.getIMChannel().send(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        int reqProtoVersion = message.getReqProtoVersion();
        boolean isSend;
        if ((client != null && reqProtoVersion == 0)) {
            isSend = true;
        } else if (client != null && reqProtoVersion <= client.version) {
            isSend = true;
        } else if (message.getType() == IMMessage.TYPE.PROGRESS
                || message.getType() == IMMessage.TYPE.RESULT
                || message.getType() == IMMessage.TYPE.PROTO
                || message.getType() == IMMessage.TYPE.CTR) {
            isSend = true;
        } else {
            isSend = false;
            Log.e("yao1", "send " + message + " to " + clientID + " not support");
        }


        if (!isSend && autoUpdate) { //执行版本拉平
            try {
                final String clientId = message.getClientTarget();
                final int protoVersion = message.getReqProtoVersion(); //消息要求的协议版本号
                boolean isPopUp = message.isPopUp();

                Log.e("yao1", "message clientId---" + clientId);
                Log.e("yao1", "message protoVersion---" + protoVersion);

                if (!isPopUp) {
                    long curTime = System.currentTimeMillis();
                    Long t = mDialogMap.get(clientId);
                    if (t != null) {
                        long time = curTime - t;
                        if (time < 60 * 60 * 1000 * 6) {  //6小时
                            return false;
                        }
                    }
                    mDialogMap.put(clientId, curTime);
                }

                final ApkItem item = new ApkItem();
                item.setClientId(clientId);

                if (!mSendList.contains(item)) {
                    mSendList.add(item);

                    HttpSubscribe<HttpResult<ApkInfo>> httpSubscribe = new HttpSubscribe<HttpResult<ApkInfo>>() {
                        @Override
                        public void onSuccess(HttpResult<ApkInfo> result) {
                            Log.e("yao1", "ApkInfo---" + result.msg);

                            if (result.data == null
                                    || TextUtils.isEmpty(result.data.getAppPkgName())) {
                                Log.e("yao1", "get ApkInfo error---" + result.msg);
                                //请求后台服务没有相应的升级，移除
                                mSendList.remove(item);
                                return;
                            }

                            String packageName = result.data.getAppPkgName();
                            int versionCode = result.data.getVersionCode();
                            item.setPackageName(packageName);
                            item.setVersionCode(versionCode);

                            Log.e("yao1", "ApkInfo packageName---" + packageName);
                            Log.e("yao1", "ApkInfo versionCode---" + versionCode);
                            VersionCheck vc = new VersionCheck(ssContext);
                            vc.sendDialogToTarget(message, clientId);
                        }

                        @Override
                        public void onError(HttpThrowable error) {
                            Log.e("yao1", "reqVersionCode error---" + error.getErrMsg());
                            //请求失败，移除
                            mSendList.remove(item);
                        }
                    };
                    VersionCheck.reqVersionCode(clientId, protoVersion, httpSubscribe, ssContext.getLSID());
                } else {
                    if (ApkUtils.isTopActivity(mContext, "com.coocaa.x.app.appstore3.pages.SmartDetailActivity")) {
                        String msg;
                        if (Constants.isDangle()) {
                            msg = "共享屏正在升级相关服务，请稍后再试";
                        } else {
                            msg = "电视正在升级相关服务，请稍后再试";
                        }
                        ToastUtils.instance().showToast(mContext, msg);
                    } else {
                        mSendList.remove(item);
                    }
                }

                if (isPopUp) {
                    return false;
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //派发消息到应用
        Intent intent = new Intent();
        intent.setComponent(client.cn);
        intent.putExtra("message", message);
        intent.putExtra("binder", new ParcelableBinder(mBinder));
        try {
            if (client.type == Client.TYPE_SERVICE) {
                //new File("/vendor/TianciVersion").exists()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.i(TAG, "startForegroundService");
                    mContext.startForegroundService(intent);
                } else {
                    Log.i(TAG, "startService");
                    mContext.startService(intent);
                }
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Log.i(TAG, "startActivity");
                mContext.startActivity(intent);
            }
            Log.d(TAG, "send " + message + " to " + client);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void addOnClientChangeListener(OnClientChangeListener listener) {
        synchronized (mClientChangeListeners) {
            if (!mClientChangeListeners.contains(listener)) {
                mClientChangeListeners.add(listener);
            }
        }
    }

    @Override
    public void removeOnClientChangeListener(OnClientChangeListener listener) {
        synchronized (mClientChangeListeners) {
            mClientChangeListeners.remove(listener);
        }
    }

    @Override
    public void onClientChange(String clientID, Integer version) {
        synchronized (mClientChangeListeners) {
            for (OnClientChangeListener receiver : mClientChangeListeners) {
                try {
                    receiver.onClientChange(clientID, version);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private boolean hasMainAct(String packageName) {
        if (!TextUtils.isEmpty(packageName)) {
            return false;
        }

        return !packageName.equals("com.coocaa.danma")
                && !packageName.equals("swaiotos.runtime.h5.app");
    }

    private void startClientEvent(Context mContext, String clientID, Client client, IMMessage msg) {
        try {
            String pkgName = "";
            String className = "";
            String message = "";
            if (client != null && client.cn != null) {
                ComponentName cn = client.cn;
                pkgName = cn.getPackageName();
                className = cn.getClassName();
            }
            if (msg != null) {
                message = msg.encode();
            }
            StartClientEvent clientEvent = new StartClientEvent(clientID, pkgName, className, message);
            EventBus.getDefault().post(clientEvent);
            Log.d("state", "startClientEvent success !! clientID:" + clientID);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void checkAIStandby() {
        try {
            ISystemInfo iSystemInfo = SAL.getModule(mContext, SalModule.SYSTEM_INFO);
            if (iSystemInfo.isAIStandByOn()) {
                Log.d("state", "set AI ScreenOn");
                ISystem iSystem = SAL.getModule(mContext, SalModule.SYSTEM);
                iSystem.setAIScreenMode(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
