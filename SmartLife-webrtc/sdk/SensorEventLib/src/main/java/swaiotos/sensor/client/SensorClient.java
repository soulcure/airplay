package swaiotos.sensor.client;

import android.content.Context;
import android.os.DeadObjectException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import swaiotos.sensor.channel.ChannelMsgSender;
import swaiotos.sensor.channel.IMsgSender;
import swaiotos.sensor.client.data.ClientBusinessInfo;
import swaiotos.sensor.connect.IConnectCallback;
import swaiotos.sensor.data.AccountInfo;
import swaiotos.sensor.data.ChannelEvent;
import swaiotos.sensor.data.ClientCmdInfo;
import swaiotos.sensor.data.ServerCmdInfo;
import swaiotos.sensor.mgr.InfoManager;
import swaiotos.sensor.server.data.ServerInfo;
import swaiotos.sensor.touch.InputTouchView;

/**
 * @Author: yuzhan
 */
public class SensorClient {

    private Context context;
    private IConnectClient client;
    private boolean bStart = false;
    protected IMsgSender sender;
    private InputTouchView view;
    private InfoManager infoManager;
    private ISmartApi smartApi;
    private boolean sensorEnable = false;
    private int sensorThreshold = 20;

    private static final String TAG = "SSCClient";

    public SensorClient(Context context, ClientBusinessInfo businessInfo, AccountInfo accountInfo) {
        this.context = context;
        String id = accountInfo.mobile;//UUID.randomUUID().toString();
        Log.d(TAG, "new SensorClient, accountInfo=" + accountInfo + ", businessInfo=" + businessInfo + ", id=" + id);

        if(businessInfo == null) {
            throw new RuntimeException("BusinessInfo must not be null.");
        }
        infoManager = new InfoManager();
        infoManager.setId(id);
        infoManager.setAccountInfo(accountInfo);
        infoManager.setBusinessInfo(businessInfo);

        client = new SensorConnectClient(infoManager);
        sender = new ChannelMsgSender(context, businessInfo.clientSSId);
        sender.setProtoVersion(businessInfo.protoVersion);
        ((ChannelMsgSender) sender).setAccountInfo(accountInfo);

        InfoManager.setAppContext(context);

        ClientCmdInfo.setBusinessInfo(businessInfo);
    }

    public void setSmartApi(ISmartApi smartApi) {
        this.smartApi = smartApi;
        client.setSmartApi(smartApi);
    }

    public void refreshAccountInfo(AccountInfo accountInfo) {
        Log.d(TAG, "refreshAccountInfo, accountInfo=" + accountInfo);
        infoManager.setAccountInfo(accountInfo);
    }

    public void sendMsgSticky(String content, String targetId) {
        try {
            sender.sendMsgSticky(content, targetId);
        } catch (DeadObjectException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void setShowTips(boolean showTips) {
        ((ChannelMsgSender) sender).setShowTips(showTips);
    }

    public void start() {
        Log.d(TAG, "call start...");
        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        bStart = true;
        try {
            sender.sendMsgSticky(JSON.toJSONString(ClientCmdInfo.build(infoManager, ClientCmdInfo.CMD_CLIENT_START)), infoManager.getBusinessInfo().targetSSId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(view != null) {
            view.onStart();
        }
    }

    public void stop() {
        Log.d(TAG, "call stop...");
        bStart = false;
//        sender.sendMsg(JSON.toJSONString(ClientCmdInfo.build(ClientCmdInfo.CMD_CLIENT_STOP)), InfoManager.getBusinessInfo().targetSSId);
        client.send(JSON.toJSONString(ClientCmdInfo.build(infoManager, ClientCmdInfo.CMD_CLIENT_STOP)));
        client.disconnect();
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        if(view != null) {
            view.onStop();
        }
    }

    public View getView() {
        if(view == null) {
            view = new InputTouchView(context);
            view.setSize(infoManager.getBusinessInfo().width, infoManager.getBusinessInfo().height);
            view.setClient(client);
            view.setSensorEnable(sensorEnable);
            view.setSensorThreshold(sensorThreshold);
        }
        return view;
    }

    public void setSensorEnable(boolean enable) {
        if(view != null) {
            view.setSensorEnable(enable);
        } else {
            sensorEnable = enable;
        }
    }

    public void setSensorThreshold(int t) {
        if(view != null) {
            view.setSensorThreshold(t);
        } else {
            sensorThreshold = t;
        }
    }

    public void send(String data) {
        client.send(data);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChannelEvent event) {
        Log.d(TAG, "onEvent : " + event);
        if(event != null && !TextUtils.isEmpty(event.content)) {
            JSONObject jsonObject = JSON.parseObject(event.content);
            String cmd = jsonObject.getString("cmd");
            Log.d(TAG, "cmd : " + cmd);
            if(ServerCmdInfo.CMD_SERVER_RECEIVE_CONNECT.equals(cmd)) {
                boolean isConnected = client.isConnected();
                Log.d(TAG, "client isConnected=" + isConnected);
                if(!isConnected) {
                    try {
                        ServerCmdInfo info = JSON.parseObject(event.content, ServerCmdInfo.class);
                        Log.d(TAG, "cId : " + info.cId + ", myId=" + infoManager.getId());
                        if(TextUtils.equals(info.cId, infoManager.getId())) {
                            ServerInfo serverInfo = JSON.parseObject(info.content, ServerInfo.class);
                            Log.d(TAG, "server cmd info=" + info + ", start connect : " + serverInfo.url);
                            client.connect(serverInfo.url, callback);
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "connect error" + e);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void connect(String url) {
        boolean isConnected = client.isConnected();
        Log.d(TAG, "connect server : " + url + ", isConnected=" + isConnected);
        if(!isConnected) {
            client.connect(url, callback);
        } else {
            if(callback != null) {
                callback.onSuccess();
            }
        }
    }

    private IConnectCallback parentCallback;
    public void setCallback(IConnectCallback callback) {
        this.parentCallback = callback;
    }

    private IConnectCallback callback = new IConnectCallback() {
        @Override
        public void onSuccess() {
            Log.d(TAG, "client connect onSuccess");
            ClientCmdInfo info = ClientCmdInfo.build(infoManager, ClientCmdInfo.CMD_CLIENT_CONNECT);
            client.send(JSON.toJSONString(info));
            if(parentCallback != null)
                parentCallback.onSuccess();
        }

        @Override
        public void onFail(String reason) {
            Log.d(TAG, "client connect onFail : " + reason);
            if(parentCallback != null)
                parentCallback.onFail(reason);
        }

        @Override
        public void onFailOnce(String reason) {
            Log.d(TAG, "onFailOnce : " + reason);
            if(parentCallback != null)
                parentCallback.onFailOnce(reason);
        }

        @Override
        public void onClose() {
            Log.d(TAG, "client connect onClose");
            if(parentCallback != null)
                parentCallback.onClose();
        }

        @Override
        public void onMessage(String msg) {
            Log.d(TAG, "client connect onMessage : " + msg);
            if(parentCallback != null)
                parentCallback.onMessage(msg);
        }
    };
}
