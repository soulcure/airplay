package swaiotos.sensor.data;

import android.view.View;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import swaiotos.sensor.client.data.ClientBusinessInfo;
import swaiotos.sensor.mgr.InfoManager;

/**
 * @Author: yuzhan
 */
public class ClientCmdInfo implements Serializable {

    public String cmd;
    public String cid;
    public String clientChannelId;
    public AccountInfo accountInfo;
    public DisplayInfo display;
    public String content;
    public boolean zip = false;
    public Map<String, String> extra;

    public static int width, height;
    public static int screenWidth, screenHeight;
    public static int offsetX, offsetY;

    public static ClientCmdInfo build(InfoManager infoManager, String cmd) {
        return build(infoManager, cmd, null);
    }

    static int[] pos = new int[2];
    public static ClientCmdInfo build(InfoManager infoManager, String cmd, View view) {
        ClientCmdInfo info = new ClientCmdInfo();
        info.cmd = cmd;
        info.accountInfo = infoManager.getAccountInfo();
        info.cid = infoManager.getId();

        info.clientChannelId = infoManager.getBusinessInfo().clientSSId;

        initScreen();

        info.display = new DisplayInfo();
        info.display.screenWidth = screenWidth;
        info.display.screenHeight = screenHeight;
        info.display.width = width;
        info.display.height = height;
        if(view != null) {
            view.getLocationOnScreen(pos);
            info.display.x = pos[0];
            info.display.y = pos[1];
        } else {
            info.display.x = offsetX;
            info.display.y = offsetY;
        }
        return info;
    }

    public static void setBusinessInfo(ClientBusinessInfo info) {
        width = info.width;
        height = info.height;
        initScreen();
        offsetX = info.offsetX;
        offsetY = info.offsetY;
    }

    protected static void initScreen() {
        if(screenWidth <= 0) {
            screenWidth = InfoManager.getAppContext().getResources().getDisplayMetrics().widthPixels;
            screenHeight = InfoManager.getAppContext().getResources().getDisplayMetrics().heightPixels;
        }
    }

    public void addExtra(String key, String value) {
        if(extra == null) {
            extra = new HashMap<>();
        }
        extra.put(key ,value);
    }

    public String getExtra(String key) {
        return extra == null ? null : extra.get(key);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public final static String CMD_CLIENT_START = "client-start";

    public final static String CMD_CLIENT_STOP = "client-stop";

    public final static String CMD_CLIENT_CONNECT = "client-connect";
    public final static String CMD_CLIENT_MOTION_EVENT = "client-motion-event";
    public final static String CMD_CLIENT_SENSOR_EVENT = "client-sensor-event";
}
