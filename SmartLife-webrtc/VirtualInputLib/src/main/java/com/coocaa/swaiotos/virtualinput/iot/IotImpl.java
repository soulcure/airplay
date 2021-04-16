package com.coocaa.swaiotos.virtualinput.iot;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.coocaa.smartscreen.utils.CmdUtil;

import swaiotos.runtime.h5.H5ChannelInstance;

/**
 * @Author: yuzhan
 */
public class IotImpl implements IIOT{

    private Context context;

    public static final String REMOTE_CONTROL_OPERATION = "remote_control_operation";

    private final static String TAG = "SmartVI";

    public IotImpl(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void sendCmd(String cmd, String type, String paramsJson, String clientId) {
        sendCmd(cmd, type, paramsJson, clientId, null);
    }

    @Override
    public void sendCmd(String cmd, String type, String paramsJson, String clientId, String owner) {
        Log.d(TAG, "sendCmd, cmd=" + cmd + ", paramsJson=" + paramsJson + ", clientId=" + clientId +",owner="+owner);
//        Toast.makeText(context, "sendCmd:" + cmd, Toast.LENGTH_SHORT).show();
        CmdData jsonCmd = new CmdData();
        jsonCmd.cmd = "";
        jsonCmd.type = "custom_event";
        CmdData cmdData = new CmdData();
        cmdData.cmd = cmd;
        cmdData.type = type;
        cmdData.param = paramsJson;
        jsonCmd.param = cmdData.toJson();

        String json = jsonCmd.toJson();
        Log.d(TAG, "send cmd json=" + json);
        try {
            H5ChannelInstance.getSingleton().sendText(clientId, json, owner, null);
//            SSConnectManager.getInstance().sendTextMessage(json, clientId,owner);
//            SSConnectManager.getInstance().sendDeviceCmd(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendCmdCommon(String clientId, String cmd, String owner) {
        try {
            H5ChannelInstance.getSingleton().sendText(clientId, cmd, owner, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendCmdCommon(String clientId, String cmd, String owner, int protoVersion) {
        try {
            H5ChannelInstance.getSingleton().sendText(clientId, cmd, owner, null, protoVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendKeyEvent(int keyCode, int action) {
        Log.d(TAG, "sendKeyEvent, keyCode=" + keyCode + ", action=" + action);
        H5ChannelInstance.getSingleton().sendKey(keyCode);
//        if (SSConnectManager.getInstance().isConnected()) {
//            SSConnectManager.getInstance().sendKey(keyCode);
//            SSConnectManager.getInstance().sendScreenshot();
//            Map<String, String> map = new HashMap<>();
//            map.put("event", keyCode + "");
//            MobclickAgent.onEvent(context, REMOTE_CONTROL_OPERATION, map);
//        } else {
//            ToastUtils.getInstance().showGlobalLong("请先连接设备");
//        }
    }

    @Override
    public void sendTouchEvent(int action, float x, float y) {
        Log.d(TAG, "sendTouchEvent, action=" + action + ", x=" + x + ", y=" + y);
        Toast.makeText(context, "sendTouchEvent, action=" + action + ", x=" + x + ", y=" + y, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void sendTouchEvent(MotionEvent event) {
        Log.d(TAG, "sendTouchEvent, action=" + event.getAction() + ", x=" + event.getX() + ", y=" + event.getY());
        CmdUtil.sendTouchEvent(event);
    }
}
